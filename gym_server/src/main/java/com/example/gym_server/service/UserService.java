package com.example.gym_server.service;

import com.example.gym_server.constant.RoleConstant;
import com.example.gym_server.constant.StatusConstant;
import com.example.gym_server.entity.User;
import com.example.gym_server.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

    }


    // 注册用户
    // 注册用户
    public User register(User user) {


        // 检查用户名是否存在
        Optional<User> existUser =
                userRepository.findByUsername(user.getUsername());


        if (existUser.isPresent()) {

            throw new RuntimeException("用户名已存在");

        }



        // 密码加密
        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );



        // 默认普通用户
        user.setRole(RoleConstant.USER);



        // 默认账号正常
        user.setStatus(StatusConstant.NORMAL);



        // 创建时间
        user.setCreateTime(
                java.time.LocalDateTime.now()
        );



        return userRepository.save(user);

    }



    // 根据用户名查询用户
    public User findByUsername(String username) {

        return userRepository.findByUsername(username)
                .orElse(null);

    }



    // 用户登录
    public User login(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElse(null);


        if (user == null) {
            throw new RuntimeException("用户不存在");
        }


        // BCrypt密码匹配
        if (!passwordEncoder.matches(
                password,
                user.getPassword()
        )) {

            throw new RuntimeException("密码错误");

        }


        return user;
    }

}