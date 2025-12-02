package com.tricoq.trigger.http.admin;

import com.tricoq.api.IAiClientAdminService;
import com.tricoq.api.dto.AiClientQueryRequestDTO;
import com.tricoq.api.dto.AiClientRequestDTO;
import com.tricoq.api.dto.AiClientResponseDTO;
import com.tricoq.api.response.Response;
import com.tricoq.application.service.AiClientAdminService;
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
 * AI客户端管理控制器
 *
 * @author trico qiang
 * @description AI客户端配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/ai-client")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AiClientAdminController implements IAiClientAdminService {

    private final AiClientAdminService aiClientAdminService;

    @Override
    @PostMapping("/create")
    public Response<Boolean> createAiClient(@RequestBody AiClientRequestDTO request) {
        try {
            boolean success = aiClientAdminService.createAiClient(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("创建AI客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @PutMapping("/update-by-id")
    public Response<Boolean> updateAiClientById(@RequestBody AiClientRequestDTO request) {
        try {
            boolean success = aiClientAdminService.updateAiClientById(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("根据ID更新AI客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @PutMapping("/update-by-client-id")
    public Response<Boolean> updateAiClientByClientId(@RequestBody AiClientRequestDTO request) {
        try {
            boolean success = aiClientAdminService.updateAiClientByClientId(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("根据客户端ID更新AI客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @DeleteMapping("/delete-by-id/{id}")
    public Response<Boolean> deleteAiClientById(@PathVariable Long id) {
        try {
            boolean success = aiClientAdminService.deleteAiClientById(id);
            return success(success);
        } catch (Exception e) {
            log.error("根据ID删除AI客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @DeleteMapping("/delete-by-client-id/{clientId}")
    public Response<Boolean> deleteAiClientByClientId(@PathVariable String clientId) {
        try {
            boolean success = aiClientAdminService.deleteAiClientByClientId(clientId);
            return success(success);
        } catch (Exception e) {
            log.error("根据客户端ID删除AI客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @GetMapping("/query-by-id/{id}")
    public Response<AiClientResponseDTO> queryAiClientById(@PathVariable Long id) {
        try {
            AiClientResponseDTO data = aiClientAdminService.queryAiClientById(id);
            if (data == null) {
                return fail(ResponseCode.UN_ERROR, "未找到对应的AI客户端配置", null);
            }
            return success(data);
        } catch (Exception e) {
            log.error("根据ID查询AI客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-client-id/{clientId}")
    public Response<AiClientResponseDTO> queryAiClientByClientId(@PathVariable String clientId) {
        try {
            AiClientResponseDTO data = aiClientAdminService.queryAiClientByClientId(clientId);
            if (data == null) {
                return fail(ResponseCode.UN_ERROR, "未找到对应的AI客户端配置", null);
            }
            return success(data);
        } catch (Exception e) {
            log.error("根据客户端ID查询AI客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-enabled")
    public Response<List<AiClientResponseDTO>> queryEnabledAiClients() {
        try {
            return success(aiClientAdminService.queryEnabledAiClients());
        } catch (Exception e) {
            log.error("查询所有启用的AI客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @PostMapping("/query-list")
    public Response<List<AiClientResponseDTO>> queryAiClientList(@RequestBody AiClientQueryRequestDTO request) {
        try {
            return success(aiClientAdminService.queryAiClientList(request));
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), null);
        } catch (Exception e) {
            log.error("根据条件查询AI客户端配置列表失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-all")
    public Response<List<AiClientResponseDTO>> queryAllAiClients() {
        try {
            return success(aiClientAdminService.queryAllAiClients());
        } catch (Exception e) {
            log.error("查询所有AI客户端配置失败", e);
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
