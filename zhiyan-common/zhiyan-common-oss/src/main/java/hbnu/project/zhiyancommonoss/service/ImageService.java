package hbnu.project.zhiyancommonoss.service;

import hbnu.project.zhiyancommonbasic.exception.file.FileProcessException;
import hbnu.project.zhiyancommonoss.properties.MinioProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片处理服务
 * 提供图片压缩、缩放、裁剪等功能
 *
 * @author ErgouTree
 */
@Getter
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final MinioProperties minioProperties;


    /**
     * 获取图片尺寸信息
     *
     * @param imageStream 图片输入流
     * @return [宽度, 高度]
     */
    public int[] getImageDimensions(InputStream imageStream) {
        try {
            BufferedImage image = ImageIO.read(imageStream);
            if (image != null) {
                return new int[]{image.getWidth(), image.getHeight()};
            }
            throw new FileProcessException("无法读取图片尺寸");
        } catch (IOException e) {
            log.error("获取图片尺寸失败", e);
            throw new FileProcessException("获取图片尺寸失败: " + e.getMessage(), e);
        }
    }


    /**
     * 转换图片格式
     *
     * @param imageStream 图片输入流
     * @param format      目标格式（jpg, png, webp等）
     * @return 转换后的字节数组
     */
    public byte[] convertFormat(InputStream imageStream, String format) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thumbnails.of(imageStream)
                    .scale(1.0)
                    .outputFormat(format)
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("转换图片格式失败: format={}", format, e);
            throw new FileProcessException("转换图片格式失败: " + e.getMessage(), e);
        }
    }


    /**
     * 生成指定尺寸的缩略图
     *
     * @param originalImage 原始图片输入流
     * @param width         目标宽度
     * @param height        目标高度
     * @param keepRatio     是否保持宽高比
     * @return 缩略图字节数组
     */
    public byte[] generateThumbnail(InputStream originalImage, int width, int height, boolean keepRatio) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            MinioProperties.ImageConfig imageConfig = new MinioProperties.ImageConfig();
            float quality = imageConfig.getQuality();

            if (keepRatio) {
                // 保持宽高比，可能会有留白
                Thumbnails.of(originalImage)
                        .size(width, height)
                        .outputQuality(quality)
                        .outputFormat("jpg")
                        .toOutputStream(outputStream);
            } else {
                // 强制指定尺寸，不保持宽高比
                Thumbnails.of(originalImage)
                        .forceSize(width, height)
                        .outputQuality(quality)
                        .outputFormat("jpg")
                        .toOutputStream(outputStream);
            }

            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("生成缩略图失败: width={}, height={}", width, height, e);
            throw new FileProcessException("生成缩略图失败: " + e.getMessage(), e);
        }
    }


    /**
     * 压缩图片到指定质量
     *
     * @param imageStream 图片输入流
     * @param quality     质量（0.0-1.0）
     * @return 压缩后的字节数组
     */
    public byte[] compressImage(InputStream imageStream, float quality) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thumbnails.of(imageStream)
                    .scale(1.0)
                    .outputQuality(quality)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("压缩图片失败: quality={}", quality, e);
            throw new FileProcessException("压缩图片失败: " + e.getMessage(), e);
        }
    }


    /**
     * 将字节数组转为InputStream
     *
     * @param bytes 字节数组
     * @return InputStream
     */
    public InputStream bytesToInputStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }


    /**
     * 验证图片是否有效
     *
     * @param imageStream 图片输入流
     * @return 是否有效
     */
    public boolean validateImage(InputStream imageStream) {
        try {
            BufferedImage image = ImageIO.read(imageStream);
            return image != null && image.getWidth() > 0 && image.getHeight() > 0;
        } catch (IOException e) {
            log.warn("图片验证失败", e);
            return false;
        }
    }

}
