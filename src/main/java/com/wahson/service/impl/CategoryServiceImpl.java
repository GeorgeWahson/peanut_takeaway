package com.wahson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wahson.common.CustomException;
import com.wahson.entity.Category;
import com.wahson.entity.Dish;
import com.wahson.entity.Employee;
import com.wahson.entity.Setmeal;
import com.wahson.mapper.CategoryMapper;
import com.wahson.mapper.EmployeeMapper;
import com.wahson.service.CategoryService;
import com.wahson.service.DishService;
import com.wahson.service.EmployeeService;
import com.wahson.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {


    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;
    /**
     * 根据id删除分类，删除之前需要进行判断
     * @param id
     */
    @Override
    public void remove(Long id) {

        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id进行查询
        lqw.eq(Dish::getCategoryId, id);
        //查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        int count = dishService.count(lqw);
        if (count > 0) {
            // 已经关联菜品，抛出一个业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int setMealCount = setmealService.count(setmealLambdaQueryWrapper);
        if (setMealCount > 0) {
            // 已经关联套餐，抛出一个业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }
        //正常删除分类
        super.removeById(id);
    }
}
