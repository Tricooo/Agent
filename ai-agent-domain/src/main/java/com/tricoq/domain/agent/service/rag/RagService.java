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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    //todo 这里需要设计保证原子性
    @Override
    public void storeRagFile(String name, String tag, List<MultipartFile> files) {
        String ragId = UUID.randomUUID().toString().replace("-", "");

        List<Document> allDocuments = new ArrayList<>();

        for (MultipartFile file : files) {
            TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
            List<Document> documents = tokenTextSplitter.apply(documentReader.get());

            documents.forEach(doc -> {
                doc.getMetadata().put("knowledge", tag);
                doc.getMetadata().put("rag_id", ragId);
                doc.getMetadata().put("file_name", file.getOriginalFilename());
            });

            allDocuments.addAll(documents);
        }

        vectorStore.accept(allDocuments);

        AiRagOrderDTO aiRagOrderVO = new AiRagOrderDTO();
        aiRagOrderVO.setRagId(ragId);
        aiRagOrderVO.setRagName(name);
        aiRagOrderVO.setKnowledgeTag(tag);
        aiRagOrderVO.setStatus(1);
        ragRepository.insert(aiRagOrderVO);

    }

}
