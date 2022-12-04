package org.pdgen.runtimeapi;

/**
 * This base interface is used to implement user built in functions.
 * User: patrick
 * Date: Dec 6, 2006
 * Time: 12:52:33 PM
 */
public interface BuiltInMethodBase
{
    /**
     * This method has to return the name of type of the runtime return value.
     * @return the name of the type
     */
    String getReturnType();

    /**
     * This methods checks during parsing of the formula, if the number and the type of the arguments can be used.
     * @param parameterTypes array of the names of the argument types.
     * @return is the parameter types are usable
     */
    boolean checkParameterTypes(String [] parameterTypes);
}
