package com.wahson.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wahson.common.BaseContext;
import com.wahson.common.Result;
import com.wahson.entity.Employee;
import com.wahson.entity.ShoppingCart;
import com.wahson.service.EmployeeService;
import com.wahson.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("shopping cart: {}", shoppingCart);

        // 设置用户id,
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        // 查询当前菜品或套餐是否已存在，一样则number+1
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, currentId);

        if (dishId != null) {
            // TODO 套餐口味不一
            lqw.eq(ShoppingCart::getDishId, dishId).eq(shoppingCart.getDishFlavor() != null, ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());
        } else {
            lqw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart cart = shoppingCartService.getOne(lqw);

        // 如果已存在，number+1，不存在添加购物车，默认1

        if (cart != null) {
            Integer number = cart.getNumber();
            cart.setNumber(number + 1);
            cart.setCreateTime(LocalDateTime.now());
            shoppingCartService.updateById(cart);
        } else {
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cart = shoppingCart;
        }

        return Result.success(cart);
    }

    /**
     * 从购物车查询
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        log.info("查看购物车。。。");
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        // 查询当前用户
        lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId()).orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(lqw);
        return Result.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public Result<String> clean() {
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(lqw);

        return Result.success("清空购物车成功!");
    }
}
