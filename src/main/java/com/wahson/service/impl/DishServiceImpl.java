package com.wahson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wahson.dto.DishDto;
import com.wahson.entity.Dish;
import com.wahson.entity.DishFlavor;
import com.wahson.mapper.DishMapper;
import com.wahson.service.DishFlavorService;
import com.wahson.service.DishService;
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
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Value("${peanut.path}")
    private String basePath;

    /**
     * 新增菜品，同时保存口味数据
     * @param dishDto
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品的基本信息到菜品表dish
        this.save(dishDto); // dto 继承至 Dish

        // 保存菜品口味的基本信息到菜品口味表dish_flavor
        // 获取菜品id
        Long dishDtoId = dishDto.getId();
        // 菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDtoId);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品信息及口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 查询菜品基本信息，从dish查询
        Dish dish = this.getById(id);
        // 拷贝属性
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        // 查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(lqw);

        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 更新dish dish_flavor
     * @param dishDto
     * @return
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息(name,price等)
        this.updateById(dishDto);

        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        // 一道菜一个口味是一条数据，一道菜四个口味则在库里有四条数据。更新不能删除原本存在的数据，只能全删再插
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 删除菜品的同时删除口味表
     * @param ids
     */
    @Override
    public void deleteWithFlavor(String ids) {
        // 前端传入的String类型的id以,分隔
        String[] idList = ids.split(",");
        // 对每个id 执行 删除dish表，本地图片，dish_flavor表操作。
        for (String id : idList) {
            log.info("被删除的id: {}", id);
//            LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
//            lqw.eq(Dish::getId, id);
            // 获取图片名称，删除图片
            Dish deleteDish = this.getById(id);
            String imageName = deleteDish.getImage();
            File file = new File(basePath + imageName);
            boolean delete = file.delete();
            log.info("图片：{}, 删除结果：{}", imageName, delete);
            // 删除dish_flavor表对应数据
            LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
            lqw.eq(DishFlavor::getDishId, id);
            dishFlavorService.remove(lqw);
            // 删除dish表数据
            this.removeById(id);
        }
    }

    /**
     * 批量起售与批量停售
     * @param ids
     */
    @Override
    public void changeDishStatus(String ids) {

    }


}
