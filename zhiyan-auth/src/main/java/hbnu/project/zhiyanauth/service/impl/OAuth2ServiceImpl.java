package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyanauth.model.dto.TokenDTO;
import hbnu.project.zhiyanauth.model.dto.UserDTO;
import hbnu.project.zhiyanauth.model.entity.User;
import hbnu.project.zhiyanauth.model.enums.UserStatus;
import hbnu.project.zhiyanauth.model.response.UserLoginResponse;
import hbnu.project.zhiyanauth.repository.UserRepository;
import hbnu.project.zhiyanauth.service.AuthService;
import hbnu.project.zhiyanauth.service.OAuth2Service;
import hbnu.project.zhiyanauth.service.RoleService;
import hbnu.project.zhiyanactivelog.model.entity.LoginLog;
import hbnu.project.zhiyanactivelog.model.enums.LoginStatus;
import hbnu.project.zhiyanactivelog.model.enums.LoginType;
import hbnu.project.zhiyanactivelog.service.OperationLogplusService;
import hbnu.project.zhiyancommonoauth.exception.OAuth2Exception;
import hbnu.project.zhiyancommonoauth.model.dto.OAuth2UserInfo;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.utils.ServletUtils;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonbasic.utils.ip.IpUtils;
import hbnu.project.zhiyancommonsecurity.utils.PasswordUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

