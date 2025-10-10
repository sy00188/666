package com.archive.management.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 邮件工具类
 * 提供邮件发送的常用方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Component
public class EmailUtil {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.mail.default-subject:系统通知}")
    private String defaultSubject;

    @Value("${app.mail.max-attachment-size:10485760}")
    private long maxAttachmentSize; // 10MB

    /**
     * 邮箱格式验证正则
     */
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    
    private static final Pattern EMAIL_REGEX = Pattern.compile(EMAIL_PATTERN);

    /**
     * 支持的附件类型
     */
    private static final String[] ALLOWED_ATTACHMENT_TYPES = {
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", 
        "txt", "jpg", "jpeg", "png", "gif", "zip", "rar"
    };

    // ========== 邮箱验证 ==========

    /**
     * 验证邮箱格式
     * 
     * @param email 邮箱地址
     * @return 是否有效
     */
    public static boolean isValidEmail(String email) {
        return StringUtil.isNotEmpty(email) && EMAIL_REGEX.matcher(email).matches();
    }

    /**
     * 验证多个邮箱格式
     * 
     * @param emails 邮箱地址列表
     * @return 是否全部有效
     */
    public static boolean isValidEmails(String... emails) {
        if (emails == null || emails.length == 0) {
            return false;
        }
        
        for (String email : emails) {
            if (!isValidEmail(email)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证邮箱列表
     * 
     * @param emails 邮箱地址列表
     * @return 是否全部有效
     */
    public static boolean isValidEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return false;
        }
        
        return emails.stream().allMatch(EmailUtil::isValidEmail);
    }

    // ========== 简单邮件发送 ==========

    /**
     * 发送简单文本邮件
     * 
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @return 是否发送成功
     */
    public boolean sendSimpleEmail(String to, String subject, String content) {
        if (!isValidEmail(to)) {
            throw new IllegalArgumentException("收件人邮箱格式不正确");
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(StringUtil.isNotEmpty(subject) ? subject : defaultSubject);
            message.setText(content);
            message.setSentDate(new java.util.Date());
            
            mailSender.send(message);
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送简单文本邮件（多个收件人）
     * 
     * @param to 收件人列表
     * @param subject 主题
     * @param content 内容
     * @return 是否发送成功
     */
    public boolean sendSimpleEmail(String[] to, String subject, String content) {
        if (!isValidEmails(to)) {
            throw new IllegalArgumentException("收件人邮箱格式不正确");
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(StringUtil.isNotEmpty(subject) ? subject : defaultSubject);
            message.setText(content);
            message.setSentDate(new java.util.Date());
            
            mailSender.send(message);
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送简单邮件（带抄送）
     * 
     * @param to 收件人
     * @param cc 抄送人
     * @param subject 主题
     * @param content 内容
     * @return 是否发送成功
     */
    public boolean sendSimpleEmailWithCc(String to, String[] cc, String subject, String content) {
        if (!isValidEmail(to)) {
            throw new IllegalArgumentException("收件人邮箱格式不正确");
        }
        
        if (cc != null && !isValidEmails(cc)) {
            throw new IllegalArgumentException("抄送人邮箱格式不正确");
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            if (cc != null && cc.length > 0) {
                message.setCc(cc);
            }
            message.setSubject(StringUtil.isNotEmpty(subject) ? subject : defaultSubject);
            message.setText(content);
            message.setSentDate(new java.util.Date());
            
            mailSender.send(message);
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== HTML邮件发送 ==========

    /**
     * 发送HTML邮件
     * 
     * @param to 收件人
     * @param subject 主题
     * @param htmlContent HTML内容
     * @return 是否发送成功
     */
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!isValidEmail(to)) {
            throw new IllegalArgumentException("收件人邮箱格式不正确");
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(StringUtil.isNotEmpty(subject) ? subject : defaultSubject);
            helper.setText(htmlContent, true);
            helper.setSentDate(new java.util.Date());
            
            mailSender.send(message);
            return true;
            
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送HTML邮件（多个收件人）
     * 
     * @param to 收件人列表
     * @param subject 主题
     * @param htmlContent HTML内容
     * @return 是否发送成功
     */
    public boolean sendHtmlEmail(String[] to, String subject, String htmlContent) {
        if (!isValidEmails(to)) {
            throw new IllegalArgumentException("收件人邮箱格式不正确");
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(StringUtil.isNotEmpty(subject) ? subject : defaultSubject);
            helper.setText(htmlContent, true);
            helper.setSentDate(new java.util.Date());
            
            mailSender.send(message);
            return true;
            
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送HTML邮件（带抄送和密送）
     * 
     * @param to 收件人
     * @param cc 抄送人
     * @param bcc 密送人
     * @param subject 主题
     * @param htmlContent HTML内容
     * @return 是否发送成功
     */
    public boolean sendHtmlEmailWithCcBcc(String to, String[] cc, String[] bcc, 
                                         String subject, String htmlContent) {
        if (!isValidEmail(to)) {
            throw new IllegalArgumentException("收件人邮箱格式不正确");
        }
        
        if (cc != null && !isValidEmails(cc)) {
            throw new IllegalArgumentException("抄送人邮箱格式不正确");
        }
        
        if (bcc != null && !isValidEmails(bcc)) {
            throw new IllegalArgumentException("密送人邮箱格式不正确");
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            if (cc != null && cc.length > 0) {
                helper.setCc(cc);
            }
            if (bcc != null && bcc.length > 0) {
                helper.setBcc(bcc);
            }
            helper.setSubject(StringUtil.isNotEmpty(subject) ? subject : defaultSubject);
            helper.setText(htmlContent, true);
            helper.setSentDate(new java.util.Date());
            
            mailSender.send(message);
            return true;
            
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== 带附件邮件发送 ==========

    /**
     * 发送带附件的邮件
     * 
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @param attachmentPath 附件路径
     * @return 是否发送成功
     */
    public boolean sendEmailWithAttachment(String to, String subject, String content, String attachmentPath) {
        if (!isValidEmail(to)) {
            throw new IllegalArgumentException("收件人邮箱格式不正确");
        }
        
        if (StringUtil.isEmpty(attachmentPath)) {
            throw new IllegalArgumentException("附件路径不能为空");
        }
        
        File file = new File(attachmentPath);
        if (!file.exists()) {
            throw new IllegalArgumentException("附件文件不存在");
        }
        
        if (!isAllowedAttachmentType(file.getName())) {
            throw new IllegalArgumentException("不支持的附件类型");
        }
        
        if (file.length() > maxAttachmentSize) {
            throw new IllegalArgumentException("附件大小超过限制");
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(StringUtil.isNotEmpty(subject) ? subject : defaultSubject);
            helper.setText(content);
            helper.setSentDate(new java.util.Date());
            
            // 添加附件
            FileSystemResource fileResource = new FileSystemResource(file);
            helper.addAttachment(file.getName(), fileResource);
            
            mailSender.send(message);
            return true;
            
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送带多个附件的邮件
     * 
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @param attachmentPaths 附件路径列表
     * @return 是否发送成功
     */
    public boolean sendEmailWithAttachments(String to, String subject, String content, String[] attachmentPaths) {
        if (!isValidEmail(to)) {
            throw new IllegalArgumentException("收件人邮箱格式不正确");
        }
        
        if (attachmentPaths == null || attachmentPaths.length == 0) {
            throw new IllegalArgumentException("附件路径不能为空");
        }
        
        // 验证所有附件
        for (String path : attachmentPaths) {
            if (StringUtil.isEmpty(path)) {
                throw new IllegalArgumentException("附件路径不能为空");
            }
            
            File file = new File(path);
            if (!file.exists()) {
                throw new IllegalArgumentException("附件文件不存在: " + path);
            }
            
            if (!isAllowedAttachmentType(file.getName())) {
                throw new IllegalArgumentException("不支持的附件类型: " + file.getName());
            }
            
            if (file.length() > maxAttachmentSize) {
                throw new IllegalArgumentException("附件大小超过限制: " + file.getName());
            }
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(StringUtil.isNotEmpty(subject) ? subject : defaultSubject);
            helper.setText(content);
            helper.setSentDate(new java.util.Date());
            
            // 添加附件
            for (String path : attachmentPaths) {
                File file = new File(path);
                FileSystemResource fileResource = new FileSystemResource(file);
                helper.addAttachment(file.getName(), fileResource);
            }
            
            mailSender.send(message);
            return true;
            
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送带上传文件附件的邮件
     * 
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @param attachments 上传文件列表
     * @return 是否发送成功
     */
    public boolean sendEmailWithUploadedFiles(String to, String subject, String content, 
                                             List<MultipartFile> attachments) {
        if (!isValidEmail(to)) {
            throw new IllegalArgumentException("收件人邮箱格式不正确");
        }
        
        if (attachments == null || attachments.isEmpty()) {
            throw new IllegalArgumentException("附件不能为空");
        }
        
        // 验证所有附件
        for (MultipartFile file : attachments) {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("附件文件为空");
            }
            
            if (!isAllowedAttachmentType(file.getOriginalFilename())) {
                throw new IllegalArgumentException("不支持的附件类型: " + file.getOriginalFilename());
            }
            
            if (file.getSize() > maxAttachmentSize) {
                throw new IllegalArgumentException("附件大小超过限制: " + file.getOriginalFilename());
            }
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(StringUtil.isNotEmpty(subject) ? subject : defaultSubject);
            helper.setText(content);
            helper.setSentDate(new java.util.Date());
            
            // 添加附件
            for (MultipartFile file : attachments) {
                helper.addAttachment(file.getOriginalFilename(), file);
            }
            
            mailSender.send(message);
            return true;
            
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== 模板邮件发送 ==========

    /**
     * 发送模板邮件
     * 
     * @param to 收件人
     * @param subject 主题
     * @param templateContent 模板内容
     * @param variables 变量映射
     * @return 是否发送成功
     */
    public boolean sendTemplateEmail(String to, String subject, String templateContent, 
                                   Map<String, Object> variables) {
        if (!isValidEmail(to)) {
            throw new IllegalArgumentException("收件人邮箱格式不正确");
        }
        
        if (StringUtil.isEmpty(templateContent)) {
            throw new IllegalArgumentException("模板内容不能为空");
        }
        
        // 替换模板变量
        String processedContent = processTemplate(templateContent, variables);
        
        return sendHtmlEmail(to, subject, processedContent);
    }

    /**
     * 处理模板变量
     * 
     * @param template 模板内容
     * @param variables 变量映射
     * @return 处理后的内容
     */
    private String processTemplate(String template, Map<String, Object> variables) {
        if (StringUtil.isEmpty(template)) {
            return template;
        }
        
        String result = template;
        
        if (variables != null && !variables.isEmpty()) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, value);
            }
        }
        
        return result;
    }

    // ========== 常用邮件模板 ==========

    /**
     * 发送验证码邮件
     * 
     * @param to 收件人
     * @param code 验证码
     * @param expireMinutes 过期时间（分钟）
     * @return 是否发送成功
     */
    public boolean sendVerificationCode(String to, String code, int expireMinutes) {
        String subject = "验证码通知";
        String content = buildVerificationCodeTemplate(code, expireMinutes);
        
        return sendHtmlEmail(to, subject, content);
    }

    /**
     * 发送密码重置邮件
     * 
     * @param to 收件人
     * @param username 用户名
     * @param resetUrl 重置链接
     * @return 是否发送成功
     */
    public boolean sendPasswordReset(String to, String username, String resetUrl) {
        String subject = "密码重置通知";
        String content = buildPasswordResetTemplate(username, resetUrl);
        
        return sendHtmlEmail(to, subject, content);
    }

    /**
     * 发送系统通知邮件
     * 
     * @param to 收件人
     * @param title 通知标题
     * @param message 通知内容
     * @return 是否发送成功
     */
    public boolean sendSystemNotification(String to, String title, String message) {
        String subject = "系统通知 - " + title;
        String content = buildSystemNotificationTemplate(title, message);
        
        return sendHtmlEmail(to, subject, content);
    }

    // ========== 邮件模板构建 ==========

    /**
     * 构建验证码邮件模板
     * 
     * @param code 验证码
     * @param expireMinutes 过期时间
     * @return HTML内容
     */
    private String buildVerificationCodeTemplate(String code, int expireMinutes) {
        return String.format(
            "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
            "<h2 style='color: #333; text-align: center;'>验证码通知</h2>" +
            "<div style='background: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 0;'>" +
            "<p>您的验证码是：</p>" +
            "<div style='font-size: 24px; font-weight: bold; color: #007bff; text-align: center; " +
            "background: white; padding: 15px; border-radius: 5px; letter-spacing: 5px;'>%s</div>" +
            "<p style='color: #666; margin-top: 15px;'>验证码有效期为 %d 分钟，请及时使用。</p>" +
            "</div>" +
            "<p style='color: #999; font-size: 12px;'>如果您没有请求此验证码，请忽略此邮件。</p>" +
            "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
            "<p style='color: #999; font-size: 12px; text-align: center;'>此邮件由系统自动发送，请勿回复。</p>" +
            "</div>",
            code, expireMinutes
        );
    }

    /**
     * 构建密码重置邮件模板
     * 
     * @param username 用户名
     * @param resetUrl 重置链接
     * @return HTML内容
     */
    private String buildPasswordResetTemplate(String username, String resetUrl) {
        return String.format(
            "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
            "<h2 style='color: #333; text-align: center;'>密码重置通知</h2>" +
            "<div style='background: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 0;'>" +
            "<p>尊敬的 %s：</p>" +
            "<p>您申请了密码重置，请点击下面的链接重置您的密码：</p>" +
            "<div style='text-align: center; margin: 20px 0;'>" +
            "<a href='%s' style='background: #007bff; color: white; padding: 12px 30px; " +
            "text-decoration: none; border-radius: 5px; display: inline-block;'>重置密码</a>" +
            "</div>" +
            "<p style='color: #666; font-size: 14px;'>如果按钮无法点击，请复制以下链接到浏览器地址栏：</p>" +
            "<p style='word-break: break-all; color: #007bff;'>%s</p>" +
            "<p style='color: #666; margin-top: 15px;'>此链接有效期为24小时，请及时使用。</p>" +
            "</div>" +
            "<p style='color: #999; font-size: 12px;'>如果您没有申请密码重置，请忽略此邮件。</p>" +
            "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
            "<p style='color: #999; font-size: 12px; text-align: center;'>此邮件由系统自动发送，请勿回复。</p>" +
            "</div>",
            username, resetUrl, resetUrl
        );
    }

    /**
     * 构建系统通知邮件模板
     * 
     * @param title 通知标题
     * @param message 通知内容
     * @return HTML内容
     */
    private String buildSystemNotificationTemplate(String title, String message) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        return String.format(
            "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
            "<h2 style='color: #333; text-align: center;'>系统通知</h2>" +
            "<div style='background: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 0;'>" +
            "<h3 style='color: #007bff; margin-top: 0;'>%s</h3>" +
            "<div style='background: white; padding: 15px; border-radius: 5px; line-height: 1.6;'>%s</div>" +
            "<p style='color: #666; margin-top: 15px; font-size: 14px;'>通知时间：%s</p>" +
            "</div>" +
            "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
            "<p style='color: #999; font-size: 12px; text-align: center;'>此邮件由系统自动发送，请勿回复。</p>" +
            "</div>",
            title, message, currentTime
        );
    }

    // ========== 辅助方法 ==========

    /**
     * 检查是否是允许的附件类型
     * 
     * @param fileName 文件名
     * @return 是否允许
     */
    private boolean isAllowedAttachmentType(String fileName) {
        if (StringUtil.isEmpty(fileName)) {
            return false;
        }
        
        String extension = FileUtil.getFileExtension(fileName).toLowerCase();
        
        for (String allowedType : ALLOWED_ATTACHMENT_TYPES) {
            if (allowedType.equals(extension)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 获取邮件发送状态信息
     * 
     * @return 状态信息
     */
    public String getMailSenderInfo() {
        return String.format(
            "邮件服务配置信息：\n" +
            "发送邮箱：%s\n" +
            "默认主题：%s\n" +
            "最大附件大小：%s\n" +
            "支持的附件类型：%s",
            fromEmail,
            defaultSubject,
            FileUtil.formatFileSize(maxAttachmentSize),
            String.join(", ", ALLOWED_ATTACHMENT_TYPES)
        );
    }
}