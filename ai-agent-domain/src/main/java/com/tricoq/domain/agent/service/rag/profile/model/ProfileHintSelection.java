package com.tricoq.domain.agent.service.rag.profile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileHintSelection {

    private String profileSource;

    private List<String> selectedHints;
}
