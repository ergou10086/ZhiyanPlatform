package hbnu.project.zhiyannacos.rotate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 抽象东西，别笑
 *
 * @author ErgouTree
 */
public class RotatingDonut {
    private static class WinSize {
        int rows;    // 终端行数
        int cols;    // 终端列数
        int xpixel;  // 宽度像素
        int ypixel;  // 高度像素
        double xRatio; // 列数/宽度像素比
        double yRatio; // 行数/高度像素比
    }

    private static WinSize ws = new WinSize();
    private static final float THETA_SPACING = 0.07f;
    private static final float PHI_SPACING = 0.02f;
    private static final float R1 = 1;
    private static final float R2 = 2;
    private static final float K2 = 5;
    private static float K1;

    static {
        try {
            setWinSize();
        } catch (IOException e) {
            // 初始化失败时使用默认窗口大小
            System.err.println("警告：获取窗口大小失败，使用默认值");
            setDefaultWinSize();
        }
    }

    // 适配 Windows 和 Linux 的窗口大小获取
    private static void setWinSize() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows 系统：使用 mode 命令获取控制台大小
            Process p = Runtime.getRuntime().exec("mode con");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8)
            );
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("列:")) {
                    ws.cols = Integer.parseInt(line.split(":")[1].trim());
                } else if (line.startsWith("行:")) {
                    ws.rows = Integer.parseInt(line.split(":")[1].trim());
                }
            }
            reader.close();
        } else {
            // Linux/macOS 系统：使用 stty 命令
            Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "stty size 2>/dev/null"});
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8)
            );
            String output = reader.readLine();
            reader.close();
            if (output != null && !output.isEmpty()) {
                String[] parts = output.trim().split(" ");
                ws.rows = Integer.parseInt(parts[0]);
                ws.cols = Integer.parseInt(parts[1]);
            }
        }

        // 若获取失败，使用默认值
        if (ws.rows <= 0 || ws.cols <= 0) {
            setDefaultWinSize();
        }

        // 计算像素和比例（假设字符宽高比 1:2）
        ws.xpixel = ws.cols * 8;  // 每个字符8像素宽
        ws.ypixel = ws.rows * 16; // 每个字符16像素高
        ws.xRatio = (float) ws.cols / ws.xpixel;
        ws.yRatio = (float) ws.rows / ws.ypixel;
        K1 = ws.ypixel * K2 * 3f / (8f * (R1 + R2));
    }

    // 设置默认窗口大小（避免初始化失败）
    private static void setDefaultWinSize() {
        ws.rows = 24;
        ws.cols = 80;
        ws.xpixel = ws.cols * 8;
        ws.ypixel = ws.rows * 16;
        ws.xRatio = (float) ws.cols / ws.xpixel;
        ws.yRatio = (float) ws.rows / ws.ypixel;
        K1 = ws.ypixel * K2 * 3f / (8f * (R1 + R2));
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        float A = 0;
        float B = (float) Math.PI / 2;  // 初始角度（弧度）

        // 隐藏光标（Windows Terminal 支持）
        System.out.print("\033[?25l");

        try {
            while (true) {
                renderFrame(A, B);
                Thread.sleep(50);  // 控制帧率
                A += 0.1f;
                B += 0.1f;
            }
        } finally {
            // 恢复光标显示
            System.out.print("\033[?25h");
        }
    }

    private static void renderFrame(float A, float B) throws IOException {
        try {
            setWinSize();  // 每次渲染前更新窗口大小
        } catch (IOException e) {
            // 忽略更新失败，使用上次的窗口大小
        }

        // 初始化输出缓冲区和深度缓冲区
        char[][] output = new char[ws.cols][ws.rows];
        float[][] zbuffer = new float[ws.cols][ws.rows];

        // 填充空格
        for (char[] row : output) {
            Arrays.fill(row, ' ');
        }
        // 深度缓冲区初始化为0
        for (float[] row : zbuffer) {
            Arrays.fill(row, 0f);
        }

        float cosA = (float) Math.cos(A);
        float sinA = (float) Math.sin(A);
        float cosB = (float) Math.cos(B);
        float sinB = (float) Math.sin(B);

        // 计算甜甜圈的每个点
        for (float theta = 0; theta < 2 * Math.PI; theta += THETA_SPACING) {
            float cosTheta = (float) Math.cos(theta);
            float sinTheta = (float) Math.sin(theta);

            for (float phi = 0; phi < 2 * Math.PI; phi += PHI_SPACING) {
                float cosPhi = (float) Math.cos(phi);
                float sinPhi = (float) Math.sin(phi);

                // 圆环上的点坐标
                float circleX = R2 + R1 * cosTheta;
                float circleY = R1 * sinTheta;

                // 3D旋转计算
                float x = circleX * (cosB * cosPhi + sinA * sinB * sinPhi) - cosA * sinB * circleY;
                float y = circleX * (cosPhi * sinB - cosB * sinA * sinPhi) + cosA * cosB * circleY;
                float z = K2 + cosA * circleX * sinPhi + circleY * sinA;
                float ooz = 1f / z;  // 1/z，用于透视投影

                // 投影到2D屏幕
                int xp = (int) ((ws.xpixel / 2f + K1 * ooz * x) * ws.xRatio);
                int yp = (int) ((ws.ypixel / 2f - K1 * ooz * y) * ws.yRatio + 1);

                // 计算亮度 (光照)
                float L = cosPhi * cosTheta * sinB
                        - cosA * cosTheta * sinPhi
                        - sinA * sinTheta
                        + cosB * (cosA * sinTheta - cosTheta * sinA * sinPhi);

                // 只绘制朝向观察者的面 (L > 0)
                if (L > 0) {
                    // 检查坐标是否在屏幕范围内
                    if (xp >= 0 && xp < ws.cols && yp >= 0 && yp < ws.rows) {
                        // 深度测试：只绘制更近的点
                        if (ooz > zbuffer[xp][yp]) {
                            zbuffer[xp][yp] = ooz;
                            int luminanceIndex = (int) (L * 8);
                            // 确保索引在有效范围内
                            luminanceIndex = Math.min(11, Math.max(0, luminanceIndex));
                            output[xp][yp] = ".,-~:;=!*#$@".charAt(luminanceIndex);
                        }
                    }
                }
            }
        }

        // 清屏并移动光标到左上角（兼容 Windows Terminal）
        System.out.print("\033[H\033[2J");

        // 绘制帧
        OutputStream out = System.out;
        for (int j = 0; j < ws.rows; j++) {
            for (int i = 0; i < ws.cols; i++) {
                out.write(output[i][j]);
            }
            out.write('\n');
        }
        out.flush();
    }
}