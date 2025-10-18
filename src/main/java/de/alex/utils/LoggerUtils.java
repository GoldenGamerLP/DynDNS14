package de.alex.utils;

import java.util.logging.*;

public class LoggerUtils {

    private static final StreamHandler handler = new ConsoleStreamHandler();

    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);

        return logger;
    }

    private static class ConsoleStreamHandler extends StreamHandler {

        public ConsoleStreamHandler() {
            super(System.out,new LoggerFormatter());
        }

        @Override
        public void publish(LogRecord record) {
            super.publish(record);
            flush();
        }

        @Override
        public void close() {
            flush();
        }
    }
}
