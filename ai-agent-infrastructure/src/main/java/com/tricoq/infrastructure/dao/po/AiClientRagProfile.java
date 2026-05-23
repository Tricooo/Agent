package com.tricoq.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库检索画像表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("ai_client_rag_profile")
public class AiClientRagProfile {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String ragId;

    private String knowledgeTag;

    private String profileVersion;

    private String profileJson;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
