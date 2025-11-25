/**
 * 用例服务（接口/实现分离）
 * 应用层通常是用例编排，更多是“服务类”而非“SPI”，可以默认用具体类；如果要对外暴露（如给 trigger 或其他模块依赖的契约），再抽接口
 * 只有单一实现、不会切换/代理时：直接用具体类（@Service）即可，减少样板。
 * 需要跨模块暴露契约、可能多实现/可插拔、方便用 Mock 注入测试时：保留接口 + 实现
 *
 * @author trico qiang
 * @date 11/25/25
 */
package com.tricoq.application.service;