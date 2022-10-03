package com.wahson.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wahson.dto.DishDto;
import com.wahson.dto.SetmealDto;
import com.wahson.entity.Setmeal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    // 新增套餐，同时保存套餐和菜品关联信息
    void saveWithDish(SetmealDto setmealDto);

    // 删除套餐，同时删除setmeal_dish中的菜品关联数据
    void removeWithDish(List<Long> ids);

    // 修改时获取套餐，同时获取套餐和菜品关联信息
    SetmealDto getWithDish(Long id);

    //修改套餐时，修改套餐菜品
    void updateWithDish(SetmealDto setmealDto);

    void statusHandle(String ids, String setmealStatus);
}
