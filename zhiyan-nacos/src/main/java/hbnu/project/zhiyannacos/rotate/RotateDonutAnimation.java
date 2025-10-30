package hbnu.project.zhiyannacos.rotate;

/**
 * 抽象东西
 * 基于 Andy Sloane 的经典算法
 * 
 * @author yui
 */
public class RotateDonutAnimation {

    private static final int SCREEN_WIDTH = 80;
    private static final int SCREEN_HEIGHT = 22;
    private static final double THETA_SPACING = 0.07;
    private static final double PHI_SPACING = 0.02;
    
    private static final double R1 = 1;
    private static final double R2 = 2;
    private static final double K2 = 5;
    private static final double K1 = SCREEN_WIDTH * K2 * 3 / (8 * (R1 + R2));

    /**
     * 播放旋转甜甜圈动画（普通版本）
     * 
     * @param duration 持续时间（秒）
     */
    public static void play(int duration) {
        System.out.println("\n🍩 经典旋转甜甜圈动画启动中...\n");
        
        // 给用户一点时间准备
        sleep(500);
        
        long startTime = System.currentTimeMillis();
        double A = 0, B = 0;
        
        // 先输出占位空行，为后续的覆盖做准备
        for (int i = 0; i < SCREEN_HEIGHT + 2; i++) {
            System.out.println();
        }
        
        while ((System.currentTimeMillis() - startTime) / 1000 < duration) {
            String frame = renderFrame(A, B);
            
            // 使用 \r 和向上移动光标来实现原地更新
            // 移动光标到帧的起始位置
            System.out.print("\033[" + (SCREEN_HEIGHT + 2) + "A"); // 向上移动
            System.out.print(frame);
            System.out.flush(); // 强制刷新输出
            
            A += 0.04;
            B += 0.02;
            
            sleep(50); // 控制帧率 ~20 FPS
        }
        
        System.out.println("\n");
    }

    /**
     * 播放彩色版本的旋转甜甜圈（推荐）
     * 
     * @param duration 持续时间（秒）
     */
    public static void playColorful(int duration) {
        System.out.println("\n🍩 彩色旋转甜甜圈动画启动中...\n");
        
        // 给用户一点时间准备
        sleep(500);
        
        long startTime = System.currentTimeMillis();
        double A = 0, B = 0;
        
        // ANSI 颜色代码数组（彩虹渐变）
        String[] colors = {
            "\033[38;5;196m", // 红色
            "\033[38;5;208m", // 橙色
            "\033[38;5;226m", // 黄色
            "\033[38;5;46m",  // 绿色
            "\033[38;5;51m",  // 青色
            "\033[38;5;21m",  // 蓝色
            "\033[38;5;201m"  // 紫色
        };
        
        int frameCount = 0;
        
        // 先输出占位空行
        for (int i = 0; i < SCREEN_HEIGHT + 2; i++) {
            System.out.println();
        }
        
        while ((System.currentTimeMillis() - startTime) / 1000 < duration) {
            String color = colors[(frameCount / 3) % colors.length]; // 每3帧切换一次颜色
            String frame = renderColorfulFrame(A, B, color);
            
            // 移动光标到帧的起始位置
            System.out.print("\033[" + (SCREEN_HEIGHT + 2) + "A");
            System.out.print(frame);
            System.out.flush();
            
            A += 0.04;
            B += 0.02;
            frameCount++;
            
            sleep(50); // ~20 FPS
        }
        
        System.out.print("\033[0m"); // 重置颜色
        System.out.println("\n");
    }

