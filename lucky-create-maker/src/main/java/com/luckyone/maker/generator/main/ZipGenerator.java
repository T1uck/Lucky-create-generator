package com.luckyone.maker.generator.main;

/**
 * 生成代码生成器压缩包
 */
public class ZipGenerator extends GenerateTemplate{
    @Override
    protected String buildDist(String outputPath, String jarPath, String shellOutputFilePath, String sourceCopyDestPath) {
        System.out.println("生成精简版本jar包");
        String buildDist = super.buildDist(outputPath, jarPath, shellOutputFilePath, sourceCopyDestPath);
        return super.buildZip(buildDist);
    }
}
