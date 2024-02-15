package com.luckyone.maker.generator.main;


public class MainGenerator extends GenerateTemplate{
    @Override
    protected String buildDist(String outputPath, String jarPath, String shellOutputFilePath, String sourceCopyDestPath) {
        System.out.println("不用生成精简版版本");
        return "";
    }
}
