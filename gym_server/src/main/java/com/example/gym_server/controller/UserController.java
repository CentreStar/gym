package com.example.gym_server.controller;

import com.example.gym_server.dto.ApiResponse;
import com.example.gym_server.dto.LoginRequest;
import com.example.gym_server.dto.LoginResponse;
import com.example.gym_server.dto.PasswordChangeRequest;
import com.example.gym_server.entity.User;
import com.example.gym_server.service.UserService;
import com.example.gym_server.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // 注册接口（注册成功后自动登录）
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@RequestBody User user) {
        User saved = userService.register(user);
        return ApiResponse.ok(new LoginResponse(jwtUtil.generateToken(saved.getUsername()), saved));
    }

    // 查询用户
    @GetMapping("/{username}")
    public ApiResponse<User> getUser(@PathVariable String username) {
        return ApiResponse.ok(userService.findByUsername(username));
    }

    // 登录接口
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());
        return ApiResponse.ok(new LoginResponse(jwtUtil.generateToken(user.getUsername()), user));
    }

    // 修改密码
    @PostMapping("/password/change")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword());
        return ApiResponse.ok();
    }
}
