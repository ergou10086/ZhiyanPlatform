package hbnu.project.zhiyanproject.service;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanproject.model.dto.ProjectMemberDTO;
import hbnu.project.zhiyanproject.model.dto.ProjectMemberDetailDTO;
import hbnu.project.zhiyanproject.model.dto.RoleInfoDTO;
import hbnu.project.zhiyanproject.model.entity.ProjectMember;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.model.form.InviteMemberRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * 项目成员服务接口
 * 项目成员采用直接邀请方式，无需申请审批流程
 *
 * @author ErgouTree
 * @author AI Assistant (增强版)
 */
public interface ProjectMemberService {

    // ==================== 成员管理相关 ====================

    /**
     * 邀请用户加入项目（项目负责人直接添加成员，无需对方同意）
     * 业务流程：负责人通过用户ID搜索并直接将用户添加为项目成员
     *
     * @param projectId 项目ID
     * @param request   邀请信息（包含用户ID和角色）
     * @param inviterId 邀请人ID（当前登录用户，必须是项目负责人）
     * @return 项目成员记录
     */
    ProjectMember inviteMember(Long projectId, InviteMemberRequest request, Long inviterId);

    /**
     * 添加项目成员（内部方法，不验证用户）
     * 用于系统内部调用，如创建项目时自动添加创建者
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @param role      角色枚举
     * @return 成员记录
     */
    ProjectMember addMemberInternal(Long projectId, Long userId, ProjectMemberRole role);

    /**
     * 添加项目成员（对外接口，验证用户存在）
     * 用于通过 API 添加成员，会验证用户是否存在
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @param role      角色枚举
     * @return 操作结果
     */
    R<Void> addMemberWithValidation(Long projectId, Long userId, ProjectMemberRole role);

    /**
     * 移除项目成员（新增重载方法，返回 R 类型）
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 操作结果
     */
    R<Void> removeMember(Long projectId, Long userId);

    /**
     * 移除项目成员（原有方法）
     * 业务流程：项目负责人移除成员
     *
     * @param projectId  项目ID
     * @param userId     用户ID
     * @param operatorId 操作人ID（当前登录用户）
     */
    void removeMember(Long projectId, Long userId, Long operatorId);

    /**
     * 更新成员角色（新增重载方法，返回 R 类型）
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @param newRole   新角色
     * @return 操作结果
     */
    R<Void> updateMemberRole(Long projectId, Long userId, ProjectMemberRole newRole);

    /**
     * 更新成员角色（原有方法）
     * 业务流程：项目负责人修改成员在项目中的角色
     *
     * @param projectId  项目ID
     * @param userId     用户ID
     * @param newRole    新角色
     * @param operatorId 操作人ID（当前登录用户）
     * @return 更新后的成员记录
     */
    ProjectMember updateMemberRole(Long projectId, Long userId, ProjectMemberRole newRole, Long operatorId);

    /**
     * 主动退出项目（成员自己退出）
     *
     * @param projectId 项目ID
     * @param userId    用户ID（当前登录用户）
     */
    void leaveProject(Long projectId, Long userId);

    // ==================== 查询相关 ====================

    /**
     * 获取项目成员列表（新增方法，返回 ProjectMemberDTO）
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return 成员列表
     */
    R<Page<ProjectMemberDTO>> getProjectMembers(Long projectId, Pageable pageable);

    /**
     * 获取我参与的所有项目
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 项目成员列表
     */
    Page<ProjectMember> getMyProjects(Long userId, Pageable pageable);

    /**
     * 获取项目中指定角色的成员
     *
     * @param projectId 项目ID
     * @param role      角色
     * @return 成员列表
     */
    List<ProjectMember> getMembersByRole(Long projectId, ProjectMemberRole role);

    /**
     * 检查用户是否为项目成员
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 是否为项目成员
     */
    boolean isMember(Long projectId, Long userId);

    /**
     * 检查用户是否为项目负责人
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 是否为项目负责人
     */
    boolean isOwner(Long projectId, Long userId);

    /**
     * 获取用户在项目中的角色
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 用户角色（Optional）
     */
    ProjectMemberRole getUserRole(Long projectId, Long userId);

    /**
     * 获取用户在项目中的角色（新增方法，返回 R 类型）
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @return 角色枚举
     */
    R<ProjectMemberRole> getMemberRole(Long projectId, Long userId);

    /**
     * 获取用户在项目中的角色信息（新增方法）
     *
     * @param userId    用户ID
     * @param projectId 项目ID
     * @return 角色信息 DTO
     */
    R<RoleInfoDTO> getUserRoleInfo(Long userId, Long projectId);

    /**
     * 获取用户在项目中的权限列表（新增方法）
     *
     * @param userId    用户ID
     * @param projectId 项目ID
     * @return 权限代码集合
     */
    R<Set<String>> getUserPermissions(Long userId, Long projectId);

    /**
     * 检查用户是否拥有指定权限（新增方法）
     *
     * @param userId         用户ID
     * @param projectId      项目ID
     * @param permissionCode 权限代码
     * @return 是否拥有权限
     */
    R<Boolean> hasPermission(Long userId, Long projectId, String permissionCode);

    /**
     * 获取项目成员数量
     *
     * @param projectId 项目ID
     * @return 成员数量
     */
    long getMemberCount(Long projectId);
}
