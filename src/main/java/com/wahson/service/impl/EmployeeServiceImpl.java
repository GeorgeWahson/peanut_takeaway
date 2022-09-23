package com.wahson.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wahson.entity.Employee;
import com.wahson.mapper.EmployeeMapper;
import com.wahson.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService{
}
