package org.pdgen.runtimeapi;

import java.util.Locale;

/**
 * This interface is used to implement user built in functions with locales.
 * User: patrick
 * Date: Dec 6, 2006
 * Time: 12:50:17 PM
 */
public interface BuiltInMethodLocale extends BuiltInMethodBase
{
    /**
     * This method is called to execute the built in function
     * @param from          scope object, normaly not used
     * @param parameters    the array of the parameters values.
     * @param locale        the locale for the current report
     * @return              the return value of the built in function
     */
    Object executeBuiltIn(Object from, Object[] parameters, Locale locale);
}
