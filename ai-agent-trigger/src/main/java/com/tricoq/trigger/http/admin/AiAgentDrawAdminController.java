package com.tricoq.trigger.http.admin;

import com.tricoq.api.IAiAgentDrawAdminService;
import com.tricoq.api.dto.AiAgentDrawConfigQueryRequestDTO;
import com.tricoq.api.dto.AiAgentDrawConfigRequestDTO;
import com.tricoq.api.dto.AiAgentDrawConfigResponseDTO;
import com.tricoq.api.response.Response;
import com.tricoq.application.model.dto.command.SaveAgentDrawCommand;
import com.tricoq.application.service.AiAgentDrawAdminService;
import com.tricoq.types.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 拖拉拽
 *
 * @author trico qiang
 * 2025/9/28 07:35
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/ai-agent-draw")
@CrossOrigin(origins = "*", allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class AiAgentDrawAdminController implements IAiAgentDrawAdminService {

    private final AiAgentDrawAdminService agentDrawAdminService;

    /**
     * 保存draw config
     *
     * @param request 配置请求参数
     * @return 响应
     */
    @Override
    @PostMapping("/save-config")
    public Response<String> saveDrawConfig(@RequestBody AiAgentDrawConfigRequestDTO request) {

        SaveAgentDrawCommand command = SaveAgentDrawCommand.builder()
                .configData(request.getConfigData())
                .agentId(request.getAgentId())
                .configExecData(request.getConfigExecData())
                .description(request.getDescription())
                .configName(request.getConfigName())
                .configId(request.getConfigId())
                .build();
        try {
            agentDrawAdminService.saveDrawConfig(command);
            return success(null);
        } catch (Exception e) {
            log.error("保存流程图失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    /**
     * 分页查询拖拉拽流程图配置列表
     *
     * @param request 查询条件与分页参数
     * @return 配置列表
     */
    @Override
    @PostMapping("/query-list")
    public Response<List<AiAgentDrawConfigResponseDTO>> queryDrawConfigList(AiAgentDrawConfigQueryRequestDTO request) {
        try {
            return success(agentDrawAdminService.queryDrawConfigList(request));
        } catch (Exception e) {
            log.error("查询异常", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }

    }

    @Override
    @GetMapping("/get-config/{configId}")
    public Response<AiAgentDrawConfigResponseDTO> getDrawConfig(@PathVariable String configId) {
        try {
            log.info("获取流程图配置请求，configId: {}", configId);
            if (StringUtils.isEmpty(configId)) {
                return fail(ResponseCode.ILLEGAL_PARAMETER, ResponseCode.ILLEGAL_PARAMETER.getInfo(), null);
            }
            return success(agentDrawAdminService.getDrawConfig(configId));
        } catch (Exception e) {
            log.error("获取流程图配置失败", e);
            return fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), null);
        }
    }

    @Override
    @DeleteMapping("/delete-config/{configId}")
    public Response<String> deleteDrawConfig(@PathVariable String configId) {
        try {
            log.info("删除流程图配置请求，configId: {}", configId);

            if (StringUtils.isEmpty(configId)) {
                return fail(ResponseCode.ILLEGAL_PARAMETER, ResponseCode.ILLEGAL_PARAMETER.getInfo(), null);
            }

            return agentDrawAdminService.deleteByConfigId(configId) ? success("删除成功") :
                    fail(ResponseCode.UN_ERROR, ResponseCode.UN_ERROR.getInfo(), "删除失败");
        } catch (Exception e) {
            log.error("删除流程图配置失败", e);
            return fail(ResponseCode.UN_ERROR, "删除失败：" + e.getMessage() , null);
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
