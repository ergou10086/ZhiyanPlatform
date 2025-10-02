//package hbnu.project.zhiyancommonsecurity.service;
//
//import hbnu.project.zhiyanauth.model.entity.User;
//import hbnu.project.zhiyanauth.model.enums.PermissionModule;
//import hbnu.project.zhiyanauth.model.enums.SysRole;
//import hbnu.project.zhiyanauth.repository.UserRepository;
//import hbnu.project.zhiyancommonsecurity.context.LoginUserBody;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.*;

/**
 * 自定义用户详情服务
 * 实现Spring Security的UserDetailsService接口
 *给它拿到auth中去了，见UserDetailsService 抽象类
 * 前问题：common-security模块中的
 * CustomUserDetailsService和LoginFailureService是具体实现，
 * 且依赖了auth模块的类（如UserRepository, UserService），导致循环依赖。
 * @author ErgouTree
 */
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//
//    /**
//     * 重写 loadUserByUsername，根据用户邮箱加载用户详细信息
//     */
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        log.debug("开始加载用户信息: {}", email);
//
//        // 1. 查找用户基本信息
//        Optional<User> optionalUser = userRepository.findByEmailAndIsDeletedFalse(email);
//        if (optionalUser.isEmpty()) {
//            log.warn("用户不存在: {}", email);
//            throw new UsernameNotFoundException("用户不存在: " + email);
//        }
//
//        User user = optionalUser.get();
//
//        // 2. 检查用户状态（让Spring Security的认证流程处理）
//        if (Boolean.TRUE.equals(user.getIsLocked())) {
//            log.warn("用户账户已被锁定: {}", email);
//        }
//
//
//        // TODO：2. 加载用户权限（这是关键！）
//        // TODO: 初始给用户加载普通用户的角色，给予他对应的权限，当用户加入或创建项目的时候，再分给他项目相关的角色和权限
//        // 3. 加载用户权限和角色
//        Set<String> permissions = loadUserPermissions(user);
//        List<String> roles = loadUserRoles(user);
//
//        // 4. 构建 Spring Security 需要的权限集合
//        Collection<GrantedAuthority> authorities = buildAuthorities(roles, permissions);
//
//
//        // TODO：3. 构建 Spring Security 需要的 UserDetails 对象
//        // 5. 构建 LoginUserBody 对象
//        return LoginUserBody.builder()
//                .userId(user.getId())
//                .email(user.getEmail())
//                .name(user.getName())
//                .avatarUrl(user.getAvatarUrl())
//                .title(user.getTitle())
//                .institution(user.getInstitution())
//                .roles(roles)
//                .permissions(permissions)
//                .isLocked(user.getIsLocked())
//                .passwordHash(user.getPasswordHash())
//                .authorities(authorities)
//                .build();
//    }
//
//
//    /**
//     * 加载用户权限
//     * 根据用户的系统角色分配基础权限，项目相关权限需要在加入项目时动态添加
//     */
//    private Set<String> loadUserPermissions(User user) {
//        Set<String> permissions = new HashSet<>();
//
//        // 1. 根据用户注册状态分配基础权限
//        // 所有注册用户都有基础用户权限
//        permissions.addAll(PermissionModule.BASIC_USER.getPermissionStrings());
//
//        // 2. 检查是否为开发者（可以通过数据库字段或特殊标识判断）
//        if (isDeveloper(user)) {
//            // 开发者拥有系统管理员的所有权限
//            permissions.addAll(PermissionModule.SYSTEM_ADMIN.getPermissionStrings());
//            log.debug("用户[{}]具有开发者权限", user.getEmail());
//        }
//
//        // 3. 项目相关权限需要在用户加入项目时动态加载
//        // 这里可以预留接口，后续在项目服务中实现
//        // permissions.addAll(loadProjectPermissions(user.getId()));
//
//        log.debug("用户[{}]权限加载完成，共{}个权限: {}", user.getEmail(), permissions.size(), permissions);
//        return permissions;
//    }
//
//
//    /**
//     * 加载用户角色
//     */
//    private List<String> loadUserRoles(User user) {
//        List<String> roles = new ArrayList<>();
//
//        // 1. 检查是否为开发者
//        if (isDeveloper(user)) {
//            roles.add(SysRole.DEVELOPER.getCode());
//        } else {
//            // 2. 所有注册用户默认为普通用户
//            roles.add(SysRole.USER.getCode());
//        }
//
//        // 访客角色通常不会到这里，因为访客不会进行登录认证
//        log.debug("用户[{}]角色加载完成: {}", user.getEmail(), roles);
//        return roles;
//    }
//
//
//    /**
//     * 构建Spring Security权限集合
//     */
//    private Collection<GrantedAuthority> buildAuthorities(List<String> roles, Set<String> permissions) {
//        Set<GrantedAuthority> authorities = new HashSet<>();
//
//        // 添加角色权限（ROLE_前缀）
//        for (String role : roles) {
//            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
//        }
//
//        // 添加具体权限
//        for (String permission : permissions) {
//            authorities.add(new SimpleGrantedAuthority(permission));
//        }
//
//        return authorities;
//    }
//
//
//
//    /**
//     * 判断用户是否为开发者
//     * 可以根据邮箱域名、特定标识字段或数据库配置判断
//     */
//    private boolean isDeveloper(User user) {
//        // 方案1：通过邮箱判断（示例）
//        // return user.getEmail().endsWith("@company.com");
//
//        // 方案2：通过数据库中的角色关联判断
//        // 这里需要查询user_roles表，判断是否有DEVELOPER角色
//
//        // 方案3：临时方案，后续可以通过管理界面手动设置
//        // 可以在User实体中添加isDeveloper字段，或通过特殊配置判断
//
//        // 临时实现：假设有特定邮箱的用户为开发者
//        String[] developerEmails = {"admin@zhiyan.com", "developer@zhiyan.com"};
//        return Arrays.asList(developerEmails).contains(user.getEmail());
//    }
//
//
//    /**
//     * 加载项目相关权限（预留接口）
//     * 在用户加入或创建项目时，需要调用此方法动态添加项目权限
//     */
//    public Set<String> loadProjectPermissions(Long userId) {
//        Set<String> projectPermissions = new HashSet<>();
//
//        // TODO: 实现项目权限加载逻辑
//        // 1. 查询用户参与的所有项目
//        // 2. 根据用户在各项目中的角色（OWNER/MEMBER）添加相应权限
//        // 3. 示例：
//        // List<ProjectMember> projectMembers = projectMemberRepository.findByUserId(userId);
//        // for (ProjectMember member : projectMembers) {
//        //     if (member.getRole() == ProjectRole.OWNER) {
//        //         projectPermissions.addAll(ProjectRole.OWNER.getPermissions().stream()
//        //                 .map(SystemPermission::getPermission)
//        //                 .collect(Collectors.toList()));
//        //     } else if (member.getRole() == ProjectRole.MEMBER) {
//        //         projectPermissions.addAll(ProjectRole.MEMBER.getPermissions().stream()
//        //                 .map(SystemPermission::getPermission)
//        //                 .collect(Collectors.toList()));
//        //     }
//        // }
//
//        return projectPermissions;
//    }
//}