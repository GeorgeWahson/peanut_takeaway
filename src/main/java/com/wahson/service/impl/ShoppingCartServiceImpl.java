package com.wahson.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wahson.entity.ShoppingCart;
import com.wahson.mapper.ShoppingCartMapper;
import com.wahson.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
