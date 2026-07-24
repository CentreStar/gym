package com.example.gym_server.controller;

import com.example.gym_server.dto.LoginRequest;
import com.example.gym_server.dto.LoginResponse;
import com.example.gym_server.entity.User;
import com.example.gym_server.service.UserService;
import com.example.gym_server.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {


    private final UserService userService;

    private final JwtUtil jwtUtil;

    public UserController(
            UserService userService,
            JwtUtil jwtUtil
    ) {

        this.userService = userService;
        this.jwtUtil = jwtUtil;

    }


    // 注册接口
    @PostMapping("/register")
    public String register(@RequestBody User user) {

        userService.register(user);

        return "注册成功";

    }


    // 查询用户
    @GetMapping("/{username}")
    public User getUser(@PathVariable String username) {

        return userService.findByUsername(username);

    }

    // 登录接口
    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request
    ){

        User user = userService.login(
                request.getUsername(),
                request.getPassword()
        );


        String token =
                jwtUtil.generateToken(user.getUsername());


        return new LoginResponse(token);

    }

}