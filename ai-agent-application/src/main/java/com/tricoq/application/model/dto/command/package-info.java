/**
 * 命名为 Command 是为了表达语义：它代表“执行某个用例/动作的指令”，通常是写操作（创建/更新/删除），与查询用的 Query 区分。
 * 与 DTO/VO 的区别：
 * DTO 是笼统的“数据传输对象”概念，未区分用途；Command/Query/Result 是在应用层对 DTO 做了语义细分，便于理解和约束。
 * VO（在领域层）强调值对象，有业务语义与不变式；Command 不承载业务行为，仅用来承接接口层数据并驱动用例。
 *
 * @author trico qiang
 * @date 11/25/25
 */
package com.tricoq.application.model.dto.command;