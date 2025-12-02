package com.tricoq.trigger.http.admin;

import com.tricoq.api.IAiClientRagOrderAdminService;
import com.tricoq.api.dto.AiClientRagOrderQueryRequestDTO;
import com.tricoq.api.dto.AiClientRagOrderRequestDTO;
import com.tricoq.api.dto.AiClientRagOrderResponseDTO;
import com.tricoq.api.response.Response;
import com.tricoq.application.service.AiClientRagOrderAdminService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/ai-client-rag-order")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AiClientRagOrderAdminController implements IAiClientRagOrderAdminService {

    private final AiClientRagOrderAdminService aiClientRagOrderAdminService;

    @Override
    @PostMapping("/create")
    public Response<Boolean> createAiClientRagOrder(@RequestBody AiClientRagOrderRequestDTO request) {
        try {
            boolean success = aiClientRagOrderAdminService.createAiClientRagOrder(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("创建知识库配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @PutMapping("/update-by-id")
    public Response<Boolean> updateAiClientRagOrderById(@RequestBody AiClientRagOrderRequestDTO request) {
        try {
            boolean success = aiClientRagOrderAdminService.updateAiClientRagOrderById(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("根据ID更新知识库配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @PutMapping("/update-by-rag-id")
    public Response<Boolean> updateAiClientRagOrderByRagId(@RequestBody AiClientRagOrderRequestDTO request) {
        try {
            boolean success = aiClientRagOrderAdminService.updateAiClientRagOrderByRagId(request);
            return success(success);
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), false);
        } catch (Exception e) {
            log.error("根据知识库ID更新知识库配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @DeleteMapping("/delete-by-id/{id}")
    public Response<Boolean> deleteAiClientRagOrderById(@PathVariable Long id) {
        try {
            boolean success = aiClientRagOrderAdminService.deleteAiClientRagOrderById(id);
            return success(success);
        } catch (Exception e) {
            log.error("根据ID删除知识库配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @DeleteMapping("/delete-by-rag-id/{ragId}")
    public Response<Boolean> deleteAiClientRagOrderByRagId(@PathVariable String ragId) {
        try {
            boolean success = aiClientRagOrderAdminService.deleteAiClientRagOrderByRagId(ragId);
            return success(success);
        } catch (Exception e) {
            log.error("根据知识库ID删除知识库配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), false);
        }
    }

    @Override
    @GetMapping("/query-by-id/{id}")
    public Response<AiClientRagOrderResponseDTO> queryAiClientRagOrderById(@PathVariable Long id) {
        try {
            return success(aiClientRagOrderAdminService.queryAiClientRagOrderById(id));
        } catch (Exception e) {
            log.error("根据ID查询知识库配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-rag-id/{ragId}")
    public Response<AiClientRagOrderResponseDTO> queryAiClientRagOrderByRagId(@PathVariable String ragId) {
        try {
            return success(aiClientRagOrderAdminService.queryAiClientRagOrderByRagId(ragId));
        } catch (Exception e) {
            log.error("根据知识库ID查询知识库配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-enabled")
    public Response<List<AiClientRagOrderResponseDTO>> queryEnabledAiClientRagOrders() {
        try {
            return success(aiClientRagOrderAdminService.queryEnabledAiClientRagOrders());
        } catch (Exception e) {
            log.error("查询启用的知识库配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-knowledge-tag/{knowledgeTag}")
    public Response<List<AiClientRagOrderResponseDTO>> queryAiClientRagOrdersByKnowledgeTag(@PathVariable String knowledgeTag) {
        try {
            return success(aiClientRagOrderAdminService.queryAiClientRagOrdersByKnowledgeTag(knowledgeTag));
        } catch (Exception e) {
            log.error("根据知识标签查询知识库配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-by-status/{status}")
    public Response<List<AiClientRagOrderResponseDTO>> queryAiClientRagOrdersByStatus(@PathVariable Integer status) {
        try {
            return success(aiClientRagOrderAdminService.queryAiClientRagOrdersByStatus(status));
        } catch (Exception e) {
            log.error("根据状态查询知识库配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @PostMapping("/query-list")
    public Response<List<AiClientRagOrderResponseDTO>> queryAiClientRagOrderList(@RequestBody AiClientRagOrderQueryRequestDTO request) {
        try {
            return success(aiClientRagOrderAdminService.queryAiClientRagOrderList(request));
        } catch (IllegalArgumentException e) {
            return fail(ResponseCode.ILLEGAL_PARAMETER, e.getMessage(), null);
        } catch (Exception e) {
            log.error("分页查询知识库配置列表失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @GetMapping("/query-all")
    public Response<List<AiClientRagOrderResponseDTO>> queryAllAiClientRagOrders() {
        try {
            return success(aiClientRagOrderAdminService.queryEnabledAiClientRagOrders());
        } catch (Exception e) {
            log.error("查询所有知识库配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @RequestMapping(value = "file/upload", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
    public Response<Boolean> uploadRagFile(@RequestParam("name") String name, @RequestParam("tag") String tag, @RequestParam("files") List<MultipartFile> files) {
        try {
            boolean success = aiClientRagOrderAdminService.uploadRagFile(name, tag, files);
            return success(success);
        } catch (Exception e) {
            log.error("上传知识库，异常 {}", name, e);
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
