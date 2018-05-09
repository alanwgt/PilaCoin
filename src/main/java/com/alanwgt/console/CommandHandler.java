package com.alanwgt.console;

import com.alanwgt.console.command.Command;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.*;

public class CommandHandler {

    public static final Map<String, Command> COMMANDS = new HashMap<>();
    private static ArrayList<String> availableCommands;

    private final Terminator term;

    public CommandHandler(Terminator term) {
        this.term = term;
        registerCommands();
    }

    private void registerCommands() {
        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setScanners(
                                new SubTypesScanner(false),
                                new ResourcesScanner()
                        ).setUrls(ClasspathHelper.forClassLoader(
                                classLoadersList.toArray(new ClassLoader[0]))
                        ).filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("com.alanwgt.console.command")))
        );

        Set<Class<? extends Command>> classes = reflections.getSubTypesOf(Command.class);

        for (Class<? extends Command> c : classes) {
            if (c.equals(Command.class)) {
                continue;
            }

            try {
                Command comm = c.newInstance();
                comm.registerSubCommands();
                if (comm.isTopCommand()) {
                    COMMANDS.put(comm.name(), comm);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        availableCommands = new ArrayList<>(COMMANDS.keySet());
    }

    public Disposable start() {
        return term.linesFromInput()
                .subscribeOn(Schedulers.trampoline())
                .subscribe(this::handle);
    }

    private void handle(String[] command) {
        String root = command[0];
        if (COMMANDS.containsKey(root)) {
            COMMANDS.get(root).handle(command);
        } else {
            unknown(command);
        }
    }

    public static void unknown(String[] command) {
        Logger.warning(String.format("command '%s' not found.", command[0]));
    }

    public static ArrayList<String> getAvailableCommands() {
        return availableCommands;
    }
}
