package com.luckyone.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 静态文件生成
 */
public class StaticGenerator {
    public static void main(String[] args) {
        // 获取整个项目的根路径
        String projectPath = System.getProperty("user.dir");
        File projectFile = new File(projectPath).getParentFile();
        // 输入路径：acm实例代码模版目录
        String inputPath = new File(projectFile,"lucky-create-generator-demo/acm-template").getAbsolutePath();
        // 输出路径：直接输出到项目的根目录
        String outputPath = projectPath;
        // 使用hutool工具类生成
//        copyFilesByHutool(inputPath, outputPath);
        // 自主实现循环拷贝
        copyFilesByRecursive(inputPath, outputPath);
    }

    /**
     * 使用(hutool 工具类进行文件拷贝)
     * @param inputPath
     * @param outputPath
     */
    public static void copyFilesByHutool(String inputPath, String outputPath){
        FileUtil.copy(inputPath, outputPath,false);
        System.out.println("文件生成完毕");
    }

    /**
     * 自主实现文件拷贝
     * @param inputPath
     * @param outputPath
     */
    public static void copyFilesByRecursive(String inputPath,String outputPath){
        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);
        try {
            copyFileByRecursive(inputFile,outputFile);
        } catch (IOException e) {
            System.out.println("文件拷贝失败");
            e.printStackTrace();
        }
    }

    private static void copyFileByRecursive(File inputFile, File outputFile) throws IOException {
        // 区分是文件还是目录
        if (inputFile.isDirectory()){
            // 输出文件名称
            System.out.println("文件项目目录：" + inputFile.getName());
            File destOutputFile = new File(outputFile, inputFile.getName());
            // 如果是目录，首先创建目标目录
            if (!destOutputFile.exists()) {
                destOutputFile.mkdirs();
            }
            // 获取目录下的所有文件和子目录
            File[] files = inputFile.listFiles();
            // 无子文件，直接结束
            if (ArrayUtil.isEmpty(files)) {
                return;
            }
            for (File file : files) {
                // 递归拷贝下一层文件
                copyFileByRecursive(file, destOutputFile);
            }
        }
        else {
            // 是文件，直接复制到目标目录下
            Path destPath = outputFile.toPath().resolve(inputFile.getName());
            Files.copy(inputFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
