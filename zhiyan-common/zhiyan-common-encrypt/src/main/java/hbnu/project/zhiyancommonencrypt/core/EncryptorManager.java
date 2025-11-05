package hbnu.project.zhiyancommonencrypt.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonencrypt.annotation.EncryptField;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 加密器管理器
 * 负责管理和缓存所有的加密器实例，以及扫描需要加密的实体字段
 *
 * @author ErgouTree
 * @version 2.0.0
 */
@Slf4j
@NoArgsConstructor
public class EncryptorManager {

    /**
     * 加密器实例缓存
     * Key: 加密上下文的 hashCode
     * Value: 加密器实例
     */
    private final Map<Integer, IEncryptor> encryptorMap = new ConcurrentHashMap<>();

    /**
     * 类加密字段缓存
     * Key: 实体类 Class
     * Value: 该类中标注了 @EncryptField 注解的字段集合
     */
    private final Map<Class<?>, Set<Field>> fieldCache = new ConcurrentHashMap<>();

    /**
     * 构造方法，传入实体类包路径进行扫描
     *
     * @param typeAliasesPackage 实体类包路径（多个包用逗号分隔）
     */
    public EncryptorManager(String typeAliasesPackage) {
        if (StringUtils.isNotEmpty(typeAliasesPackage)) {
            scanEncryptClasses(typeAliasesPackage);
        }
    }

    /**
     * 获取类的加密字段缓存
     *
     * @param sourceClazz 实体类
     * @return 加密字段集合
     */
    public Set<Field> getFieldCache(Class<?> sourceClazz) {
        return fieldCache.get(sourceClazz);
    }

    /**
     * 注册并获取加密器
     * 如果缓存中已存在，则直接返回；否则创建新实例并缓存
     *
     * @param encryptContext 加密上下文
     * @return 加密器实例
     */
    public IEncryptor registAndGetEncryptor(EncryptContext encryptContext) {
        int key = encryptContext.hashCode();
        if (encryptorMap.containsKey(key)) {
            return encryptorMap.get(key);
        }

        // 使用反射创建加密器实例
        IEncryptor encryptor = ReflectUtil.newInstance(
            encryptContext.getAlgorithm().getClazz(),
            encryptContext
        );

        encryptorMap.put(key, encryptor);
        log.debug("注册加密器: {}", encryptContext.getAlgorithm());
        return encryptor;
    }

    /**
     * 移除缓存中的加密器
     *
     * @param encryptContext 加密上下文
     */
    public void removeEncryptor(EncryptContext encryptContext) {
        int key = encryptContext.hashCode();
        encryptorMap.remove(key);
        log.debug("移除加密器: {}", encryptContext.getAlgorithm());
    }

    /**
     * 加密字符串
     *
     * @param value          待加密的值
     * @param encryptContext 加密上下文
     * @return 加密后的字符串
     */
    public String encrypt(String value, EncryptContext encryptContext) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }

        try {
            IEncryptor encryptor = registAndGetEncryptor(encryptContext);
            return encryptor.encrypt(value, encryptContext.getEncode());
        } catch (Exception e) {
            log.error("加密失败: {}", e.getMessage(), e);
            throw new RuntimeException("加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解密字符串
     *
     * @param value          待解密的值
     * @param encryptContext 加密上下文
     * @return 解密后的字符串
     */
    public String decrypt(String value, EncryptContext encryptContext) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }

        try {
            IEncryptor encryptor = registAndGetEncryptor(encryptContext);
            return encryptor.decrypt(value);
        } catch (Exception e) {
            log.error("解密失败: {}", e.getMessage(), e);
            throw new RuntimeException("解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 扫描指定包路径下的实体类，缓存标注了 @EncryptField 注解的字段
     *
     * @param typeAliasesPackage 实体类包路径
     */
    private void scanEncryptClasses(String typeAliasesPackage) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory();

        // 分割多个包路径
        String[] packagePatternArray = typeAliasesPackage.split(
            ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS
        );
        String classpath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

        try {
            for (String packagePattern : packagePatternArray) {
                String path = ClassUtils.convertClassNameToResourcePath(packagePattern.trim());
                Resource[] resources = resolver.getResources(classpath + path + "/**/*.class");

                for (Resource resource : resources) {
                    try {
                        ClassMetadata classMetadata = factory.getMetadataReader(resource).getClassMetadata();
                        String className = classMetadata.getClassName();

                        // 加载类
                        Class<?> clazz = Class.forName(className);

                        // 获取加密字段集合
                        Set<Field> encryptFieldSet = getEncryptFieldSetFromClazz(clazz);
                        if (CollUtil.isNotEmpty(encryptFieldSet)) {
                            fieldCache.put(clazz, encryptFieldSet);
                            log.debug("缓存加密字段: {} - {} 个字段", clazz.getSimpleName(), encryptFieldSet.size());
                        }
                    } catch (Exception e) {
                        log.warn("扫描类失败: {}", resource.getFilename(), e);
                    }
                }
            }

            log.info("加密字段扫描完成，共缓存 {} 个类", fieldCache.size());
        } catch (Exception e) {
            log.error("扫描加密字段时出错: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取一个类中所有标注了 @EncryptField 注解的字段
     *
     * @param clazz 实体类
     * @return 加密字段集合
     */
    private Set<Field> getEncryptFieldSetFromClazz(Class<?> clazz) {
        Set<Field> fieldSet = new HashSet<>();

        // 过滤接口、内部类、匿名类
        if (clazz.isInterface() || clazz.isMemberClass() || clazz.isAnonymousClass()) {
            return fieldSet;
        }

        // 遍历类及其父类，获取所有字段
        Class<?> currentClazz = clazz;
        while (currentClazz != null) {
            Field[] fields = currentClazz.getDeclaredFields();
            fieldSet.addAll(Arrays.asList(fields));
            currentClazz = currentClazz.getSuperclass();
        }

        // 过滤出标注了 @EncryptField 注解且类型为 String 的字段
        fieldSet = fieldSet.stream()
            .filter(field -> field.isAnnotationPresent(EncryptField.class))
            .filter(field -> field.getType() == String.class)
            .collect(Collectors.toSet());

        // 设置字段可访问
        fieldSet.forEach(field -> field.setAccessible(true));

        return fieldSet;
    }

    /**
     * 清空所有缓存
     */
    public void clearCache() {
        encryptorMap.clear();
        fieldCache.clear();
        log.info("清空加密器缓存");
    }

    /**
     * 获取缓存的加密器数量
     *
     * @return 加密器数量
     */
    public int getEncryptorCount() {
        return encryptorMap.size();
    }

    /**
     * 获取缓存的类数量
     *
     * @return 类数量
     */
    public int getFieldCacheCount() {
        return fieldCache.size();
    }
}
