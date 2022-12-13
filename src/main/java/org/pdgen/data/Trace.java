// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.env.Env;
import org.pdgen.util.Log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Trace {
    //MARKER The strings in this file shall not be translated
    static int traceLevel = 3;

    private static final int debugLogLevel = 4;
    private static final int infoLogLevel = 3;
    private static final int warnLogLevel = 2;
    public static final int errorLogLevel = 1;
    public static final int fatalLogLevel = 0;
    public static final int compiledLogLevel = 4;

    public static final long other = 1;
    public static final long schema = 1 << 1;
    public static final long variables = 1 << 2;
    public static final long fill = 1 << 3;
    public static final long transaction = 1 << 4;
    public static final long layout = 1 << 5;
    public static final long template = 1 << 6;
    public static final long browser = 1 << 7;
    public static final long dialogs = 1 << 8;
    public static final long edit = 1 << 9; // any edit action via properties or dialog
    public static final long init = 1 << 10;
    public static final long view = 1 << 11;
    public static final long action = 1 << 12;
    public static final long dnd = 1 << 13;
    public static final long run = 1 << 14;
    public static final long ui = 1 << 15; // laf of the app
    public static final long serialize = 1 << 16;
    public static final long dump = 1 << 17;
    public static final long wizards = 1 << 18; // setup and behaviour of wizards
    public static final long actionlog = 1 << 19;
    public static final long memory = 1 << 20;
    public static final long mapdir = 1 << 21;
    public static final long stepper = 1 << 22;
    public static final long copy = 1 << 23;
    public static final long paint = 1 << 24;
    public static final long baseInit = 1 << 25;
    public static final long undo = 1 << 26;
    static long modules = baseInit;
    protected static PrintStream logStream = System.out;
    protected static ZipOutputStream logDeflater;
    static final String[] mapModules = { //
            "other", //trdone
            "schema", //trdone
            "variables", //trdone
            "fill", //trdone
            "transaction", //trdone
            "layout", //trdone
            "template", //trdone
            "browser",
            "dialogs",
            "edit",
            "init",
            "view",
            "action",
            "dnd",
            "run",
            "ui",
            "style",
            "serialize",
            "dump", "wizards", "actionlog", "memory",
            "mapdir", "stepper", "copy", "paint",
            "baseInit"};
    static final Vector<String> actionLog = new Vector<String>();

    /*
     * add the string to the action log
     * the action log lists all relevant user actions, so that there is a chance of recreating
     * the session.
     */
    public static void action(String txt) {
        if (actionLog.size() >= 1000) {
            for (int i = 100; i < 1000; i++) {
                actionLog.set(i - 100, actionLog.get(i));
            }
            actionLog.setSize(900);
        }
        actionLog.add(txt);
        Log.action.debug(txt);
    }

    /*
     * Utility function to get the name of a JoriaAccess within its declaring class
     */
    public static String axs(JoriaAccess ja) {
        if (ja.getDefiningClass() != null)
            return ja.getDefiningClass().getName() + '.' + ja.getName();
        else
            return ja.getName();
    }

    public static Vector<String> getActionLog() {
        return actionLog;
    }

    public static void addModule(String s) {
        s = s.trim();
        for (int i = 0; i < mapModules.length; i++) {
            if (mapModules[i].equalsIgnoreCase(s)) {
                logStream.println("Now tracing module: " + s);
                modules |= 1 << i;
                return;
            }
        }
        logStream.println("Failed tracing module: " + s);
    }

    public static void addModule(long m) {
        modules |= m;
    }

    public static void delModule(String s) {
        for (int i = 0; i < mapModules.length; i++) {
            logStream.println("Stop tracing module: " + s);
            if (mapModules[i].equalsIgnoreCase(s)) {
                modules &= ~i;
                return;
            }
        }
        logStream.println("Failed to stop tracing module: " + s);
    }

    public static long getModules() {
        return modules;
    }

    public static int getTraceLevel() {
        return traceLevel;
    }

    public static void log(long module, String message) {
        // noinspection ConstantConditions
        if (compiledLogLevel >= infoLogLevel && traceLevel >= infoLogLevel && ((modules & module) != 0))
            logStream.println(message);
    }

    public static void logWarn(String message) {
        // noinspection ConstantConditions
        if (compiledLogLevel >= warnLogLevel && warnLogLevel <= traceLevel) {
            printWithSource(message);
        }
    }

    private static void printWithSource(String message) {
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[1];
        String className = stackTraceElement.getClassName();
        String methodName = stackTraceElement.getMethodName();
        int lineNumber = stackTraceElement.getLineNumber();
        String source = className + "." + methodName + ":" + lineNumber + " " + message;
        logStream.println(source);

    }

    public static void logError(String message) {
        // noinspection ConstantConditions
        if (compiledLogLevel >= errorLogLevel && errorLogLevel <= traceLevel)
            printWithSource(message);
    }

    public static void logDebug(long module, String message) {
        // noinspection ConstantConditions
        if (compiledLogLevel >= debugLogLevel && debugLogLevel <= traceLevel && ((modules & module) != 0))
            logStream.println(message);
    }


    public static boolean isLog(long module) {
        return (modules & module) != 0;
    }

    public static void log(String message) {
        logStream.println(message);
    }

    public static void check(Object param) {
        if (param == null) {
            Throwable t = new JoriaAssertionError("Null not allowed here");
            Env.instance().handle(t);
        }
    }

    public static void traceNull(Object param) {
        if (param == null) {
            Throwable t = new JoriaAssertionError("Null not allowed here");//trdone
            t.printStackTrace();
            System.currentTimeMillis();
        }
    }

    public static void check(Object param, String msg) {
        if (param == null) {
            Throwable t = new JoriaAssertionError(msg + " may not be null.");
            Env.instance().handle(t);
        }
    }

    public static void check(boolean cond, String msg) {
        if (!cond) {
            Throwable t = new JoriaAssertionError(msg);
            Env.instance().handle(t);
        }
    }

    public static void check(boolean cond) {
        if (!cond) {
            Throwable t = new JoriaAssertionError("Assertion failed");
            Env.instance().handle(t);
        }
    }

    public static void check(Object o, Class<?> c) {
        if (!c.isInstance(o)) {
            Throwable t = new JoriaAssertionError("Cast assertion for " + c + " failed. Object(" + o.toString() + " of " + o.getClass());
            Env.instance().handle(t);
        }
    }

    public static void setModules(long tl) {
        modules = tl;
    }

    public static void setTraceLevel(int tl) {
        traceLevel = tl;
        logStream.println("TraceLevel now=" + traceLevel);
    }

    public static void log(Throwable ex) {
        if (traceLevel <= 2)
            return;
        logStream.println(ex.getMessage());
        ex.printStackTrace(logStream);
    }

    public static void closeLogStream() throws IOException {
        if (logStream != System.out) {
            if (logDeflater != null)
                logDeflater.closeEntry();
            logStream.close();
        }
    }

    public static void setLogStream(String newLogStream) {
        try {
            File lf = new File(newLogStream);
            OutputStream ls;
            if (lf.isDirectory()) {
                SimpleDateFormat sdf = new SimpleDateFormat("'rwlog'yyMMddHHmmss'.txt'");
                String en = sdf.format(new Date());
                File theFile = new File(lf, en);
                ls = new FileOutputStream(theFile);
            } else if (newLogStream.endsWith(".zip")) {
                logDeflater = new ZipOutputStream(new FileOutputStream(newLogStream));
                SimpleDateFormat sdf = new SimpleDateFormat("'rwlog'yyMMddHHmmss'.txt'");
                String en = sdf.format(new Date());
                ZipEntry ze = new ZipEntry(en);
                logDeflater.putNextEntry(ze);
                ls = logDeflater;
            } else
                ls = new FileOutputStream(newLogStream);
            logStream = new PrintStream(ls);
        } catch (IOException ex) {
            System.err.println("Could not init " + newLogStream + " for logging (using System.out instead): " + ex.getMessage());
            logStream = System.out;
        }
    }

    public static void logMem(String s) {
        if (compiledLogLevel >= infoLogLevel && traceLevel >= infoLogLevel && ((modules & memory) != 0))
            log(memory, s + " Memory used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 + "/" + Runtime.getRuntime().totalMemory() / 1024);
    }

    public static void logGC(String s) {
        if (compiledLogLevel >= infoLogLevel && traceLevel >= infoLogLevel && ((modules & memory) != 0)) {
            final long uMem1 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024;
            final long tMem1 = Runtime.getRuntime().totalMemory() / 1024;
            System.gc();
            final long uMem2 = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024;
            log(memory, s + " Memory used: " + uMem1 + "/" + tMem1 + " freed " + (uMem1 - uMem2));
        }
    }

    public static String[] moduleNames() {
        return mapModules;
    }

    public static void breakHere() {
    }

    public static void log(final Throwable ex, final String message) {
        if (traceLevel <= 2)
            return;
        logStream.println(message);
        logStream.println(ex.getMessage());
        ex.printStackTrace(logStream);
    }
}
