package hbnu.project.zhiyancommonbasic.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 数学工具类
 * 旨在快速的安全的进行运算
 *
 * @author ErgouTree
 * @rewrite yui
 */
public class MathUtils {

    private MathUtils() {
        throw new UnsupportedOperationException("MathUtils cannot be instantiated");
    }

    // 雷神之锤III中著名的快速平方根倒数算法常量
    private static final float THREE_HALVES = 1.5f;
    private static final int MAGIC_NUMBER = 0x5f3759df;


    /**
     * 雷神之锤III著名的快速平方根倒数算法
     * 用于快速计算 1/sqrt(x)
     * 精度在小数点后三位以上
     *
     * @param x 输入值，必须大于0
     * @return 1/sqrt(x) 的近似值
     */
    public static float fastInverseSqrt(float x) {
        if (x <= 0) {
            throw new IllegalArgumentException("Input must be positive");
        }

        float x2 = x * 0.5f;
        float y = x;

        // 将浮点数解释为整数进行位操作
        int i = Float.floatToIntBits(y);
        i = MAGIC_NUMBER - (i >> 1); // 魔法数字和位操作
        y = Float.intBitsToFloat(i);

        // 牛顿迭代法提高精度
        y = y * (THREE_HALVES - (x2 * y * y));

        return y;
    }


    /**
     * 快速平方根计算（基于快速平方根倒数）
     *
     * @param x 输入值
     * @return sqrt(x) 的近似值
     */
    public static float fastSqrt(float x) {
        if (x < 0) {
            throw new IllegalArgumentException("Input cannot be negative");
        }
        if (x == 0) return 0;
        return x * fastInverseSqrt(x);
    }


    /**
     * 安全的除法运算（避免除零异常）
     *
     * @param dividend 被除数
     * @param divisor 除数
     * @param defaultValue 除数为0时的默认值
     * @return 除法结果或默认值
     */
    public static double safeDivide(double dividend, double divisor, double defaultValue) {
        if (divisor == 0) {
            return defaultValue;
        }
        return dividend / divisor;
    }


    /**
     * 安全的整数除法
     *
     * @param dividend 被除数
     * @param divisor 除数
     * @param defaultValue 除数为0时的默认值
     * @return 除法结果或默认值
     */
    public static int safeDivide(int dividend, int divisor, int defaultValue) {
        if (divisor == 0) {
            return defaultValue;
        }
        return dividend / divisor;
    }


    /**
     * 精确的浮点数加法
     *
     * @param values 要相加的值
     * @return 精确的加法结果
     */
    public static double preciseAdd(double... values) {
        BigDecimal result = BigDecimal.ZERO;
        for (double value : values) {
            result = result.add(BigDecimal.valueOf(value));
        }
        return result.doubleValue();
    }


    /**
     * 精确的浮点数减法
     *
     * @param a 被减数
     * @param b 减数
     * @return 精确的减法结果
     */
    public static double preciseSubtract(double a, double b) {
        return BigDecimal.valueOf(a).subtract(BigDecimal.valueOf(b)).doubleValue();
    }


    /**
     * 精确的浮点数乘法
     *
     * @param values 要相乘的值
     * @return 精确的乘法结果
     */
    public static double preciseMultiply(double... values) {
        if (values.length == 0) return 0;
        BigDecimal result = BigDecimal.ONE;
        for (double value : values) {
            result = result.multiply(BigDecimal.valueOf(value));
        }
        return result.doubleValue();
    }


    /**
     * 精确的浮点数除法
     *
     * @param dividend 被除数
     * @param divisor 除数
     * @param scale 精度
     * @return 精确的除法结果
     */
    public static double preciseDivide(double dividend, double divisor, int scale) {
        if (divisor == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return BigDecimal.valueOf(dividend)
                .divide(BigDecimal.valueOf(divisor), scale, RoundingMode.HALF_UP)
                .doubleValue();
    }


    /**
     * 线性插值（Lerp）
     * 常用于加载动画
     *
     * @param a 起始值
     * @param b 结束值
     * @param t 插值因子 [0, 1]
     * @return 插值结果
     */
    public static float lerp(float a, float b, float t) {
        t = clamp(t, 0, 1);
        return a + (b - a) * t;
    }


    /**
     * 双线性插值
     *
     * @param q11 点(0,0)的值
     * @param q12 点(0,1)的值
     * @param q21 点(1,0)的值
     * @param q22 点(1,1)的值
     * @param x X方向插值因子 [0,1]
     * @param y Y方向插值因子 [0,1]
     * @return 插值结果
     */
    public static float bilinearInterpolation(float q11, float q12, float q21, float q22, float x, float y) {
        float r1 = lerp(q11, q21, x);
        float r2 = lerp(q12, q22, x);
        return lerp(r1, r2, y);
    }


    /**
     * 限制值在指定范围内
     *
     * @param value 输入值
     * @param min 最小值
     * @param max 最大值
     * @return 限制后的值
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }


    /**
     * 将值映射到新的范围
     *
     * @param value 输入值
     * @param fromMin 原范围最小值
     * @param fromMax 原范围最大值
     * @param toMin 目标范围最小值
     * @param toMax 目标范围最大值
     * @return 映射后的值
     */
    public static float map(float value, float fromMin, float fromMax, float toMin, float toMax) {
        return (value - fromMin) * (toMax - toMin) / (fromMax - fromMin) + toMin;
    }


    /**
     * 计算两点之间的欧几里得距离
     *
     * @param x1 点1的x坐标
     * @param y1 点1的y坐标
     * @param x2 点2的x坐标
     * @param y2 点2的y坐标
     * @return 两点之间的距离
     */
    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return fastSqrt((float) (dx * dx + dy * dy));
    }


