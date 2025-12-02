package com.tricoq.trigger.http.admin;

import com.tricoq.api.IAdminUserAdminService;
import com.tricoq.api.dto.AdminUserLoginRequestDTO;
import com.tricoq.api.dto.AdminUserQueryRequestDTO;
import com.tricoq.api.dto.AdminUserRequestDTO;
import com.tricoq.api.dto.AdminUserResponseDTO;
import com.tricoq.api.response.Response;
import com.tricoq.application.service.AdminUserAdminService;
import com.tricoq.types.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员用户管理控制器
 *
 * @author bugstack虫洞栈
 * @description 管理员用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/admin-user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AdminUserAdminController implements IAdminUserAdminService {

    private final AdminUserAdminService adminUserAdminService;

    @Override
    @PostMapping("/create")
    public Response<Boolean> createAdminUser(@RequestBody AdminUserRequestDTO request) {
        try {
            boolean success = adminUserAdminService.createAdminUser(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("创建管理员用户失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @PutMapping("/update-by-id")
    public Response<Boolean> updateAdminUserById(@RequestBody AdminUserRequestDTO request) {
        try {
            boolean success = adminUserAdminService.updateAdminUserById(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("根据ID更新管理员用户失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @PutMapping("/update-by-user-id")
    public Response<Boolean> updateAdminUserByUserId(@RequestBody AdminUserRequestDTO request) {
        try {
            boolean success = adminUserAdminService.updateAdminUserByUserId(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("根据用户ID更新管理员用户失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @DeleteMapping("/delete-by-id/{id}")
    public Response<Boolean> deleteAdminUserById(@PathVariable Long id) {
        try {
            boolean success = adminUserAdminService.deleteAdminUserById(id);
            return success(success);
        } catch (Exception e) {
            log.error("根据ID删除管理员用户失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @DeleteMapping("/delete-by-user-id/{userId}")
    public Response<Boolean> deleteAdminUserByUserId(@PathVariable String userId) {
        try {
            boolean success = adminUserAdminService.deleteAdminUserByUserId(userId);
            return success(success);
        } catch (Exception e) {
            log.error("根据用户ID删除管理员用户失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @GetMapping("/query-by-id/{id}")
    public Response<AdminUserResponseDTO> queryAdminUserById(@PathVariable Long id) {
        try {
            AdminUserResponseDTO data = adminUserAdminService.queryAdminUserById(id);
            return success(data);
        } catch (Exception e) {
            log.error("根据ID查询管理员用户失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-user-id/{userId}")
    public Response<AdminUserResponseDTO> queryAdminUserByUserId(@PathVariable String userId) {
        try {
            AdminUserResponseDTO data = adminUserAdminService.queryAdminUserByUserId(userId);
            return success(data);
        } catch (Exception e) {
            log.error("根据用户ID查询管理员用户失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-username/{username}")
    public Response<AdminUserResponseDTO> queryAdminUserByUsername(@PathVariable String username) {
        try {
            AdminUserResponseDTO data = adminUserAdminService.queryAdminUserByUsername(username);
            return success(data);
        } catch (Exception e) {
            log.error("根据用户名查询管理员用户失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-enabled")
    public Response<List<AdminUserResponseDTO>> queryEnabledAdminUsers() {
        try {
            return success(adminUserAdminService.queryEnabledAdminUsers());
        } catch (Exception e) {
            log.error("查询启用状态的管理员用户列表失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-status/{status}")
    public Response<List<AdminUserResponseDTO>> queryAdminUsersByStatus(@PathVariable Integer status) {
        try {
            return success(adminUserAdminService.queryAdminUsersByStatus(status));
        } catch (Exception e) {
            log.error("根据状态查询管理员用户列表失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @PostMapping("/query-list")
    public Response<List<AdminUserResponseDTO>> queryAdminUserList(@RequestBody AdminUserQueryRequestDTO request) {
        try {
            return success(adminUserAdminService.queryAdminUserList(request));
        } catch (Exception e) {
            log.error("根据条件查询管理员用户列表失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-all")
    public Response<List<AdminUserResponseDTO>> queryAllAdminUsers() {
        try {
            return success(adminUserAdminService.queryAllAdminUsers());
        } catch (Exception e) {
            log.error("查询所有管理员用户失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @PostMapping("/login")
    public Response<AdminUserResponseDTO> loginAdminUser(@RequestBody AdminUserLoginRequestDTO request) {
        try {
            AdminUserResponseDTO data = adminUserAdminService.loginAdminUser(request);
            return success(data);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), null);
        } catch (Exception e) {
            log.error("管理员用户登录失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @PostMapping("/validate-login")
    public Response<Boolean> validateAdminUserLogin(@RequestBody AdminUserLoginRequestDTO request) {
        try {
            boolean success = adminUserAdminService.validateAdminUserLogin(request);
            if (!success) {
                return fail(ResponseCode.LOGIN_FAILED, ResponseCode.LOGIN_FAILED.getInfo(), false);
            }
            return success(true);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("管理员用户登录校验失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    private <T> Response<T> success(T data) {
        return Response.<T>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }

    private <T> Response<T> fail(ResponseCode code, String info, T data) {
        return Response.<T>builder()
                .code(code.getCode())
                .info(info)
                .data(data)
                .build();
    }

}
