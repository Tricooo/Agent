package com.tricoq.domain.agent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员用户领域对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserDTO {

    private Long id;

    private String userId;

    private String username;

    private String password;

    /**
     * 状态(0:禁用,1:启用,2:锁定)
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
