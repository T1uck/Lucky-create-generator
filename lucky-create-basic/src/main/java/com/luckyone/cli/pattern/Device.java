package com.luckyone.cli.pattern;

/**
 * 接收者（相当与被遥控的设备，知道如何执行具体的操作）
 */
public class Device {
    private String name;

    public Device(String name) {
        this.name = name;
    }

    public void turnon(){
        System.out.println(name + "设备打开");
    }

    public void turnoff(){
        System.out.println(name + "设备关闭");
    }
}
