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
    ACHIEVEMENT_FILES("achievement_files"),

    /**
     * Wiki资源桶
     */
    WIKI_ASSETS("wiki_assets"),

    /**
     * 临时上传桶
     */
    TEMP_UPLOADS("temp_uploads"),

    /**
     * 项目封面桶
     */
    PROJECT_COVERS("project_covers"),

    /**
     * 用户头像桶
     */
    USER_AVATARS("user_avatars");


    /**
     * 桶实际名字
     */
    private final String bucketName;
}
