package hbnu.project.zhiyanknowledge.client;

import hbnu.project.zhiyancommonbasic.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Auth服务Feign客户端
 * 用于调用认证服务的接口
 *
 * @author ErgouTree
 */
@FeignClient(name = "zhiyan-auth-service", url = "http://localhost:8091", path = "/zhiyan/auth")
public interface AuthServiceClient {

    /**
     * 验证令牌有效性
     * 
     * @param token Authorization header中的token（包含"Bearer "前缀）
     * @return 验证结果，包含用户ID、角色等信息
     */
    @GetMapping("/validate")
    R<TokenValidateResponse> validateToken(@RequestHeader("Authorization") String token);
    
    /**
     * Token验证响应
     */
    class TokenValidateResponse {
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
}

