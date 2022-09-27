package com.wahson.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wahson.dto.DishDto;
import com.wahson.entity.Dish;

public interface DishService extends IService<Dish> {
    // 新增菜品，同时插入dish 和 dishflavor 两张表
    public void saveWithFlavor(DishDto dishDto);

    // 根据id查询菜品信息及口味信息
    public DishDto getByIdWithFlavor(Long id);

    // 更新菜品信息，及口味信息
    public void updateWithFlavor(DishDto dishDto);
}
