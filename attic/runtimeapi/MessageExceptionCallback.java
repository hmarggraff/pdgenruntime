package org.pdgen.runtimeapi;

/**
 * User: patrick
 * Date: Sep 26, 2006
 * Time: 7:42:47 AM
 */
public interface MessageExceptionCallback extends MessageCallback
{
    void handle(String text, Throwable exception);
}
