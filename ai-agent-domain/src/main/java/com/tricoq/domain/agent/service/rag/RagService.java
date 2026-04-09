package com.tricoq.domain.agent.service.rag;

import com.tricoq.domain.agent.adapter.repository.IAiClientRagOrderRepository;
import com.tricoq.domain.agent.model.dto.AiRagOrderDTO;
import com.tricoq.domain.agent.service.IRagService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库服务
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/10/4 09:12
 */
@Slf4j
@Service
public class RagService implements IRagService {

    @Resource
    private TokenTextSplitter tokenTextSplitter;

    @Resource
    private PgVectorStore vectorStore;

    @Resource
    private IAiClientRagOrderRepository ragRepository;

    /**
     * 上传的文件会先做文档解析，再按 token 切分成适合检索的小片段，并给每个片段打上知识标签，后续检索时可以按标签召回对应知识
     * @param name
     * @param tag
     * @param files
     */
    @Override
    public void storeRagFile(String name, String tag, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            //把上传的文件解析成可读取的文档内容
            TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
            //把长文档切成多个较小文本块，方便后面做向量化和检索
            List<Document> documentList = tokenTextSplitter.apply(documentReader.get());

            //给每个文本块打上知识库标签，后面检索时可以按标签过滤
            documentList.forEach(doc -> doc.getMetadata().put("knowledge", tag));

            // 存储知识库文件
            vectorStore.accept(documentList);

            // 存储到数据库
            AiRagOrderDTO aiRagOrderVO = new AiRagOrderDTO();
            aiRagOrderVO.setRagName(name);
            aiRagOrderVO.setKnowledgeTag(tag);
            ragRepository.insert(aiRagOrderVO);
        }
    }

}
