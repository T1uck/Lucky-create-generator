package com.luckyone.generator;

/**
 * @author: С�ɵĵ���
 * @Date: 2023/11/13 - 11 - 13 - 18:23
 * @Description: com.luckyone.generator
 * @version: 1.0
 */

import com.luckyone.model.MainTemplateConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.Locale;

/**
 * 动态文件生成
 */
public class DynamicGenerator {

    public static void main(String[] args) throws IOException, TemplateException {
        String projectPath = System.getProperty("user.dir");
        String inputPath = projectPath + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        String outputPath = projectPath + File.separator + "MainTemplate.java";
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("luckyone");
        mainTemplateConfig.setLoop(true);
        mainTemplateConfig.setOutputText("求和结果");
        doGenerate(inputPath, outputPath, mainTemplateConfig);
        System.out.println("文件生成结束");
        System.out.println("文件生成地址为：" + outputPath);
    }

    public static void doGenerate(String inputPath, String outputPath ,Object model) throws IOException, TemplateException {
        // new 出 Configuration 对象，参数为 Freemarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);

        // ָ指定模版文件所在的路径
        File templateDir = new File(inputPath).getParentFile();
        configuration.setDirectoryForTemplateLoading(templateDir);

        // 设置模版文件使用的字符集
        configuration.setEncoding(new Locale("zh_CN"),"utf-8");

        // 创建数字模版
        configuration.setNumberFormat("0.######");

        // 创建模版对象，加载指定模版
        String templateName = new File(inputPath).getName();
        Template template = configuration.getTemplate(templateName);

        // 生成数据模型
        Writer out =  new OutputStreamWriter(new FileOutputStream(outputPath),"utf-8");
        template.process(model, out);

        // 关闭
        out.close();
    }
}
