package com.ai.baemin.common.advisor;

import java.util.List;

public interface GuardrailProperties {
    List<String> injectionPatterns();
    List<String> allowedKeywords();
}
