package hbnu.project.zhiyancommonbasic.utils.ip;

import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * IP 匹配工具类
 * 支持多种IP匹配模式
 *
 * @author ErgouTree
 */
@Slf4j
public class IpMatchUtils {

    /**
     * 检查IP是否在允许列表中
     *
     * @param clientIp   客户端IP
     * @param allowedIps 允许的IP列表
     * @return 是否匹配
     */
    public static boolean matches(String clientIp, Set<String> allowedIps) {
        if (allowedIps == null || allowedIps.isEmpty()) {
            return true; // 没有IP限制
        }

        if (StringUtils.isBlank(clientIp)) {
            return false;
        }

        for (String allowedIp : allowedIps) {
            if (matchSinglePattern(clientIp, allowedIp)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 匹配单个IP模式
     *
     * @param clientIp  客户端IP
     * @param pattern   IP模式
     * @return 是否匹配
     */
    private static boolean matchSinglePattern(String clientIp, String pattern) {
        if (StringUtils.isBlank(pattern)) {
            return false;
        }

        // 1. 精确匹配
        if (clientIp.equals(pattern)) {
            return true;
        }

        // 2. 通配符匹配（例如：192.168.1.*）
        if (pattern.contains("*")) {
            return matchWildcard(clientIp, pattern);
        }

        // 3. CIDR匹配（例如：192.168.1.0/24）
        if (pattern.contains("/")) {
            return matchCidr(clientIp, pattern);
        }

        // 4. IP段匹配（例如：192.168.1.1-192.168.1.100）
        if (pattern.contains("-")) {
            return matchRange(clientIp, pattern);
        }

        return false;
    }

    /**
     * 通配符匹配
     */
    private static boolean matchWildcard(String ip, String pattern) {
        String regex = pattern.replace(".", "\\.").replace("*", ".*");
        return ip.matches(regex);
    }

    /**
     * CIDR匹配
     */
    private static boolean matchCidr(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            String networkIp = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            long ipLong = ipToLong(ip);
            long networkLong = ipToLong(networkIp);
            long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;

            return (ipLong & mask) == (networkLong & mask);
        } catch (Exception e) {
            log.warn("CIDR匹配失败: {} - {}", ip, cidr, e);
            return false;
        }
    }

    /**
     * IP段匹配
     */
    private static boolean matchRange(String ip, String range) {
        try {
            String[] parts = range.split("-");
            String startIp = parts[0].trim();
            String endIp = parts[1].trim();

            long ipLong = ipToLong(ip);
            long startLong = ipToLong(startIp);
            long endLong = ipToLong(endIp);

            return ipLong >= startLong && ipLong <= endLong;
        } catch (Exception e) {
            log.warn("IP段匹配失败: {} - {}", ip, range, e);
            return false;
        }
    }

    /**
     * IP转换为Long
     */
    private static long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = result * 256 + Integer.parseInt(parts[i]);
        }
        return result;
    }
}
