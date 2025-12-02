package com.tricoq.trigger.http.admin;

import com.tricoq.api.IAiClientAdvisorAdminService;
import com.tricoq.api.dto.AiClientAdvisorQueryRequestDTO;
import com.tricoq.api.dto.AiClientAdvisorRequestDTO;
import com.tricoq.api.dto.AiClientAdvisorResponseDTO;
import com.tricoq.api.response.Response;
import com.tricoq.application.service.AiClientAdvisorAdminService;
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
 * 顾问配置管理控制器
 *
 * @author trico qiang
 * @description 顾问配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/ai-client-advisor")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class AiClientAdvisorAdminController implements IAiClientAdvisorAdminService {

    private final AiClientAdvisorAdminService aiClientAdvisorAdminService;

    @Override
    @PostMapping("/create")
    public Response<Boolean> createAiClientAdvisor(@RequestBody AiClientAdvisorRequestDTO request) {
        try {
            boolean success = aiClientAdvisorAdminService.createAiClientAdvisor(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("创建顾问配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @PutMapping("/update-by-id")
    public Response<Boolean> updateAiClientAdvisorById(@RequestBody AiClientAdvisorRequestDTO request) {
        try {
            boolean success = aiClientAdvisorAdminService.updateAiClientAdvisorById(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("根据ID更新顾问配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @PutMapping("/update-by-advisor-id")
    public Response<Boolean> updateAiClientAdvisorByAdvisorId(@RequestBody AiClientAdvisorRequestDTO request) {
        try {
            boolean success = aiClientAdvisorAdminService.updateAiClientAdvisorByAdvisorId(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("根据顾问ID更新顾问配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @DeleteMapping("/delete-by-id/{id}")
    public Response<Boolean> deleteAiClientAdvisorById(@PathVariable Long id) {
        try {
            boolean success = aiClientAdvisorAdminService.deleteAiClientAdvisorById(id);
            return success(success);
        } catch (Exception e) {
            log.error("根据ID删除顾问配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @DeleteMapping("/delete-by-advisor-id/{advisorId}")
    public Response<Boolean> deleteAiClientAdvisorByAdvisorId(@PathVariable String advisorId) {
        try {
            boolean success = aiClientAdvisorAdminService.deleteAiClientAdvisorByAdvisorId(advisorId);
            return success(success);
        } catch (Exception e) {
            log.error("根据顾问ID删除顾问配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @GetMapping("/query-by-id/{id}")
    public Response<AiClientAdvisorResponseDTO> queryAiClientAdvisorById(@PathVariable Long id) {
        try {
            AiClientAdvisorResponseDTO data = aiClientAdvisorAdminService.queryAiClientAdvisorById(id);
            return success(data);
        } catch (Exception e) {
            log.error("根据ID查询顾问配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-advisor-id/{advisorId}")
    public Response<AiClientAdvisorResponseDTO> queryAiClientAdvisorByAdvisorId(@PathVariable String advisorId) {
        try {
            AiClientAdvisorResponseDTO data = aiClientAdvisorAdminService.queryAiClientAdvisorByAdvisorId(advisorId);
            return success(data);
        } catch (Exception e) {
            log.error("根据顾问ID查询顾问配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-enabled")
    public Response<List<AiClientAdvisorResponseDTO>> queryEnabledAiClientAdvisors() {
        try {
            return success(aiClientAdvisorAdminService.queryEnabledAiClientAdvisors());
        } catch (Exception e) {
            log.error("查询所有启用的顾问配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-status/{status}")
    public Response<List<AiClientAdvisorResponseDTO>> queryAiClientAdvisorsByStatus(@PathVariable Integer status) {
        try {
            return success(aiClientAdvisorAdminService.queryAiClientAdvisorsByStatus(status));
        } catch (Exception e) {
            log.error("根据状态查询顾问配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-type/{advisorType}")
    public Response<List<AiClientAdvisorResponseDTO>> queryAiClientAdvisorsByType(@PathVariable String advisorType) {
        try {
            return success(aiClientAdvisorAdminService.queryAiClientAdvisorsByType(advisorType));
        } catch (Exception e) {
            log.error("根据顾问类型查询顾问配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @PostMapping("/query-list")
    public Response<List<AiClientAdvisorResponseDTO>> queryAiClientAdvisorList(@RequestBody AiClientAdvisorQueryRequestDTO request) {
        try {
            return success(aiClientAdvisorAdminService.queryAiClientAdvisorList(request));
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), null);
        } catch (Exception e) {
            log.error("根据条件查询顾问配置列表失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-all")
    public Response<List<AiClientAdvisorResponseDTO>> queryAllAiClientAdvisors() {
        try {
            return success(aiClientAdvisorAdminService.queryAllAiClientAdvisors());
        } catch (Exception e) {
            log.error("查询所有顾问配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
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
