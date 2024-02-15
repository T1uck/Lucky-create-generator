package com.luckyone.maker.generator.file;

import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * 核心代码生成器
 */
public class FileGenerator {
    public static void doGenerate(Object model) throws TemplateException, IOException {
        // 整个项目的根目录
        String projectPath = System.getProperty("user.dir");
        File parentFile = new File(projectPath).getParentFile();
        // 输入路径
        String inputPath = new File(parentFile, "lucky-create-generator-demo/acm-template").getAbsolutePath();
        String outputPath = projectPath;
        // 生成静态文件
        StaticFileGenerator.copyFilesByHutool(inputPath, outputPath);
        // 生成动态文件
        String inputDynamicPath = projectPath + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        String outputDynamicPath = outputPath + File.separator + "acm-template/src/com/luckyone/acm/MainTemplate.java";
        DynamicFileGenerator.doGenerate(inputDynamicPath, outputDynamicPath, model);
    }
}
