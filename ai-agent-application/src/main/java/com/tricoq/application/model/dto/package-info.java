/**
 * 应用层输入/输出模型（Command/Query/Result），用于 Controller 交互，避免直接暴露领域对象
 * 使用 Command/Query/Result DTO（如 SaveAgentDrawCommand、QueryClientsResult）
 * 承接 Controller DTO → 领域模型/VO 的转换；不要直接暴露领域 VO
 *
 * @author trico qiang
 * @date 11/25/25
 */
package com.tricoq.application.model.dto;