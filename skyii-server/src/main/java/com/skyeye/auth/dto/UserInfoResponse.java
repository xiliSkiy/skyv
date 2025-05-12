package com.skyeye.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户信息响应对象")
public class UserInfoResponse {

    @ApiModelProperty("用户ID")
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("头像")
    private String avatar;

    @ApiModelProperty("角色列表")
    private List<String> roles;

    @ApiModelProperty("状态(1:正常,0:禁用)")
    private Integer status;

    @ApiModelProperty("最后登录时间")
    private LocalDateTime lastLoginTime;
} 