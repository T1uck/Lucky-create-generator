package com.luckyone.maker.template.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class TemplateMakerModelConfig {

    private List<ModelInfoConfig> models;

    private ModelGroupConfig modelGroupConfig;

    /**
     * 用来替换文件路径 里面的fieldName models中的一个 只要分步制作用过即可
     */
    private ModelInfoConfig fileDirPathConfig;

    @NoArgsConstructor
    @Data
    public static class ModelInfoConfig {
        private String fieldName;

        private String type;

        private String description;

        private Object defaultValue;

        private String abbr;

        private String replaceText;
    }

    @Data
    public static class ModelGroupConfig {

        private String condition;

        private String groupKey;

        private String groupName;

        private String type;

        private String description;
    }
}
