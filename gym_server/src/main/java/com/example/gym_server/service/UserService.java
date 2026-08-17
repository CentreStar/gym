package com.example.gym_server.service;

import com.example.gym_server.constant.ErrorCode;
import com.example.gym_server.constant.RoleConstant;
import com.example.gym_server.constant.StatusConstant;
import com.example.gym_server.entity.User;
import com.example.gym_server.exception.BusinessException;
import com.example.gym_server.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw BusinessException.of(ErrorCode.USERNAME_EXISTS, "用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(RoleConstant.USER);
        user.setStatus(StatusConstant.NORMAL);
        user.setCreateTime(java.time.LocalDateTime.now());
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在"));
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.of(ErrorCode.PASSWORD_WRONG, "用户名或密码错误"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw BusinessException.of(ErrorCode.PASSWORD_WRONG, "用户名或密码错误");
        }
        if (StatusConstant.DISABLED.equals(user.getStatus())) {
            throw BusinessException.of(ErrorCode.ACCESS_DENIED, "账户已被禁用");
        }
        return user;
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.of(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw BusinessException.of(ErrorCode.PASSWORD_WRONG, "旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
