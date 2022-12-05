// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;
//MARKER The strings in this file shall not be translated

import org.pdgen.env.Res;
import org.pdgen.model.run.RunEnv;

import java.io.ObjectStreamException;
import java.util.Calendar;

public class JoriaSqlDate extends AbstractJoriaClass {
    private static final long serialVersionUID = 7L;
    protected static JoriaSqlDate myInstance = new JoriaSqlDate();

    protected JoriaSqlDate() {
        members = new JoriaAccess[]{
                new DateMemberField(this, Res.asis("DayOfMonth"), Calendar.DAY_OF_MONTH),
                new DateMemberField(this, Res.asis("DayOfWeek"), Calendar.DAY_OF_WEEK),
                new DateMemberField(this, Res.asis("DayOfYear"), Calendar.DAY_OF_YEAR),
                new DateMemberField(this, Res.asis("Month"), Calendar.MONTH),
                new DateMemberField(this, Res.asis("WeekOfYear"), Calendar.WEEK_OF_YEAR),
                new DateMemberField(this, Res.asis("Year"), Calendar.YEAR),
                new DateMemberField(this, Res.asis("YearMonth"), Calendar.MILLISECOND) {
                    private static final long serialVersionUID = 7L;

                    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) {
                        DBDateTime dt = (DBDateTime) from;
                        Calendar cal = dt.getCalendar();
                        int y = cal.get(Calendar.YEAR);
                        int m = cal.get(Calendar.MONTH);
                        String s = Integer.toString(m + 1);
                        return new DBStringImpl(this, y + (m < 9 ? ".0" : ".") + s);
                    }

                    public JoriaType getType() {
                        return DefaultStringLiteral.instance();
                    }
                },
                new DateMemberField(this, Res.asis("Quarter"), Calendar.MILLISECOND) {
                    private static final long serialVersionUID = 7L;

                    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) {
                        DBDateTime dt = (DBDateTime) from;
                        Calendar cal = dt.getCalendar();
                        int m = cal.get(Calendar.MONTH);
                        return new DBStringImpl(this, Integer.toString((m + 1) / 4 + 1));
                    }
                },
                new DateMemberField(this, Res.asis("YearQuarter"), Calendar.MILLISECOND) {
                    private static final long serialVersionUID = 7L;

                    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) {
                        DBDateTime dt = (DBDateTime) from;
                        Calendar cal = dt.getCalendar();
                        int y = cal.get(Calendar.YEAR);
                        int m = cal.get(Calendar.MONTH);
                        String s = Integer.toString((m + 1) / 4 + 1);
                        return new DBStringImpl(this, y + "." + s);
                    }

                    public JoriaType getType() {
                        return DefaultStringLiteral.instance();
                    }
                },
        };
        //exp 040507 patrick curtain is added later
        //myCurtain = new CurtainNode(this);
    }

    public String getParamString() {
        return Res.asis("JoriaSqlDate");
    }

    public String getName() {
        return Res.asis("JoriaSqlDate");
    }

    public static JoriaSqlDate instance() {
        return myInstance;
    }

    public boolean isDate() {
        return true;
    }

    protected Object readResolve() throws ObjectStreamException {
        return myInstance;
    }
}
