package com.alanwgt.console;

import br.ufsm.csi.seguranca.pila.model.Mensagem;
import com.alanwgt.helpers.SocketIOInterface;
import com.alanwgt.helpers.SocketNotConnectedException;
import com.google.gson.Gson;

import java.util.Base64;

public class Logger {

    public static final String DEFAULT_BACKGROUND = ConsoleColors.BLACK_BACKGROUND_BRIGHT;
    private static final String ERROR_COLORS   = DEFAULT_BACKGROUND + ConsoleColors.RED_BRIGHT;
    private static final String WARNING_COLORS = DEFAULT_BACKGROUND + ConsoleColors.YELLOW_BRIGHT;
    private static final String INFO_COLORS    = DEFAULT_BACKGROUND + ConsoleColors.CYAN_BRIGHT;
    private static final String DEBUG_COLORS   = DEFAULT_BACKGROUND + ConsoleColors.PURPLE_BRIGHT;
    private static final String SUCCESS_COLORS = DEFAULT_BACKGROUND + ConsoleColors.BLUE_BRIGHT;

    public static boolean DEBUG = false;
    private static SocketIOInterface ioInterface;
    private static Gson gson = new Gson();

    private Logger() {}

    enum LogType {
        DEBUG,
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
        TERM_RESPONSE,
        TERM_WARNING,
        TERM_ERROR
    }

    public static boolean hasIOInterface() {
        return ioInterface != null;
    }

    public static void setIoInterface(SocketIOInterface ioInterface) {
        Logger.ioInterface = ioInterface;
    }

    public static void error(final String message) {
        print(message, LogType.ERROR, ERROR_COLORS);
    }

    public static void info(final String message) {
        print(message, LogType.INFO, INFO_COLORS);
    }

    public static void warning(final String message) {
        print(message, LogType.WARNING, WARNING_COLORS);
    }

    public static void success(String message) {
        print(message, LogType.SUCCESS, SUCCESS_COLORS);
    }

    public static void termResponse(String message) {
        print(message, LogType.TERM_RESPONSE, SUCCESS_COLORS);
    }

    public static void termWarning(String message) {
        print(message, LogType.TERM_WARNING, WARNING_COLORS);
    }

    public static void termError(String message) {
        print(message, LogType.TERM_ERROR, ERROR_COLORS);
    }

    public static void debug(String... message) {
//        if (!DEBUG) {
//            return;
//        }

        for (String s : message) {
            print(s, LogType.DEBUG, DEBUG_COLORS);
        }
    }

    private static void print(String message, LogType logType, String... opts) {
        StringBuilder stringBuilder = new StringBuilder();

        for (final String opt : opts) {
            stringBuilder.append(opt);
        }

        StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
        String debugLine = ste.getClassName() + ":" + ste.getLineNumber() + " => ";

        if (DEBUG) {
            stringBuilder.append(debugLine);
        }

        stringBuilder.append(message);
        stringBuilder.append(ConsoleColors.RESET);
        System.out.println(stringBuilder.toString());

        if (logType != LogType.DEBUG && ioInterface != null) {
            try {
                if (logType == LogType.TERM_RESPONSE ||
                        logType == LogType.TERM_WARNING ||
                        logType == LogType.TERM_ERROR) {
                    ioInterface.emit("log", gson.toJson(new LogMessage(message, logType)));
                } else {
                    ioInterface.emit("log", gson.toJson(new LogMessage(debugLine + message, logType)));
                }
            } catch (SocketNotConnectedException e) {
                System.err.println("Couldn't emit message to IO interface");
            }
        }
    }

    public static void log(Mensagem m) {
        try {
            print("Origin ID: " + m.getIdOrigem(), LogType.INFO, DEFAULT_BACKGROUND, ConsoleColors.GREEN_BOLD);
            print("IsMaster?  " + m.isMaster(), LogType.INFO, DEFAULT_BACKGROUND, ConsoleColors.GREEN_BOLD);
            print("Type:      " + m.getTipo(), LogType.INFO, DEFAULT_BACKGROUND, ConsoleColors.GREEN_BOLD);
            print("Address:   " + m.getEndereco(), LogType.INFO, DEFAULT_BACKGROUND, ConsoleColors.GREEN_BOLD);
            print("Signature: " + Base64.getEncoder().encodeToString(m.getAssinatura()), LogType.INFO, DEFAULT_BACKGROUND, ConsoleColors.GREEN_BOLD);
        } catch (NullPointerException __) {}
    }

    static class LogMessage {

        public String message;
        public String logType;

        public LogMessage(String message, LogType logType) {
            this.message = message;
            this.logType = logType.name();
        }
    }
}
