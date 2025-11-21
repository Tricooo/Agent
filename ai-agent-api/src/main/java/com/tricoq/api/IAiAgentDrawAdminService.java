package com.tricoq.api;

import com.tricoq.api.dto.AiAgentDrawConfigRequestDTO;
import com.tricoq.api.dto.AiAgentDrawConfigResponseDTO;
import com.tricoq.api.response.Response;

/**
 * AI智能体拖拉拽配置管理服务接口
 *
 * @author trico qiang
 * 2025/9/28 07:35
 */
public interface IAiAgentDrawAdminService {

    /**
     * 保存拖拉拽流程图配置
     *
     * @param request 配置请求参数
     * @return 保存结果
     */
    Response<String> saveDrawConfig(AiAgentDrawConfigRequestDTO request);

    /**
     * 获取拖拉拽流程图配置
     *
     * @param configId 配置ID
     * @return 配置数据
     */
    Response<AiAgentDrawConfigResponseDTO> getDrawConfig(String configId);

    /**
     * 删除拖拉拽流程图配置
     *
     * @param configId 配置ID
     * @return 删除结果
     */
    Response<String> deleteDrawConfig(String configId);

}
