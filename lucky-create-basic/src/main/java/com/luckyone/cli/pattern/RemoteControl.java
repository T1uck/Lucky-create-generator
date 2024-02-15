package com.luckyone.cli.pattern;

/**
 * 调用者（相当与遥控器，接收客户端的命令并执行）
 */
public class RemoteControl {
    private Command command;

    public void setCommand(Command command) {
        this.command = command;
    }

    public void pressButton() {
        command.execute();
    }
}
