package com.luckyone.maker.template.model;

import com.luckyone.maker.template.enums.CodeCheckTypeEnums;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class TemplateMakerFileConfig {

    /**
     * 文件过滤信息配置列表
     */
    private List<FileInfoConfig> files;

    /**
     * 文件分组配置
     */
    private FileGroupConfig fileGroupConfig;


    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class FileInfoConfig {
        private String path;

        private String condition;

        private List<FileFilterConfig> filterConfigList;

        /**
         * 需要被控制生成的代码列表
         */
        private List<ControlCodeInfoConfig> controlCodeConfigList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ControlCodeInfoConfig {
        /**
         * 为true 则是 当存在时生成 为false时 需要增加一个 !
         */
        public boolean conditionExist;

        /**
         * 代码生成条件
         */
        private String condition;

        /**
         * 需要被控制的代码
         */
        public String controlCode;

        /**
         * 代码校验类型 默认是包含
         */
        public String codeCheckType = CodeCheckTypeEnums.EQUALS.getValue();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileGroupConfig {

        private String condition;

        private String groupKey;

        private String groupName;
    }
}
