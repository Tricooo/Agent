package com.tricoq.trigger.http.admin;

import com.tricoq.api.IAiClientModelAdminService;
import com.tricoq.api.dto.AiClientModelQueryRequestDTO;
import com.tricoq.api.dto.AiClientModelRequestDTO;
import com.tricoq.api.dto.AiClientModelResponseDTO;
import com.tricoq.api.response.Response;
import com.tricoq.application.service.AiClientModelAdminService;
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
 * AI客户端模型管理控制器
 *
 * @author trico qiang
 * @description AI客户端模型配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/ai-client-model")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class AiClientModelAdminController implements IAiClientModelAdminService {

    private final AiClientModelAdminService aiClientModelAdminService;

    @Override
    @PostMapping("/create")
    public Response<Boolean> createAiClientModel(@RequestBody AiClientModelRequestDTO request) {
        try {
            boolean success = aiClientModelAdminService.createAiClientModel(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("创建AI客户端模型配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @PutMapping("/update-by-id")
    public Response<Boolean> updateAiClientModelById(@RequestBody AiClientModelRequestDTO request) {
        try {
            boolean success = aiClientModelAdminService.updateAiClientModelById(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("根据ID更新AI客户端模型配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @PutMapping("/update-by-model-id")
    public Response<Boolean> updateAiClientModelByModelId(@RequestBody AiClientModelRequestDTO request) {
        try {
            boolean success = aiClientModelAdminService.updateAiClientModelByModelId(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("根据模型ID更新AI客户端模型配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @DeleteMapping("/delete-by-id/{id}")
    public Response<Boolean> deleteAiClientModelById(@PathVariable Long id) {
        try {
            boolean success = aiClientModelAdminService.deleteAiClientModelById(id);
            return success(success);
        } catch (Exception e) {
            log.error("根据ID删除AI客户端模型配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @DeleteMapping("/delete-by-model-id/{modelId}")
    public Response<Boolean> deleteAiClientModelByModelId(@PathVariable String modelId) {
        try {
            boolean success = aiClientModelAdminService.deleteAiClientModelByModelId(modelId);
            return success(success);
        } catch (Exception e) {
            log.error("根据模型ID删除AI客户端模型配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @GetMapping("/query-by-id/{id}")
    public Response<AiClientModelResponseDTO> queryAiClientModelById(@PathVariable Long id) {
        try {
            AiClientModelResponseDTO data = aiClientModelAdminService.queryAiClientModelById(id);
            if (data == null) {
                return fail(ResponseCode.UN_ERROR, "未找到对应的AI客户端模型配置", null);
            }
            return success(data);
        } catch (Exception e) {
            log.error("根据ID查询AI客户端模型配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-model-id/{modelId}")
    public Response<AiClientModelResponseDTO> queryAiClientModelByModelId(@PathVariable String modelId) {
        try {
            AiClientModelResponseDTO data = aiClientModelAdminService.queryAiClientModelByModelId(modelId);
            if (data == null) {
                return fail(ResponseCode.UN_ERROR, "未找到对应的AI客户端模型配置", null);
            }
            return success(data);
        } catch (Exception e) {
            log.error("根据模型ID查询AI客户端模型配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-api-id/{apiId}")
    public Response<List<AiClientModelResponseDTO>> queryAiClientModelsByApiId(@PathVariable String apiId) {
        try {
            return success(aiClientModelAdminService.queryAiClientModelsByApiId(apiId));
        } catch (Exception e) {
            log.error("根据API配置ID查询AI客户端模型配置列表失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-model-type/{modelType}")
    public Response<List<AiClientModelResponseDTO>> queryAiClientModelsByModelType(@PathVariable String modelType) {
        try {
            return success(aiClientModelAdminService.queryAiClientModelsByModelType(modelType));
        } catch (Exception e) {
            log.error("根据模型类型查询AI客户端模型配置列表失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-enabled")
    public Response<List<AiClientModelResponseDTO>> queryEnabledAiClientModels() {
        try {
            return success(aiClientModelAdminService.queryEnabledAiClientModels());
        } catch (Exception e) {
            log.error("查询所有启用的AI客户端模型配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @PostMapping("/query-list")
    public Response<List<AiClientModelResponseDTO>> queryAiClientModelList(@RequestBody AiClientModelQueryRequestDTO request) {
        try {
            return success(aiClientModelAdminService.queryAiClientModelList(request));
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), null);
        } catch (Exception e) {
            log.error("根据条件查询AI客户端模型配置列表失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-all")
    public Response<List<AiClientModelResponseDTO>> queryAllAiClientModels() {
        try {
            return success(aiClientModelAdminService.queryAllAiClientModels());
        } catch (Exception e) {
            log.error("查询所有AI客户端模型配置失败", e);
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
