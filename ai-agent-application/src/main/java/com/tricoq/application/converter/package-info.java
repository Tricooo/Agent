/**
 * 1. 负责 Controller DTO ↔ Command/Query/Result ↔ Domain VO
 * 2. 应用层自定义 MapStruct/手写转换器
 * // ai-agent-application/src/main/java/com/tricoq/application/assembler/AgentDrawAssembler.java
 * `@Mapper(componentModel = "spring")
 * public interface AgentDrawAssembler {
 *     AgentDrawCommand toCommand(AiAgentDrawConfigRequestDTO dto);
 *     AiAgentDrawConfigResponseDTO toResponse(AgentDrawResult result);
 * }
 * @author trico qiang
 * @date 11/25/25
 */
package com.tricoq.application.converter;