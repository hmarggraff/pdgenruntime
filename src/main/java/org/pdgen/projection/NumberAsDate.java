// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.projection;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.data.view.MutableAccess;
import org.pdgen.data.view.MutableView;
import org.pdgen.data.view.NameableAccess;
import org.pdgen.model.run.RunEnv;

import java.util.Map;

public class NumberAsDate extends MutableAccess {
    public static final String JAVADATE = "JavaDate";
    public static final String POSIXDATE = "PosixDate";
    public static final String NUMBER = "Number";
    public static final String ENUM = "Enumeration";
    private static final long serialVersionUID = 7L;

    protected String dateType;

    private NumberAsDate(JoriaClass parent, JoriaAccess from, String dateType) {
        super(parent, JoriaDateTime.instance(), from);
        this.dateType = dateType;
        makeName();
    }

    public NameableAccess dup(JoriaClass newParent, Map<Object, Object> alreadyCopied) {
        final Object duplicate = alreadyCopied.get(this);
        if (duplicate != null)
            return (NameableAccess) duplicate;

        NumberAsDate ret = new NumberAsDate(newParent, myBaseAccess, dateType);
        alreadyCopied.put(this, ret);

        fillDup(ret, alreadyCopied);
        MutableView newType = (MutableView) alreadyCopied.get(type);
        if (newType == null) {
            newType = ((MutableView) type).dup(alreadyCopied);
        }
        ret.type = newType;
        return ret;
    }

    public void makeName() {
        makeName(dateType);
    }

    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        DBInt iv = (DBInt) getBaseAccess().getValue(from, getBaseAccess(), env);
        if (dateType.equals(POSIXDATE))
            return new DBDateTime(this, iv.getIntValue() * 1000); // convert seconds to milliseconds
        else
            return new DBDateTime(this, iv.getIntValue()); // gets a long and creates a java date
    }

    public boolean isPlain() {
        return false;
    }

    public String getDateType() {
        return dateType;
    }

    public JoriaType getSourceTypeForChildren() {
        return JoriaDateTime.instance();
    }

    public long getIntValue(DBObject from, RunEnv env) throws JoriaDataException {
        throw new JoriaAssertionError("NumberAsDate does not get an int");
    }

    protected NumberAsDate(JoriaClass parent, String name) {
        super(parent, name);
    }

}
