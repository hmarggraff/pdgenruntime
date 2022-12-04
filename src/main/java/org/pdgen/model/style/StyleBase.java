// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import org.pdgen.data.NameableProtectedRemove;
import org.pdgen.data.NameableTracer;
import org.pdgen.data.SortedNamedVector;
import org.pdgen.data.Trace;

import java.io.ObjectStreamException;
import java.io.Serializable;

public abstract class StyleBase implements NameableProtectedRemove, Serializable {
    public static final Integer FillNone = 0;
    public static final Integer FillHorizontal = 1;
    public static final Integer FillVertical = 2;
    public static final Integer FillBoth = 3;
    public static final Integer FillSymmetric = 4;
    public static final Integer ONE = 1;
    private static final long serialVersionUID = 7L;
    protected String name;

    protected StyleBase() {
    }

    protected StyleBase(String name) {
        this.name = name;
    }

    public void setName(String newName) {
        NameableTracer.notifyListenersPre(this);
        name = newName;
        NameableTracer.notifyListenersPost(this);
    }

    public boolean isDefault() {
        return name.startsWith("*");
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }

    public static boolean eq(Object s, Object t) {
        if (s == null)
            return t == null;
        else {
            //noinspection SimplifiableIfStatement
            if (t == null)
                return false;
            return s.equals(t);
        }
    }

    public static boolean eqLog(String what, Object s, Object t) {
        if (s == null)
            return returnLog(what, s, t, t == null);
        else {
            if (t == null)
                return returnLog(what, s, t, false);
            return returnLog(what, s, t, s.equals(t));
        }
    }

    static boolean returnLog(String what, Object s, Object t, boolean result) {
        if (!result) {
            Trace.log(Trace.copy, "Found diff in " + what + ": " + s + " = " + t);
        }
        return result;
    }

    protected static StyleBase getLocalMaster(StyleBase from, boolean really) {
        if (!really)
            return from;
        if (from == null)
            return null;
        else
            return from;
    }

    protected Object readResolve() throws ObjectStreamException {
        storeSpecialStyle();
        return this;
    }

    public boolean hasName() {
        return name != null;
    }

    protected abstract void storeSpecialStyle();

    public abstract SortedNamedVector<?> getGlobalStyleList();

    public boolean isRemoveable() {
        return name == null || name.length() == 1 || name.charAt(0) != '*';
    }
}