/**
 * OAuth2第三方登录服务实现类
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogplusService operationLogService;

    /**
     * 处理OAuth2登录/注册
     */
    @Override
    @Transactional
    public R<UserLoginResponse> handleOAuth2Login(OAuth2UserInfo oauth2UserInfo) {
        log.info("处理OAuth2登录 - 提供商: {}, 用户ID: {}, 邮箱: {}", 
                oauth2UserInfo.getProvider(), oauth2UserInfo.getProviderUserId(), oauth2UserInfo.getEmail());

        try {
            // 1. 验证OAuth2用户信息
            validateOAuth2UserInfo(oauth2UserInfo);

            // 2. 查找或创建用户
            User user = findOrCreateUser(oauth2UserInfo);

            // 3. 检查用户状态
            if (user.getIsLocked()) {
                recordLoginLog(user.getId(), user.getEmail(), LoginStatus.FAILED, "账户已被锁定");
                return R.fail("账户已被锁定，请联系管理员");
            }

            if (user.getIsDeleted()) {
                recordLoginLog(user.getId(), user.getEmail(), LoginStatus.FAILED, "账户已被删除");
                return R.fail("账户不存在");
            }

            // 4. 生成JWT Token（不记住我，OAuth2登录默认不记住）
            boolean rememberMe = false;
            TokenDTO tokenDTO = authService.generateTokens(user.getId(), rememberMe);

            // 5. 获取用户角色信息
            var rolesResult = roleService.getUserRoles(user.getId());
            var roleNames = rolesResult.getData() != null 
                    ? new ArrayList<>(rolesResult.getData()) 
                    : new ArrayList<String>();

            // 6. 构建用户DTO
            UserDTO userDTO = UserDTO.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .avatarUrl(user.getAvatarUrl())
                    .title(user.getTitle())
                    .institution(user.getInstitution())
                    .roles(roleNames)
                    .permissions(new ArrayList<>()) // 权限需要单独查询，这里先留空
                    .build();

            // 7. 构建登录响应
            UserLoginResponse response = UserLoginResponse.builder()
                    .user(userDTO)
                    .accessToken(tokenDTO.getAccessToken())
                    .refreshToken(tokenDTO.getRefreshToken())
                    .expiresIn(tokenDTO.getExpiresIn())
                    .tokenType(tokenDTO.getTokenType())
                    .rememberMe(rememberMe)
                    .rememberMeToken(null) // OAuth2登录不使用RememberMe
                    .build();

            log.info("OAuth2登录成功 - 用户ID: {}, 邮箱: {}, 提供商: {}", 
                    user.getId(), user.getEmail(), oauth2UserInfo.getProvider());

            // 8. 记录登录日志
            recordLoginLog(user.getId(), user.getEmail(), LoginStatus.SUCCESS, null);

            return R.ok(response, "登录成功");

        } catch (OAuth2Exception e) {
            log.error("OAuth2登录失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("OAuth2登录异常", e);
            return R.fail("登录失败，请稍后重试");
        }
    }

    /**
     * 验证OAuth2用户信息
     */
    private void validateOAuth2UserInfo(OAuth2UserInfo oauth2UserInfo) {
        if (oauth2UserInfo == null) {
            throw new OAuth2Exception("OAuth2用户信息不能为空");
        }

        if (StringUtils.isEmpty(oauth2UserInfo.getProvider())) {
            throw new OAuth2Exception("OAuth2提供商不能为空");
        }

        if (StringUtils.isEmpty(oauth2UserInfo.getProviderUserId())) {
            throw new OAuth2Exception("OAuth2用户ID不能为空");
        }

        // 邮箱可以为空（某些OAuth2提供商可能不提供邮箱）
        // 但如果邮箱为空，我们需要使用其他方式标识用户
        if (StringUtils.isEmpty(oauth2UserInfo.getEmail()) && StringUtils.isEmpty(oauth2UserInfo.getUsername())) {
            throw new OAuth2Exception("OAuth2用户信息不完整：缺少邮箱或用户名");
        }
    }

    /**
     * 查找或创建用户
     * 优先通过邮箱匹配，如果邮箱为空则通过用户名匹配
     */
    private User findOrCreateUser(OAuth2UserInfo oauth2UserInfo) {
        // 1. 优先通过邮箱查找用户
        if (StringUtils.isNotEmpty(oauth2UserInfo.getEmail())) {
            Optional<User> userOpt = userRepository.findByEmail(oauth2UserInfo.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                log.info("找到已存在用户（通过邮箱匹配） - 用户ID: {}, 邮箱: {}", user.getId(), user.getEmail());
                
                // 更新用户信息（头像、昵称等可能变化）
                updateUserFromOAuth2(user, oauth2UserInfo);
                return userRepository.save(user);
            }
        }

        // 2. 如果邮箱为空或未找到，尝试通过用户名查找（GitHub的login字段）
        if (StringUtils.isNotEmpty(oauth2UserInfo.getUsername())) {
            Optional<User> userOpt = userRepository.findByNameAndIsDeletedFalse(oauth2UserInfo.getUsername());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                log.info("找到已存在用户（通过用户名匹配） - 用户ID: {}, 用户名: {}", user.getId(), user.getName());
                
                // 如果用户没有邮箱，更新邮箱
                if (StringUtils.isEmpty(user.getEmail()) && StringUtils.isNotEmpty(oauth2UserInfo.getEmail())) {
                    user.setEmail(oauth2UserInfo.getEmail());
                }
                
                updateUserFromOAuth2(user, oauth2UserInfo);
                return userRepository.save(user);
            }
        }

        // 3. 用户不存在，创建新用户
        log.info("用户不存在，创建新用户 - 提供商: {}, 邮箱: {}", 
                oauth2UserInfo.getProvider(), oauth2UserInfo.getEmail());

        User newUser = createUserFromOAuth2(oauth2UserInfo);
        User savedUser = userRepository.save(newUser);

        // 4. 为新用户分配默认角色
        assignDefaultRoleToUser(savedUser.getId());

        log.info("新用户创建成功 - 用户ID: {}, 邮箱: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    /**
     * 从OAuth2用户信息创建新用户
     */
    private User createUserFromOAuth2(OAuth2UserInfo oauth2UserInfo) {
        // 确定邮箱：优先使用OAuth2邮箱，如果为空则使用用户名+@placeholder.com
        String email = oauth2UserInfo.getEmail();
        if (StringUtils.isEmpty(email)) {
            email = oauth2UserInfo.getUsername() + "@oauth2.placeholder.com";
            log.warn("OAuth2用户邮箱为空，使用占位符邮箱: {}", email);
        }

        // 确定用户名：优先使用昵称，其次使用用户名
        String name = StringUtils.isNotEmpty(oauth2UserInfo.getNickname()) 
                ? oauth2UserInfo.getNickname() 
                : oauth2UserInfo.getUsername();

        // 生成随机密码（OAuth2用户不需要密码，但数据库字段要求非空）
        // 使用UUID生成随机密码，确保安全性
        String randomPassword = java.util.UUID.randomUUID().toString().replace("-", "") + 
                                java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        String passwordHash = passwordEncoder.encode(randomPassword);

        return User.builder()
                .email(email)
                .passwordHash(passwordHash)
                .name(name)
                .avatarUrl(oauth2UserInfo.getAvatarUrl())
                .title(null) // OAuth2不提供职称信息
                .institution(null) // OAuth2不提供机构信息
                .status(UserStatus.ACTIVE)
                .isDeleted(false)
                .isLocked(false)
                .build();
    }

    /**
     * 更新用户信息（从OAuth2获取的最新信息）
     */
    private void updateUserFromOAuth2(User user, OAuth2UserInfo oauth2UserInfo) {
        // 更新头像（如果OAuth2提供了新头像）
        if (StringUtils.isNotEmpty(oauth2UserInfo.getAvatarUrl())) {
            user.setAvatarUrl(oauth2UserInfo.getAvatarUrl());
        }

        // 更新昵称（如果OAuth2提供了昵称且与当前不同）
        if (StringUtils.isNotEmpty(oauth2UserInfo.getNickname()) 
                && !oauth2UserInfo.getNickname().equals(user.getName())) {
            // 可以选择更新或不更新，这里选择不自动更新（避免覆盖用户手动修改的名称）
            // user.setName(oauth2UserInfo.getNickname());
        }

        // 如果用户没有邮箱，且OAuth2提供了邮箱，则更新
        if (StringUtils.isEmpty(user.getEmail()) && StringUtils.isNotEmpty(oauth2UserInfo.getEmail())) {
            user.setEmail(oauth2UserInfo.getEmail());
        }
    }

    /**
     * 为新用户分配默认角色
     */
    private void assignDefaultRoleToUser(Long userId) {
        try {
            log.info("为新用户分配默认角色 - 用户ID: {}", userId);
            R<Long> roleResult = roleService.getRoleIdByName("USER");
            
            if (R.isSuccess(roleResult) && roleResult.getData() != null) {
                Long roleId = roleResult.getData();
                R<Void> assignResult = roleService.assignRolesToUser(userId, java.util.List.of(roleId));
                
                if (R.isSuccess(assignResult)) {
                    log.info("成功为用户分配默认角色 USER - 用户ID: {}", userId);
                } else {
                    log.warn("为用户分配默认角色失败 - 用户ID: {}, 错误: {}", userId, assignResult.getMsg());
                }
            } else {
                log.warn("未找到 USER 角色，无法分配默认角色 - 用户ID: {}", userId);
            }
        } catch (Exception e) {
            log.error("为用户分配默认角色发生异常 - 用户ID: {}", userId, e);
        }
    }

    /**
     * 记录登录日志
     */
    private void recordLoginLog(Long userId, String email, LoginStatus status, String failureReason) {
        try {
            HttpServletRequest request = ServletUtils.getRequest();
            String ipAddress = request != null ? IpUtils.getIpAddr(request) : null;
            String userAgent = request != null ? ServletUtils.getHeader(request, "User-Agent") : null;

            LoginLog loginLog = LoginLog.builder()
                    .userId(userId != null ? userId : 0L)
                    .username(email)
                    .loginType(LoginType.OAUTH) // OAuth2登录类型
                    .loginIp(ipAddress)
                    .userAgent(userAgent)
                    .loginStatus(status)
                    .failureReason(failureReason)
                    .loginTime(java.time.LocalDateTime.now())
                    .build();

            operationLogService.saveLoginLog(loginLog);
            log.debug("OAuth2登录日志记录成功 - 用户ID: {}, 状态: {}", userId, status);
        } catch (Exception e) {
            log.error("记录OAuth2登录日志失败 - 邮箱: {}, 状态: {}", email, status, e);
        }
    }
}

