package hbnu.project.zhiyanknowledge.config;

import hbnu.project.zhiyancommonsecurity.service.RememberMeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Knowledge模块专用的 RememberMe 服务实现
 * 
 * 由于Knowledge模块不直接处理用户认证逻辑,此实现仅提供占位功能
 * 实际的 RememberMe 功能由认证服务处理
 * Knowledge模块主要通过JWT Token进行身份验证
 * 
 * @author ErgouTree
 */
@Slf4j
@Service
@ConditionalOnMissingBean(RememberMeService.class)
public class KnowledgeRememberMeService implements RememberMeService {

    @Override
    public String createRememberMeToken(Long userId) {
        log.debug("Knowledge模块不支持创建 RememberMe Token - userId: {}", userId);
        return null;
    }

    @Override
    public Optional<Long> validateRememberMeToken(String token) {
        log.debug("Knowledge模块不支持验证 RememberMe Token - token: {}", 
                token != null ? token.substring(0, Math.min(token.length(), 10)) + "..." : "null");
        return Optional.empty();
    }

    @Override
    public void deleteRememberMeToken(String token) {
        log.debug("Knowledge模块不支持删除 RememberMe Token - token: {}", 
                token != null ? token.substring(0, Math.min(token.length(), 10)) + "..." : "null");
    }

    @Override
    public void deleteRememberMeToken(Long userId) {
        log.debug("Knowledge模块不支持删除用户的 RememberMe Token - userId: {}", userId);
    }
}

