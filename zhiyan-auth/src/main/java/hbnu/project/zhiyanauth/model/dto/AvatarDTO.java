package hbnu.project.zhiyanauth.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 用户头像DTO
 * 包含所有尺寸的头像URL
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户头像信息")
public class AvatarDTO {

    @Schema(description = "MinIO原图URL")
    private String minioUrl;

    @Schema(description = "CDN URL（如果配置）")
    private String cdnUrl;

    @Schema(description = "所有尺寸的缩略图URL")
    private Map<String, String> sizes;

    /**
     * 获取指定尺寸的URL
     *
     * @param size 尺寸（32, 64, 128, 256）
     * @return URL
     */
    public String getSizeUrl(int size) {
        if (sizes == null) {
            return minioUrl;
        }
        return sizes.getOrDefault(String.valueOf(size), minioUrl);
    }
}
