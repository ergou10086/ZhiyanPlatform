package hbnu.project.zhiyanknowledge.service.impl;

import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyanknowledge.converter.AchievementConverter;
import hbnu.project.zhiyanknowledge.model.dto.AchievementDTO;
import hbnu.project.zhiyanknowledge.model.dto.AchievementFileDTO;
import hbnu.project.zhiyanknowledge.model.dto.AchievementQueryDTO;
import hbnu.project.zhiyanknowledge.model.entity.Achievement;
import hbnu.project.zhiyanknowledge.model.entity.AchievementDetail;
import hbnu.project.zhiyanknowledge.model.entity.AchievementFile;
import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanknowledge.model.enums.AchievementType;
import hbnu.project.zhiyanknowledge.repository.AchievementFileRepository;
import hbnu.project.zhiyanknowledge.repository.AchievementRepository;
import hbnu.project.zhiyanknowledge.service.AchievementSearchService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 成果搜索部分的服务实现
 * 使用MySQL + JPA Specification实现动态查询
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementSearchServiceImpl implements AchievementSearchService {

    @Autowired
    private final AchievementRepository achievementRepository;

    @Autowired
    private final AchievementFileRepository achievementFileRepository;

    private final AchievementConverter achievementConverter;

    /**
     * 根据项目ID查询成果列表
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return 成果分页列表
     */
    @Override
    public Page<AchievementDTO> getAchievementsByProjectId(Long projectId, Pageable pageable) {
        log.info("根据项目ID查询成果列表: projectId={}", projectId);

        if (projectId == null) {
            throw new ServiceException("项目ID不能为空");
        }

        Page<Achievement> achievementPage = achievementRepository.findByProjectId(projectId, pageable);
        return achievementPage.map(achievementConverter::toDTO);
    }

    /**
     * 分页查询成果列表
     *
     * @param queryDTO 查询条件
     * @param pageable 分页参数
     * @return 成果分页列表
     */
    @Override
    public Page<AchievementDTO> queryAchievements(AchievementQueryDTO queryDTO, Pageable pageable) {
        log.info("分页查询成果列表: queryDTO={}", queryDTO);

        // 1. 构建动态查询条件
        Specification<Achievement> spec = buildSpecification(queryDTO);

        // 2. 构建分页和排序参数
        Pageable pageRequest = buildPageable(queryDTO, pageable);

        // 3. 执行查询
        Page<Achievement> achievementPage = achievementRepository.findAll(spec, pageRequest);

        // 4. 转换为DTO
        Page<AchievementDTO> resultPage = achievementPage.map(achievementConverter::toDTO);

        log.info("查询成功: totalElements={}, totalPages={}",
                resultPage.getTotalElements(), resultPage.getTotalPages());

        return resultPage;
    }

    /**
     * 根据成果名模糊查询成果
     *
     * @param achievementName 成果名
     * @return 成果
     */
    @Override
    public AchievementDTO getAchievementByName(String achievementName) {
        log.info("根据成果名模糊查询成果: achievementName={}", achievementName);

        if (StringUtils.isEmpty(achievementName)) {
            throw new ServiceException("成果名称不能为空");
        }

        // 构建查询条件，标题模糊匹配
        Specification<Achievement> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 标题包含关键字（不区分大小写）
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + achievementName.toLowerCase() + "%"
            ));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 查询第一个匹配的成果
        List<Achievement> achievements = achievementRepository.findAll(spec,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

        if (achievements.isEmpty()) {
            log.warn("未找到匹配的成果: achievementName={}", achievementName);
            return null;
        }

        Achievement achievement = achievements.getFirst();
        log.info("找到成果: id={}, title={}", achievement.getId(), achievement.getTitle());

        return achievementConverter.toDTO(achievement);
    }

    /**
     * 根据成果中的文件名模糊查询文件
     *
     * @param achievementFileName 文件名
     * @return 文件
     */
    @Override
    public AchievementFileDTO getAchievementFileByName(String achievementFileName) {
        log.info("根据文件名模糊查询文件: fileName={}", achievementFileName);

        if (StringUtils.isEmpty(achievementFileName)) {
            throw new ServiceException("文件名不能为空");
        }

        // 使用JPA的Specification查询，查询文件名包含关键字的文件
        Specification<AchievementFile> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("fileName")),
                        "%" + achievementFileName.toLowerCase() + "%"
                );

        List<AchievementFile> files = achievementFileRepository.findAll(
                spec,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "uploadAt"))
        ).getContent();

        if (files.isEmpty()) {
            log.warn("未找到匹配的文件: fileName={}", achievementFileName);
            return null;
        }

        AchievementFile file = files.getFirst();
        log.info("找到文件: id={}, fileName={}", file.getId(), file.getFileName());

        return achievementConverter.fileToDTO(file);
    }

    /**
     * 组合搜索：多关键字搜索
     *
     * @param keyword  搜索关键字
     * @param pageable 分页参数
     * @return 成果分页列表
     */
    @Override
    public Page<AchievementDTO> combinationSearch(String keyword, Pageable pageable) {
        log.info("高级搜索: keyword={}", keyword);

        if (StringUtils.isEmpty(keyword)) {
            throw new ServiceException("搜索关键字不能为空");
        }

        Specification<Achievement> spec = (root, query, criteriaBuilder) -> {
            // 不区分大小写
            String lowerKeyword = keyword.toLowerCase();

            // 左连接detail表
            Join<Achievement, AchievementDetail> detailJoin = root.join("detail", JoinType.LEFT);

            // 在标题、摘要中搜索（OR关系）
            Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + lowerKeyword + "%"
            );

            Predicate abstractPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(detailJoin.get("abstractText")),
                    "%" + lowerKeyword + "%"
            );

            // 组合条件（OR关系）
            return criteriaBuilder.or(titlePredicate, abstractPredicate);
        };

        Page<Achievement> achievements = achievementRepository.findAll(spec, pageable);
        return achievements.map(achievementConverter::toDTO);
    }

    /**
     * 统计查询：按类型统计成果数量
     *
     * @param projectId 项目ID
     * @return 统计结果Map
     */
    @Override
    public Map<String, Long> statisticsByType(Long projectId) {
        log.info("按类型统计成果数量: projectId={}", projectId);

        Map<String, Long> statistics = new HashMap<>();

        for (AchievementType type : AchievementType.values()) {
            long count = achievementRepository.countByProjectIdAndType(projectId, type);
            statistics.put(type.name(), count);
        }

        return statistics;
    }

    /**
     * 根据创建时间范围查询成果
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageable  分页参数
     * @return 成果分页列表
     */
    @Override
    public Page<AchievementDTO> searchByDateRange(Long projectId,
                                                  LocalDateTime startTime,
                                                  LocalDateTime endTime,
                                                  Pageable pageable) {
        log.info("根据时间范围查询成果: projectId={}, startTime={}, endTime={}",
                projectId, startTime, endTime);

        if (projectId == null) {
            throw new ServiceException("项目ID不能为空");
        }

        if (startTime == null || endTime == null) {
            throw new ServiceException("时间范围不能为空");
        }

        if (startTime.isAfter(endTime)) {
            throw new ServiceException("开始时间不能晚于结束时间");
        }

        Page<Achievement> achievementPage = achievementRepository
                .findByProjectIdAndCreatedAtBetween(projectId, startTime, endTime, pageable);

        return achievementPage.map(achievementConverter::toDTO);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建动态查询条件（Specification）
     * 根据查询DTO动态组合查询条件
     *
     * @param queryDTO 查询条件
     * @return Specification对象
     */
    private Specification<Achievement> buildSpecification(AchievementQueryDTO queryDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 项目ID条件
            if (queryDTO.getProjectId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("projectId"), queryDTO.getProjectId()));
            }

            // 2. 成果类型条件
            if (queryDTO.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), queryDTO.getType()));
            }

            // 3. 成果状态条件
            if (queryDTO.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), queryDTO.getStatus()));
            }

            // 4. 创建者ID条件
            if (queryDTO.getCreatorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("creatorId"), queryDTO.getCreatorId()));
            }

            // 5. 标题关键词模糊查询 - 修复：添加了缺失的标题关键词查询
            if (StringUtils.isNotEmpty(queryDTO.getTitleKeyword())) {
                String keyword = queryDTO.getTitleKeyword().toLowerCase();
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + keyword + "%"
                ));
            }

            // 6. 摘要关键词模糊查询
            if (StringUtils.isNotEmpty(queryDTO.getAbstractKeyword())) {
                // 左连接AchievementDetail表
                Join<Achievement, AchievementDetail> detailJoin = root.join("detail", JoinType.LEFT);
                String keyword = queryDTO.getAbstractKeyword().toLowerCase();
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(detailJoin.get("abstractText")),
                        "%" + keyword + "%"
                ));
            }

            // 7. 创建时间范围查询
            if (queryDTO.getCreatedAtStart() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        queryDTO.getCreatedAtStart()
                ));
            }
            if (queryDTO.getCreatedAtEnd() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        queryDTO.getCreatedAtEnd()
                ));
            }

            // 8. 只查询已发布的成果（可选）
            if (queryDTO.getOnlyPublished() != null && queryDTO.getOnlyPublished()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("status"),
                        AchievementStatus.published
                ));
            }

            // 组合所有条件（AND关系）
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 构建分页和排序参数
     *
     * @param queryDTO 查询DTO
     * @param pageable 原始分页参数（可为null）
     * @return 构建后的Pageable对象
     */
    private Pageable buildPageable(AchievementQueryDTO queryDTO, Pageable pageable) {
        // 如果传入了pageable且有排序规则，优先使用传入的pageable
        if (pageable != null && pageable.getSort().isSorted()) {
            return pageable;
        }

        // 从queryDTO中获取分页参数
        int page = queryDTO.getPage() != null ? queryDTO.getPage() : 0;
        int size = queryDTO.getSize() != null ? queryDTO.getSize() : 10;

        // 防止页码和页大小异常，限制每页最多20条
        page = Math.max(page, 0);
        size = Math.min(Math.max(size, 1), 20);

        // 构建排序规则
        Sort sort = buildSort(queryDTO);

        return PageRequest.of(page, size, sort);
    }

    /**
     * 构建排序规则
     *
     * @param queryDTO 查询DTO
     * @return Sort对象
     */
    private Sort buildSort(AchievementQueryDTO queryDTO) {
        String sortBy = StringUtils.isNotEmpty(queryDTO.getSortBy())
                ? queryDTO.getSortBy()
                : "createdAt";

        String sortOrder = StringUtils.isNotEmpty(queryDTO.getSortOrder())
                ? queryDTO.getSortOrder()
                : "DESC";

        // 验证排序字段，防止SQL注入
        sortBy = validateSortField(sortBy);

        // 构建排序方向
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, sortBy);
    }

    /**
     * 验证排序字段是否合法
     * 防止SQL注入
     *
     * @param sortField 排序字段
     * @return 合法的排序字段
     */
    private String validateSortField(String sortField) {
        // 允许的排序字段白名单
        List<String> allowedFields = List.of(
                "createdAt", "updatedAt", "title", "type", "status"
        );

        if (allowedFields.contains(sortField)) {
            return sortField;
        }

        log.warn("非法的排序字段: {}, 使用默认字段: createdAt", sortField);
        return "createdAt";
    }
}