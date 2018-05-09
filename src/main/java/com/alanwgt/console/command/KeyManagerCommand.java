package com.alanwgt.console.command;

import com.alanwgt.console.Logger;
import com.alanwgt.helpers.KeyManager;

import java.util.Base64;
import java.util.Map;

public class KeyManagerCommand extends Command<String> {

    @Override
    public String name() {
        return "keyset";
    }

    @Override
    protected void handleImpl(String param) {}

    @Override
    void registerSubCommandsImpl() {
        subCommands.put("show", new ShowKeysCommand());
    }

    @Override
    public String help() {
        return "this command manages the keys used by the program. Use it with caution!\nIf you generate a new keyset you may not be able to recover your PilaCoins!";
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

    public static class ShowKeysCommand extends Command<String> {

        private void showAllKeys() {
            showPrivateKey();
            showPublicKey();
        }

        private void showPrivateKey() {
            if (KeyManager.getPrivateKey() == null) {
                Logger.error("Private key not defined");
            } else {
                Logger.info("Private key: " + Base64.getEncoder().encodeToString(KeyManager.getPrivateKey().getEncoded()));
            }
        }

        private void showPublicKey() {
            if (KeyManager.getPublicKey() == null) {
                Logger.error("Public key not defined");
            } else {
                Logger.info("Public key: " + Base64.getEncoder().encodeToString(KeyManager.getPublicKey().getEncoded()));
            }
        }

        @Override
        public String name() {
            return "show [, public, private]";
        }

        @Override
        protected void handleImpl(String param) {
            if (param == null) {
                showAllKeys();
            } else if (param.equals("public")) {
                showPublicKey();
            } else if (param.equals("private")) {
                showPrivateKey();
            } else {
                Logger.error("undefined key type " + param);
            }
        }

        @Override
        void registerSubCommandsImpl() {}

        @Override
        public String help() {
            return "shows the selected keys";
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
}
