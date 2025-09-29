package hbnu.project.zhiyanauth.model.entity;

import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import jakarta.persistence.*;
import lombok.*;

/**
 * 角色权限关联复合主键
 *
 * @author ErgouTree
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionId implements java.io.Serializable {

    @LongToString
    @Column(name = "role_id")
    private Long roleId;

    @LongToString
    @Column(name = "permission_id")
    private Long permissionId;
}
