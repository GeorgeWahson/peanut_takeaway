package com.wahson.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wahson.common.Result;
import com.wahson.entity.Employee;
import com.wahson.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

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

    /**
     * 员工点击退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        // 清除Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return Result.success("退出成功！");
    }

    @PostMapping
    public Result<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}", employee.toString());
        //设置初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employeeService.save(employee);

        return Result.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * 在控制器方法的形参位置，**设置和请求参数同名的形参**，
     * 当浏览器发送请求，匹配到请求映射时，在DispatcherServlet中就会
     * 将请求参数赋值给相应的形参
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        log.info("page: {}, pageSize: {}, name: {}", page, pageSize, name);

        // 构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper();
        lqw.like(StringUtils.isNotEmpty(name), Employee::getName, name);

        lqw.orderByDesc(Employee::getUpdateTime);

        employeeService.page(pageInfo, lqw);

        return Result.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public Result<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info(employee.toString());
        log.info("线程id为：{}", Thread.currentThread().getId());

        // 雪花算法id js对long型精度遗失
        employeeService.updateById(employee);

        return Result.success("员工信息修改成功!");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息...");
        Employee empById = employeeService.getById(id);
        if(empById != null){
            return Result.success(empById);
        }
        return Result.error("没有查询到对应员工信息");
    }



}
