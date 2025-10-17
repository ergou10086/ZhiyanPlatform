package hbnu.project.zhiyancommonbasic.utils.text;


import hbnu.project.zhiyancommonbasic.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * 中文编码转换工具类
 * 专门处理 GBK 和 UTF-8 编码转换，避免网站中文加载异常
 *
 * @author ErgouTree
 */
public class EncodingUtils {

    /**
     * 检测字符串是否为乱码
     *
     * @param str 待检测字符串
     * @return true表示可能是乱码
     */
    public static boolean isGarbled(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }

        // 检测是否包含大量乱码特征字符
        int garbledCount = 0;
        for (char c : str.toCharArray()) {
            if (c == '?' || c == '�' || (c >= 0xFFFD && c <= 0xFFFE)) {
                garbledCount++;
            }
        }

        // 如果乱码字符超过10%，认为是乱码
        return garbledCount > str.length() * 0.1;
    }

    /**
     * 智能修复中文乱码
     * 自动检测并尝试修复ISO-8859-1错误编码的中文
     *
     * @param str 可能乱码的字符串
     * @return 修复后的字符串
     */
    public static String fixGarbled(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        try {
            // 尝试ISO-8859-1转UTF-8
            String fixed = new String(str.getBytes(StandardCharsets.ISO_8859_1),
                    StandardCharsets.UTF_8);
            if (!isGarbled(fixed)) {
                return fixed;
            }

            // 尝试ISO-8859-1转GBK
            fixed = new String(str.getBytes(StandardCharsets.ISO_8859_1),
                    CharsetKit.CHARSET_GBK);
            if (!isGarbled(fixed)) {
                return fixed;
            }
        } catch (Exception e) {
            // 转换失败，返回原字符串
        }

        return str;
    }

    /**
     * GBK转UTF-8
     *
     * @param str GBK编码的字符串
     * @return UTF-8编码的字符串
     */
    public static String gbkToUtf8(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return CharsetKit.convert(str, CharsetKit.CHARSET_GBK,
                StandardCharsets.UTF_8);
    }

    /**
     * UTF-8转GBK
     *
     * @param str UTF-8编码的字符串
     * @return GBK编码的字符串
     */
    public static String utf8ToGbk(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return CharsetKit.convert(str, StandardCharsets.UTF_8,
                CharsetKit.CHARSET_GBK);
    }

    /**
     * ISO-8859-1转UTF-8
     * 常用于修复HTTP请求中的中文乱码
     *
     * @param str ISO-8859-1编码的字符串
     * @return UTF-8编码的字符串
     */
    public static String isoToUtf8(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return CharsetKit.convert(str, StandardCharsets.ISO_8859_1,
                StandardCharsets.UTF_8);
    }

    /**
     * ISO-8859-1转GBK
     *
     * @param str ISO-8859-1编码的字符串
     * @return GBK编码的字符串
     */
    public static String isoToGbk(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return CharsetKit.convert(str, StandardCharsets.ISO_8859_1,
                CharsetKit.CHARSET_GBK);
    }

    /**
     * 获取字符串的字节数组（UTF-8编码）
     *
     * @param str 字符串
     * @return 字节数组
     */
    public static byte[] getUtf8Bytes(String str) {
        if (str == null) {
            return null;
        }
        return str.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 获取字符串的字节数组（GBK编码）
     *
     * @param str 字符串
     * @return 字节数组
     */
    public static byte[] getGbkBytes(String str) {
        if (str == null) {
            return null;
        }
        return str.getBytes(CharsetKit.CHARSET_GBK);
    }

    /**
     * 从UTF-8字节数组构建字符串
     *
     * @param bytes 字节数组
     * @return 字符串
     */
    public static String fromUtf8Bytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 从GBK字节数组构建字符串
     *
     * @param bytes 字节数组
     * @return 字符串
     */
    public static String fromGbkBytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new String(bytes, CharsetKit.CHARSET_GBK);
    }

    /**
     * 编码转换（通用方法）
     *
     * @param str 原字符串
     * @param fromEncoding 源编码
     * @param toEncoding 目标编码
     * @return 转换后的字符串
     */
    public static String convert(String str, String fromEncoding, String toEncoding) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return CharsetKit.convert(str, fromEncoding, toEncoding);
    }

    /**
     * 修复URL参数中的中文乱码
     * 处理GET请求参数常见的ISO-8859-1编码问题
     *
     * @param param URL参数值
     * @return 修复后的参数值
     */
    public static String fixUrlParam(String param) {
        if (StringUtils.isEmpty(param)) {
            return param;
        }

        try {
            // 先尝试ISO-8859-1 -> UTF-8
            String fixed = new String(param.getBytes(StandardCharsets.ISO_8859_1),
                    StandardCharsets.UTF_8);

            // 检查是否包含中文字符
            if (containsChinese(fixed)) {
                return fixed;
            }

            // 再尝试ISO-8859-1 -> GBK
            fixed = new String(param.getBytes(StandardCharsets.ISO_8859_1),
                    CharsetKit.CHARSET_GBK);

            if (containsChinese(fixed)) {
                return fixed;
            }
        } catch (Exception e) {
            // 转换失败，返回原值
        }

        return param;
    }

    /**
     * 判断字符串是否包含中文字符
     *
     * @param str 待检测字符串
     * @return true表示包含中文
     */
    public static boolean containsChinese(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }

        for (char c : str.toCharArray()) {
            // 中文字符的Unicode范围
            if ((c >= 0x4E00 && c <= 0x9FA5) || // 基本汉字
                    (c >= 0x3400 && c <= 0x4DBF) || // 扩展A
                    (c >= 0x20000 && c <= 0x2A6DF) || // 扩展B
                    (c >= 0x2A700 && c <= 0x2B73F) || // 扩展C
                    (c >= 0x2B740 && c <= 0x2B81F)) { // 扩展D
                return true;
            }
        }

        return false;
    }

    /**
     * 安全的编码转换，失败时返回原字符串
     *
     * @param str 原字符串
     * @param fromCharset 源字符集
     * @param toCharset 目标字符集
     * @return 转换后的字符串，失败返回原字符串
     */
    public static String safeConvert(String str, Charset fromCharset, Charset toCharset) {
        if (StringUtils.isEmpty(str) || fromCharset.equals(toCharset)) {
            return str;
        }

        try {
            return new String(str.getBytes(fromCharset), toCharset);
        } catch (Exception e) {
            // 转换失败，返回原字符串
            return str;
        }
    }

    /**
     * 批量修复字符串数组中的中文乱码
     *
     * @param strArray 字符串数组
     * @return 修复后的字符串数组
     */
    public static String[] fixGarbledArray(String[] strArray) {
        if (strArray == null || strArray.length == 0) {
            return strArray;
        }

        String[] result = new String[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            result[i] = fixGarbled(strArray[i]);
        }

        return result;
    }

    /**
     * 强制使用UTF-8编码
     * 用于确保字符串以UTF-8格式存储
     *
     * @param str 字符串
     * @return UTF-8编码的字符串
     */
    public static String ensureUtf8(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        // 获取原始字节
        byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);

        // 重新以UTF-8解码
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 强制使用GBK编码
     * 用于确保字符串以GBK格式存储
     *
     * @param str 字符串
     * @return GBK编码的字符串
     */
    public static String ensureGbk(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        // 获取原始字节
        byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);

        // 重新以GBK解码
        return new String(bytes, CharsetKit.CHARSET_GBK);
    }

}
