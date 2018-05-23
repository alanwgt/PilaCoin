package com.alanwgt.console.command;

public class MasterCommand extends Command {
    @Override
    public String name() {
        return "master";
    }

    @Override
    protected void handleImpl(Object param) {}

    @Override
    void registerSubCommandsImpl() {

    }

    @Override
    public String help() {
        return "bundle of commands to interact with the master server";
    }

    @Override
    public Class paramType() {
        return null;
    }

    @Override
    public boolean canBeCalled() {
        return false;
    }

    @Override
    public boolean isTopCommand() {
        return true;
    }
}
