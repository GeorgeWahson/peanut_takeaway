package com.wahson.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wahson.common.Result;
import com.wahson.dto.DishDto;
import com.wahson.entity.Category;
import com.wahson.entity.Dish;
import com.wahson.entity.DishFlavor;
import com.wahson.service.CategoryService;
import com.wahson.service.DishFlavorService;
import com.wahson.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto) {
        log.info("dishDto: {}", dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        // 清理所有菜品缓存数据
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        // 清理某个分类下面的缓存
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return Result.success("新增菜品成功!");

    }

    /**
     * 分页查询菜品
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        // 构造分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        // 条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper();
        lqw.like(name != null, Dish::getName, name);
        // 排序条件
        lqw.orderByDesc(Dish::getUpdateTime);
        // 执行分页查询
        dishService.page(pageInfo, lqw);

        // 对象拷贝，忽略list集合，集合需要处理添加category name
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        // 处理没有name的records，遍历每一条数据
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            // 将普通属性拷给dishDto, id,name,price...
            BeanUtils.copyProperties(item, dishDto);
            // 查出对象，获得名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return Result.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishDto> get(@PathVariable Long id) {
        log.info("get id for update category: {}", id);
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return Result.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto) {
        log.info("dishDto: {}", dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        // 清理所有菜品缓存数据
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        // 清理某个分类下面的缓存
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return Result.success("修改菜品成功!");
    }


//    /**
//     * 根据条件查询菜品数据
//     * 传对象通用性高
//     * @param dish
//     * @return
//     */
//    @GetMapping("/list")
//    public Result<List<Dish>> list(Dish dish) {
//
//        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
//        lqw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
//        // 只查起售的菜
//        lqw.eq(Dish::getStatus, 1);
//        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list = dishService.list(lqw);
//
//        return Result.success(list);
//    }

    /**
     * 根据条件查询菜品数据
     * 传对象通用性高
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public Result<List<DishDto>> list(Dish dish) {
        List<DishDto> dtoList = null;
        // 设置key
        String key = "dish_" + dish.getCategoryId() + "_"  + dish.getStatus();
        // 先从Redis中获取缓存数据
        dtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        // 如果存在，直接返回，无需查询数据库
        if (dtoList != null) {
            return Result.success(dtoList);
        }

        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // 只查起售的菜
        lqw.eq(Dish::getStatus, 1);
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(lqw);

        dtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            // 将普通属性拷给dishDto, id,name,price...
            BeanUtils.copyProperties(item, dishDto);
            // 查出对象，获得名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            // 当前菜品id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);
            dishDto.setFlavors(dishFlavors);

            return dishDto;
        }).collect(Collectors.toList());

        // 如果不存在，查询数据库，将查询到的菜品缓存到Redis
        redisTemplate.opsForValue().set(key, dtoList, 60, TimeUnit.MINUTES);

        return Result.success(dtoList);
    }


    @DeleteMapping
    public Result<String> deleteDish(String ids) {
        // 先判断每个菜品是否都已经停售，若有菜品未停售，则放回error
        String[] idList = ids.split(",");
        for (String id : idList) {
            Dish dish = dishService.getById(id);
            if (dish.getStatus() == 1) {
                return Result.error("存在菜品状态未停售，无法删除!");
            }
        }
        dishService.deleteWithFlavor(ids);
        return Result.success("删除成功！");
    }

    /**
     * 批量，单个 修改菜品状态信息
     * @param ids
     * @param dishStatus
     * @return
     */
    @PostMapping("/status/{dishStatus}")
    public Result<String> statusHandle(@RequestParam String ids, @PathVariable String dishStatus) {
        log.info("ids: {}, dishStatus: {} ", ids, dishStatus);
        String[] statusIds = ids.split(",");
        for (String id : statusIds) {
            log.info("需要修改状态的id: {}", id);
            Dish dish = dishService.getById(id);
            dish.setStatus(Integer.valueOf(dishStatus));
            dishService.updateById(dish);

            // 修改状态不走update方法，需要删除缓存
            String key = "dish_" + dish.getCategoryId() + "_1";
            redisTemplate.delete(key);
        }
        return Result.success("修改状态成功!");
    }


}
