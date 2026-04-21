package com.hongshu.ai.api.locallm.base.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class BaseChatCompletion implements Serializable {

    private Boolean stream = false;

}


