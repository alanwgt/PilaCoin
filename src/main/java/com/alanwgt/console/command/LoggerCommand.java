package com.alanwgt.console.command;

import com.alanwgt.console.Logger;

public class LoggerCommand extends Command<String> {

    @Override
    public String name() {
        return "logger";
    }

    @Override
    protected void handleImpl(String param) {}

    @Override
    public void registerSubCommandsImpl() {
        subCommands.put("debug", new DebugCommand());
    }

    @Override
    public String help() {
        return "helper to manage how the Logger class must behave.";
    }

    @Override
    public Class<String> paramType() {
        return String.class;
    }

    @Override
    public boolean canBeCalled() {
        return false;
    }

    @Override
    public boolean isTopCommand() {
        return true;
    }

    public static class DebugCommand extends Command<Boolean> {

        @Override
        public String name() {
            return "debug";
        }

        @Override
        protected void handleImpl(Boolean param) {
            Logger.DEBUG = param;
            try {
                Logger.termResponse(
                        Logger.DEBUG ? "activated verbose mode" :
                                "deactivated verbose mode"
                );
            } catch (Exception e) {
                Logger.termError("please, set only true or false.");
            }
        }

        @Override
        void registerSubCommandsImpl() {

        }

        @Override
        public String help() {
            return "Sets the mode of logger to verbose or not.";
        }

        @Override
        public Class<Boolean> paramType() {
            return Boolean.class;
        }

        @Override
        public boolean canBeCalled() {
            return false;
        }

        @Override
        public boolean isTopCommand() {
            return false;
        }
    }

}
