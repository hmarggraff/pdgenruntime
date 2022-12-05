// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

/**
 * User: patrick
 * Date: Feb 7, 2007
 * Time: 8:20:38 AM
 */
public class EnumRes<T extends Enum<?>> {
    public T inner;

    public EnumRes(T inner) {
        this.inner = inner;
    }

    public String toString() {
        return stringOf(inner);
    }

    public static String stringOf(Enum<?> inner) {
        String className = inner.getClass().getName();
        if (className.contains("."))
            className = className.substring(className.lastIndexOf('.') + 1);
        return Res.str(className + "." + inner);
    }

}
