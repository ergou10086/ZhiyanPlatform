package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyanauth.client.KnowledgeServiceClient;
import hbnu.project.zhiyanauth.model.dto.UserAchievementDTO;
import hbnu.project.zhiyanauth.model.entity.UserAchievement;
import hbnu.project.zhiyanauth.model.form.AchievementLinkBody;
import hbnu.project.zhiyanauth.model.form.UpdateAchievementLinkBody;
import hbnu.project.zhiyanauth.repository.UserAchievementRepository;
import hbnu.project.zhiyanauth.service.UserAchievementService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户成果关联服务实现
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAchievementServiceImpl implements  UserAchievementService {

    @Resource
    private final UserAchievementRepository userAchievementRepository;

    @Resource
    private final KnowledgeServiceClient knowledgeServiceClient;

    /**
     * 关联学术成果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<UserAchievementDTO> linkAchievement(Long userId, AchievementLinkBody linkBody) {
        log.info("用户[{}]关联成果: achievementId={}, projectId={}",
                userId, linkBody.getAchievementId(), linkBody.getProjectId());

        // 1.检查是否已经被关联
        if(userAchievementRepository.existsByUserIdAndAchievementId(userId, linkBody.getAchievementId())) {
            return R.fail("该成果已关联，请勿重复操作");
        }

        // 2. 调用知识库服务验证成果是否存在且公开
        R<Object> achievementResult = knowledgeServiceClient.getAchievementById(linkBody.getAchievementId());
        if (!R.isSuccess(achievementResult)) {
            return R.fail("成果不存在或无权访问");
        }

        // 解析成果信息（需要判断是否公开）
        @SuppressWarnings("unchecked")
        Map<String, Object> achievementData = (Map<String, Object>) achievementResult.getData();
        Boolean isPublic = (Boolean) achievementData.get("isPublic");

        if(isPublic == null || !isPublic) {
            return R.fail("只能关联公开的学术成果");
        }

        // 3. 限制用户最多关联10个成果
        long count = userAchievementRepository.countByUserId(userId);
        if (count >= 10) {
            return R.fail("最多只能关联10个学术成果");
        }

        // 4.创建关联的记录
        UserAchievement userAchievement = UserAchievement.builder()
                .userId(userId)
                .achievementId(linkBody.getAchievementId())
                .projectId(linkBody.getProjectId())
                .displayOrder(linkBody.getDisplayOrder() != null ? linkBody.getDisplayOrder() : 0)
                .remark(linkBody.getRemark())
                .build();

        userAchievementRepository.save(userAchievement);

        // 5. 构建返回DTO
        UserAchievementDTO dto = buildUserAchievementDTO(userAchievement, achievementData);

        log.info("用户[{}]成功关联成果[{}]", userId, linkBody.getAchievementId());
        return R.ok(dto, "关联成功");
    }


    /**
     * 取消关联学术成果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> unlinkAchievement(Long userId, Long achievementId) {
        log.info("用户[{}]取消关联成果: achievementId={}", userId, achievementId);

        UserAchievement userAchievement = userAchievementRepository
                .findByUserIdAndAchievementId(userId, achievementId)
                .orElse(null);

        if (userAchievement == null) {
            return R.fail("未找到关联记录");
        }

        userAchievementRepository.delete(userAchievement);

        log.info("用户[{}]成功取消关联成果[{}]", userId, achievementId);
        return R.ok(null, "取消关联成功");
    }


    /**
     * 更新成果的关联信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<UserAchievementDTO> updateAchievementLink(Long userId, Long achievementId,
                                                       UpdateAchievementLinkBody updateBody) {
        log.info("用户[{}]更新成果关联信息: achievementId={}", userId, achievementId);

        UserAchievement userAchievement = userAchievementRepository.findByUserIdAndAchievementId(userId, achievementId)
                .orElseThrow(() -> new BusinessException("未找到关联记录"));

        // 更新字段
        if(updateBody.getDisplayOrder() != null) {
            userAchievement.setDisplayOrder(updateBody.getDisplayOrder());
        }
        if (updateBody.getRemark() != null) {
            userAchievement.setRemark(updateBody.getRemark());
        }

        userAchievementRepository.save(userAchievement);

        // 获取成果信息
        R<Object> achievementResult = knowledgeServiceClient.getAchievementById(achievementId);
        @SuppressWarnings("unchecked")
        Map<String, Object> achievementData = R.isSuccess(achievementResult)
                ? (Map<String, Object>) achievementResult.getData()
                : null;

        UserAchievementDTO dto = buildUserAchievementDTO(userAchievement, achievementData);

        return R.ok(dto, "更新成功");
    }


    /**
     * 查询用户关联的所有成果
     */
    @Override
    public R<List<UserAchievementDTO>> getUserAllAchievements(Long userId) {
        log.info("查询用户[{}]的所有关联成果", userId);

        List<UserAchievement> userAchievements = userAchievementRepository
                .findByUserIdOrderByDisplayOrderAsc(userId);

        if (userAchievements.isEmpty()) {
            return R.ok(new ArrayList<>());
        }

        // 批量查询成果信息
        String achievementIds = userAchievements.stream()
                .map(ua -> ua.getAchievementId().toString())
                .collect(Collectors.joining(","));

        R<Object> batchResult = knowledgeServiceClient.getAchievementsByIds(achievementIds);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> achievementList = R.isSuccess(batchResult)
                ? (List<Map<String, Object>>) batchResult.getData()
                : new ArrayList<>();

        Map<Long, Map<String, Object>> achievementMap = achievementList.stream()
                .collect(Collectors.toMap(
                        a -> Long.parseLong(a.get("id").toString()),
                        a -> a
                ));

        // 组装DTO
        List<UserAchievementDTO> dtoList = userAchievements.stream()
                .map(ua -> buildUserAchievementDTO(ua, achievementMap.get(ua.getAchievementId())))
                .collect(Collectors.toList());

        return R.ok(dtoList);
    }


        /**
         * 构建用户成果关联DTO
         */
    private UserAchievementDTO buildUserAchievementDTO(UserAchievement userAchievement,
                                                       Map<String, Object> achievementData) {
        UserAchievementDTO dto = UserAchievementDTO.builder()
                .id(userAchievement.getId().toString())
                .userId(userAchievement.getUserId().toString())
                .achievementId(userAchievement.getAchievementId().toString())
                .projectId(userAchievement.getProjectId().toString())
                .displayOrder(userAchievement.getDisplayOrder())
                .remark(userAchievement.getRemark())
                .createdAt(userAchievement.getCreatedAt())
                .build();

        // 填充成果信息
        if (achievementData != null) {
            dto.setAchievementTitle((String) achievementData.get("title"));
            dto.setAchievementType((String) achievementData.get("type"));
            dto.setAchievementStatus((String) achievementData.get("status"));
        }

        return dto;
    }
}
