package com.luckyone.model;

import lombok.Data;

/**
 * 动态判断
 */
@Data
public class MainTemplateConfig {

    /**
     * 是否循环
     */
    private boolean loop;

    /**
     * 作者
     */
    private String author = "lucky";

    /**
     * 输出文本
     */
    private String outputText = "sum = ";
}
