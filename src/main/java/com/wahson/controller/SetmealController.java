package com.wahson.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wahson.common.Result;
import com.wahson.dto.DishDto;
import com.wahson.dto.SetmealDto;
import com.wahson.entity.Category;
import com.wahson.entity.Dish;
import com.wahson.entity.Setmeal;
import com.wahson.service.CategoryService;
import com.wahson.service.SetmealDishService;
import com.wahson.service.SetmealService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 套餐管理
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public Result<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);

        setmealService.saveWithDish(setmealDto);
        return Result.success("新增套餐成功!");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();

        lqw.like(name != null, Setmeal::getName, name);
        lqw.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, lqw);
        // 对象拷贝，拷贝除了records的属性
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list =  records.stream().map((item) ->{
            SetmealDto setmealDto = new SetmealDto();
            // 拷贝records里除了categoryName的属性。
            BeanUtils.copyProperties(item, setmealDto);
            // 分类id
            Long categoryId = item.getCategoryId();
            // 获取分类名称
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        // 将缺的records赋给dtoPage
        dtoPage.setRecords(list);

        return Result.success(dtoPage);
    }

    /**
     * 删除套餐同时删除setmeal_dish表数据
     * 删除时删除setmealCache下所有缓存
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("ids：{}", ids);
        setmealService.removeWithDish(ids);
        return Result.success("删除套餐数据成功!");
    }

    /**
     * 手机端 套餐内
     * 前端发请求为：http://localhost:8080/setmeal/list?categoryId=1574026245941882881&status=1
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public Result<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        // 前端传的status = 1,所以直接写1也行
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<SetmealDto> get(@PathVariable Long id) {
        log.info("get id for update setmeal: {}", id);
        SetmealDto setmealDto = setmealService.getWithDish(id);
        return Result.success(setmealDto);
    }


    @PutMapping
    @CacheEvict(value = "setmealCache", allEntries = true)
    public Result<String> update(@RequestBody SetmealDto setmealDto) {
        log.info("setmealDto: {}", setmealDto.toString());
        setmealService.updateWithDish(setmealDto);

        return Result.success("修改套餐成功!");
    }

    /**
     * 单个及批量 起售与停售
     * @param ids
     * @param setmealStatus
     * @return
     */
    @PostMapping("/status/{setmealStatus}")
    public Result<String> statusHandle(@RequestParam String ids, @PathVariable String setmealStatus) {
        setmealService.statusHandle(ids, setmealStatus);
        return Result.success("修改状态成功!");
    }

}
