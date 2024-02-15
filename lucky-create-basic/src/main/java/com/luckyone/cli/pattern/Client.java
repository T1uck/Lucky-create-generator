package com.luckyone.cli.pattern;

public class Client {
    public static void main(String[] args) {
        // 创建接收者对象
        Device tv = new Device(("TV"));
        Device stereo = new Device("sterro");

        // 创建具体命令对象，可以绑定不同设备
        TurnOnCommand turnOnCommand = new TurnOnCommand(tv);
        TurnOffCommand turnOffCommand = new TurnOffCommand(stereo);

        // 创建调用者
        RemoteControl remoteControl = new RemoteControl();

        // 执行命令
        remoteControl.setCommand(turnOnCommand);
        remoteControl.pressButton();

        remoteControl.setCommand(turnOffCommand);
        remoteControl.pressButton();
    }
}
