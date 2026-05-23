package com.tricoq.domain.agent.model.valobj;

import com.tricoq.domain.agent.model.dto.AiClientAdvisorDTO;
import org.junit.Assert;
import org.junit.Test;

public class RetrievalOptionsVOTest {

    @Test
    public void shouldEnableContextSalienceByDefault() {
        RetrievalOptionsVO options = RetrievalOptionsVO.from(new AiClientAdvisorDTO.RagAnswer(), 4);

        Assert.assertTrue(options.isContextSalienceEnabled());
    }

    @Test
    public void shouldAllowContextSalienceToBeDisabledByAdvisorConfig() {
        AiClientAdvisorDTO.RagAnswer ragAnswer = new AiClientAdvisorDTO.RagAnswer();
        ragAnswer.setContextSalienceEnabled(false);

        RetrievalOptionsVO options = RetrievalOptionsVO.from(ragAnswer, 4);

        Assert.assertFalse(options.isContextSalienceEnabled());
    }
}
