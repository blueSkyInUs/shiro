package com.init;

import com.init.config.SimpleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@SpringBootApplication
@Slf4j
@MapperScan(basePackages = {"com.mybatis"})
public class HelloController {

    @Autowired
    private SimpleService simpleService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String QUEUE_NAME="lesson";


    @RequestMapping("/read")
    @ResponseBody
    String read(String msg) {
        System.out.println(msg);
        simpleService.readRestrictedCall();

        rabbitTemplate.convertAndSend("lesson","hello");

        return "Hello World!";
    }

    @RequestMapping("/write")
    @ResponseBody
    String write() {
        simpleService.writeRestrictedCall();
        return "Hello World!11111111111111";
    }

    @RequestMapping("/exception")
    String exceptionTest() {
        throw new RuntimeException("error");
    }

    @RequestMapping("/logout")
    @ResponseBody
    String logout() {
        SecurityUtils.getSubject().logout();
        return "logout";
    }

    @Bean
    Queue getQueue(){
        return new Queue(QUEUE_NAME);
    }


    @RequestMapping(value = "/login", method = RequestMethod.POST)
    String loginFromPost(@RequestParam String username, @RequestParam String password,@RequestParam Boolean rememberMe) {
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password,rememberMe);
        subject.login(token);
        return "redirect:/read";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    String loginFromGet() {
        return "/login";
    }



    public static void main(String[] args) throws Exception {
        SpringApplication.run(HelloController.class, args);
    }
}
