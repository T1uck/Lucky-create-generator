package com.luckyone.maker.template.model;

import lombok.Data;

@Data
public class TemplateMakerOutputConfig {

    // 从未分组文件中移除组内的同名文件
    private boolean removeGroupGilesFromRoot = true;

    /**
     * 多个生成条件下 将文件移出组内 放在外层
     */
    private boolean removeFileFromGroup = true;
}
