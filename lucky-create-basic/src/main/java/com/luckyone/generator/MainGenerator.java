package com.luckyone.generator;

import com.luckyone.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * 核心代码生成器
 */
public class MainGenerator {
    public static void doGenerate(Object model) throws TemplateException, IOException {
        // 整个项目的根目录
        String projectPath = System.getProperty("user.dir");
        File parentFile = new File(projectPath).getParentFile();
        // 输入路径
        String inputPath = new File(parentFile, "lucky-create-generator-demo/acm-template").getAbsolutePath();
        String outputPath = projectPath;
        // 生成静态文件
        StaticGenerator.copyFilesByRecursive(inputPath, outputPath);
        // 生成动态文件
        String inputDynamicPath = projectPath + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        String outputDynamicPath = outputPath + File.separator + "acm-template/src/com/luckyone/acm/MainTemplate.java";
        DynamicGenerator.doGenerate(inputDynamicPath, outputDynamicPath, model);
    }

    public static void main(String[] args) throws TemplateException, IOException {
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setLoop(false);
        mainTemplateConfig.setAuthor("lucky");
        mainTemplateConfig.setOutputText("求和结果：");
        doGenerate(mainTemplateConfig);

    }
}
