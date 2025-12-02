package com.tricoq.trigger.http.admin;

import com.tricoq.api.IAiClientSystemPromptAdminService;
import com.tricoq.api.dto.AiClientSystemPromptQueryRequestDTO;
import com.tricoq.api.dto.AiClientSystemPromptRequestDTO;
import com.tricoq.api.dto.AiClientSystemPromptResponseDTO;
import com.tricoq.api.response.Response;
import com.tricoq.application.service.AiClientSystemPromptAdminService;
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
 * 系统提示词配置管理控制器
 *
 * @author trico qiang
 * @description 系统提示词配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/ai-client-system-prompt")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AiClientSystemPromptAdminController implements IAiClientSystemPromptAdminService {

    private final AiClientSystemPromptAdminService aiClientSystemPromptAdminService;

    @Override
    @PostMapping("/create")
    public Response<Boolean> createAiClientSystemPrompt(@RequestBody AiClientSystemPromptRequestDTO request) {
        try {
            boolean success = aiClientSystemPromptAdminService.createAiClientSystemPrompt(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("创建系统提示词配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @PutMapping("/update-by-id")
    public Response<Boolean> updateAiClientSystemPromptById(@RequestBody AiClientSystemPromptRequestDTO request) {
        try {
            boolean success = aiClientSystemPromptAdminService.updateAiClientSystemPromptById(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("根据ID更新系统提示词配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @PutMapping("/update-by-prompt-id")
    public Response<Boolean> updateAiClientSystemPromptByPromptId(@RequestBody AiClientSystemPromptRequestDTO request) {
        try {
            boolean success = aiClientSystemPromptAdminService.updateAiClientSystemPromptByPromptId(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("根据提示词ID更新系统提示词配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @DeleteMapping("/delete-by-id/{id}")
    public Response<Boolean> deleteAiClientSystemPromptById(@PathVariable Long id) {
        try {
            boolean success = aiClientSystemPromptAdminService.deleteAiClientSystemPromptById(id);
            return success(success);
        } catch (Exception e) {
            log.error("根据ID删除系统提示词配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @DeleteMapping("/delete-by-prompt-id/{promptId}")
    public Response<Boolean> deleteAiClientSystemPromptByPromptId(@PathVariable String promptId) {
        try {
            boolean success = aiClientSystemPromptAdminService.deleteAiClientSystemPromptByPromptId(promptId);
            return success(success);
        } catch (Exception e) {
            log.error("根据提示词ID删除系统提示词配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @GetMapping("/query-by-id/{id}")
    public Response<AiClientSystemPromptResponseDTO> queryAiClientSystemPromptById(@PathVariable Long id) {
        try {
            AiClientSystemPromptResponseDTO data = aiClientSystemPromptAdminService.queryAiClientSystemPromptById(id);
            return success(data);
        } catch (Exception e) {
            log.error("根据ID查询系统提示词配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-prompt-id/{promptId}")
    public Response<AiClientSystemPromptResponseDTO> queryAiClientSystemPromptByPromptId(@PathVariable String promptId) {
        try {
            AiClientSystemPromptResponseDTO data = aiClientSystemPromptAdminService.queryAiClientSystemPromptByPromptId(promptId);
            return success(data);
        } catch (Exception e) {
            log.error("根据提示词ID查询系统提示词配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-all")
    public Response<List<AiClientSystemPromptResponseDTO>> queryAllAiClientSystemPrompts() {
        try {
            return success(aiClientSystemPromptAdminService.queryAllAiClientSystemPrompts());
        } catch (Exception e) {
            log.error("查询所有系统提示词配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-enabled")
    public Response<List<AiClientSystemPromptResponseDTO>> queryEnabledAiClientSystemPrompts() {
        try {
            return success(aiClientSystemPromptAdminService.queryEnabledAiClientSystemPrompts());
        } catch (Exception e) {
            log.error("查询启用的系统提示词配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-prompt-name/{promptName}")
    public Response<List<AiClientSystemPromptResponseDTO>> queryAiClientSystemPromptsByPromptName(@PathVariable String promptName) {
        try {
            return success(aiClientSystemPromptAdminService.queryAiClientSystemPromptsByPromptName(promptName));
        } catch (Exception e) {
            log.error("根据提示词名称查询系统提示词配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @PostMapping("/query-list")
    public Response<List<AiClientSystemPromptResponseDTO>> queryAiClientSystemPromptList(@RequestBody AiClientSystemPromptQueryRequestDTO request) {
        try {
            return success(aiClientSystemPromptAdminService.queryAiClientSystemPromptList(request));
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), null);
        } catch (Exception e) {
            log.error("根据条件查询系统提示词配置列表失败", e);
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