    /**
     * 渲染单帧（普通版本）
     */
    private static String renderFrame(double A, double B) {
        double cosA = Math.cos(A), sinA = Math.sin(A);
        double cosB = Math.cos(B), sinB = Math.sin(B);

        char[][] output = new char[SCREEN_HEIGHT][SCREEN_WIDTH];
        double[][] zBuffer = new double[SCREEN_HEIGHT][SCREEN_WIDTH];

        // 初始化缓冲区
        for (int i = 0; i < SCREEN_HEIGHT; i++) {
            for (int j = 0; j < SCREEN_WIDTH; j++) {
                output[i][j] = ' ';
                zBuffer[i][j] = 0;
            }
        }

        // 渲染甜甜圈
        for (double theta = 0; theta < 2 * Math.PI; theta += THETA_SPACING) {
            double cosTheta = Math.cos(theta), sinTheta = Math.sin(theta);

            for (double phi = 0; phi < 2 * Math.PI; phi += PHI_SPACING) {
                double cosPhi = Math.cos(phi), sinPhi = Math.sin(phi);

                // 圆环上的点坐标
                double circleX = R2 + R1 * cosTheta;
                double circleY = R1 * sinTheta;

                // 3D 旋转
                double x = circleX * (cosB * cosPhi + sinA * sinB * sinPhi)
                        - circleY * cosA * sinB;
                double y = circleX * (sinB * cosPhi - sinA * cosB * sinPhi)
                        + circleY * cosA * cosB;
                double z = K2 + cosA * circleX * sinPhi + circleY * sinA;
                double ooz = 1 / z;

                // 投影到 2D 屏幕
                int xp = (int) (SCREEN_WIDTH / 2 + K1 * ooz * x);
                int yp = (int) (SCREEN_HEIGHT / 2 - K1 * ooz * y);

                // 计算光照
                double L = cosPhi * cosTheta * sinB - cosA * cosTheta * sinPhi
                        - sinA * sinTheta + cosB * (cosA * sinTheta - cosTheta * sinA * sinPhi);

                // 边界检查
                if (xp >= 0 && xp < SCREEN_WIDTH && yp >= 0 && yp < SCREEN_HEIGHT) {
                    if (ooz > zBuffer[yp][xp]) {
                        zBuffer[yp][xp] = ooz;
                        int luminanceIndex = (int) (L * 8);
                        output[yp][xp] = ".,-~:;=!*#$@".charAt(Math.max(0, Math.min(11, luminanceIndex)));
                    }
                }
            }
        }

        // 构建输出字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SCREEN_HEIGHT; i++) {
            for (int j = 0; j < SCREEN_WIDTH; j++) {
                sb.append(output[i][j]);
            }
            sb.append('\n');
        }
        
        return sb.toString();
    }

    /**
     * 渲染单帧（彩色版本）
     */
    private static String renderColorfulFrame(double A, double B, String color) {
        double cosA = Math.cos(A), sinA = Math.sin(A);
        double cosB = Math.cos(B), sinB = Math.sin(B);

        char[][] output = new char[SCREEN_HEIGHT][SCREEN_WIDTH];
        double[][] zBuffer = new double[SCREEN_HEIGHT][SCREEN_WIDTH];

        for (int i = 0; i < SCREEN_HEIGHT; i++) {
            for (int j = 0; j < SCREEN_WIDTH; j++) {
                output[i][j] = ' ';
                zBuffer[i][j] = 0;
            }
        }

        for (double theta = 0; theta < 2 * Math.PI; theta += THETA_SPACING) {
            double cosTheta = Math.cos(theta), sinTheta = Math.sin(theta);

            for (double phi = 0; phi < 2 * Math.PI; phi += PHI_SPACING) {
                double cosPhi = Math.cos(phi), sinPhi = Math.sin(phi);

                double circleX = R2 + R1 * cosTheta;
                double circleY = R1 * sinTheta;

                double x = circleX * (cosB * cosPhi + sinA * sinB * sinPhi)
                        - circleY * cosA * sinB;
                double y = circleX * (sinB * cosPhi - sinA * cosB * sinPhi)
                        + circleY * cosA * cosB;
                double z = K2 + cosA * circleX * sinPhi + circleY * sinA;
                double ooz = 1 / z;

                int xp = (int) (SCREEN_WIDTH / 2 + K1 * ooz * x);
                int yp = (int) (SCREEN_HEIGHT / 2 - K1 * ooz * y);

                double L = cosPhi * cosTheta * sinB - cosA * cosTheta * sinPhi
                        - sinA * sinTheta + cosB * (cosA * sinTheta - cosTheta * sinA * sinPhi);

                if (xp >= 0 && xp < SCREEN_WIDTH && yp >= 0 && yp < SCREEN_HEIGHT) {
                    if (ooz > zBuffer[yp][xp]) {
                        zBuffer[yp][xp] = ooz;
                        int luminanceIndex = (int) (L * 8);
                        output[yp][xp] = ".,-~:;=!*#$@".charAt(Math.max(0, Math.min(11, luminanceIndex)));
                    }
                }
            }
        }

        // 构建带颜色的输出字符串
        StringBuilder sb = new StringBuilder();
        sb.append(color); // 设置颜色
        for (int i = 0; i < SCREEN_HEIGHT; i++) {
            for (int j = 0; j < SCREEN_WIDTH; j++) {
                sb.append(output[i][j]);
            }
            sb.append('\n');
        }
        
        return sb.toString();
    }

    /**
     * 辅助方法：线程休眠
     */
    private static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
