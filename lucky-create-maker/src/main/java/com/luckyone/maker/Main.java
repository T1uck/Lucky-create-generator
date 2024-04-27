package com.luckyone.maker;

import com.luckyone.maker.generator.JarGenerator;
import com.luckyone.maker.generator.main.GenerateTemplate;
import com.luckyone.maker.generator.main.MainGenerator;
import com.luckyone.maker.generator.main.ZipGenerator;
import freemarker.template.TemplateException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
        GenerateTemplate generateTemplate = new ZipGenerator();
        generateTemplate.doGenerate();
    }
}