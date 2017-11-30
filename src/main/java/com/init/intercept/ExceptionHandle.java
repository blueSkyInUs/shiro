package com.init.intercept;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
@Slf4j
public class ExceptionHandle {


    public static final String DEFAULT_LOGIN_PAGE = "login";


    public static final String DEFAULT_ERROR_PAGE = "default_error";

    /**
     * 非法访问强制跳到登录页
     *
     * @param exp
     * @return
     * @throws Exception
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView doHandle(Exception exp) throws Exception {
        log.error("illegal access:  " + exp.getMessage(), exp);
        ModelAndView modelAndView = new ModelAndView();

        if (exp instanceof AuthorizationException) {
            modelAndView.addObject("msg", "权限不够,请以其他用户登录");
            modelAndView.setViewName(DEFAULT_LOGIN_PAGE);
        } else if (exp instanceof AuthenticationException) {
            modelAndView.addObject("msg", "用户名或者密码不对");
            modelAndView.setViewName(DEFAULT_LOGIN_PAGE);
        } else {
            modelAndView.setViewName(DEFAULT_ERROR_PAGE);
        }
        return modelAndView;
    }


}
