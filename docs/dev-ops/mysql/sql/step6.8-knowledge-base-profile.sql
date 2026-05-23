CREATE TABLE IF NOT EXISTS `ai_client_rag_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rag_id` varchar(50) NOT NULL COMMENT '知识库ID',
  `knowledge_tag` varchar(100) NOT NULL COMMENT '知识标签',
  `profile_version` varchar(32) NOT NULL COMMENT '画像版本',
  `profile_json` mediumtext NOT NULL COMMENT '知识库画像JSON',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_knowledge_tag` (`knowledge_tag`),
  KEY `idx_rag_id` (`rag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库检索画像表';
