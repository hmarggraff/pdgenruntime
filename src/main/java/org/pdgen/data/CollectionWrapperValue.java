// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.ClassProjection;

import java.lang.reflect.Method;

public class CollectionWrapperValue extends AbstractDBObject {

    private final DBData collCache;
    private final Method method;

    public CollectionWrapperValue() {
        collCache = null;
        method = null;
    }

    public CollectionWrapperValue(JoriaAccess asView, DBData collCache, Method method) {
        super(asView);
        this.collCache = collCache;
        this.method = method;
    }

    public boolean isNull() {
        return false; // never true
    }

    public boolean same(DBData theOther) {
        if (theOther instanceof CollectionWrapperValue) {
            CollectionWrapperValue cwv = (CollectionWrapperValue) theOther;
            return cwv.myAccess.equals(myAccess);
        } else {
            return false;
        }
    }

    public boolean isAssignableTo(JoriaType t) {
        JoriaType actualType = getActualType();
        while (actualType instanceof ClassProjection)
            actualType = ((ClassProjection) actualType).getBase();
        return t == actualType;
    }

    public Object getValue() throws JoriaDataException {
        if (method == null)
            return null;
        Object ret = null;
        try {
            ret = method.invoke(collCache);
        } catch (Throwable e) {
            throw new JoriaDataException("getting collection root", e);
        }
        return ret;
    }
}
