package com.alanwgt.console.command;

import com.alanwgt.console.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class Command<T> {

    public abstract String name();
    protected abstract void handleImpl(T param);
    abstract void registerSubCommandsImpl();
    public abstract String help();
    public abstract Class<T> paramType();
    public abstract boolean canBeCalled();
    public abstract boolean isTopCommand();

    final Map<String, Command> subCommands = new HashMap<>();
//    private ArrayList<String> availableSubCommands;

    public final void registerSubCommands() {
        registerSubCommandsImpl();
//        availableSubCommands = new ArrayList<>(subCommands.keySet());
    }

    public final void handle(String[] command) {
        if (command.length == 1 && !canBeCalled()) {
            Logger.warning("this command expects an argument!");
            Logger.info("use with: ");
            printSubCommands();
            return;
        }

        String param = command.length == 1 ? null : command[1];

        // this means that we need to call the current handleImpl, otherwise we need to check if the param is a subcommand
        if (param == null) {
            handleImpl(null);
            return;
        }

        if (!subCommands.containsKey(param)) {
            // TODO cast the param to the expected type
//            if (!canBeCalled()) {
//                wrongCommand(command);
//                return;
//            }
            if (paramType().equals(String.class)) {
                handleImpl((T) param);
            } else if (paramType().equals(Boolean.class)) {
                handleImpl((T) Boolean.valueOf(param));
            } else {
                Logger.error("unknown type casting: " + paramType().toString());
            }
            return;
        }

        // check if its a subcommand
        Command c = subCommands.get(param);
        c.handle(
                Arrays.copyOfRange(
                        command, 1, command.length
                )
        );
    }

    void wrongCommand(String[] command) {
        String[] children = Arrays.copyOfRange(
                command, 1, command.length
        );
        String format = new String(
                new char[children.length]
        ).replace("\0", "[ %s ]");

        String message = String.format("%s: ", name()) + format;
        message = String.format(message + " is not a valid command.", (Object[]) children);

        Logger.warning(message);
    }

//    ArrayList<String> availableSubCommands() {
//        return availableSubCommands;
//    }

    void printSubCommands() {
        for (Object subComm : subCommands.values()) {
            Command<?> c = (Command<?>) subComm;
            Logger.info("\t\t=> " + c.name());
        }
    }

}
