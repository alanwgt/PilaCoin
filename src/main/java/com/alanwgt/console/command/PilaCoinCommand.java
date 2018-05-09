package com.alanwgt.console.command;

import com.alanwgt.App;
import com.alanwgt.console.Logger;
import com.alanwgt.helpers.PilaCoinManager;

public class PilaCoinCommand extends Command<String> {
    @Override
    public String name() {
        return "pila-coin";
    }

    @Override
    protected void handleImpl(String param) {}

    @Override
    void registerSubCommandsImpl() {
        subCommands.put("balance", new PilaCoinBalanceCommand());
        subCommands.put("hashes", new PilaCoinHashesCommand());
        subCommands.put("status", new PilaCoinsStatusCommand());
        subCommands.put("mine", new PilaCoinMineCommand());
    }

    @Override
    public String help() {
        return "bundle of helper commands to manipulate tha pilas";
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

    // ----------------------------------------------------------------------------------------------------------------

    public static class PilaCoinBalanceCommand extends Command<String> {

        @Override
        public String name() {
            return "balance";
        }

        @Override
        protected void handleImpl(String param) {
            if (PilaCoinManager.getPilaStore() == null) {
                Logger.termWarning("PilaStore not initialized!");
                return;
            }
            Logger.termResponse("Pila balance: " + PilaCoinManager.getPilaStore().getPilaSize());
        }

        @Override
        void registerSubCommandsImpl() { }

        @Override
        public String help() {
            return "shows the balance of pilas mined";
        }

        @Override
        public Class<String> paramType() {
            return null;
        }

        @Override
        public boolean canBeCalled() {
            return true;
        }

        @Override
        public boolean isTopCommand() {
            return false;
        }
    }

    public static class PilaCoinHashesCommand extends Command<String> {

        @Override
        public String name() {
            return "hashes";
        }

        @Override
        protected void handleImpl(String param) {
            Logger.termResponse("Hash count: " + PilaCoinManager.getHashCount());
        }

        @Override
        void registerSubCommandsImpl() {}

        @Override
        public String help() {
            return "shows the number of hashes that are being calculated per second (Hs/s)";
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
            return false;
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    public static class PilaCoinsStatusCommand extends Command {

        @Override
        public String name() {
            return "status";
        }

        @Override
        protected void handleImpl(Object param) {
            Logger.termResponse("Is mining: " + Boolean.toString(PilaCoinManager.isMining()));
        }

        @Override
        void registerSubCommandsImpl() {}

        @Override
        public String help() {
            return "shows if the computer is mining or not";
        }

        @Override
        public Class paramType() {
            return null;
        }

        @Override
        public boolean canBeCalled() {
            return true;
        }

        @Override
        public boolean isTopCommand() {
            return false;
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    public static class PilaCoinMineCommand extends Command<Boolean> {

        @Override
        public String name() {
            return "mine";
        }

        @Override
        protected void handleImpl(Boolean param) {
            if (param) {
                if (PilaCoinManager.isMining()) {
                    Logger.termResponse("miner is already started");
                    return;
                }
                PilaCoinManager.start(App.generateBasePila());
                App.getIoInterface().getSocket().emit("power-on-success");
                Logger.termResponse("the miner has started!");
            } else {
                if (!PilaCoinManager.isMining()) {
                    Logger.termResponse("miner is already stopped!");
                    return;
                }
                Logger.termWarning("received shutdown signal");
                PilaCoinManager.stopMining(() -> {
                    App.getIoInterface().getSocket().emit("power-off-success");
                    Logger.termWarning("the miner has stopped!");
                });
            }
        }

        @Override
        void registerSubCommandsImpl() { }

        @Override
        public String help() {
            return "activates or deactivates the miner";
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

    // ----------------------------------------------------------------------------------------------------------------

}
