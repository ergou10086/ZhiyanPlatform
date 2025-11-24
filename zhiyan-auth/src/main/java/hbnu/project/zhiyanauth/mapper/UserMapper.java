package hbnu.project.zhiyanauth.mapper;

import hbnu.project.zhiyanauth.model.dto.AvatarDTO;
import hbnu.project.zhiyanauth.model.dto.UserAchievementDTO;
import hbnu.project.zhiyanauth.model.dto.UserDTO;
import hbnu.project.zhiyanauth.model.entity.User;
import hbnu.project.zhiyanauth.model.entity.UserAchievement;
import hbnu.project.zhiyanauth.model.entity.UserRole;
import hbnu.project.zhiyanauth.model.form.RegisterBody;
import hbnu.project.zhiyanauth.model.form.UserProfileUpdateBody;
import hbnu.project.zhiyancommonbasic.utils.JsonUtils;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户实体转换器
 * 使用MapStruct提供User实体与UserDTO之间的高效转换功能
 *
 * @author ErgouTree
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * 将User实体转换为UserDTO
     * 基础转换，不包含角色和权限信息
     * 特别处理 avatarUrl：从JSON中提取可直接访问的URL
     */
    @Named("toDTO")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "avatarUrl", expression = "java(extractAvatarUrlFromJson(user.getAvatarUrl()))")
    @Mapping(target = "researchTags", expression = "java(user.getResearchTagList())")
    UserDTO toDTO(User user);

    /**
     * 将User实体转换为包含角色和权限的UserDTO
     * 完整转换，包含用户的角色和权限信息
     */
    @Named("toDTOWithRolesAndPermissions")
    @Mapping(target = "roles", expression = "java(extractRoleNames(user.getUserRoles()))")
    @Mapping(target = "permissions", expression = "java(extractPermissionNames(user.getUserRoles()))")
    @Mapping(target = "avatarUrl", expression = "java(extractAvatarUrlFromJson(user.getAvatarUrl()))")
    @Mapping(target = "researchTags", expression = "java(user.getResearchTagList())")
    UserDTO toDTOWithRolesAndPermissions(User user);

    /**
     * 将User实体转换为包含角色的UserDTO，权限单独设置
     * 避免懒加载问题
     */
    @Named("toDTOWithRoles")
    @Mapping(target = "roles", expression = "java(extractRoleNames(user.getUserRoles()))")
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "avatarUrl", expression = "java(extractAvatarUrlFromJson(user.getAvatarUrl()))")
    @Mapping(target = "researchTags", expression = "java(user.getResearchTagList())")
    UserDTO toDTOWithRoles(User user);

    /**
     * 从avatarUrl JSON中提取可直接访问的URL
     * avatarUrl可能是两种格式：
     * 1. JSON格式：{"minioUrl":"...", "cdnUrl":"...", "sizes":{...}}
     * 2. 直接URL：http://...
     * 优先返回 cdnUrl，其次是 minioUrl，最后返回原始值
     */
    default String extractAvatarUrlFromJson(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return null;
        }

        try {
            // 尝试解析为JSON
            AvatarDTO avatarDTO = JsonUtils.parseObject(avatarUrl, AvatarDTO.class);
            if (avatarDTO != null) {
                // 优先返回 cdnUrl，其次是 minioUrl
                if (avatarDTO.getCdnUrl() != null && !avatarDTO.getCdnUrl().isEmpty()) {
                    return avatarDTO.getCdnUrl();
                }
                if (avatarDTO.getMinioUrl() != null && !avatarDTO.getMinioUrl().isEmpty()) {
                    return avatarDTO.getMinioUrl();
                }
            }
        } catch (Exception e) {
            // JSON解析失败，说明是直接的URL，直接返回
        }

        // 如果不是JSON或解析失败，直接返回原值（可能是URL）
        return avatarUrl;
    }

    /**
     * 从UserRole关联中提取角色名称列表
     */
    default List<String> extractRoleNames(List<UserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        return userRoles.stream()
                .map(ur -> ur.getRole().getName())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 从UserRole关联中提取权限名称列表
     */
    default List<String> extractPermissionNames(List<UserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        return userRoles.stream()
                .flatMap(ur -> ur.getRole().getRolePermissions().stream())
                .map(rp -> rp.getPermission().getName()).distinct().collect(Collectors.toList());
    }

    /**
     * 将User实体列表转换为UserDTO列表
     */
    @IterableMapping(qualifiedByName = "toDTO")
    List<UserDTO> toDTOList(List<User> users);

    /**
     * 将注册表单转换为User实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "isLocked", constant = "false")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "institution", ignore = true)
    @Mapping(target = "status", ignore = true)
    User fromRegisterBody(RegisterBody registerBody, String passwordHash);

    /**
     * 从UserProfileUpdateBody更新User实体
     * 允许更新姓名、职称、机构、头像等字段
     *
     * @param user 目标用户实体
     * @param updateBody 更新表单
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "isLocked", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "researchTags", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserProfile(@MappingTarget User user, UserProfileUpdateBody updateBody);

    /**
     * 创建简化的UserDTO
     * 只包含基础信息，用于列表展示
     *
     * @param user 用户实体
     * @return 简化的UserDTO
     */
    @Named("toSimpleDTO")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "avatarUrl", expression = "java(extractAvatarUrlFromJson(user.getAvatarUrl()))")
    @Mapping(target = "researchTags", expression = "java(user.getResearchTagList())")
    UserDTO toSimpleDTO(User user);



    /**
     * 将UserAchievement实体转换为UserAchievementDTO（基础转换）
     */
    @Named("toDTO")
    @Mapping(target = "id", expression = "java(userAchievement.getId().toString())")
    @Mapping(target = "userId", expression = "java(userAchievement.getUserId().toString())")
    @Mapping(target = "achievementId", expression = "java(userAchievement.getAchievementId().toString())")
    @Mapping(target = "projectId", expression = "java(userAchievement.getProjectId().toString())")
    @Mapping(target = "achievementTitle", ignore = true)
    @Mapping(target = "achievementType", ignore = true)
    @Mapping(target = "achievementStatus", ignore = true)
    UserAchievementDTO toUserAchievementDTO(UserAchievement userAchievement);


    /**
     * 从成果数据中提取成果标题
     */
    default String extractAchievementTitle(Map<String, Object> achievementData) {
        if (achievementData != null && achievementData.containsKey("title")) {
            return (String) achievementData.get("title");
        }
        return null;
    }

    /**
     * 从成果数据中提取成果类型
     */
    default String extractAchievementType(Map<String, Object> achievementData) {
        if (achievementData != null && achievementData.containsKey("type")) {
            return (String) achievementData.get("type");
        }
        return null;
    }

    /**
     * 从成果数据中提取成果状态
     */
    default String extractAchievementStatus(Map<String, Object> achievementData) {
        if (achievementData != null && achievementData.containsKey("status")) {
            return (String) achievementData.get("status");
        }
        return null;
    }
}

