package com.archive.management.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Bean工具类
 * 提供Bean操作的常用方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Component
public class BeanUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    
    /**
     * 属性缓存
     */
    private static final Map<Class<?>, PropertyDescriptor[]> PROPERTY_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 字段缓存
     */
    private static final Map<Class<?>, Field[]> FIELD_CACHE = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    // ========== Spring Bean 获取 ==========

    /**
     * 根据名称获取Bean
     * 
     * @param name Bean名称
     * @return Bean实例
     */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 根据类型获取Bean
     * 
     * @param clazz Bean类型
     * @param <T> Bean类型泛型
     * @return Bean实例
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * 根据名称和类型获取Bean
     * 
     * @param name Bean名称
     * @param clazz Bean类型
     * @param <T> Bean类型泛型
     * @return Bean实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 获取指定类型的所有Bean
     * 
     * @param clazz Bean类型
     * @param <T> Bean类型泛型
     * @return Bean映射
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return applicationContext.getBeansOfType(clazz);
    }

    /**
     * 检查是否包含指定名称的Bean
     * 
     * @param name Bean名称
     * @return 是否包含
     */
    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    /**
     * 检查Bean是否为单例
     * 
     * @param name Bean名称
     * @return 是否为单例
     */
    public static boolean isSingleton(String name) {
        return applicationContext.isSingleton(name);
    }

    /**
     * 获取Bean的类型
     * 
     * @param name Bean名称
     * @return Bean类型
     */
    public static Class<?> getType(String name) {
        return applicationContext.getType(name);
    }

    // ========== Bean 复制 ==========

    /**
     * 复制Bean属性
     * 
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target);
    }

    /**
     * 复制Bean属性（忽略null值）
     * 
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        
        String[] nullPropertyNames = getNullPropertyNames(source);
        BeanUtils.copyProperties(source, target, nullPropertyNames);
    }

    /**
     * 复制Bean属性（指定忽略的属性）
     * 
     * @param source 源对象
     * @param target 目标对象
     * @param ignoreProperties 忽略的属性名
     */
    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target, ignoreProperties);
    }

    /**
     * 复制Bean并返回新实例
     * 
     * @param source 源对象
     * @param targetClass 目标类型
     * @param <T> 目标类型泛型
     * @return 新实例
     */
    public static <T> T copyBean(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Bean复制失败: " + e.getMessage(), e);
        }
    }

    /**
     * 复制Bean并返回新实例（忽略null值）
     * 
     * @param source 源对象
     * @param targetClass 目标类型
     * @param <T> 目标类型泛型
     * @return 新实例
     */
    public static <T> T copyBeanIgnoreNull(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            copyPropertiesIgnoreNull(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Bean复制失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量复制Bean
     * 
     * @param sourceList 源对象列表
     * @param targetClass 目标类型
     * @param <S> 源类型泛型
     * @param <T> 目标类型泛型
     * @return 目标对象列表
     */
    public static <S, T> List<T> copyBeanList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return sourceList.stream()
            .filter(Objects::nonNull)
            .map(source -> copyBean(source, targetClass))
            .collect(Collectors.toList());
    }

    /**
     * 批量复制Bean（忽略null值）
     * 
     * @param sourceList 源对象列表
     * @param targetClass 目标类型
     * @param <S> 源类型泛型
     * @param <T> 目标类型泛型
     * @return 目标对象列表
     */
    public static <S, T> List<T> copyBeanListIgnoreNull(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return sourceList.stream()
            .filter(Objects::nonNull)
            .map(source -> copyBeanIgnoreNull(source, targetClass))
            .collect(Collectors.toList());
    }

    // ========== Bean 转换 ==========

    /**
     * Bean转Map
     * 
     * @param bean Bean对象
     * @return Map
     */
    public static Map<String, Object> beanToMap(Object bean) {
        if (bean == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> map = new HashMap<>();
        BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
        
        PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(bean.getClass());
        for (PropertyDescriptor pd : propertyDescriptors) {
            String propertyName = pd.getName();
            if (!"class".equals(propertyName)) {
                Object value = beanWrapper.getPropertyValue(propertyName);
                map.put(propertyName, value);
            }
        }
        
        return map;
    }

    /**
     * Bean转Map（忽略null值）
     * 
     * @param bean Bean对象
     * @return Map
     */
    public static Map<String, Object> beanToMapIgnoreNull(Object bean) {
        if (bean == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> map = new HashMap<>();
        BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
        
        PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(bean.getClass());
        for (PropertyDescriptor pd : propertyDescriptors) {
            String propertyName = pd.getName();
            if (!"class".equals(propertyName)) {
                Object value = beanWrapper.getPropertyValue(propertyName);
                if (value != null) {
                    map.put(propertyName, value);
                }
            }
        }
        
        return map;
    }

    /**
     * Map转Bean
     * 
     * @param map Map对象
     * @param beanClass Bean类型
     * @param <T> Bean类型泛型
     * @return Bean对象
     */
    public static <T> T mapToBean(Map<String, Object> map, Class<T> beanClass) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        
        try {
            T bean = beanClass.getDeclaredConstructor().newInstance();
            BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
            
            PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(beanClass);
            for (PropertyDescriptor pd : propertyDescriptors) {
                String propertyName = pd.getName();
                if (map.containsKey(propertyName) && pd.getWriteMethod() != null) {
                    Object value = map.get(propertyName);
                    if (value != null) {
                        beanWrapper.setPropertyValue(propertyName, value);
                    }
                }
            }
            
            return bean;
        } catch (Exception e) {
            throw new RuntimeException("Map转Bean失败: " + e.getMessage(), e);
        }
    }

    // ========== Bean 属性操作 ==========

    /**
     * 获取Bean属性值
     * 
     * @param bean Bean对象
     * @param propertyName 属性名
     * @return 属性值
     */
    public static Object getProperty(Object bean, String propertyName) {
        if (bean == null || StringUtil.isEmpty(propertyName)) {
            return null;
        }
        
        BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
        return beanWrapper.getPropertyValue(propertyName);
    }

    /**
     * 设置Bean属性值
     * 
     * @param bean Bean对象
     * @param propertyName 属性名
     * @param value 属性值
     */
    public static void setProperty(Object bean, String propertyName, Object value) {
        if (bean == null || StringUtil.isEmpty(propertyName)) {
            return;
        }
        
        BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
        if (beanWrapper.isWritableProperty(propertyName)) {
            beanWrapper.setPropertyValue(propertyName, value);
        }
    }

    /**
     * 检查Bean是否有指定属性
     * 
     * @param bean Bean对象
     * @param propertyName 属性名
     * @return 是否有属性
     */
    public static boolean hasProperty(Object bean, String propertyName) {
        if (bean == null || StringUtil.isEmpty(propertyName)) {
            return false;
        }
        
        BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
        return beanWrapper.isReadableProperty(propertyName);
    }

    /**
     * 检查Bean属性是否可写
     * 
     * @param bean Bean对象
     * @param propertyName 属性名
     * @return 是否可写
     */
    public static boolean isWritableProperty(Object bean, String propertyName) {
        if (bean == null || StringUtil.isEmpty(propertyName)) {
            return false;
        }
        
        BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
        return beanWrapper.isWritableProperty(propertyName);
    }

    /**
     * 获取Bean的所有属性名
     * 
     * @param beanClass Bean类型
     * @return 属性名数组
     */
    public static String[] getPropertyNames(Class<?> beanClass) {
        PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(beanClass);
        return Arrays.stream(propertyDescriptors)
            .map(PropertyDescriptor::getName)
            .filter(name -> !"class".equals(name))
            .toArray(String[]::new);
    }

    /**
     * 获取Bean的属性类型
     * 
     * @param beanClass Bean类型
     * @param propertyName 属性名
     * @return 属性类型
     */
    public static Class<?> getPropertyType(Class<?> beanClass, String propertyName) {
        PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(beanClass);
        for (PropertyDescriptor pd : propertyDescriptors) {
            if (pd.getName().equals(propertyName)) {
                return pd.getPropertyType();
            }
        }
        return null;
    }

    // ========== Bean 比较 ==========

    /**
     * 比较两个Bean的属性差异
     * 
     * @param source 源对象
     * @param target 目标对象
     * @return 差异映射
     */
    public static Map<String, Object[]> compareBean(Object source, Object target) {
        if (source == null || target == null) {
            return new HashMap<>();
        }
        
        if (!source.getClass().equals(target.getClass())) {
            throw new IllegalArgumentException("对象类型不匹配");
        }
        
        Map<String, Object[]> differences = new HashMap<>();
        BeanWrapper sourceWrapper = new BeanWrapperImpl(source);
        BeanWrapper targetWrapper = new BeanWrapperImpl(target);
        
        PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(source.getClass());
        for (PropertyDescriptor pd : propertyDescriptors) {
            String propertyName = pd.getName();
            if (!"class".equals(propertyName)) {
                Object sourceValue = sourceWrapper.getPropertyValue(propertyName);
                Object targetValue = targetWrapper.getPropertyValue(propertyName);
                
                if (!Objects.equals(sourceValue, targetValue)) {
                    differences.put(propertyName, new Object[]{sourceValue, targetValue});
                }
            }
        }
        
        return differences;
    }

    /**
     * 检查两个Bean是否相等
     * 
     * @param source 源对象
     * @param target 目标对象
     * @return 是否相等
     */
    public static boolean isEqual(Object source, Object target) {
        if (source == target) {
            return true;
        }
        
        if (source == null || target == null) {
            return false;
        }
        
        if (!source.getClass().equals(target.getClass())) {
            return false;
        }
        
        Map<String, Object[]> differences = compareBean(source, target);
        return differences.isEmpty();
    }

    // ========== Bean 验证 ==========

    /**
     * 检查Bean是否为空（所有属性都为null）
     * 
     * @param bean Bean对象
     * @return 是否为空
     */
    public static boolean isEmpty(Object bean) {
        if (bean == null) {
            return true;
        }
        
        BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
        PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(bean.getClass());
        
        for (PropertyDescriptor pd : propertyDescriptors) {
            String propertyName = pd.getName();
            if (!"class".equals(propertyName)) {
                Object value = beanWrapper.getPropertyValue(propertyName);
                if (value != null) {
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * 检查Bean是否不为空（至少有一个属性不为null）
     * 
     * @param bean Bean对象
     * @return 是否不为空
     */
    public static boolean isNotEmpty(Object bean) {
        return !isEmpty(bean);
    }

    // ========== Bean 克隆 ==========

    /**
     * 深度克隆Bean
     * 
     * @param source 源对象
     * @param <T> 对象类型泛型
     * @return 克隆对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepClone(T source) {
        if (source == null) {
            return null;
        }
        
        try {
            // 使用JSON序列化进行深度克隆
            String json = JsonUtil.toJson(source);
            return (T) JsonUtil.fromJson(json, source.getClass());
        } catch (Exception e) {
            throw new RuntimeException("Bean深度克隆失败: " + e.getMessage(), e);
        }
    }

    // ========== Bean 转换器 ==========

    /**
     * 创建Bean转换器
     * 
     * @param sourceClass 源类型
     * @param targetClass 目标类型
     * @param <S> 源类型泛型
     * @param <T> 目标类型泛型
     * @return 转换器函数
     */
    public static <S, T> Function<S, T> createConverter(Class<S> sourceClass, Class<T> targetClass) {
        return source -> copyBean(source, targetClass);
    }

    /**
     * 创建Bean转换器（忽略null值）
     * 
     * @param sourceClass 源类型
     * @param targetClass 目标类型
     * @param <S> 源类型泛型
     * @param <T> 目标类型泛型
     * @return 转换器函数
     */
    public static <S, T> Function<S, T> createConverterIgnoreNull(Class<S> sourceClass, Class<T> targetClass) {
        return source -> copyBeanIgnoreNull(source, targetClass);
    }

    // ========== 辅助方法 ==========

    /**
     * 获取null属性名数组
     * 
     * @param source 源对象
     * @return null属性名数组
     */
    private static String[] getNullPropertyNames(Object source) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(source.getClass());
        
        List<String> nullPropertyNames = new ArrayList<>();
        for (PropertyDescriptor pd : propertyDescriptors) {
            String propertyName = pd.getName();
            if (beanWrapper.getPropertyValue(propertyName) == null) {
                nullPropertyNames.add(propertyName);
            }
        }
        
        return nullPropertyNames.toArray(new String[0]);
    }

    /**
     * 获取属性描述符（带缓存）
     * 
     * @param beanClass Bean类型
     * @return 属性描述符数组
     */
    private static PropertyDescriptor[] getPropertyDescriptors(Class<?> beanClass) {
        return PROPERTY_CACHE.computeIfAbsent(beanClass, clazz -> {
            BeanWrapper beanWrapper = new BeanWrapperImpl(clazz);
            return beanWrapper.getPropertyDescriptors();
        });
    }

    /**
     * 获取字段数组（带缓存）
     * 
     * @param beanClass Bean类型
     * @return 字段数组
     */
    private static Field[] getFields(Class<?> beanClass) {
        return FIELD_CACHE.computeIfAbsent(beanClass, Class::getDeclaredFields);
    }

    /**
     * 清除缓存
     */
    public static void clearCache() {
        PROPERTY_CACHE.clear();
        FIELD_CACHE.clear();
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    public static Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("propertyCache", PROPERTY_CACHE.size());
        stats.put("fieldCache", FIELD_CACHE.size());
        return stats;
    }
}