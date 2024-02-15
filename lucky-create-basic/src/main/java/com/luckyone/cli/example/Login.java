package com.luckyone.cli.example;

import cn.hutool.core.util.ReflectUtil;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.concurrent.Callable;


public class Login implements Callable<Integer> {
    @Option(names = {"-u", "--user"}, description = "用户名")
    String user;

    @Option(names = {"-p", "--password"}, description = "请输入密码：", arity = "0..1", interactive = true)
    String password;

    @Option(names = {"-cp", "--checkPassword"}, description = "确认密码", arity = "0..1", interactive = true)
    String checkPassword;

    @Override
    public Integer call() throws Exception {
        System.out.println("password = " + password);
        System.out.println("checkPassword = " + checkPassword);
        return 0;
    }

    public static void main(String[] args) {
        new CommandLine(new Login()).execute("-u","usr123","-p","-cp");

    }
}