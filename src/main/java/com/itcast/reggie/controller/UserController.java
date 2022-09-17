package com.itcast.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itcast.reggie.common.R;
import com.itcast.reggie.entity.User;
import com.itcast.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    UserService userService;

    /**
     * 移动端用户登录
     * @param loginUser
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody User loginUser, HttpSession session){
        log.info("接收的手机号：{}",loginUser.getPhone());
        //获取手机号
        String phone = loginUser.getPhone();
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<User>();
        lqw.eq(User::getPhone,phone);

        User user = userService.getOne(lqw);
        if(user == null){
            //判断当前手机号对应的用户是否为新用户,如果是新用户就自动完成注册
            user = new User();
            user.setPhone(phone);
            user.setStatus(1);
            userService.save(user);
        }
        session.setAttribute("user", user.getId());
        //测试
        return R.success(user);
    }

    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session){
        session.removeAttribute("user");
        return R.success("退出登录成功");

    }
}
