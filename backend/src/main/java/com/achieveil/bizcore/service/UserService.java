package com.achieveil.bizcore.service;

import com.achieveil.bizcore.dto.LoginRequest;
import com.achieveil.bizcore.dto.RegisterRequest;
import com.achieveil.bizcore.dto.UserVO;
import com.achieveil.bizcore.entity.User;
import com.achieveil.bizcore.repository.UserRepository;
import com.achieveil.bizcore.util.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserVO register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        PasswordPolicy.ensureWithinSupportedLength(request.getPassword());

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setSecurityQuestion(request.getSecurityQuestion());
        user.setSecurityAnswer(request.getSecurityAnswer());
        user.setRole("USER");
        user.setStatus(1);

        User savedUser = userRepository.save(user);
        return convertToVO(savedUser);
    }

    public UserVO login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (user.getStatus() == 0) {
            throw new RuntimeException("账户已被禁用");
        }

        PasswordPolicy.ensureWithinSupportedLength(request.getPassword());
        try {
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("用户名或密码错误");
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("密码长度超过当前账号支持的范围，请重置密码");
        }

        return convertToVO(user);
    }

    public UserVO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToVO(user);
    }

    public List<UserVO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        PasswordPolicy.ensureWithinSupportedLength(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(String username, String securityAnswer, String newPassword, String phoneMiddle) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证密保答案 - 忽略大小写和前后空格
        if (user.getSecurityAnswer() == null || user.getSecurityAnswer().trim().isEmpty()) {
            throw new RuntimeException("用户未设置密保问题");
        }
        if (!securityAnswer.trim().equalsIgnoreCase(user.getSecurityAnswer().trim())) {
            throw new RuntimeException("密保答案错误");
        }

        // 验证手机号中间四位
        String phone = user.getPhone();
        if (phone == null || phone.length() < 11) {
            throw new RuntimeException("用户未绑定手机号");
        }
        // 提取手机号中间四位（索引3-6）
        String actualMiddle = phone.substring(3, 7);
        if (!actualMiddle.equals(phoneMiddle)) {
            throw new RuntimeException("手机号验证失败");
        }

        PasswordPolicy.ensureWithinSupportedLength(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public String getSecurityQuestion(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return user.getSecurityQuestion();
    }

    /**
     * 验证密保答案
     */
    public void verifySecurityAnswer(String username, String securityAnswer) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证密保答案 - 忽略大小写和前后空格
        if (user.getSecurityAnswer() == null || user.getSecurityAnswer().trim().isEmpty()) {
            throw new RuntimeException("用户未设置密保问题");
        }
        if (!securityAnswer.trim().equalsIgnoreCase(user.getSecurityAnswer().trim())) {
            throw new RuntimeException("密保答案错误");
        }
    }

    /**
     * 验证手机号
     */
    public void verifyPhone(String username, String phoneMiddle) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String phone = user.getPhone();
        if (phone == null || phone.length() < 11) {
            throw new RuntimeException("用户未绑定手机号");
        }
        // 提取手机号中间四位（索引3-6）
        String actualMiddle = phone.substring(3, 7);
        if (!actualMiddle.equals(phoneMiddle)) {
            throw new RuntimeException("手机号验证失败");
        }
    }

    /**
     * 获取脱敏的手机号
     */
    public String getMaskedPhone(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        String phone = user.getPhone();
        if (phone == null || phone.length() < 11) {
            return "***-****-****";
        }
        // 显示前3位和后4位，中间4位用*隐藏
        return phone.substring(0, 3) + "-****-" + phone.substring(7);
    }

    /**
     * 验证手机号中间四位
     */
    public boolean verifyPhoneMiddle(String username, String phoneMiddle) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        String phone = user.getPhone();
        if (phone == null || phone.length() < 11) {
            throw new RuntimeException("用户未绑定手机号");
        }
        // 提取手机号中间四位（索引3-6）
        String actualMiddle = phone.substring(3, 7);
        return actualMiddle.equals(phoneMiddle);
    }

    /**
     * 管理员重置用户密码
     */
    @Transactional
    public void adminResetPassword(Long userId, String newPassword, Long adminId) {
        // 验证管理员权限
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));

        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("无权限执行此操作");
        }

        // 重置用户密码
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        PasswordPolicy.ensureWithinSupportedLength(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreatedTime(user.getCreatedTime());
        return vo;
    }
}
