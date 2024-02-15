package com.luckyone.maker.generator.file;

import cn.hutool.core.io.FileUtil;

/**
 * 静态文件生成
 */
public class StaticFileGenerator {
    /**
     * 使用(hutool 工具类进行文件拷贝)
     * @param inputPath
     * @param outputPath
     */
    public static void copyFilesByHutool(String inputPath, String outputPath){
        FileUtil.copy(inputPath, outputPath,false);
        System.out.println("文件生成完毕");
    }
}
