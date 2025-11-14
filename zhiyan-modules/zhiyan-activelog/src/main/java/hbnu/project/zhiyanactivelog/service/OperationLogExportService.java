package hbnu.project.zhiyanactivelog.service;

import hbnu.project.zhiyanactivelog.model.entity.*;
import hbnu.project.zhiyanactivelog.model.vo.UnifiedOperationLogVO;
import hbnu.project.zhiyanactivelog.repository.*;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志导出服务
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogExportService {

    private final ProjectOperationLogRepository projectLogRepository;
    private final TaskOperationLogRepository taskLogRepository;
    private final WikiOperationLogRepository wikiLogRepository;
    private final AchievementOperationLogRepository achievementLogRepository;
    private final LoginLogRepository loginLogRepository;
    private final OperationLogMyselfActionService myselfActionService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    

}
