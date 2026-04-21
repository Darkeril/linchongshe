package com.hongshu.ai.api.openai.entity.edits;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hongshu.ai.api.openai.entity.common.Choice;
import com.hongshu.ai.api.openai.entity.common.Usage;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EditResponse implements Serializable {
    private String id;
    private String object;
    private long created;
    private String model;
    private Choice[] choices;
    private Usage usage;
}
