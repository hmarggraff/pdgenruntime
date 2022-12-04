// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.model.run.RunEnv;
import org.pdgen.env.Res;

import java.io.ObjectStreamException;
import java.util.Calendar;

public class JoriaDateTime extends AbstractJoriaClass
{
    private static final long serialVersionUID = 7L;
    protected static JoriaDateTime myInstance = new JoriaDateTime();

	protected JoriaDateTime()
	{
		members = new JoriaAccess[]{new DateMemberField(this, Res.asis("AM_PM"), Calendar.AM_PM)
		{
            private static final long serialVersionUID = 7L;

            public JoriaType getType()
			{
				return DefaultStringLiteral.instance();
			}

			public DBData getValue(DBData from, JoriaAccess asView, RunEnv env)
			{
				DBDateTime dt = (DBDateTime) from;
				Calendar cal = dt.getCalendar();
				int t = cal.get(field);
				return new DBStringImpl(this, t == 0 ? Res.asis("AM") : Res.asis("PM"));
			}
		}, new DateMemberField(this, Res.asis("DayOfMonth"), Calendar.DAY_OF_MONTH), new DateMemberField(this, Res.asis("DayOfWeek"), Calendar.DAY_OF_MONTH), new DateMemberField(this, Res.asis("DayOfYear"), Calendar.DAY_OF_YEAR), new DateMemberField(this, Res.asis("Hour12"), Calendar.HOUR), new DateMemberField(this, Res.asis("Hour24"), Calendar.HOUR_OF_DAY), new DateMemberField(this, Res.asis("Millisecond"), Calendar.MILLISECOND), new DateMemberField(this, Res.asis("Minute"), Calendar.MINUTE), new DateMemberField(this, Res.asis("Month"), Calendar.MONTH), new DateMemberField(this, Res.asis("Second"), Calendar.SECOND), new DateMemberField(this, Res.asis("WeekOfYear"), Calendar.WEEK_OF_YEAR), new DateMemberField(this, Res.asis("Year"), Calendar.YEAR), new DateMemberField(this, Res.asis("YearMonth"), Calendar.MILLISECOND)
		{
            private static final long serialVersionUID = 7L;

            public DBData getValue(DBData from, JoriaAccess asView, RunEnv env)
			{
				DBDateTime dt = (DBDateTime) from;
				Calendar cal = dt.getCalendar();
				int y = cal.get(Calendar.YEAR);
				int m = cal.get(Calendar.MONTH);
				return new DBStringImpl(this, y + (m < 9 ? ".0" : ".") + (m + 1));
			}

			public JoriaType getType()
			{
				return DefaultStringLiteral.instance();
			}
		}, new DateMemberField(this, Res.asis("Quarter"), Calendar.MILLISECOND)
		{
            private static final long serialVersionUID = 7L;

            public DBData getValue(DBData from, JoriaAccess asView, RunEnv env)
			{
				DBDateTime dt = (DBDateTime) from;
				Calendar cal = dt.getCalendar();
				int m = cal.get(Calendar.MONTH);
				return new DBIntImpl(this, ((m) / 3 + 1));
			}
		}, new DateMemberField(this, Res.asis("YearQuarter"), Calendar.MILLISECOND)
		{
            private static final long serialVersionUID = 7L;

            public DBData getValue(DBData from, JoriaAccess asView, RunEnv env)
			{
				DBDateTime dt = (DBDateTime) from;
				Calendar cal = dt.getCalendar();
				int y = cal.get(Calendar.YEAR);
				int m = cal.get(Calendar.MONTH);
				final int q0 = (m) / 3;
				final int q = q0 + 1;
				return new DBStringImpl(this, y + "." + q);
			}

			public JoriaType getType()
			{
				return DefaultStringLiteral.instance();
			}
		}, new DateMemberField(this, Res.asis("Seconds1970"), Calendar.MILLISECOND)
		{
            private static final long serialVersionUID = 7L;

            public DBData getValue(DBData from, JoriaAccess asView, RunEnv env)
			{
				DBDateTime dt = (DBDateTime) from;
				Calendar cal = dt.getCalendar();
				int t = cal.get(Calendar.MILLISECOND);
				return new DBIntImpl(this, t / 1000);
			}
		},};
		//exp 040507 patrick curtain is added later
		//myCurtain = new ProjectionHolder(this);
	}

	public String getParamString()
	{
		return Res.str("JoriaDateTime");
	}

	public String getName()
	{
		return Res.str("JoriaDateTime");
	}

	public static JoriaDateTime instance()
	{
		return myInstance;
	}

	public boolean isDate()
	{
		return true;
	}

	protected Object readResolve() throws ObjectStreamException
	{
		return myInstance;
	}
}
