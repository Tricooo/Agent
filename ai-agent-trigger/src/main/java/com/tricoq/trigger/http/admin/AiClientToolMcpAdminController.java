package com.tricoq.trigger.http.admin;

import com.tricoq.api.IAiClientToolMcpAdminService;
import com.tricoq.api.dto.AiClientToolMcpQueryRequestDTO;
import com.tricoq.api.dto.AiClientToolMcpRequestDTO;
import com.tricoq.api.dto.AiClientToolMcpResponseDTO;
import com.tricoq.api.response.Response;
import com.tricoq.application.service.AiClientToolMcpAdminService;
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
 * MCP客户端配置管理控制器
 *
 * @author trico qiang
 * @description MCP客户端配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/ai-client-tool-mcp")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class AiClientToolMcpAdminController implements IAiClientToolMcpAdminService {

    private final AiClientToolMcpAdminService aiClientToolMcpAdminService;

    @Override
    @PostMapping("/create")
    public Response<Boolean> createAiClientToolMcp(@RequestBody AiClientToolMcpRequestDTO request) {
        try {
            boolean success = aiClientToolMcpAdminService.createAiClientToolMcp(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("创建MCP客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @PutMapping("/update-by-id")
    public Response<Boolean> updateAiClientToolMcpById(@RequestBody AiClientToolMcpRequestDTO request) {
        try {
            boolean success = aiClientToolMcpAdminService.updateAiClientToolMcpById(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("根据ID更新MCP客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @PutMapping("/update-by-mcp-id")
    public Response<Boolean> updateAiClientToolMcpByMcpId(@RequestBody AiClientToolMcpRequestDTO request) {
        try {
            boolean success = aiClientToolMcpAdminService.updateAiClientToolMcpByMcpId(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("根据MCP ID更新MCP客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @DeleteMapping("/delete-by-id/{id}")
    public Response<Boolean> deleteAiClientToolMcpById(@PathVariable Long id) {
        try {
            boolean success = aiClientToolMcpAdminService.deleteAiClientToolMcpById(id);
            return success(success);
        } catch (Exception e) {
            log.error("根据ID删除MCP客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @DeleteMapping("/delete-by-mcp-id/{mcpId}")
    public Response<Boolean> deleteAiClientToolMcpByMcpId(@PathVariable String mcpId) {
        try {
            boolean success = aiClientToolMcpAdminService.deleteAiClientToolMcpByMcpId(mcpId);
            return success(success);
        } catch (Exception e) {
            log.error("根据MCP ID删除MCP客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @GetMapping("/query-by-id/{id}")
    public Response<AiClientToolMcpResponseDTO> queryAiClientToolMcpById(@PathVariable Long id) {
        try {
            AiClientToolMcpResponseDTO data = aiClientToolMcpAdminService.queryAiClientToolMcpById(id);
            return success(data);
        } catch (Exception e) {
            log.error("根据ID查询MCP客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-mcp-id/{mcpId}")
    public Response<AiClientToolMcpResponseDTO> queryAiClientToolMcpByMcpId(@PathVariable String mcpId) {
        try {
            AiClientToolMcpResponseDTO data = aiClientToolMcpAdminService.queryAiClientToolMcpByMcpId(mcpId);
            return success(data);
        } catch (Exception e) {
            log.error("根据MCP ID查询MCP客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-all")
    public Response<List<AiClientToolMcpResponseDTO>> queryAllAiClientToolMcps() {
        try {
            return success(aiClientToolMcpAdminService.queryAllAiClientToolMcps());
        } catch (Exception e) {
            log.error("查询所有MCP客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-status/{status}")
    public Response<List<AiClientToolMcpResponseDTO>> queryAiClientToolMcpsByStatus(@PathVariable Integer status) {
        try {
            return success(aiClientToolMcpAdminService.queryAiClientToolMcpsByStatus(status));
        } catch (Exception e) {
            log.error("根据状态查询MCP客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-transport-type/{transportType}")
    public Response<List<AiClientToolMcpResponseDTO>> queryAiClientToolMcpsByTransportType(@PathVariable String transportType) {
        try {
            return success(aiClientToolMcpAdminService.queryAiClientToolMcpsByTransportType(transportType));
        } catch (Exception e) {
            log.error("根据传输类型查询MCP客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-enabled")
    public Response<List<AiClientToolMcpResponseDTO>> queryEnabledAiClientToolMcps() {
        try {
            return success(aiClientToolMcpAdminService.queryEnabledAiClientToolMcps());
        } catch (Exception e) {
            log.error("查询启用的MCP客户端配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @PostMapping("/query-list")
    public Response<List<AiClientToolMcpResponseDTO>> queryAiClientToolMcpList(@RequestBody AiClientToolMcpQueryRequestDTO request) {
        try {
            return success(aiClientToolMcpAdminService.queryAiClientToolMcpList(request));
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), null);
        } catch (Exception e) {
            log.error("根据查询条件查询MCP客户端配置列表失败", e);
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
