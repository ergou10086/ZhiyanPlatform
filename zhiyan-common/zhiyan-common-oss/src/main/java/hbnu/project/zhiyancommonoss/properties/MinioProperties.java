package hbnu.project.zhiyancommonoss.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO配置属性
 *
 * @author ErgouTree
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * MinIO服务地址
     */
    private String endpoint;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 密钥
     */
    private String secretKey;

    /**
     * 是否使用HTTPS
     */
    private Boolean secure = false;

    /**
     * 桶权限类型(0private 1public 2custom)
     */
    private String accessPolicy;

    /**
     * 桶配置
     */
    private BucketConfig buckets = new BucketConfig();

    /**
     * 文件上传配置
     */
    private UploadConfig upload = new UploadConfig();

    /**
     * 图片处理配置
     */
    private ImageConfig image = new ImageConfig();

    /**
     * 桶设置
     */
    @Data
    public static class BucketConfig {
        /**
         * 成果附件桶
         */
        private String achievementFiles = "achievement-files";

        /**
         * Wiki资源桶
         */
        private String wikiAssets = "wiki-assets";

        /**
         * 临时上传桶
         */
        private String tempUploads = "temp-uploads";

        /**
         * 项目封面桶
         */
        private String projectCovers = "project-covers";

        /**
         * 用户头像桶
         */
        private String userAvatars = "user-avatars";
    }

    /**
     * 文件上传设置
     * 所有类型的文件都能被允许
     */
    @Data
    public static class UploadConfig {
        /**
         * 最大文件大小（字节），默认500MB
         */
        private Long maxFileSize = 5 * 100 * 1024 * 1024L;

        /**
         * 最大图片大小（字节），默认10MB
         */
        private Long maxImageSize = 10 * 1024 * 1024L;

        /**
         * 临时文件保留时间（小时）
         */
        private Integer tempFileRetentionHours = 24;

        /**
         * 允许的图片类型
         */
        private String[] allowedImageTypes = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};

        /**
         * 允许的文件类型（* 表示允许所有类型）
         */
        private String[] allowedFileTypes = {"*"};
    }

    /**
     * 图片设置
     */
    @Data
    public static class ImageConfig {

        /**
         * 图片质量（0.0-1.0）
         */
        private Float quality = 0.85f;

        /**
         * 是否生成WebP格式
         */
        private Boolean generateWebP = true;
    }

}
