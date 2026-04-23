package com.ai.baemin.common.advisor;

public enum AdvisorOrder {
    INPUT_GUARDRAIL(0),
    INPUT_NORMALIZATION(1),
    SYSTEM_PROMPT(2),
    OUTPUT_GUARDRAIL(Integer.MAX_VALUE);

    private final int value;

    AdvisorOrder(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
