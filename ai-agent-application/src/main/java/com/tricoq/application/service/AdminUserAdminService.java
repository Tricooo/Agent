package com.tricoq.application.service;

import com.tricoq.api.dto.AdminUserLoginRequestDTO;
import com.tricoq.api.dto.AdminUserQueryRequestDTO;
import com.tricoq.api.dto.AdminUserRequestDTO;
import com.tricoq.api.dto.AdminUserResponseDTO;
import com.tricoq.domain.agent.adapter.repository.IAdminUserRepository;
import com.tricoq.domain.agent.model.dto.AdminUserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 管理员用户应用服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserAdminService {

    private final IAdminUserRepository adminUserRepository;

    public boolean createAdminUser(AdminUserRequestDTO request) {
        log.info("创建管理员用户请求：{}", request);
        AdminUserDTO adminUser = toDomain(request);
        adminUser.setCreateTime(LocalDateTime.now());
        adminUser.setUpdateTime(LocalDateTime.now());
        return adminUserRepository.insert(adminUser);
    }

    public boolean updateAdminUserById(AdminUserRequestDTO request) {
        log.info("根据ID更新管理员用户请求：{}", request);
        if (request.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        AdminUserDTO adminUser = toDomain(request);
        adminUser.setUpdateTime(LocalDateTime.now());
        return adminUserRepository.updateById(adminUser);
    }

    public boolean updateAdminUserByUserId(AdminUserRequestDTO request) {
        log.info("根据用户ID更新管理员用户请求：{}", request);
        if (!StringUtils.hasText(request.getUserId())) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        AdminUserDTO adminUser = toDomain(request);
        adminUser.setUpdateTime(LocalDateTime.now());
        return adminUserRepository.updateByUserId(adminUser);
    }

    public boolean deleteAdminUserById(Long id) {
        log.info("根据ID删除管理员用户请求：{}", id);
        return adminUserRepository.deleteById(id);
    }

    public boolean deleteAdminUserByUserId(String userId) {
        log.info("根据用户ID删除管理员用户请求：{}", userId);
        return adminUserRepository.deleteByUserId(userId);
    }

    public AdminUserResponseDTO queryAdminUserById(Long id) {
        log.info("根据ID查询管理员用户请求：{}", id);
        AdminUserDTO adminUser = adminUserRepository.queryById(id);
        return toResponse(adminUser);
    }

    public AdminUserResponseDTO queryAdminUserByUserId(String userId) {
        log.info("根据用户ID查询管理员用户请求：{}", userId);
        AdminUserDTO adminUser = adminUserRepository.queryByUserId(userId);
        return toResponse(adminUser);
    }

    public AdminUserResponseDTO queryAdminUserByUsername(String username) {
        log.info("根据用户名查询管理员用户请求：{}", username);
        AdminUserDTO adminUser = adminUserRepository.queryByUsername(username);
        return toResponse(adminUser);
    }

    public List<AdminUserResponseDTO> queryEnabledAdminUsers() {
        log.info("查询启用状态的管理员用户列表");
        return adminUserRepository.queryEnabledUsers().stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AdminUserResponseDTO> queryAdminUsersByStatus(Integer status) {
        log.info("根据状态查询管理员用户列表请求：{}", status);
        return adminUserRepository.queryByStatus(status).stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AdminUserResponseDTO> queryAdminUserList(AdminUserQueryRequestDTO request) {
        log.info("根据条件查询管理员用户列表请求：{}", request);
        List<AdminUserDTO> users = adminUserRepository.queryAll();
        List<AdminUserDTO> filtered = users.stream()
                .filter(user -> {
                    if (StringUtils.hasText(request.getUserId()) && !request.getUserId().equals(user.getUserId())) {
                        return false;
                    }
                    if (StringUtils.hasText(request.getUsername()) && (user.getUsername() == null || !user.getUsername().contains(request.getUsername()))) {
                        return false;
                    }
                    if (request.getStatus() != null && !request.getStatus().equals(user.getStatus())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        int pageNum = request.getPageNum() != null ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        int startIndex = Math.max(0, (pageNum - 1) * pageSize);
        int endIndex = Math.min(startIndex + pageSize, filtered.size());
        return filtered.subList(startIndex, endIndex).stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<AdminUserResponseDTO> queryAllAdminUsers() {
        log.info("查询所有管理员用户");
        return adminUserRepository.queryAll().stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public AdminUserResponseDTO loginAdminUser(AdminUserLoginRequestDTO request) {
        log.info("管理员用户登录请求：{}", request.getUsername());
        AdminUserDTO adminUser = adminUserRepository.queryByUsernameAndPassword(request.getUsername(), request.getPassword());
        if (adminUser == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (adminUser.getStatus() == 0) {
            throw new IllegalStateException("用户已被禁用");
        }
        if (adminUser.getStatus() == 2) {
            throw new IllegalStateException("用户已被锁定");
        }
        return toResponse(adminUser);
    }

    public boolean validateAdminUserLogin(AdminUserLoginRequestDTO request) {
        log.info("管理员用户登录校验请求：{}", request.getUsername());
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("用户名或密码不能为空");
        }
        AdminUserDTO adminUser = adminUserRepository.queryByUsernameAndPassword(request.getUsername(), request.getPassword());
        if (adminUser == null) {
            return false;
        }
        if (adminUser.getStatus() == 0 || adminUser.getStatus() == 2) {
            return false;
        }
        return true;
    }

    private AdminUserDTO toDomain(AdminUserRequestDTO requestDTO) {
        AdminUserDTO adminUser = new AdminUserDTO();
        BeanUtils.copyProperties(requestDTO, adminUser);
        return adminUser;
    }

    private AdminUserResponseDTO toResponse(AdminUserDTO adminUser) {
        if (adminUser == null) {
            return null;
        }
        AdminUserResponseDTO responseDTO = new AdminUserResponseDTO();
        BeanUtils.copyProperties(adminUser, responseDTO);
        return responseDTO;
    }
}
