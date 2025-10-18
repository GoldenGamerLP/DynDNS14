package de.alex.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LoggerFormatter extends Formatter {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        // Format the log message
        sb.append(dateFormat.format(new Date(record.getMillis()))).append(" ");
        sb.append("[").append(record.getLevel()).append("] ");
        sb.append(record.getSourceClassName()).append(".");
        sb.append(record.getSourceMethodName()).append(" - ");
        sb.append(formatMessage(record)).append("\n");

        // Add the thrown exception stack trace if available
        if (record.getThrown() != null) {
            sb.append("\t");
            sb.append(getStackTraceAsString(record.getThrown()));
        }

        return sb.toString();
    }

    private String getStackTraceAsString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

}
