package com.tricoq.trigger.http.admin;

import com.tricoq.api.IAiAgentDataStatisticsAdminService;
import com.tricoq.api.dto.DataStatisticsResponseDTO;
import com.tricoq.api.response.Response;
import com.tricoq.application.service.AiAgentDataStatisticsAdminService;
import com.tricoq.types.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * 数据统计
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/10/4 10:33
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/data/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AiAgentDataStatisticsAdminController implements IAiAgentDataStatisticsAdminService {

    private final AiAgentDataStatisticsAdminService aiAgentDataStatisticsAdminService;

    @Override
    @GetMapping("/get-data-statistics")
    public Response<DataStatisticsResponseDTO> getDataStatistics() {
        try {
            DataStatisticsResponseDTO data = aiAgentDataStatisticsAdminService.getDataStatistics();
            return Response.<DataStatisticsResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(data)
                    .build();
        } catch (Exception e) {
            log.error("获取系统数据统计失败", e);
            return Response.<DataStatisticsResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(null)
                    .build();
        }
    }

}