    /**
     * 计算三维两点之间的欧几里得距离
     *
     * @param x1 点1的x坐标
     * @param y1 点1的y坐标
     * @param z1 点1的z坐标
     * @param x2 点2的x坐标
     * @param y2 点2的y坐标
     * @param z2 点2的z坐标
     * @return 两点之间的距离
     */
    public static double distance3D(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return fastSqrt((float) (dx * dx + dy * dy + dz * dz));
    }


    /**
     * 判断点是否在圆形内
     *
     * @param px 点的x坐标
     * @param py 点的y坐标
     * @param cx 圆心的x坐标
     * @param cy 圆心的y坐标
     * @param radius 圆的半径
     * @return 是否在圆形内
     */
    public static boolean isPointInCircle(double px, double py, double cx, double cy, double radius) {
        double dx = px - cx;
        double dy = py - cy;
        return dx * dx + dy * dy <= radius * radius;
    }


    /**
     * 判断点是否在矩形内
     *
     * @param px 点的x坐标
     * @param py 点的y坐标
     * @param rx 矩形左上角x坐标
     * @param ry 矩形左上角y坐标
     * @param rw 矩形宽度
     * @param rh 矩形高度
     * @return 是否在矩形内
     */
    public static boolean isPointInRect(double px, double py, double rx, double ry, double rw, double rh) {
        return px >= rx && px <= rx + rw && py >= ry && py <= ry + rh;
    }


    /**
     * 角度转弧度
     *
     * @param degrees 角度
     * @return 弧度
     */
    public static double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180.0;
    }


    /**
     * 弧度转角度
     *
     * @param radians 弧度
     * @return 角度
     */
    public static double radiansToDegrees(double radians) {
        return radians * 180.0 / Math.PI;
    }


    /**
     * 标准化角度到 [0, 360) 范围
     *
     * @param degrees 输入角度
     * @return 标准化后的角度
     */
    public static double normalizeAngle(double degrees) {
        double normalized = degrees % 360;
        if (normalized < 0) {
            normalized += 360;
        }
        return normalized;
    }


    /**
     * 计算两个角度之间的最小差值
     *
     * @param angle1 角度1
     * @param angle2 角度2
     * @return 角度差值 [-180, 180]
     */
    public static double angleDifference(double angle1, double angle2) {
        double diff = (angle2 - angle1) % 360;
        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }
        return diff;
    }


    /**
     * 快速近似指数函数（游戏常用）
     *
     * @param x 指数
     * @return e^x 的近似值
     */
    public static float fastExp(float x) {
        x = 1.0f + x / 256.0f;
        x *= x; x *= x; x *= x; x *= x;
        x *= x; x *= x; x *= x; x *= x;
        return x;
    }


    /**
     * 快速近似对数函数（游戏常用）
     *
     * @param x 输入值
     * @return log(x) 的近似值
     */
    public static float fastLog(float x) {
        if (x <= 0) return Float.NaN;

        int bits = Float.floatToIntBits(x);
        float exponent = ((bits >> 23) & 0xFF) - 127;
        float mantissa = (bits & 0x7FFFFF) / (float) 0x7FFFFF;

        return exponent + mantissa;
    }


    /**
     * 伪随机数生成
     * 闪耀星骑士抽卡逻辑
     * by yui
     */
    public static class FastRandom {
        private long seed;

        public FastRandom(long seed) {
            this.seed = seed;
        }

        public FastRandom() {
            this(System.currentTimeMillis());
        }

        public int nextInt() {
            seed = (seed * 1103515245 + 12345) & 0x7fffffff;
            return (int) seed;
        }

        public int nextInt(int bound) {
            return nextInt() % bound;
        }

        public float nextFloat() {
            return (nextInt() & 0x7fffff) / (float) 0x7fffff;
        }
    }
}
