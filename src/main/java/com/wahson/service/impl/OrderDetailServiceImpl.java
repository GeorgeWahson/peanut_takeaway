package com.wahson.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wahson.entity.OrderDetail;
import com.wahson.mapper.OrderDetailMapper;
import com.wahson.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
