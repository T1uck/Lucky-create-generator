package com.luckyone.cli;

import com.luckyone.cli.command.ConfigCommand;
import com.luckyone.cli.command.GenerateCommand;
import com.luckyone.cli.command.ListCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;


@Command(name = "lucky", mixinStandardHelpOptions = true)
public class CommandExecutor implements Runnable{
    private final CommandLine commandLine;

    {
        commandLine = new CommandLine(this)
                .addSubcommand(new GenerateCommand())
                .addSubcommand(new ConfigCommand())
                .addSubcommand(new ListCommand());
    }

    @Override
    public void run() {
        System.out.println("请输入具体命令，或者输入 --help 查看命令提示");
    }


    public Integer doExecute(String[] args) {
        return commandLine.execute(args);
    }
}
