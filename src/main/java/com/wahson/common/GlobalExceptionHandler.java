package com.wahson.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理类
 */
@Slf4j
@RestControllerAdvice
// 上面一行 和下面两行 二选一
//@ControllerAdvice(annotations = {RestController.class, Controller.class})
//@RequestBody
public class GlobalExceptionHandler {
    /**
     * 异常处理方法
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException e) {
        log.error(e.getMessage());
        // 如果报错信息中有“Duplicate entry”，说明用户名或分类名重复
        if (e.getMessage().contains("Duplicate entry")) {
            String[] split = e.getMessage().split(" ");
            // Duplicate entry 'zhangsan'
            String msg = "【" + split[2].substring(1, split[2].length()-1) + "】已存在，请换个名称!";
            return Result.error(msg);
        }
        return Result.error("未知错误。。。");
    }

    /**
     * 自定义异常处理方法
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public Result<String> exceptionHandler(CustomException e) {
        log.error(e.getMessage());

        return Result.error(e.getMessage());
    }
}
