package hbnu.project.zhiyancommonbasic.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务专用JWT工具类
 * 扩展通用JWT工具类，提供更多功能
 *
 * @author ErgouTree
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret:zhiyan-platform-secret-key-2025}")
    private String secret;

    @Value("${jwt.issuer:zhiyan-platform}")
    private String issuer;

    /**
     * 创建JWT令牌
     *
     * @param subject 主体（用户ID）
     * @param expireMinutes 过期时间（分钟）
     * @return JWT令牌
     */
    public String createToken(String subject, int expireMinutes) {
        return createToken(subject, expireMinutes, new HashMap<>());
    }

    /**
     * 创建JWT令牌
     *
     * @param subject 主体（用户ID）
     * @param expireMinutes 过期时间（分钟）
     * @param claims 自定义声明
     * @return JWT令牌
     */
    public String createToken(String subject, int expireMinutes, Map<String, Object> claims) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expireMinutes * 60 * 1000L);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 解析JWT令牌
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public String parseToken(String token) {
        try {
            if (StringUtils.isBlank(token)) {
                return null;
            }

            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();

        } catch (ExpiredJwtException e) {
            log.debug("JWT令牌已过期: {}", e.getMessage());
            return null;
        } catch (UnsupportedJwtException e) {
            log.debug("不支持的JWT令牌: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            log.debug("JWT令牌格式错误: {}", e.getMessage());
            return null;
        } catch (SignatureException e) {
            log.debug("JWT令牌签名验证失败: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            log.debug("JWT令牌参数错误: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("JWT令牌解析失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取JWT令牌的Claims
     *
     * @param token JWT令牌
     * @return Claims
     */
    public Claims getClaims(String token) {
        try {
            if (StringUtils.isBlank(token)) {
                return null;
            }

            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();

        } catch (Exception e) {
            log.debug("获取JWT Claims失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取JWT令牌剩余有效时间（秒）
     *
     * @param token JWT令牌
     * @return 剩余时间（秒），如果已过期或无效则返回null
     */
    public Long getRemainingTime(String token) {
        try {
            Claims claims = getClaims(token);
            if (claims == null) {
                return null;
            }

            Date expiration = claims.getExpiration();
            Date now = new Date();

            if (expiration.before(now)) {
                return null;
            }

            return (expiration.getTime() - now.getTime()) / 1000;

        } catch (Exception e) {
            log.debug("获取JWT剩余时间失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证JWT令牌是否有效
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }

    /**
     * 从JWT令牌中获取过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDate(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 从JWT令牌中获取签发时间
     *
     * @param token JWT令牌
     * @return 签发时间
     */
    public Date getIssuedAt(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.getIssuedAt() : null;
    }

    /**
     * 检查JWT令牌是否即将过期
     *
     * @param token JWT令牌
     * @param minutes 提前多少分钟算作即将过期
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token, int minutes) {
        try {
            Claims claims = getClaims(token);
            if (claims == null) {
                return true;
            }

            Date expiration = claims.getExpiration();
            Date threshold = new Date(System.currentTimeMillis() + minutes * 60 * 1000L);

            return expiration.before(threshold);

        } catch (Exception e) {
            log.debug("检查JWT过期状态失败: {}", e.getMessage());
            return true;
        }
    }
}