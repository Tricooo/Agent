package com.tricoq.trigger.http.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tricoq.api.IAiAgentDrawAdminService;
import com.tricoq.api.dto.AiAgentDrawConfigRequestDTO;
import com.tricoq.api.dto.AiAgentDrawConfigResponseDTO;
import com.tricoq.api.response.Response;
import com.tricoq.application.model.dto.command.SaveAgentDrawCommand;
import com.tricoq.application.service.AiAgentDrawAdminService;
import com.tricoq.types.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private final AiAgentDrawAdminService aiAgentDrawAdminService;

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
            aiAgentDrawAdminService.saveDrawConfig(command);
        }catch (Exception e){

        }

        return null;
    }

    @Override
    @GetMapping("/get-config/{configId}")
    public Response<AiAgentDrawConfigResponseDTO> getDrawConfig(@PathVariable String configId) {
//        try {
//            log.info("获取流程图配置请求，configId: {}", configId);
//
//            if (!StringUtils.hasText(configId)) {
//                return Response.<AiAgentDrawConfigResponseDTO>builder()
//                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
//                        .info("配置ID不能为空")
//                        .build();
//            }
//
//            AiAgentDrawConfig drawConfig = aiAgentDrawConfigDao.queryByConfigId(configId);
//
//            if (drawConfig == null) {
//                return Response.<AiAgentDrawConfigResponseDTO>builder()
//                        .code(ResponseCode.UN_ERROR.getCode())
//                        .info("配置不存在")
//                        .build();
//            }
//
//            AiAgentDrawConfigResponseDTO responseDTO = new AiAgentDrawConfigResponseDTO();
//            BeanUtils.copyProperties(drawConfig, responseDTO);
//
//            return Response.<AiAgentDrawConfigResponseDTO>builder()
//                    .code(ResponseCode.SUCCESS.getCode())
//                    .info(ResponseCode.SUCCESS.getInfo())
//                    .data(responseDTO)
//                    .build();
//
//        } catch (Exception e) {
//            log.error("获取流程图配置失败", e);
//            return Response.<AiAgentDrawConfigResponseDTO>builder()
//                    .code(ResponseCode.UN_ERROR.getCode())
//                    .info("获取失败：" + e.getMessage())
//                    .build();
//        }
        return null;
    }

    @Override
    @DeleteMapping("/delete-config/{configId}")
    public Response<String> deleteDrawConfig(@PathVariable String configId) {
//        try {
//            log.info("删除流程图配置请求，configId: {}", configId);
//
//            if (!StringUtils.hasText(configId)) {
//                return Response.<String>builder()
//                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
//                        .info("配置ID不能为空")
//                        .build();
//            }
//
//            int result = aiAgentDrawConfigDao.deleteByConfigId(configId);
//
//            if (result > 0) {
//                return Response.<String>builder()
//                        .code(ResponseCode.SUCCESS.getCode())
//                        .info(ResponseCode.SUCCESS.getInfo())
//                        .data("删除成功")
//                        .build();
//            } else {
//                return Response.<String>builder()
//                        .code(ResponseCode.UN_ERROR.getCode())
//                        .info("删除失败，配置不存在")
//                        .build();
//            }
//
//        } catch (Exception e) {
//            log.error("删除流程图配置失败", e);
//            return Response.<String>builder()
//                    .code(ResponseCode.UN_ERROR.getCode())
//                    .info("删除失败：" + e.getMessage())
//                    .build();
//        }
        return null;
    }
}
