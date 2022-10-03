package com.wahson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wahson.common.CustomException;
import com.wahson.dto.DishDto;
import com.wahson.dto.SetmealDto;
import com.wahson.entity.Dish;
import com.wahson.entity.DishFlavor;
import com.wahson.entity.Setmeal;
import com.wahson.entity.SetmealDish;
import com.wahson.mapper.SetmealMapper;
import com.wahson.service.SetmealDishService;
import com.wahson.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Value("${peanut.path}")
    private String basePath;
    /**
     * 新增套餐，同时保存套餐和菜品关联信息
     *
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐的基本信息，操作setmeal,执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        // 保存套餐和菜品的关联信息，操作setmeal_dish，执行insert操作
        setmealDishService.saveBatch(setmealDishes);

    }

    /**
     * 删除套餐，同时删除setmeal_dish中的菜品关联数据
     *
     * @param ids
     */
    @Override
    public void removeWithDish(List<Long> ids) {
        // 起售不可删除
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId, ids);
        lqw.eq(Setmeal::getStatus, 1);

        int count = this.count(lqw);
        if (count > 0) {
            throw new CustomException("有套餐正在售卖中，不能删除!");
        }
        // 删除套餐图片
        for (Long id : ids) {
            Setmeal setmeal = this.getById(id);
            String imageName = setmeal.getImage();
            File file = new File(basePath + imageName);
            boolean delete = file.delete();
            log.info("套餐图片名称: {}, 删除结果：{}", imageName, delete);
        }


        // 删除setmeal
        this.removeByIds(ids);


        // 删除setmeal_dish
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);

        setmealDishService.remove(lambdaQueryWrapper);
    }

    /**
     * 获取套餐时获取菜品信息
     *
     * @param id
     */
    @Override
    public SetmealDto getWithDish(Long id) {
        // 查询套餐基本信息，从dish查询
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        // 拷贝属性
        BeanUtils.copyProperties(setmeal, setmealDto);
        // 查询当前套餐对应的菜品信息，从setmeal_dish表查询
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId, id);

        List<SetmealDish> dishList = setmealDishService.list(lqw);
        setmealDto.setSetmealDishes(dishList);

        return setmealDto;

    }

    @Override
    public void updateWithDish(SetmealDto setmealDto) {

        //更新setmeal表基本信息(name,price等)
        this.updateById(setmealDto);

        // 清理当前套餐对应菜品数据---setmeal_dish表的delete操作
        // 套餐里每道菜都是一条数据，更新不能删除原本存在的数据，只能全删再插
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(lqw);
        //添加当前提交过来的菜品数据---setmeal_dish表的insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public void statusHandle(String ids, String setmealStatus) {
        log.info("ids: {}, setmealStatus: {} ", ids, setmealStatus);
        String[] statusIds = ids.split(",");
        for (String id : statusIds) {
            log.info("需要修改状态的套餐id: {}", id);
            Setmeal setmeal = this.getById(id);
            setmeal.setStatus(Integer.valueOf(setmealStatus));
            this.updateById(setmeal);
        }


    }
}

