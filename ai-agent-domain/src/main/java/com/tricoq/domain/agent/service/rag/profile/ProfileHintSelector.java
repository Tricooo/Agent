package com.tricoq.domain.agent.service.rag.profile;

import com.tricoq.domain.agent.service.rag.profile.model.ProfileHint;
import com.tricoq.domain.agent.service.rag.profile.model.ProfileHintSelection;

import java.util.List;

public interface ProfileHintSelector {

    String SOURCE_AUTO_PROFILE = "AUTO_PROFILE";
    String SOURCE_MANUAL_CONFIG = "MANUAL_CONFIG";
    String SOURCE_NO_PROFILE_MATCH = "NO_PROFILE_MATCH";
    String SOURCE_NONE = "NONE";

    ProfileHintSelection select(String userText, List<ProfileHint> profileHints, int topN);
}
