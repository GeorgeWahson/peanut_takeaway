package com.wahson.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wahson.common.Result;
import com.wahson.dto.SetmealDto;
import com.wahson.entity.Category;
import com.wahson.entity.Setmeal;
import com.wahson.service.CategoryService;
import com.wahson.service.SetmealDishService;
import com.wahson.service.SetmealService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("ids：{}", ids);
        setmealService.removeWithDish(ids);
        return Result.success("删除套餐数据成功!");
    }

    /**
     * 根据条件查询套餐数据
     * 前端发请求为：http://localhost:8080/setmeal/list?categoryId=1574026245941882881&status=1
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public Result<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        // 前端传的status = 1,所以直接写1也行
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return Result.success(list);
    }
}
