package hbnu.project.zhiyanactivelog.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志导出控制器
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/zhiyan/activelog")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "日志导出管理", description = "提供对操作日志导出的相关接口")
public class OperationLogExportController {
}
