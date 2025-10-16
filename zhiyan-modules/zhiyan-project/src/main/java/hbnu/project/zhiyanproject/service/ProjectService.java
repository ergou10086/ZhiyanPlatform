package hbnu.project.zhiyanproject.service;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanproject.model.entity.Project;
import hbnu.project.zhiyanproject.model.enums.ProjectStatus;
import hbnu.project.zhiyanproject.model.enums.ProjectVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * 项目服务接口
 *
 * @author Tokito
 */
public interface ProjectService {

    /**
     * 创建项目
     *
     * @param name 项目名称
     * @param description 项目描述
     * @param visibility 可见性
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param creatorId 创建者ID
     * @return 创建的项目
     */
    R<Project> createProject(String name, String description, ProjectVisibility visibility,
                             LocalDate startDate, LocalDate endDate, Long creatorId);

    /**
     * 更新项目信息
     *
     * @param projectId 项目ID
     * @param name 项目名称
     * @param description 项目描述
     * @param visibility 可见性
     * @param status 项目状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 更新后的项目
     */
    R<Project> updateProject(Long projectId, String name, String description,
                             ProjectVisibility visibility, ProjectStatus status,
                             LocalDate startDate, LocalDate endDate);

    /**
     * 删除项目
     *
     * @param projectId 项目ID
     * @param userId 操作用户ID
     * @return 删除结果
     */
    R<Void> deleteProject(Long projectId, Long userId);

    /**
     * 根据ID获取项目
     *
     * @param projectId 项目ID
     * @return 项目信息
     */
    R<Project> getProjectById(Long projectId);

    /**
     * 获取所有项目（分页）
     *
     * @param pageable 分页参数
     * @return 项目列表
     */
    R<Page<Project>> getAllProjects(Pageable pageable);

    /**
     * 根据创建者获取项目列表
     *
     * @param creatorId 创建者ID
     * @param pageable 分页参数
     * @return 项目列表
     */
    R<Page<Project>> getProjectsByCreator(Long creatorId, Pageable pageable);

    /**
     * 根据状态获取项目列表
     *
     * @param status 项目状态
     * @param pageable 分页参数
     * @return 项目列表
     */
    R<Page<Project>> getProjectsByStatus(ProjectStatus status, Pageable pageable);

    /**
     * 获取用户参与的所有项目
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 项目列表
     */
    R<Page<Project>> getUserProjects(Long userId, Pageable pageable);

    /**
     * 搜索项目（按名称或描述）
     *
     * @param keyword 关键字
     * @param pageable 分页参数
     * @return 项目列表
     */
    R<Page<Project>> searchProjects(String keyword, Pageable pageable);

    /**
     * 获取公开的活跃项目
     *
     * @param pageable 分页参数
     * @return 项目列表
     */
    R<Page<Project>> getPublicActiveProjects(Pageable pageable);

    /**
     * 更新项目状态
     *
     * @param projectId 项目ID
     * @param status 新状态
     * @return 更新后的项目
     */
    R<Project> updateProjectStatus(Long projectId, ProjectStatus status);

    /**
     * 归档项目
     *
     * @param projectId 项目ID
     * @param userId 操作用户ID
     * @return 归档结果
     */
    R<Void> archiveProject(Long projectId, Long userId);

    /**
     * 检查用户是否有权限访问项目
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    R<Boolean> hasAccessPermission(Long projectId, Long userId);

    /**
     * 统计用户创建的项目数量
     *
     * @param userId 用户ID
     * @return 项目数量
     */
    R<Long> countUserCreatedProjects(Long userId);

    /**
     * 统计用户参与的项目数量
     *
     * @param userId 用户ID
     * @return 项目数量
     */
    R<Long> countUserParticipatedProjects(Long userId);
}
