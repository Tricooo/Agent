package com.tricoq.infrastructure.adapter.repository;

import com.tricoq.domain.agent.adapter.repository.IAdminUserRepository;
import com.tricoq.domain.agent.model.dto.AdminUserDTO;
import com.tricoq.infrastructure.dao.IAdminUserDao;
import com.tricoq.infrastructure.dao.po.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 管理员用户仓储实现
 */
@Repository
@RequiredArgsConstructor
public class AdminUserRepositoryImpl implements IAdminUserRepository {

    private final IAdminUserDao adminUserDao;

    @Override
    public boolean insert(AdminUserDTO adminUser) {
        return adminUser != null && adminUserDao.insert(toPo(adminUser)) > 0;
    }

    @Override
    public boolean updateById(AdminUserDTO adminUser) {
        return adminUser != null && adminUserDao.updateById(toPo(adminUser)) > 0;
    }

    @Override
    public boolean updateByUserId(AdminUserDTO adminUser) {
        return adminUser != null && adminUserDao.updateByUserId(toPo(adminUser)) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return id != null && adminUserDao.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByUserId(String userId) {
        return userId != null && adminUserDao.deleteByUserId(userId) > 0;
    }

    @Override
    public AdminUserDTO queryById(Long id) {
        if (id == null) {
            return null;
        }
        return toDto(adminUserDao.queryById(id));
    }

    @Override
    public AdminUserDTO queryByUserId(String userId) {
        if (userId == null) {
            return null;
        }
        return toDto(adminUserDao.queryByUserId(userId));
    }

    @Override
    public AdminUserDTO queryByUsername(String username) {
        if (username == null) {
            return null;
        }
        return toDto(adminUserDao.queryByUsername(username));
    }

    @Override
    public AdminUserDTO queryByUsernameAndPassword(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        return toDto(adminUserDao.queryByUsernameAndPassword(username, password));
    }

    @Override
    public List<AdminUserDTO> queryEnabledUsers() {
        return adminUserDao.queryEnabledUsers()
                .stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminUserDTO> queryByStatus(Integer status) {
        if (status == null) {
            return List.of();
        }
        return adminUserDao.queryByStatus(status)
                .stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminUserDTO> queryAll() {
        return adminUserDao.queryAll()
                .stream()
                .map(this::toDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private AdminUserDTO toDto(AdminUser adminUser) {
        if (adminUser == null) {
            return null;
        }
        AdminUserDTO dto = new AdminUserDTO();
        BeanUtils.copyProperties(adminUser, dto);
        return dto;
    }

    private AdminUser toPo(AdminUserDTO adminUser) {
        if (adminUser == null) {
            return null;
        }
        AdminUser po = new AdminUser();
        BeanUtils.copyProperties(adminUser, po);
        return po;
    }
}
