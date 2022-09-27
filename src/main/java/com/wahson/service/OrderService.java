package com.wahson.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wahson.entity.Orders;

public interface OrderService extends IService<Orders> {

    // 用户下单
    public void submit(Orders orders);
}
