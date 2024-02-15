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
 * @author: С�ɵĵ���
 * @Date: 2023/11/13 - 11 - 13 - 11:18
 * @Description: PACKAGE_NAME
 * @version: 1.0
 */
public class FreeMarkerTest {

    @Test
    public void test() throws IOException, TemplateException {
        // new �� Configuration ���󣬲���Ϊ FreeMarker �汾��
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        File file = new File("src/main/resources/templates");
        System.out.println(file);
        // ָ��ģ���ļ����ڵ�·��
        configuration.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));

        // ����ģ���ļ�ʹ�õ��ַ���
        configuration.setDefaultEncoding("GBK");

        // �ı����ָ�ʽ
        configuration.setNumberFormat("0.######");

        // ����ģ���ļ�������ָ��ģ��
        Template template = configuration.getTemplate("myweb.html.ftl");

        // ��������ģ��
        HashMap<String, Object> dataModel = new HashMap<>();
        dataModel.put("currentYear", 2023);
        List<Map<String,Object>> menuItems = new ArrayList<>();
        Map<String, Object> menuItem1 = new HashMap<>();
        menuItem1.put("url", "https://baidu.com");
        menuItem1.put("label", "�ٶ�");
        HashMap<String, Object> menuItem2 = new HashMap<>();
        menuItem2.put("url", "https://codefather.cn");
        menuItem2.put("label", "��̵���");
        menuItems.add(menuItem1);
        menuItems.add(menuItem2);
        dataModel.put("menuItems", menuItems);

        // ָ�����ɵ��ļ�
        Writer out = new FileWriter("myweb.html");

        // �����ļ�
        template.process(dataModel, out);

        // �ر�
        out.close();
    }
}
