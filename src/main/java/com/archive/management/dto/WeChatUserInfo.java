package com.archive.management.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 微信用户信息DTO
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Data
public class WeChatUserInfo {
    
    /**
     * 用户的唯一标识
     */
    @JsonProperty("openid")
    private String openid;
    
    /**
     * 用户在开放平台的唯一标识
     */
    @JsonProperty("unionid")
    private String unionid;
    
    /**
     * 用户昵称
     */
    @JsonProperty("nickname")
    private String nickname;
    
    /**
     * 性别（1为男性，2为女性，0为未知）
     */
    @JsonProperty("sex")
    private Integer sex;
    
    /**
     * 省份
     */
    @JsonProperty("province")
    private String province;
    
    /**
     * 城市
     */
    @JsonProperty("city")
    private String city;
    
    /**
     * 国家
     */
    @JsonProperty("country")
    private String country;
    
    /**
     * 用户头像URL
     */
    @JsonProperty("headimgurl")
    private String headimgurl;
    
    /**
     * 用户特权信息
     */
    @JsonProperty("privilege")
    private String[] privilege;
}

