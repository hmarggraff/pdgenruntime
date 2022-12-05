// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.model.run.RunEnv;

import java.util.Calendar;
import java.util.Date;

public class DateMemberField extends AbstractMember {
    private static final long serialVersionUID = 7L;
    int field;

    public DateMemberField(JoriaClass parent, String name, int field) {
        super(parent, name);
        this.field = field;
        makeLongName();
    }

    public JoriaType getType() {
        return DefaultIntLiteral.instance();
    }

    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        DBDateTime dt = (DBDateTime) from;
        Date d = dt.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int t = cal.get(field);
        return new DBIntImpl(this, t);
    }

    protected Object readResolve() {
        return JoriaDateTime.instance().findMember(name);
    }
}
