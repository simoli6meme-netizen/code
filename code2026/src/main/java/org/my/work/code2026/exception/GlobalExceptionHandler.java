package org.my.work.code2026.exception;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.my.work.code2026.common.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice(basePackages = "org.my.work.code2026.controller")
public class GlobalExceptionHandler {

    private static final Log log = LogFactory.get();


    //统一异常处理@ExceptionHandler,主要用于Exception
    @ExceptionHandler(Exception.class)
    @ResponseBody//返回json串
    public Result error(HttpServletRequest request, Exception e) {
        log.error("异常信息：", e);
        // 把真实原因带回去，方便排查（只取前 120 个字符，避免把整段 SQL 抛给前端）
        String msg = e.getMessage();
        if (msg != null && !msg.isBlank()) {
            msg = msg.length() > 120 ? msg.substring(0, 120) + "…" : msg;
            return Result.error("请求失败：" + msg);
        }
        return Result.error();
    }

    @ExceptionHandler(CustomException.class)
    @ResponseBody//返回json串
    public Result customError(HttpServletRequest request, CustomException e) {
        return Result.error(e.getMsg());
    }

}
