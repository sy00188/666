package com.archive.management.config;

import com.archive.management.security.CustomUserDetailsService;
import com.archive.management.security.JwtAccessDeniedHandler;
import com.archive.management.security.JwtAuthenticationEntryPoint;
import com.archive.management.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security安全配置
 * 配置JWT认证、权限拦截器、密码加密等
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    prePostEnabled = true,  // 启用@PreAuthorize和@PostAuthorize注解
    securedEnabled = true,  // 启用@Secured注解
    jsr250Enabled = true    // 启用@RolesAllowed注解
)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // 使用强度为12的BCrypt
    }

    /**
     * 认证提供者
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return authentication -> authenticationProvider.authenticate(authentication);
    }

    /**
     * 配置HTTP安全
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（使用JWT时不需要）
            .csrf().disable()
            
            // 配置CORS
            .cors().configurationSource(corsConfigurationSource())
            
            .and()
            
            // 配置会话管理为无状态
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            
            .and()
            
            // 配置异常处理
            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)
            
            .and()
            
            // 配置请求授权
            .authorizeHttpRequests(authz -> authz
                // 公开接口 - 无需认证
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/archive/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/category/public/**").permitAll()
                
                // Swagger文档接口
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                
                // 健康检查接口
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                
                // 静态资源
                .requestMatchers("/", "/favicon.ico", "/**/*.png", "/**/*.gif", "/**/*.svg", 
                            "/**/*.jpg", "/**/*.html", "/**/*.css", "/**/*.js").permitAll()
                
                // 系统管理接口 - 需要管理员权限
                .requestMatchers("/api/system/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 用户管理接口 - 需要用户管理权限
                .requestMatchers(HttpMethod.POST, "/api/user/**").hasAuthority("USER_CREATE")
                .requestMatchers(HttpMethod.PUT, "/api/user/**").hasAuthority("USER_UPDATE")
                .requestMatchers(HttpMethod.DELETE, "/api/user/**").hasAuthority("USER_DELETE")
                .requestMatchers(HttpMethod.GET, "/api/user/**").hasAuthority("USER_VIEW")
                
                // 档案管理接口 - 需要档案管理权限
                .requestMatchers(HttpMethod.POST, "/api/archive/**").hasAuthority("ARCHIVE_CREATE")
                .requestMatchers(HttpMethod.PUT, "/api/archive/**").hasAuthority("ARCHIVE_UPDATE")
                .requestMatchers(HttpMethod.DELETE, "/api/archive/**").hasAuthority("ARCHIVE_DELETE")
                .requestMatchers(HttpMethod.GET, "/api/archive/**").hasAuthority("ARCHIVE_VIEW")
                
                // 借阅管理接口 - 需要借阅管理权限
                .requestMatchers(HttpMethod.POST, "/api/borrow/**").hasAuthority("BORROW_CREATE")
                .requestMatchers(HttpMethod.PUT, "/api/borrow/**").hasAuthority("BORROW_UPDATE")
                .requestMatchers(HttpMethod.GET, "/api/borrow/**").hasAuthority("BORROW_VIEW")
                
                // 分类管理接口 - 需要分类管理权限
                .requestMatchers(HttpMethod.POST, "/api/category/**").hasAuthority("CATEGORY_CREATE")
                .requestMatchers(HttpMethod.PUT, "/api/category/**").hasAuthority("CATEGORY_UPDATE")
                .requestMatchers(HttpMethod.DELETE, "/api/category/**").hasAuthority("CATEGORY_DELETE")
                .requestMatchers(HttpMethod.GET, "/api/category/**").hasAuthority("CATEGORY_VIEW")
                
                // 部门管理接口 - 需要部门管理权限
                .requestMatchers(HttpMethod.POST, "/api/department/**").hasAuthority("DEPARTMENT_CREATE")
                .requestMatchers(HttpMethod.PUT, "/api/department/**").hasAuthority("DEPARTMENT_UPDATE")
                .requestMatchers(HttpMethod.DELETE, "/api/department/**").hasAuthority("DEPARTMENT_DELETE")
                .requestMatchers(HttpMethod.GET, "/api/department/**").hasAuthority("DEPARTMENT_VIEW")
                
                // 角色管理接口 - 需要角色管理权限
                .requestMatchers("/api/role/**").hasAuthority("ROLE_MANAGE")
                
                // 权限管理接口 - 需要权限管理权限
                .requestMatchers("/api/permission/**").hasAuthority("PERMISSION_MANAGE")
                
                // 个人信息接口 - 已认证用户可访问
                .requestMatchers("/api/profile/**").authenticated()
                
                // 其他所有接口都需要认证
                .anyRequest().authenticated()
            );

        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        // 禁用缓存
        http.headers(headers -> headers.cacheControl(cache -> cache.disable()));
        
        return http.build();
    }

    /**
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许发送Cookie
        configuration.setAllowCredentials(true);
        
        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "X-Token-Expiring", 
            "X-Token-Remaining",
            "X-Total-Count",
            "X-Page-Number",
            "X-Page-Size"
        ));
        
        // 预检请求的缓存时间
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}