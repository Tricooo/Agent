package com.tricoq.domain.agent.adapter.repository;

import com.tricoq.domain.agent.model.dto.AdminUserDTO;

import java.util.List;

/**
 * 管理员用户仓储接口，屏蔽基础设施访问。
 */
public interface IAdminUserRepository {

    boolean insert(AdminUserDTO adminUser);

    boolean updateById(AdminUserDTO adminUser);

    boolean updateByUserId(AdminUserDTO adminUser);

    boolean deleteById(Long id);

    boolean deleteByUserId(String userId);

    AdminUserDTO queryById(Long id);

    AdminUserDTO queryByUserId(String userId);

    AdminUserDTO queryByUsername(String username);

    AdminUserDTO queryByUsernameAndPassword(String username, String password);

    List<AdminUserDTO> queryEnabledUsers();

    List<AdminUserDTO> queryByStatus(Integer status);

    List<AdminUserDTO> queryAll();
}
