package com.alanwgt.console.command;

import com.alanwgt.console.CommandHandler;
import com.alanwgt.console.Logger;

public class HelpCommand extends Command<String> {

    @Override
    public String name() {
        return "help";
    }

    @Override
    protected void handleImpl(String param) {
        if (param != null) {
            if (!CommandHandler.getAvailableCommands().contains(param)) {
                CommandHandler.unknown(new String[] {param});
            } else {
                Command comm = CommandHandler.COMMANDS.get(param);
                Logger.termResponse(comm.help());

                if (comm.subCommands.size() == 0) {
                    return;
                }

                Logger.termResponse("\nlist of available subcommands of " + comm.name() + ":");

//                comm.printSubCommands();
                for (Object subComm : comm.subCommands.values()) {
                    Command<?> c = (Command<?>) subComm;
                    Logger.termResponse("\t\t=> " + c.name());
                    Logger.termResponse("\t\t | --- > " + c.help() + "\n");
                }
            }
        } else {
            Logger.termResponse("list of available commands:");

            for (String comm : CommandHandler.getAvailableCommands()) {
                Logger.termResponse("\t\t=> " + comm);
            }

            Logger.termResponse("use 'help [command]' for command help");

        }
    }

    @Override
    public void registerSubCommandsImpl() {

    }

    @Override
    public String help() {
        return "You need help for help? Interesting...";
    }

    @Override
    public Class<String> paramType() {
        return String.class;
    }

    @Override
    public boolean canBeCalled() {
        return true;
    }

    @Override
    public boolean isTopCommand() {
        return true;
    }

}
