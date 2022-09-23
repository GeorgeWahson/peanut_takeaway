package com.wahson.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wahson.common.Result;
import com.wahson.entity.Employee;
import com.wahson.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Employee::getUsername, employee.getUsername());
        Employee emp_db = employeeService.getOne(lqw);

        //3、如果没有查询到则返回登录失败结果
        if (emp_db == null) {
            // 不直接说明原因防止撞库
            return Result.error("登录失败!!");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if (!emp_db.getPassword().equals(password)) {
            return Result.error("登录失败!");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp_db.getStatus() == 0) {
            return Result.error("账号已禁用!");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", emp_db.getId());
        return Result.success(emp_db);
    }
}
