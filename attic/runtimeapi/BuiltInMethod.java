package org.pdgen.runtimeapi;

/**
 * This interface is used to implement user built in functions without locales..
 * User: patrick
 * Date: Nov 7, 2006
 * Time: 9:29:43 AM
 */
public interface BuiltInMethod extends BuiltInMethodBase
{
    /**
     * This method is called to execute the built in function
     * @param from          scope object, normaly not used
     * @param parameters    the array of the parameters values.
     * @return              the return value of the built in function
     */
    Object executeBuiltIn(Object from, Object[] parameters);
}
