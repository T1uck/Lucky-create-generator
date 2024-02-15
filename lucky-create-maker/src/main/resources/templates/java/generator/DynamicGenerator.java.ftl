package ${basePackage}.generator;

import cn.hutool.core.io.FileUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.Locale;

/**
 * 动态文件生成
 */
public class DynamicGenerator {

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

        // 文件不存在则创建文件和父目录
        if (!FileUtil.exist(outputPath)) {
            FileUtil.touch(outputPath);
        }

        // 生成数据模型
        Writer out =  new OutputStreamWriter(new FileOutputStream(outputPath),"utf-8");
        template.process(model, out);

        // 关闭
        out.close();
    }
}