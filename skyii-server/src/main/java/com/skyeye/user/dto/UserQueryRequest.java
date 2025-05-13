package com.skyeye.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户查询请求
 */
@Data
@ApiModel(description = "用户查询请求")
public class UserQueryRequest {

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("状态(1:活跃,0:非活跃,2:锁定)")
    private Integer status;

    @ApiModelProperty("创建开始日期")
    private LocalDate startDate;

    @ApiModelProperty("创建结束日期")
    private LocalDate endDate;

    @ApiModelProperty("页码(从0开始)")
    private Integer page;

    @ApiModelProperty("每页大小")
    private Integer size;

    @ApiModelProperty("排序字段")
    private String sortBy;

    @ApiModelProperty("排序方向(asc,desc)")
    private String sortOrder;
} 