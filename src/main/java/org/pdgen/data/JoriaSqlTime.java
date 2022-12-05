// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;
//MARKER The strings in this file shall not be translated

import org.pdgen.env.Res;
import org.pdgen.model.run.RunEnv;

import java.io.ObjectStreamException;
import java.util.Calendar;

public class JoriaSqlTime extends AbstractJoriaClass {
    private static final long serialVersionUID = 7L;
    protected static JoriaSqlTime myInstance = new JoriaSqlTime();

    protected JoriaSqlTime() {
        members = new JoriaAccess[]{
                new DateMemberField(this, Res.asis("AM_PM"), Calendar.AM_PM) {
                    private static final long serialVersionUID = 7L;

                    public JoriaType getType() {
                        return DefaultStringLiteral.instance();
                    }

                    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) {
                        DBDateTime dt = (DBDateTime) from;
                        Calendar cal = dt.getCalendar();
                        int t = cal.get(field);
                        return new DBStringImpl(this, t == 0 ? Res.asis("AM") : Res.asis("PM"));
                    }
                },
                new DateMemberField(this, Res.asis("Hour12"), Calendar.HOUR),
                new DateMemberField(this, Res.asis("Hour24"), Calendar.HOUR_OF_DAY),
                new DateMemberField(this, Res.asis("Millisecond"), Calendar.MILLISECOND),
                new DateMemberField(this, Res.asis("Minute"), Calendar.MINUTE),
                new DateMemberField(this, Res.asis("Second"), Calendar.SECOND),
        };
        //exp 040507 patrick curtain is added later
        //myCurtain = new CurtainNode(this);
    }

    public String getParamString() {
        return Res.asis("JoriaSqlTime");
    }

    public String getName() {
        return Res.asis("JoriaSqlTime");
    }

    public static JoriaSqlTime instance() {
        return myInstance;
    }

    public boolean isDate() {
        return true;
    }

    protected Object readResolve() throws ObjectStreamException {
        return myInstance;
    }
}
