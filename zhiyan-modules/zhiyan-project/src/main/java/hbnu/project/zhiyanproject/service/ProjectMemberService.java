package hbnu.project.zhiyanproject.service;

import hbnu.project.zhiyanproject.model.dto.ProjectMemberDetailDTO;
import hbnu.project.zhiyanproject.model.entity.ProjectMember;
import hbnu.project.zhiyanproject.model.enums.ProjectMemberRole;
import hbnu.project.zhiyanproject.model.form.InviteMemberRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 项目成员服务接口
 * 项目成员采用直接邀请方式，无需申请审批流程
 *
 * @author ErgouTree
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
     * 移除项目成员
     * 业务流程：项目负责人移除成员
     *
     * @param projectId 项目ID
     * @param userId    用户ID
     * @param operatorId 操作人ID（当前登录用户）
     */
    void removeMember(Long projectId, Long userId, Long operatorId);

    /**
     * 更新成员角色
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
     * 获取项目成员列表（含详细信息）
     * 业务流程：在项目详情页的"成员"标签页展示
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return 成员详细信息分页列表
     */
    Page<ProjectMemberDetailDTO> getProjectMembers(Long projectId, Pageable pageable);

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
     * 获取项目成员数量
     *
     * @param projectId 项目ID
     * @return 成员数量
     */
    long getMemberCount(Long projectId);

    /**
     * 添加项目成员
     *
     * @param projectId 项目ID
     * @param creatorId 创建者ID
     * @param roleEnum 角色枚举
     * @return 项目成员记录
     */
    ProjectMember addMember(Long projectId, Long creatorId, ProjectMemberRole roleEnum);
}
