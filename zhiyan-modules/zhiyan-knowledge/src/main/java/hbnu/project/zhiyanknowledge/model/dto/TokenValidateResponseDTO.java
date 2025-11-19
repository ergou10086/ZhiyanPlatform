package hbnu.project.zhiyanknowledge.model.dto;

import lombok.Data;

/**
 * Token验证响应
 *
 * @author ErgouTree
 */
@Data
public class TokenValidateResponseDTO {
        private String userId;
        private String[] roles;
        private Long remainingTime;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String[] getRoles() {
            return roles;
        }

        public void setRoles(String[] roles) {
            this.roles = roles;
        }

        public Long getRemainingTime() {
            return remainingTime;
        }

        public void setRemainingTime(Long remainingTime) {
            this.remainingTime = remainingTime;
        }
}

