// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.DBData;
import org.pdgen.data.DBObject;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaType;
import org.pdgen.env.Env;

import java.text.Collator;
import java.util.Locale;

//User: hmf
//Date: Apr 6, 2002 12:12:42 PM

public class LabeledDBObject implements DBObject, LabeledDBObjectBase {
    DBObject object;
    DBData label;

    public LabeledDBObject(DBObject object, DBData label) {
        this.object = object;
        this.label = label;
    }

    public DBObject getObject() {
        return object;
    }

    public String toString() {
        if (label != null)
            return label.toString();
        else
            return "null"; //trdone
    }

    public JoriaAccess getAccess() {
        return object.getAccess();
    }

    public JoriaType getActualType() {
        return object.getActualType();
    }

    public boolean isAssignableTo(JoriaType t) {
        return object.isAssignableTo(t);
    }

    public boolean isValid() {
        return object.isValid();
    }

    public boolean isNull() {
        return object == null || object.isNull();
    }

    public boolean same(DBData theOther) {
        return object.same(theOther);
    }

    //TODO implement DBLiterals as comparable
    public int compareTo(LabeledDBObjectBase lo) {
        if (label == null || label.isNull()) {
            if (lo.getLabel() == null || lo.getLabel().isNull())
                return 0;
            else
                return -1;
        } else if (lo.getLabel() == null || lo.getLabel().isNull())
            return 1;
        else {
            Locale locales = (Locale) Env.instance().getThreadLocalStorage().getMap().get(Locale.class);
            if (locales == null)
                locales = Env.instance().getCurrentLocale();
            Collator coll = Collator.getInstance(locales);
            return coll.compare(label.toString(), lo.getLabel().toString());
        }
    }

    public DBData getLabel() {
        return label;
    }
}
