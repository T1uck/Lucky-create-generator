package com.luckyone.cli.pattern;

/**
 * 具体命令（相当于操控按钮，负责将请求递给接受者并执行具体的操作）
 */
public class TurnOnCommand implements Command{
    private Device device;

    public TurnOnCommand(Device device) {
        this.device = device;
    }

    @Override
    public void execute() {
        device.turnon();
    }
}
