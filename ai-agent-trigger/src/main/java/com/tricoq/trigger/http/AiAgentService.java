package com.tricoq.trigger.http;

import com.alibaba.fastjson.JSON;
import com.tricoq.api.IAiAgentService;
import com.tricoq.api.dto.AutoAgentRequestDTO;
import com.tricoq.domain.agent.model.entity.ExecuteCommandEntity;
import com.tricoq.domain.agent.service.execute.IExecuteStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 *
 * @author trico qiang
 * @date 11/4/25
 */
@RestController
@RequestMapping("/api/v1/agent")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
@Slf4j
public class AiAgentService implements IAiAgentService {

    private final IExecuteStrategy executeStrategy;

    private final ThreadPoolExecutor executor;

    public AiAgentService(@Qualifier("autoAgentExecuteStrategy") IExecuteStrategy executeStrategy,
                          ThreadPoolExecutor executor) {
        this.executeStrategy = executeStrategy;
        this.executor = executor;
    }

    @PostMapping("/auto_agent")
    @Override
    public ResponseBodyEmitter autoAgent(@RequestBody AutoAgentRequestDTO request, HttpServletResponse response) {
        log.info("AutoAgent流式执行请求开始，请求信息：{}", JSON.toJSONString(request));
        try {
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            response.setHeader("Content-Type", "text/event-stream");
            response.setCharacterEncoding("UTF-8");

            ResponseBodyEmitter emitter = new ResponseBodyEmitter(Long.MAX_VALUE);

            ExecuteCommandEntity command = ExecuteCommandEntity.builder()
                    .agentId(request.getAiAgentId())
                    .sessionId(request.getSessionId())
                    .maxSteps(request.getMaxStep())
                    .userInput(request.getMessage())
                    .build();

            executor.execute(() -> {
                try {
                    executeStrategy.execute(command, emitter);
                } catch (Exception e) {
                    log.error("AutoAgent执行异常：{}", e.getMessage(), e);
                    try {
                        emitter.send("执行异常：" + e.getMessage());
                    } catch (Exception ex) {
                        log.error("发送异常信息失败：{}", ex.getMessage(), ex);
                    }
                } finally {
                    try {
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("完成流式输出失败：{}", e.getMessage(), e);
                    }
                }
            });
            return emitter;
        } catch (Exception e) {
            log.error("AutoAgent请求处理异常：{}", e.getMessage(), e);
            ResponseBodyEmitter errorEmitter = new ResponseBodyEmitter();
            try {
                errorEmitter.send("请求处理异常：" + e.getMessage());
                errorEmitter.complete();
            } catch (Exception ex) {
                log.error("发送错误信息失败：{}", ex.getMessage(), ex);
            }
            return errorEmitter;
        }
    }
}
