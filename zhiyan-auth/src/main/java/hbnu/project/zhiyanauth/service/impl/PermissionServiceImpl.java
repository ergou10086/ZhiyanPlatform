package hbnu.project.zhiyanauth.service.impl;

import hbnu.project.zhiyanauth.mapper.PermissionMapper;
import hbnu.project.zhiyanauth.model.dto.PermissionDTO;
import hbnu.project.zhiyanauth.model.entity.Permission;
import hbnu.project.zhiyanauth.repository.PermissionRepository;
import hbnu.project.zhiyanauth.service.PermissionService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonredis.service.RedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 * 负责权限的CRUD操作、用户权限校验、权限缓存管理等核心业务逻辑
 * 采用"缓存优先"策略提升权限查询性能，权限变更时同步清理缓存保证数据一致性   ——yui
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Autowired
    private PermissionMapper permissionMapper;

    private final RedisService redisService;

    // 缓存相关常量
    private static final String USER_PERMISSIONS_CACHE_PREFIX = "user:permissions:";
    private static final String PERMISSION_CACHE_PREFIX = "permission:";
    // 30分钟过期
    private static final long CACHE_EXPIRE_TIME = 1800L;


    /**
     * 校验用户是否拥有指定权限
     * 采用"缓存优先"策略：先查缓存，缓存未命中则查数据库并更新缓存
     *
     * @param userId     用户ID（不能为空）
     * @param permission 待校验的权限名称（不能为空）
     * @return R<Boolean> - 校验结果：true=拥有权限，false=无权限；失败时返回错误信息
     */
    @Override
    public R<Boolean> hasPermission(Long userId, String permission) {
        try {
            if (userId == null || !StringUtils.hasText(permission)) {
                return R.ok(false);
            }

            // 先从缓存获取用户权限
            Set<String> userPermissions = getUserPermissionsFromCache(userId);
            if (userPermissions == null) {
                // 缓存未命中，从数据库查询
                List<Permission> permissions = permissionRepository.findAllByUserId(userId);
                // 转set去重
                userPermissions = permissions.stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet());
                
                // 缓存用户权限
                cacheUserPermissions(userId, userPermissions);
            }

            // 判断用户权限集合中是否包含目标权限
            boolean hasPermission = userPermissions.contains(permission);
            log.debug("检查用户[{}]是否拥有权限[{}]: {}", userId, permission, hasPermission);
            
            return R.ok(hasPermission);
        } catch (Exception e) {
            log.error("检查用户权限失败: userId={}, permission={}", userId, permission, e);
            return R.fail("权限检查失败");
        }
    }


    /**
     * 获取用户的所有权限列表
     * 与hasPermission共享缓存逻辑，避免重复查询
     *
     * @param userId 用户ID（不能为空）
     * @return R<Set<String>> - 成功返回用户的权限名称集合；失败返回错误信息
     */
    @Override
    public R<Set<String>> getUserPermissions(Long userId) {
        try {
            if (userId == null) {
                return R.fail("用户ID不能为空");
            }

            // 先从缓存获取
            Set<String> userPermissions = getUserPermissionsFromCache(userId);
            if (userPermissions == null) {
                // 缓存未命中，从数据库查询
                List<Permission> permissions = permissionRepository.findAllByUserId(userId);
                userPermissions = permissions.stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet());
                
                // 缓存用户权限
                cacheUserPermissions(userId, userPermissions);
            }

            log.debug("获取用户[{}]权限列表，共{}个权限", userId, userPermissions.size());
            return R.ok(userPermissions);
        } catch (Exception e) {
            log.error("获取用户权限失败: userId={}", userId, e);
            return R.fail("获取用户权限失败");
        }
    }


    /**
     * 校验用户是否拥有指定权限列表中的任一权限
     * 常用于"满足一个权限即可访问"的场景（如：管理员/操作员均可操作）
     *
     * @param userId      用户ID
     * @param permissions 待校验的权限列表（不能为空）
     * @return R<Boolean> - 校验结果：true=拥有任一权限，false=无任一权限
     */
    @Override
    public R<Boolean> hasAnyPermission(Long userId, List<String> permissions) {
        try {
            if (userId == null || permissions == null || permissions.isEmpty()) {
                return R.ok(false);
            }

            // 调用getUserPermissions获取用户所有权限
            R<Set<String>> userPermissionsResult = getUserPermissions(userId);
            if (!R.isSuccess(userPermissionsResult)) {
                return R.fail("获取用户权限失败");
            }

            Set<String> userPermissions = userPermissionsResult.getData();
            
            // 检查是否拥有任一权限
            boolean hasAny = permissions.stream()
                    .anyMatch(userPermissions::contains);

            log.debug("检查用户[{}]是否拥有权限列表{}中的任一权限: {}", userId, permissions, hasAny);
            return R.ok(hasAny);
        } catch (Exception e) {
            log.error("批量检查用户权限失败: userId={}, permissions={}", userId, permissions, e);
            return R.fail("批量权限检查失败");
        }
    }


    /**
     * 分页查询所有权限（管理员后台常用）
     * 支持分页、排序，返回DTO对象避免暴露数据库实体细节
     *
     * @param pageable 分页参数（包含页码、页大小、排序规则）
     * @return R<Page<PermissionDTO>> - 分页后的权限DTO列表
     */
    @Override
    public R<Page<PermissionDTO>> getAllPermissions(Pageable pageable) {
        try {
            Page<Permission> permissionPage = permissionRepository.findAll(pageable);
            List<PermissionDTO> permissionDTOs = permissionMapper.toDTOList(permissionPage.getContent());
            
            Page<PermissionDTO> result = new PageImpl<>(permissionDTOs, pageable, permissionPage.getTotalElements());
            
            log.debug("获取权限列表，页码: {}, 大小: {}, 总数: {}", 
                     pageable.getPageNumber(), pageable.getPageSize(), permissionPage.getTotalElements());
            return R.ok(result);
        } catch (Exception e) {
            log.error("获取权限列表失败", e);
            return R.fail("获取权限列表失败");
        }
    }



    /**
     * 创建新权限（高级别管理员操作）
     * 包含权限名称唯一性校验，事务控制确保数据一致性，创建后清理相关缓存
     *
     * @param permissionDTO 权限DTO（包含权限名称、描述等信息）
     * @return R<PermissionDTO> - 成功返回创建后的权限DTO；失败返回错误信息
     */
    @Override
    @Transactional
    public R<PermissionDTO> createPermission(PermissionDTO permissionDTO) {
        try {
            if (permissionDTO == null || !StringUtils.hasText(permissionDTO.getName())) {
                return R.fail("权限信息不完整");
            }

            // 校验权限名称唯一性：避免重复创建相同名称的权限
            if (permissionRepository.existsByName(permissionDTO.getName())) {
                return R.fail("权限名称已存在: " + permissionDTO.getName());
            }

            // DTO转换为数据库实体（Permission）
            Permission permission = permissionMapper.fromDTO(permissionDTO);
            // 保存实体到数据库，返回保存后的实体
            Permission savedPermission = permissionRepository.save(permission);

            // 实体转换为DTO，返回给前端
            PermissionDTO result = permissionMapper.toDTO(savedPermission);
            
            // 清理相关缓存
            clearPermissionCache(savedPermission.getId());
            
            log.info("创建权限成功: {}", savedPermission.getName());
            return R.ok(result, "权限创建成功");
        } catch (Exception e) {
            log.error("创建权限失败: {}", permissionDTO, e);
            return R.fail("创建权限失败");
        }
    }


    /**
     * 更新已有权限（管理员操作）
     * 包含权限存在性校验、名称唯一性校验，更新后清理权限缓存和所有用户权限缓存
     *
     * @param permissionId 待更新的权限ID
     * @param permissionDTO 新的权限信息（DTO）
     * @return R<PermissionDTO> - 成功返回更新后的权限DTO；失败返回错误信息
     */
    @Override
    @Transactional
    public R<PermissionDTO> updatePermission(Long permissionId, PermissionDTO permissionDTO) {
        try {
            if (permissionId == null || permissionDTO == null) {
                return R.fail("权限ID和权限信息不能为空");
            }

            // 校验权限是否存在
            Permission existingPermission = permissionRepository.findById(permissionId)
                    .orElse(null);
            if (existingPermission == null) {
                return R.fail("权限不存在: " + permissionId);
            }

            // 如果修改了名称，检查新名称是否已存在
            if (StringUtils.hasText(permissionDTO.getName()) && 
                !permissionDTO.getName().equals(existingPermission.getName())) {
                if (permissionRepository.existsByName(permissionDTO.getName())) {
                    return R.fail("权限名称已存在: " + permissionDTO.getName());
                }
            }

            // 更新实体信息：将DTO中的非空字段更新到现有实体
            permissionMapper.updatePermission(existingPermission, permissionDTO);
            // 保存更新
            Permission updatedPermission = permissionRepository.save(existingPermission);

            // 实体转换为DTO返回
            PermissionDTO result = permissionMapper.toDTO(updatedPermission);
            
            // 清理相关缓存
            clearPermissionCache(permissionId);
            clearAllUserPermissionsCache();
            
            log.info("更新权限成功: id={}, name={}", permissionId, updatedPermission.getName());
            return R.ok(result, "权限更新成功");
        } catch (Exception e) {
            log.error("更新权限失败: id={}, dto={}", permissionId, permissionDTO, e);
            return R.fail("更新权限失败");
        }
    }


    /**
     * 删除权限（管理员操作）
     * 包含权限存在性校验、关联角色校验（有角色关联时禁止删除），删除后清理缓存
     *
     * @param permissionId 待删除的权限ID
     * @return R<Void> - 成功返回"权限删除成功"；失败返回错误信息
     */
    @Override
    @Transactional
    public R<Void> deletePermission(Long permissionId) {
        try {
            if (permissionId == null) {
                return R.fail("权限ID不能为空");
            }

            // 校验权限是否存在
            Permission permission = permissionRepository.findById(permissionId)
                    .orElse(null);
            if (permission == null) {
                return R.fail("权限不存在: " + permissionId);
            }

            // 校验权限是否被角色关联：有角色关联时禁止删除
            if (permission.getRolePermissions() != null && !permission.getRolePermissions().isEmpty()) {
                return R.fail("无法删除权限，该权限已被角色使用");
            }

            permissionRepository.delete(permission);
            
            // 清理相关缓存
            clearPermissionCache(permissionId);
            clearAllUserPermissionsCache();
            
            log.info("删除权限成功: id={}, name={}", permissionId, permission.getName());
            return R.ok(null, "权限删除成功");
        } catch (Exception e) {
            log.error("删除权限失败: id={}", permissionId, e);
            return R.fail("删除权限失败");
        }
    }


    /**
     * 根据权限ID查询权限实体（内部使用，如权限更新/删除前的校验）
     * 同样采用"缓存优先"策略，提升查询效率
     *
     * @param permissionId 权限ID
     * @return Permission - 成功返回权限实体；失败返回null
     */
    @Override
    public Permission findById(Long permissionId) {
        if (permissionId == null) {
            return null;
        }

        try {
            // 先从缓存查找
            String cacheKey = PERMISSION_CACHE_PREFIX + permissionId;
            Permission cachedPermission = redisService.getCacheObject(cacheKey);
            
            if (cachedPermission != null) {
                return cachedPermission;
            }

            // 缓存未命中，从数据库查询
            Permission permission = permissionRepository.findById(permissionId).orElse(null);
            
            if (permission != null) {
                // 查询到权限实体时，存入缓存
                redisService.setCacheObject(cacheKey, permission, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            }

            return permission;
        } catch (Exception e) {
            log.error("根据ID查找权限失败: id={}", permissionId, e);
            // 如果缓存出错，直接从数据库查询
            return permissionRepository.findById(permissionId).orElse(null);
        }
    }


    /**
     * 从Redis缓存中获取用户的权限列表
     * 封装缓存查询逻辑，避免重复代码
     *
     * @param userId 用户ID
     * @return Set<String> - 缓存中的权限名称集合；缓存不存在或异常时返回null
     */
    private Set<String> getUserPermissionsFromCache(Long userId) {
        try {
            String cacheKey = USER_PERMISSIONS_CACHE_PREFIX + userId;
            // 从Redis读取缓存：依赖RedisService封装的通用缓存查询方法，返回权限集合
            return redisService.getCacheObject(cacheKey);
        } catch (Exception e) {
            log.warn("从缓存获取用户权限失败: userId={}", userId, e);
            return null;
        }
    }


    /**
     * 将用户权限列表存入Redis缓存
     * 封装用户权限的缓存写入逻辑，统一设置缓存过期时间，确保缓存数据时效性
     *
     * @param userId      用户ID（缓存键核心标识，与查询时的键保持一致）
     * @param permissions 需缓存的用户权限集合（从数据库查询后的数据，确保数据准确性）
     */
    private void cacheUserPermissions(Long userId, Set<String> permissions) {
        try {
            // 构建与查询逻辑一致的缓存键，保证缓存读写键的统一性，然后查询
            String cacheKey = USER_PERMISSIONS_CACHE_PREFIX + userId;
            redisService.setCacheObject(cacheKey, permissions, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("缓存用户权限失败: userId={}", userId, e);
        }
    }


    /**
     * 清理指定权限ID对应的Redis缓存
     * 用于权限信息变更（如权限名称修改、权限删除）后，清除单个权限的缓存，避免缓存数据与数据库不一致
     *
     * @param permissionId 待清理缓存的权限ID（缓存键核心标识，确保精准清理目标权限缓存）
     */
    private void clearPermissionCache(Long permissionId) {
        try {
            // 构建单个权限的缓存键，然后删除
            String cacheKey = PERMISSION_CACHE_PREFIX + permissionId;
            redisService.deleteObject(cacheKey);
        } catch (Exception e) {
            log.warn("清理权限缓存失败: permissionId={}", permissionId, e);
        }
    }


    /**
     * 批量清理所有用户的权限缓存
     * 用于全局权限变更场景（如新增/删除通用权限、权限关联关系调整），确保所有用户的权限列表重新从数据库加载
     * 避免部分用户使用旧的权限缓存，导致权限校验结果不准确
     */
    private void clearAllUserPermissionsCache() {
        try {
            // 清理用户权限相关的缓存
            Collection<String> keys = redisService.keys(USER_PERMISSIONS_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisService.deleteObject(keys);
            }
        } catch (Exception e) {
            log.warn("清理用户权限缓存失败", e);
        }
    }
}
