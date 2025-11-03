package hbnu.project.zhiyanwiki.service;

import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import hbnu.project.zhiyancommonbasic.utils.DateUtils;
import hbnu.project.zhiyanwiki.model.dto.WikiExportDTO;
import hbnu.project.zhiyanwiki.model.entity.WikiContent;
import hbnu.project.zhiyanwiki.model.entity.WikiPage;
import hbnu.project.zhiyanwiki.model.enums.ExportFormat;
import hbnu.project.zhiyanwiki.model.enums.PageType;
import hbnu.project.zhiyanwiki.repository.WikiPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Wiki导出服务
 * 提供将Wiki页面导出为不同格式的功能
 *
 * @author Tokito
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WikiExportService {

    private final WikiPageRepository wikiPageRepository;
    private final WikiContentService wikiContentService;
    private final WikiPageService wikiPageService;

    /**
     * 导出单个Wiki页面
     *
     * @param pageId    页面ID
     * @param exportDTO 导出配置
     * @return 导出的文件内容（字节数组）
     */
    public byte[] exportPage(Long pageId, WikiExportDTO exportDTO) {
        WikiPage page = wikiPageRepository.findById(pageId)
                .orElseThrow(() -> new ServiceException("Wiki页面不存在"));

        ExportFormat format = getExportFormat(exportDTO.getFormat());

        return switch (format) {
            case MARKDOWN -> exportToMarkdown(page, exportDTO);
            case PDF -> exportToPdf(page, exportDTO);
            case WORD -> exportToWord(page, exportDTO);
        };
    }

    /**
     * 批量导出Wiki页面（打包为ZIP）
     *
     * @param pageIds   页面ID列表
     * @param exportDTO 导出配置
     * @return ZIP文件内容
     */
    public byte[] exportPages(List<Long> pageIds, WikiExportDTO exportDTO) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Long pageId : pageIds) {
                try {
                    WikiPage page = wikiPageRepository.findById(pageId).orElse(null);
                    if (page == null) {
                        log.warn("页面不存在，跳过: pageId={}", pageId);
                        continue;
                    }

                    // 导出页面
                    byte[] content = exportPage(pageId, exportDTO);
                    
                    // 生成文件名
                    String fileName = generateFileName(page, exportDTO.getFormat());
                    
                    // 添加到ZIP
                    ZipEntry entry = new ZipEntry(fileName);
                    zos.putNextEntry(entry);
                    zos.write(content);
                    zos.closeEntry();

                } catch (Exception e) {
                    log.error("导出页面失败: pageId={}", pageId, e);
                }
            }

            zos.finish();
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("创建ZIP文件失败", e);
            throw new ServiceException("批量导出失败: " + e.getMessage());
        }
    }

    /**
     * 导出整个目录树
     *
     * @param rootPageId 根页面ID
     * @param exportDTO  导出配置
     * @return ZIP文件内容
     */
    public byte[] exportDirectory(Long rootPageId, WikiExportDTO exportDTO) {
        WikiPage rootPage = wikiPageRepository.findById(rootPageId)
                .orElseThrow(() -> new ServiceException("Wiki页面不存在"));

        if (rootPage.getPageType() != PageType.DIRECTORY) {
            throw new ServiceException("只能导出目录类型的页面");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // 递归导出目录及其子页面
            exportDirectoryRecursive(rootPage, "", zos, exportDTO);

            zos.finish();
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("导出目录失败", e);
            throw new ServiceException("目录导出失败: " + e.getMessage());
        }
    }

    /**
     * 递归导出目录
     */
    private void exportDirectoryRecursive(WikiPage page, String pathPrefix, 
                                         ZipOutputStream zos, WikiExportDTO exportDTO) throws IOException {
        // 获取子页面
        List<WikiPage> children = wikiPageRepository.findChildPages(page.getProjectId(), page.getId());

        for (WikiPage child : children) {
            String currentPath = pathPrefix + sanitizeFileName(child.getTitle());

            if (child.getPageType() == PageType.DIRECTORY) {
                // 如果是目录，递归处理
                exportDirectoryRecursive(child, currentPath + "/", zos, exportDTO);
            } else {
                // 如果是文档，导出内容
                try {
                    byte[] content = exportPage(child.getId(), exportDTO);
                    String fileName = currentPath + getFileExtension(exportDTO.getFormat());
                    
                    ZipEntry entry = new ZipEntry(fileName);
                    zos.putNextEntry(entry);
                    zos.write(content);
                    zos.closeEntry();
                    
                    log.debug("导出页面: {}", fileName);
                } catch (Exception e) {
                    log.error("导出页面失败: pageId={}", child.getId(), e);
                }
            }
        }
    }

    /**
     * 导出为Markdown格式
     */
    private byte[] exportToMarkdown(WikiPage page, WikiExportDTO exportDTO) {
        if (page.getPageType() == PageType.DIRECTORY) {
            throw new ServiceException("目录类型不能导出，请使用导出目录树功能");
        }

        StringBuilder markdown = new StringBuilder();

        // 添加标题
        markdown.append("# ").append(page.getTitle()).append("\n\n");

        // 添加元数据
        markdown.append("---\n");
        markdown.append("创建时间: ").append(formatDateTime(page.getCreatedAt())).append("\n");
        markdown.append("更新时间: ").append(formatDateTime(page.getUpdatedAt())).append("\n");
        markdown.append("版本: ").append(page.getCurrentVersion()).append("\n");
        if (page.getPath() != null) {
            markdown.append("路径: ").append(page.getPath()).append("\n");
        }
        markdown.append("---\n\n");

        // 添加内容
        try {
            WikiContent content = wikiContentService.getContent(page.getId());
            markdown.append(content.getContent());
        } catch (Exception e) {
            log.error("获取Wiki内容失败: pageId={}", page.getId(), e);
            markdown.append("*内容加载失败*\n");
        }

        return markdown.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 导出为PDF格式
     * TODO: 实现PDF导出（需要引入PDF库，如iText或Apache PDFBox）
     */
    private byte[] exportToPdf(WikiPage page, WikiExportDTO exportDTO) {
        throw new ServiceException("PDF导出功能暂未实现，敬请期待");
    }

    /**
     * 导出为Word格式
     * TODO: 实现Word导出（需要引入Apache POI库）
     */
    private byte[] exportToWord(WikiPage page, WikiExportDTO exportDTO) {
        throw new ServiceException("Word导出功能暂未实现，敬请期待");
    }

    /**
     * 获取导出格式
     */
    private ExportFormat getExportFormat(String format) {
        if (format == null || format.isEmpty()) {
            return ExportFormat.MARKDOWN;
        }
        try {
            return ExportFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ServiceException("不支持的导出格式: " + format);
        }
    }

    /**
     * 生成导出文件名
     */
    private String generateFileName(WikiPage page, String format) {
        String extension = getFileExtension(format);
        String safeName = sanitizeFileName(page.getTitle());
        return safeName + extension;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String format) {
        ExportFormat exportFormat = getExportFormat(format);
        return exportFormat.getExtension();
    }

    /**
     * 清理文件名（移除非法字符）
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "untitled";
        }
        // 替换Windows和Unix文件系统中的非法字符
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_")
                       .replaceAll("\\s+", "_")
                       .trim();
    }

    /**
     * 格式化日期时间
     */
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "未知";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}

