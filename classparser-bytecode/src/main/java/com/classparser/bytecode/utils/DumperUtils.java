package com.classparser.bytecode.utils;

import java.io.PrintStream;
import java.util.regex.Pattern;

/**
 * A hack class for ignoring console messages if options
 * {@link #DUMP_PROPERTY} is enabled
 * Is that option is enabled, then method {@link #ignoreDumpMessages()} should called in start of
 * application, better in static initializer block in main class.
 */
public class DumperUtils {

    private static final String DUMP_PROPERTY = "java.lang.invoke.MethodHandle.DUMP_CLASS_FILES";

    private static final String DUMPER_CLASS = "java.lang.invoke.InvokerBytecodeGenerator";

    private static boolean isMessagesIgnored = false;

    /**
     * Method is enabled ignoring dump messages
     */
    public synchronized static void ignoreDumpMessages() {
        if (!isMessagesIgnored) {
            if (Boolean.getBoolean(DUMP_PROPERTY)) {
                String illegalMessage = "Dumping class files to DUMP_CLASS_FILES/...";
                Pattern messagePattern = Pattern.compile("dump: DUMP_CLASS_FILES\\\\.*?\\.class");

                System.setOut(new PrintStream(System.out) {
                    @Override
                    public void println(String data) {
                        if (isIllegalMessage(data)) {
                            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                            if (isInvokeFromDumper(stackTrace)) {
                                return;
                            }
                        }

                        super.println(data);
                    }

                    private boolean isIllegalMessage(String message) {
                        return message.equals(illegalMessage) || messagePattern.matcher(message).matches();
                    }

                    private boolean isInvokeFromDumper(StackTraceElement[] stackTrace) {
                        return stackTrace.length > 3 && stackTrace[2].getClassName().startsWith(DUMPER_CLASS);
                    }
                });
            }

            isMessagesIgnored = true;
        }
    }
}