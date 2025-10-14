package hbnu.project.zhiyancommonbasic.constants;

/**
 * Token的Key常量
 * 
 * @author ErgouTree
 */
public class TokenConstants {
    /**
     * 令牌前缀
     */
    public static final String PREFIX = "Bearer ";

    /**
     * 令牌秘钥
     */
    public final static String SECRET = "zjmyxypxlllhshlzqzhb";

    /**
     * 访问令牌默认过期时间（分钟）
     */
    public static final int DEFAULT_ACCESS_TOKEN_EXPIRE_MINUTES = 30;

    /**
     * 刷新令牌默认过期时间（分钟）- 3天
     */
    public static final int DEFAULT_REFRESH_TOKEN_EXPIRE_MINUTES = 4320;

    /**
     * 记住我情况下刷新令牌过期时间（分钟）- 30天
     */
    public static final int REMEMBER_ME_REFRESH_TOKEN_EXPIRE_MINUTES = 43200;

    /**
     * Token类型
     */
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    /**
     * JWT声明中的用户ID键
     */
    public static final String JWT_CLAIM_USER_ID = "userId";

    /**
     * JWT声明中的Token类型键
     */
    public static final String JWT_CLAIM_TOKEN_TYPE = "type";

    /**
     * 访问令牌类型标识
     */
    public static final String TOKEN_TYPE_ACCESS = "access";

    /**
     * 刷新令牌类型标识
     */
    public static final String TOKEN_TYPE_REFRESH = "refresh";
}
