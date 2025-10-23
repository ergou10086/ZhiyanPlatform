package hbnu.project.zhiyanwiki.service;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * 内容差异对比服务
 * 使用 java-diff-utils 实现差异计算和补丁应用
 *
 * @author ErgouTree
 */
@Slf4j
@Service
public class DiffService {

    /**
     * 计算两个文本内容的差异（生成Unified Diff格式）
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

        // 将内容按行分割
        List<String> oldLines = Arrays.asList(oldContent.split("\n", -1));
        List<String> newLines = Arrays.asList(newContent.split("\n", -1));

        // 计算差异
        Patch<String> patch = DiffUtils.diff(oldLines, newLines);

        // 生成Unified Diff格式（类似git diff）
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                "old", // 旧文件名
                "new", // 新文件名
                oldLines,
                patch,
                3 // 上下文行数
        );

        // 将差异行合并成字符串
        return String.join("\n", unifiedDiff);
    }

    /**
     * 应用差异补丁到原始内容
     *
     * @param originalContent 原始内容
     * @param diffPatch       差异补丁
     * @return 应用补丁后的内容
     * @throws RuntimeException 如果补丁应用失败
     */
    public String applyPatch(String originalContent, String diffPatch) {
        if (originalContent == null) {
            originalContent = "";
        }

        try {
            List<String> originalLines = Arrays.asList(originalContent.split("\n", -1));

            // 解析Unified Diff格式的补丁
            Patch<String> patch = UnifiedDiffUtils.parseUnifiedDiff(
                    Arrays.asList(diffPatch.split("\n"))
            );

            // 应用补丁
            List<String> patchedLines = patch.applyTo(originalLines);

            return String.join("\n", patchedLines);
        } catch (PatchFailedException e) {
            log.error("应用差异补丁失败", e);
            throw new RuntimeException("应用差异补丁失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算内容的哈希值（用于快速比较内容是否相同）
     *
     * @param content 内容
     * @return SHA-256哈希值（十六进制字符串）
     */
    public String calculateHash(String content) {
        if (content == null) {
            content = "";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256算法不可用", e);
            throw new RuntimeException("计算哈希失败", e);
        }
    }

    /**
     * 计算变更统计信息
     *
     * @param oldContent 旧内容
     * @param newContent 新内容
     * @return 变更统计信息
     */
    public ChangeStats calculateStats(String oldContent, String newContent) {
        if (oldContent == null) {
            oldContent = "";
        }
        if (newContent == null) {
            newContent = "";
        }

        List<String> oldLines = Arrays.asList(oldContent.split("\n", -1));
        List<String> newLines = Arrays.asList(newContent.split("\n", -1));

        Patch<String> patch = DiffUtils.diff(oldLines, newLines);

        int addedLines = 0;
        int deletedLines = 0;

        // 统计新增和删除的行数
        patch.getDeltas().forEach(delta -> {
            switch (delta.getType()) {
                case INSERT:
                    addedLines += delta.getTarget().size();
                    break;
                case DELETE:
                    deletedLines += delta.getSource().size();
                    break;
                case CHANGE:
                    addedLines += delta.getTarget().size();
                    deletedLines += delta.getSource().size();
                    break;
            }
        });

        // 计算字符变化数
        int changedChars = Math.abs(newContent.length() - oldContent.length());

        return ChangeStats.builder()
                .addedLines(addedLines)
                .deletedLines(deletedLines)
                .changedChars(changedChars)
                .build();
    }

    /**
     * 逆向应用补丁（从新版本回退到旧版本）
     *
     * @param newContent 新内容
     * @param diffPatch  从旧到新的差异补丁
     * @return 逆向应用后的内容（旧内容）
     */
    public String reversePatch(String newContent, String diffPatch) {
        // 解析补丁并反向应用
        // 这里的实现逻辑是：如果有从old到new的补丁，我们需要反过来应用
        try {
            List<String> newLines = Arrays.asList(newContent.split("\n", -1));
            
            Patch<String> patch = UnifiedDiffUtils.parseUnifiedDiff(
                    Arrays.asList(diffPatch.split("\n"))
            );

            // 创建反向补丁
            Patch<String> reversePatch = new Patch<>();
            patch.getDeltas().forEach(delta -> {
                reversePatch.addDelta(delta);
            });

            // 应用反向补丁
            List<String> oldLines = reversePatch.restore(newLines);
            return String.join("\n", oldLines);
        } catch (Exception e) {
            log.error("逆向应用补丁失败", e);
            throw new RuntimeException("逆向应用补丁失败: " + e.getMessage(), e);
        }
    }

    /**
     * 变更统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeStats {
        /**
         * 新增行数
         */
        private Integer addedLines;

        /**
         * 删除行数
         */
        private Integer deletedLines;

        /**
         * 变更字符数
         */
        private Integer changedChars;
    }
}