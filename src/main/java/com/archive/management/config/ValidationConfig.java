package com.archive.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.boot.validation.MessageInterpolatorFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.MessageInterpolator;
import java.util.Locale;

/**
 * 参数验证配置类
 * 配置Bean Validation和方法级验证
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
public class ValidationConfig {

    /**
     * 配置本地验证器工厂Bean
     * 
     * @return LocalValidatorFactoryBean
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        
        // 设置验证消息源
        validatorFactoryBean.setValidationMessageSource(validationMessageSource());
        
        return validatorFactoryBean;
    }

    /**
     * 配置方法验证后处理器
     * 启用方法级别的参数验证
     * 
     * @return MethodValidationPostProcessor
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator());
        return processor;
    }

    /**
     * 配置验证消息源
     * 用于国际化验证错误消息
     * 
     * @return MessageSource
     */
    @Bean
    public MessageSource validationMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = 
            new ReloadableResourceBundleMessageSource();
        
        // 设置消息文件基础名称
        messageSource.setBasenames(
            "classpath:messages/validation",
            "classpath:org/hibernate/validator/ValidationMessages"
        );
        
        // 设置默认编码
        messageSource.setDefaultEncoding("UTF-8");
        
        // 设置缓存时间（秒）
        messageSource.setCacheSeconds(300);
        
        // 设置默认语言环境
        messageSource.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        
        // 设置回退到系统语言环境
        messageSource.setFallbackToSystemLocale(true);
        
        return messageSource;
    }

    /**
     * 配置自定义消息插值器
     * 
     * @return MessageInterpolator
     */
    @Bean
    public MessageInterpolator messageInterpolator() {
        return new MessageInterpolatorFactory().getObject();
    }

    /**
     * 获取验证器实例
     * 
     * @return Validator
     */
    @Bean
    public Validator validatorInstance() {
        return validator().getValidator();
    }

    /**
     * 获取验证器工厂
     * 
     * @return ValidatorFactory
     */
    @Bean
    public ValidatorFactory validatorFactory() {
        return validator();
    }
}