package com.luckyone.cli.command;


import cn.hutool.core.bean.BeanUtil;
import com.luckyone.generator.MainGenerator;
import com.luckyone.model.MainTemplateConfig;
import lombok.Data;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "generate", description = "生成代码", mixinStandardHelpOptions = true)
@Data
public class GenerateCommand implements Callable<Integer> {

    @Option(names = {"-l", "-loop"}, description = "是否循环", arity = "0..1",echo = true, interactive = true)
    private boolean loop;

    @Option(names = {"-a", "--author"}, description = "作者", arity = "0..1",echo = true, interactive = true)
    private String author = "lucky";

    @Option(names = {"-o", "--outputText"}, description = "输出文本", arity = "0..1",echo = true, interactive = true)
    private String outputText = "输出信息：";

    @Override
    public Integer call() throws Exception {
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        BeanUtil.copyProperties(this, mainTemplateConfig);
        System.out.println("配置信息：" + mainTemplateConfig);
        MainGenerator.doGenerate(mainTemplateConfig);
        return 0;
    }
}
