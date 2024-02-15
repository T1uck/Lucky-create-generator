import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: 小飞的电脑
 * @Date: 2023/11/13 - 11 - 13 - 11:18
 * @Description: PACKAGE_NAME
 * @version: 1.0
 */
public class FreeMarkerTest {

    @Test
    public void test() throws IOException, TemplateException {
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        File file = new File("src/main/resources/templates");
        System.out.println(file);
        // 指定模版文件所在的路径
        configuration.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));

        // 设置模版文件使用的字符集
        configuration.setDefaultEncoding("GBK");

        // 改变数字格式
        configuration.setNumberFormat("0.######");

        // 创建模版文件，加载指定模版
        Template template = configuration.getTemplate("myweb.html.ftl");

        // 创建数据模型
        HashMap<String, Object> dataModel = new HashMap<>();
        dataModel.put("currentYear", 2023);
        List<Map<String,Object>> menuItems = new ArrayList<>();
        Map<String, Object> menuItem1 = new HashMap<>();
        menuItem1.put("url", "https://baidu.com");
        menuItem1.put("label", "百度");
        HashMap<String, Object> menuItem2 = new HashMap<>();
        menuItem2.put("url", "https://codefather.cn");
        menuItem2.put("label", "编程导航");
        menuItems.add(menuItem1);
        menuItems.add(menuItem2);
        dataModel.put("menuItems", menuItems);

        // 指定生成的文件
        Writer out = new FileWriter("myweb.html");

        // 生成文件
        template.process(dataModel, out);

        // 关闭
        out.close();
    }
}
