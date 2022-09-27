package com.wahson.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wahson.dto.SetmealDto;
import com.wahson.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    // 新增套餐，同时保存套餐和菜品关联信息
    public void saveWithDish(SetmealDto setmealDto);

    // 删除套餐，同时删除setmeal_dish中的菜品关联数据
    public void removeWithDish(List<Long> ids);
}
