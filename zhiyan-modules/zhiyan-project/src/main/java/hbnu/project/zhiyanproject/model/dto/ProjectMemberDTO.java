package hbnu.project.zhiyanproject.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目成员 DTO
 * 用于返回项目成员及其角色信息
 *
 * @author AI Assistant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "项目成员信息")
public class ProjectMemberDTO {

    @LongToString
    @Schema(description = "成员ID")
    private Long id;

    @LongToString
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "用户头像")
    private String avatar;

    @LongToString
    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "角色代码", example = "OWNER")
    private String roleCode;

    @Schema(description = "角色名称", example = "项目拥有者")
    private String roleName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "加入时间")
    private LocalDateTime joinedAt;
}
