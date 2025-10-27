package hbnu.project.zhiyanauth.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 头像上传请求（用于前端已裁剪的情况）
 *
 * @author ErgouTree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "头像上传请求")
public class AvatarUploadRequest {

    @Schema(description = "是否使用默认头像")
    private Boolean useDefault;
}
