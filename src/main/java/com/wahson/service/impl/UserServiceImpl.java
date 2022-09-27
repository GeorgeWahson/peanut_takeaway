package com.wahson.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wahson.entity.User;
import com.wahson.mapper.UserMapper;
import com.wahson.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
