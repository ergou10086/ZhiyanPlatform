package hbnu.project.zhiyancommonoss.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 桶类型枚举
 *
 * @author ErgouTree
 */
@Getter
@RequiredArgsConstructor
public enum BucketType {
    /**
     * 成果附件桶
     */
    ACHIEVEMENT_FILES("achievementfiles"),

    /**
     * Wiki资源桶
     */
    WIKI_ASSETS("wikiassets"),

    /**
     * 临时上传桶
     */
    TEMP_UPLOADS("tempuploads"),

    /**
     * 项目封面桶
     */
    PROJECT_COVERS("projectcovers"),

    /**
     * 用户头像桶
     */
    USER_AVATARS("useravatars"),

    /**
     * 任务提交相关文件（新增）
     */
    TASK_SUBMISSION("task-submission");


    /**
     * 桶实际名字
     */
    private final String bucketName;
}
