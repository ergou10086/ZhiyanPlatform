package hbnu.project.zhiyanwiki.service;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import hbnu.project.zhiyanwiki.model.entity.ChangeStats; // 导入独立实体类
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * 内容差异对比服务
 * 使用 java-diff-utils 实现差异计算、补丁应用、哈希生成及变更统计
 *
 * @author ErgouTree
 */
@Slf4j
@Service
public class DiffService {

    /**
     * 计算两个文本内容的差异（生成Unified Diff格式，类似Git Diff）
     *
     * @param oldContent 旧内容
     * @param newContent 新内容
     * @return Unified Diff格式的差异字符串
     */
    public String calculateDiff(String oldContent, String newContent) {
        if (oldContent == null) {
            oldContent = "";
        }
        if (newContent == null) {
            newContent = "";
        }

        // 将内容按行分割（-1表示保留空行，避免丢失尾部换行符）
        List<String> oldLines = Arrays.asList(oldContent.split("\n", -1));
        List<String> newLines = Arrays.asList(newContent.split("\n", -1));

        // 计算行级差异补丁
        Patch<String> patch = DiffUtils.diff(oldLines, newLines);

        // 生成Unified Diff格式（上下文行数设为3，平衡可读性和简洁性）
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                "old",   // 旧内容标识（仅用于Diff格式显示）
                "new",   // 新内容标识（仅用于Diff格式显示）
                oldLines,
                patch,
                3
        );

        // 合并差异行为字符串返回
        return String.join("\n", unifiedDiff);
    }

    /**
     * 应用差异补丁到原始内容（从旧内容生成新内容）
     *
     * @param originalContent 原始内容（旧内容）
     * @param diffPatch       Unified Diff格式的差异补丁
     * @return 应用补丁后的新内容
     * @throws RuntimeException 补丁解析失败或应用失败时抛出
     */
    public String applyPatch(String originalContent, String diffPatch) {
        if (originalContent == null) {
            originalContent = "";
        }

        try {
            // 分割原始内容为行列表
            List<String> originalLines = Arrays.asList(originalContent.split("\n", -1));

            // 解析Unified Diff格式的补丁
            Patch<String> patch = UnifiedDiffUtils.parseUnifiedDiff(
                    Arrays.asList(diffPatch.split("\n"))
            );

            // 应用补丁生成新内容行列表
            List<String> patchedLines = patch.applyTo(originalLines);

            // 合并行列表为字符串返回
            return String.join("\n", patchedLines);
        } catch (PatchFailedException e) {
            log.error("应用差异补丁失败", e);
            throw new RuntimeException("应用差异补丁失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算内容的SHA-256哈希值（用于快速判断内容是否变更）
     *
     * @param content 待计算哈希的内容
     * @return 小写的SHA-256十六进制哈希字符串
     * @throws RuntimeException SHA-256算法不可用时抛出（理论上不会发生）
     */
    public String calculateHash(String content) {
        if (content == null) {
            content = "";
        }

        try {
            // 获取SHA-256消息摘要实例
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 计算内容的哈希字节数组（使用UTF-8编码避免字符集差异）
            byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));

            // 转换字节数组为小写十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b); // 确保单字节转换为两位十六进制
                if (hex.length() == 1) {
                    hexString.append('0'); // 补零（如0x1 → "01"）
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256算法不可用（JDK环境异常）", e);
            throw new RuntimeException("计算内容哈希失败", e);
        }
    }

    /**
     * 计算新旧内容的变更统计（新增行数、删除行数、变更字符数）
     *
     * @param oldContent 旧内容
     * @param newContent 新内容
     * @return 变更统计实体（ChangeStats）
     */
    public ChangeStats calculateStats(String oldContent, String newContent) {
        if (oldContent == null) {
            oldContent = "";
        }
        if (newContent == null) {
            newContent = "";
        }

        // 分割内容为行列表
        List<String> oldLines = Arrays.asList(oldContent.split("\n", -1));
        List<String> newLines = Arrays.asList(newContent.split("\n", -1));

        // 计算行级差异
        Patch<String> patch = DiffUtils.diff(oldLines, newLines);

        // 统计新增/删除行数（使用数组实现lambda内部变量修改）
        int[] addedLines = {0};
        int[] deletedLines = {0};

        // 遍历差异块（Delta）统计行数
        patch.getDeltas().forEach(delta -> {
            switch (delta.getType()) {
                case INSERT: // 新增行（仅目标端有内容）
                    addedLines[0] += delta.getTarget().size();
                    break;
                case DELETE: // 删除行（仅源端有内容）
                    deletedLines[0] += delta.getSource().size();
                    break;
                case CHANGE: // 修改行（源端删除、目标端新增）
                    addedLines[0] += delta.getTarget().size();
                    deletedLines[0] += delta.getSource().size();
                    break;
            }
        });

        // 计算字符变更数（新旧内容长度差值的绝对值）
        int changedChars = Math.abs(newContent.length() - oldContent.length());

        // 构建并返回统计实体
        return ChangeStats.builder()
                .addedLines(addedLines[0])
                .deletedLines(deletedLines[0])
                .changedChars(changedChars)
                .build();
    }

    /**
     * 逆向应用补丁（从新内容回退到旧内容）
     * 核心逻辑：对"旧→新"的补丁反向解析，生成"新→旧"的回退效果
     *
     * @param newContent 新内容（需要回退的内容）
     * @param diffPatch  "旧→新"的差异补丁
     * @return 回退后的旧内容
     * @throws RuntimeException 补丁解析失败或回退失败时抛出
     */
    public String reversePatch(String newContent, String diffPatch) {
        if (newContent == null) {
            newContent = "";
        }

        try {
            // 分割新内容为行列表
            List<String> newLines = Arrays.asList(newContent.split("\n", -1));

            // 解析"旧→新"的差异补丁
            Patch<String> patch = UnifiedDiffUtils.parseUnifiedDiff(
                    Arrays.asList(diffPatch.split("\n"))
            );

            // 反向应用补丁（通过restore方法从新内容恢复旧内容）
            List<String> oldLines = patch.restore(newLines);

            // 合并行列表为旧内容字符串返回
            return String.join("\n", oldLines);
        } catch (Exception e) {
            log.error("逆向应用补丁失败（回退内容版本出错）", e);
            throw new RuntimeException("逆向应用补丁失败: " + e.getMessage(), e);
        }
    }
}