package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.User;
import com.library.service.UserService;
import com.library.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/page")
    public Result<PageResult<User>> getUserPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestAttribute Long userId,
            @RequestAttribute Integer role) {
        
        PageResult<User> result = userService.getUserPage(page, size, keyword);
        
        if (role != 1) {
            result.setRecords(result.getRecords().stream()
                    .filter(u -> u.getId().equals(userId))
                    .collect(java.util.stream.Collectors.toList()));
            result.setTotal((long) result.getRecords().size());
        }
        
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id, @RequestAttribute Long userId, @RequestAttribute Integer role) {
        if (role != 1 && !id.equals(userId)) {
            return Result.error("无权限查看该用户信息");
        }
        
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    @PostMapping
    public Result<Void> addUser(@RequestBody User user, @RequestAttribute Integer role) {
        log.info("新增用户: {}", user.getUsername());
        
        if (role != 1) {
            return Result.error("无权限添加用户");
        }
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (user.getPassword().length() < 6) {
            return Result.error("密码长度不能少于6位");
        }
        
        User existUser = userService.getByUsername(user.getUsername());
        if (existUser != null) {
            return Result.error("用户名已存在");
        }
        
        user.setPassword(PasswordUtil.encode(user.getPassword()));
        
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getRole() == null) {
            user.setRole(0);
        }
        
        boolean success = userService.save(user);
        if (success) {
            log.info("用户添加成功: {}", user.getUsername());
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user, @RequestAttribute Long userId, @RequestAttribute Integer role) {
        log.info("更新用户信息: {}", id);
        
        if (role != 1 && !id.equals(userId)) {
            return Result.error("无权限修改该用户信息");
        }
        
        user.setId(id);
        
        User existUser = userService.getById(id);
        if (existUser == null) {
            return Result.error("用户不存在");
        }
        
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            if (user.getPassword().length() < 6) {
                return Result.error("密码长度不能少于6位");
            }
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        } else {
            user.setPassword(existUser.getPassword());
        }
        
        if (role != 1) {
            user.setRole(existUser.getRole());
            user.setStatus(existUser.getStatus());
        }
        
        boolean success = userService.updateById(user);
        if (success) {
            log.info("用户更新成功: {}", id);
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id, @RequestAttribute Long userId, @RequestAttribute Integer role) {
        log.info("删除用户: {}", id);
        
        if (role != 1) {
            return Result.error("无权限删除用户");
        }
        
        if (id.equals(userId)) {
            return Result.error("不能删除自己的账号");
        }
        
        boolean success = userService.removeById(id);
        if (success) {
            log.info("用户删除成功: {}", id);
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status, 
                                          @RequestAttribute Long userId, @RequestAttribute Integer role) {
        log.info("更新用户状态: {}, 状态: {}", id, status);
        
        if (role != 1) {
            return Result.error("无权限修改用户状态");
        }
        
        if (id.equals(userId)) {
            return Result.error("不能修改自己的状态");
        }
        
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        boolean success = userService.updateById(user);
        if (success) {
            return Result.success("状态更新成功");
        }
        return Result.error("状态更新失败");
    }

    @PostMapping("/{id}/password")
    public Result<Void> updatePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestAttribute Long userId) {
        
        if (!id.equals(userId)) {
            return Result.error("只能修改自己的密码");
        }
        
        log.info("修改用户密码: {}", id);
        boolean success = userService.updatePassword(id, oldPassword, newPassword);
        if (success) {
            log.info("密码修改成功: {}", id);
            return Result.success("密码修改成功");
        }
        return Result.error("密码修改失败");
    }
}
