package org.pdgen.runtimeapi;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Feb 16, 2005
 * Time: 7:03:55 PM
 * To change this template use File | Settings | File Templates.
 */
public interface JoriaSerializableFactory
{
    String serialize(Object obj);
    Object unserialize(String str);
}
