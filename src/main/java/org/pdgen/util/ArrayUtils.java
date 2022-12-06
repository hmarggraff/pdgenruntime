// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.util;

import java.lang.reflect.Array;

/**
 * Created by IntelliJ IDEA.
 * User: hmf
 * Date: 26.08.2006
 * Time: 17:48:21
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("unchecked")
public class ArrayUtils {
    public static <T> T[] remove(T[] a, int ix) {
        T[] ret = (T[]) Array.newInstance(a.getClass().getComponentType(), a.length - 1);
        System.arraycopy(a, 0, ret, 0, ix);
        System.arraycopy(a, ix + 1, ret, ix, ret.length - ix);
        return ret;
    }

    public static Object[] add(Object[] a, int ix, Object o) {
        Object[] ret = (Object[]) Array.newInstance(a.getClass().getComponentType(), a.length + 1);
        System.arraycopy(a, 0, ret, 0, ix);
        System.arraycopy(a, ix, ret, ix + 1, a.length - ix);
        ret[ix] = o;
        return ret;
    }

    public static Object[] add(Object[] a, Object o) {
        Object[] ret = (Object[]) Array.newInstance(a.getClass().getComponentType(), a.length + 1);
        System.arraycopy(a, 0, ret, 0, a.length);
        ret[a.length] = o;
        return ret;
    }

    public static <T> T[] remove(T[] a, int from, int to) {
        int len = to - from;
        if (a == null || a.length == 0 || len <= 0)
            return a;
        T[] ret = (T[]) Array.newInstance(a.getClass().getComponentType(), a.length - len);
        System.arraycopy(a, 0, ret, 0, from);
        int rem = a.length - to;
        System.arraycopy(a, from + len, ret, from, rem);
        return ret;
    }

    public static <T> T[] addArray(final T[] a, final int ix, final T[] ins) {
        T[] ret = (T[]) Array.newInstance(a.getClass().getComponentType(), a.length + ins.length);
        System.arraycopy(a, 0, ret, 0, ix);
        System.arraycopy(a, ix, ret, ix + ins.length, a.length - ix);
        System.arraycopy(ins, 0, ret, ix, ins.length);
        return ret;
    }
}
