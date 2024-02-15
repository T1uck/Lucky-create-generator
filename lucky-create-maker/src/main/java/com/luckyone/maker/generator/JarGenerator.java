package com.luckyone.maker.generator;

import java.io.*;
import java.util.Map;

public class JarGenerator {
    public static void doGenerator(String projectDir) throws IOException, InterruptedException {
        // 清理之前的构建并打包
        // 不同的操作系统，执行的命令不同
        String winMavenCommand = "mvn.cmd clean package -DskipTests=true";
        String otherMavenCommand = "mvn clean package -DskipTests=true";
        String mavenCommand = winMavenCommand;

        // 这里将路径拆分（必须）
        ProcessBuilder processBuilder = new ProcessBuilder(mavenCommand.split(" "));
        processBuilder.directory(new File(projectDir));
        Map<String, String> environment = processBuilder.environment();
        System.out.println(environment);
        Process process = processBuilder.start();

        // 读取命令的输出
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        // 等待命令执行完毕
        int exitCode = process.waitFor();
        System.out.println("命令执行结束，退出码：" + exitCode);

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        doGenerator("E:\\study\\lucky-create-generator\\lucky-create-marker\\generated\\acm-template-pro-generator");
    }
}
