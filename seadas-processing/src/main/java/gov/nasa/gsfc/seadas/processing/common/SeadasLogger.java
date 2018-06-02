/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.gsfc.seadas.processing.common;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.esa.snap.core.util.Debug;
import org.esa.snap.core.util.Guardian;
import org.hsqldb.lib.FileUtil;

/**
 *
 * @author aabduraz
 */
public class SeadasLogger {
    
    private static boolean log = true;

    private static Logger logger; // Logger.getLogger(ProcessorModel.class.getName());

    private static String _loggerFileName = "seadasLog";

    /**
     * The log level, must be one of
     * OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL.
     * Default is 'OFF'.
     */

    private static HashMap logLevels;

    static {
        logLevels = new HashMap();
        logLevels.put("OFF", Level.OFF);
        logLevels.put("SEVERE", Level.SEVERE);
        logLevels.put("WARNING", Level.WARNING);
        logLevels.put("INFO", Level.INFO);
        logLevels.put("CONFIG", Level.CONFIG);
        logLevels.put("FINE", Level.FINE);
        logLevels.put("FINER", Level.FINER);
        logLevels.put("FINEST", Level.FINEST);
        logLevels.put("ALL", Level.ALL);
    }

    public static void setLoggerFileName(String loggerFileName) {
        Guardian.assertNotNull("loggerFileName", loggerFileName);

        _loggerFileName = loggerFileName;
    }


    public static Logger getLogger() {
        if (logger == null) {
            initLogger(true);
        }
        return logger;
    }

    public static void initLogger(String loggerFileName, boolean printToConsole) {
        _loggerFileName = loggerFileName;
        if (log) {
            initLogger(printToConsole);
        } else {
            initSevereLogger();
        }
    }

    public static void initLogger(boolean printToConsole) {

        logger = Logger.getLogger("seadas");
        
                FileHandler fileTxt;
        SimpleFormatter formatterTxt = new SimpleFormatter();

        FileHandler fileHTML;
        Formatter formatterHTML = new MyHtmlFormatter();

        logger.setLevel(Level.INFO);
        String txtLogFileName = _loggerFileName + ".txt";
        String htmlLogFileName = _loggerFileName + ".html";
        
        try {
            if (new File(txtLogFileName).exists()) {
                new File(txtLogFileName).delete();
            }
            if (new File(htmlLogFileName).exists()) {
                new File(htmlLogFileName).delete();
            }
            fileTxt = new FileHandler(txtLogFileName);
            fileHTML = new FileHandler(htmlLogFileName);

            File[] files = new File(System.getProperty("user.dir")).listFiles();
            Debug.assertNotNull(files);
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    if ((fileName.indexOf(htmlLogFileName) != -1 && fileName.substring(fileName.indexOf(htmlLogFileName) + htmlLogFileName.length()).trim().length() > 0) ||
                            (fileName.indexOf(txtLogFileName) != -1 && fileName.substring(fileName.indexOf(txtLogFileName) + txtLogFileName.length()).trim().length() > 0)) {
                        file.delete();
                    }
                }
            }
            fileTxt.setFormatter(formatterTxt);
            logger.addHandler(fileTxt);
            fileHTML.setFormatter(formatterHTML);
            logger.addHandler(fileHTML);
        } catch (IOException ioe) {

        }
    }

    public static void initSevereLogger() {
        logger = Logger.getLogger("seadas");
        logger.setLevel(Level.SEVERE);
    }

    public static void deleteLoggerOnExit(boolean delete) {
        if (delete) {
            File txtFile = new File(System.getProperty("user.dir") + _loggerFileName + ".txt");
            if (txtFile.exists()) {
                txtFile.deleteOnExit();
            }
            File xmlFile = new File(System.getProperty("user.dir") + _loggerFileName + ".txt");
            if (xmlFile.exists()) {
                xmlFile.deleteOnExit();
            }
        }
    }

    public static Level convertStringToLogger(String seadasConfigLogLevel) {
        Level log = (Level) logLevels.get(seadasConfigLogLevel.toUpperCase());
        if (log == null) {
            log = Level.OFF;
        }
        return log;
    }
    
       //This custom formatter formats parts of a log record to a single line
    private static class MyHtmlFormatter extends Formatter {
        // This method is called for every log records
        public String format(LogRecord rec) {
            StringBuffer buf = new StringBuffer(1000);
            // Bold any levels >= WARNING
            buf.append("<tr>");
            buf.append("<td>");

            if (rec.getLevel().intValue() >= Level.WARNING.intValue()) {
                buf.append("<b>");
                buf.append(rec.getLevel());
                buf.append("</b>");
            } else {
                buf.append(rec.getLevel());
            }
            buf.append("</td>");

            buf.append("<td>");
            buf.append(calcDate(rec.getMillis()));
            buf.append("</td>");
            buf.append("<td>");
            buf.append(' ');
            buf.append(formatMessage(rec));
            buf.append("</td>");
            buf.append("<td>");
            buf.append(rec.getSourceClassName());
            buf.append("</td>");
            buf.append("<td>");
            buf.append(rec.getSourceMethodName());
            buf.append("</td>");
            buf.append('\n');
            buf.append("</tr>\n");
            return buf.toString();
        }

        private String calcDate(long millisecs) {
            SimpleDateFormat date_format = new SimpleDateFormat("MMM dd,yyyy HH:mm");
            Date resultdate = new Date(millisecs);
            return date_format.format(resultdate);
        }

        // This method is called just after the handler using this
        // formatter is created
        public String getHead(Handler h) {
            return "<HTML>\n<HEAD>\n" + (new Date()) + "\n</HEAD>\n<BODY>\n<PRE>\n"
                    + "<table border>\n  "
                    + "<tr><th>Level</th><th>Time</th><th>Log Message</th><th>In Class</th><th>In Method</th></tr>\n";
        }

        // This method is called just after the handler using this
        // formatter is closed
        public String getTail(Handler h) {
            return "</table>\n  </PRE></BODY>\n</HTML>\n";
        }

    }


}
