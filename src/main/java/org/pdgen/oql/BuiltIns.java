// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.data.view.ClassView;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;
import org.pdgen.model.run.RunEnvImpl;
import org.pdgen.projection.PseudoAccess;
import org.pdgen.env.JoriaInternalError;
import org.pdgen.env.JoriaUserDataException;
import org.pdgen.util.RomanNumeral;
import org.pdgen.util.TextNumeral;

import org.pdgen.env.Env;
import org.pdgen.env.Res;
import org.pdgen.env.Settings;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuiltIns {
    private static final String envGroup = Res.str("env");
    private static final String strGroup = Res.str("string");
    private static final String convGroup = Res.str("conversions");
    private static final String logicGroup = Res.str("logic");
    private static final String mathGroup = Res.str("math");
    private static final String collGroup = Res.str("collections");
    private static final String datimGroup = Res.str("date_time");
    private static final Exec.ExecIntegerAbstract biPage = new Exec.ExecIntegerAbstract("page", envGroup, Res.str("Current_page_number"))//trdone
    {
        public long execute(RunEnv env, DBData from, DBData[] args) {
            return env.getDisplayPageNo() + 1;
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a == null || a.length == 0)
                return getDefaultType();
            else
                return null;
        }

        public boolean isPageRelative() {
            return true;
        }
    };
    private static final Exec.ExecIntegerAbstract biTotalPages = new Exec.ExecIntegerAbstract("totalpages", envGroup, Res.str("Total_page_count"))//trdone
    {
        public long execute(RunEnv env, DBData from, DBData[] args) {
            return env.getTotalPagesNumber() == 0 ? 99999 : env.getTotalPagesNumber();
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a == null || a.length == 0)
                return getDefaultType();
            else
                return null;
        }

        public boolean isPageRelative() {
            return true;
        }

        public boolean isNeedsAllPages() {
            return true;
        }
    };
    private static final Exec.ExecIntegerAbstract biAbs = new Exec.ExecIntegerAbstract("abs", mathGroup, Res.str("Absolute_value_of_number"))//trdone
    {
        public long execute(RunEnv env, DBData from, DBData[] args) {
            if (!args[0].getAccess().getType().isIntegerLiteral())
                throw new JoriaAssertionError("BuiltinAbsInt executed but argument is not an int");
            if (from == null || from.isNull())
                return DBInt.NULL;
            return Math.abs(((DBInt) args[0]).getIntValue());
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length != 1)
                return null;
            NodeInterface t = a[0];
            if (t.isReal())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecIntegerAbstract biStepCounter = new Exec.ExecIntegerAbstract("stepCounter", envGroup, Res.str("increments_the_named_counter_and_returns_its_value"))//trdone
    {
        public long execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            DBData dbCName = args[0];
            if (dbCName == null || dbCName.isNull() || !dbCName.getAccess().getType().isStringLiteral())
                throw new JoriaUserDataException(Res.str("Argument_to_builtin_stepCounter_may_not_be_null"));
            String cName = args[0].toString();
            RuntimeParameter runtimeParameter = Env.instance().repo().variables.find(cName);
            if (runtimeParameter == null) {
                runtimeParameter = new RuntimeParameterLiteral(cName, DefaultIntLiteral.instance());
            }

            DBData counter = env.getRuntimeParameterValue(runtimeParameter);
            if (counter == null) {
                counter = new DBIntImplMutable(runtimeParameter,0);
                env.putRuntimeParameter(runtimeParameter, counter);
            }
            if (!(counter instanceof DBIntImplMutable))
                throw new JoriaInternalError("Value of Counter " + cName + " is not mutable.");

            DBIntImplMutable mCounter = (DBIntImplMutable) counter;
            mCounter.setValuet(mCounter.getIntValue()+1);
            return mCounter.getIntValue();
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length != 1)
                return null;
            NodeInterface t = a[0];
            if (t.isString())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecIntegerAbstract biSetCounter = new Exec.ExecIntegerAbstract("setCounter", envGroup, Res.str("SetCounterHelp"))//trdone
    {
        public long execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            DBData dbCName = args[0];
            if (dbCName == null || dbCName.isNull() || !dbCName.getAccess().getType().isStringLiteral())
                throw new JoriaUserDataException(Res.str("Argument_to_builtin_stepCounter_may_not_be_null"));
            long newVal = 0;
            if (args.length == 2) {
                DBData newValArg = args[1];
                if (!newValArg.getAccess().getType().isIntegerLiteral())
                    throw new JoriaUserDataException(Res.str("Argument_to_builtin_setCounter_must_be_int"));
                newVal = ((DBInt) newValArg).getIntValue();
            }

            String cName = args[0].toString();
            RuntimeParameter runtimeParameter = Env.instance().repo().variables.find(cName);
            if (runtimeParameter == null) {
                runtimeParameter = new RuntimeParameterLiteral(cName, DefaultIntLiteral.instance());
            }

            DBData counter = env.getRuntimeParameterValue(runtimeParameter);
            if (counter == null) {
                counter = new DBIntImplMutable(runtimeParameter,newVal);
                env.putRuntimeParameter(runtimeParameter, counter);
            }
            if (!(counter instanceof DBIntImplMutable))
                throw new JoriaInternalError("Value of Counter " + cName + " is not mutable.");

            DBIntImplMutable mCounter = (DBIntImplMutable) counter;
            mCounter.setValuet(newVal);
            return mCounter.getIntValue();
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length < 1 || a.length > 2 || !a[0].isString())
                return null;
            if (a.length == 2 && !a[1].isInteger())
                return null;
            return getDefaultType();
        }
    };

    private static final Exec.ExecDBData biFirst = new Exec.ExecDBDataAbstract("first", collGroup, Res.str("returns_the_first_element_of_the_collection"))//trdone
    {
        public DBData execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return null;
            DBCollection dbc = (DBCollection) args[0];
            return dbc.pick();
        }

        public JoriaType getType(NodeInterface[] args) {
            if (args == null || !(args[0] instanceof JoriaTypedNode))
                return null;
            return ((JoriaTypedNode) args[0]).getElementType();
        }
    };
    private static final Exec.ExecDBData biLast = new Exec.ExecDBDataAbstract("last", collGroup, Res.str("returns_the_last_element_of_the_collection"))//trdone
    {
        public DBData execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return null;
            DBCollection dbc = (DBCollection) args[0];
            return dbc.pick();
        }

        public JoriaType getType(NodeInterface[] args) {
            if (args == null || !(args[0] instanceof JoriaTypedNode))
                return null;
            return ((JoriaTypedNode) args[0]).getElementType();
        }
    };
    private static final Exec.ExecDate biSecondsToDate = new Exec.ExecDateAbstract("secondsToDate", convGroup, Res.str("converts_a_number_representing_the_seconds_since_to_a_date_object"))//trdone
    {
        public DBData execute(RunEnv env, DBData from, DBData[] args) {
            if (args[0] == null || args[0].isNull())
                return null;
            if (args[0].getAccess().getType().isIntegerLiteral()) {
                Date d = new Date(((DBInt) args[0]).getIntValue());
                return new DBDateTime(new PseudoAccess(JoriaDateTime.instance()), d);
            }
            throw new JoriaAssertionError("Builtin secondsToDate executed but argument is not an int");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 && a[0].isInteger())
                return JoriaDateTime.instance();
            return null;
        }
    };
    private static final Exec.ExecIntegerAbstract biDateToMilliSeconds = new Exec.ExecIntegerAbstract("dateToMilliseconds", convGroup, Res.str("dateToSecondsHint"))//trdone
    {
        public long execute(RunEnv env, DBData from, DBData[] args) {
            if (args[0] == null || args[0].isNull())
                return DBInt.NULL;
            if (args[0].getAccess().getType().isDate()) {
                Date d = ((DBDateTime) args[0]).getDate();
                return d.getTime();
            }
            throw new JoriaAssertionError("Builtin secondsToDate executed but argument is not an int");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 && a[0].isDate())
                return getDefaultType();
            return null;
        }
    };

    private static final Exec.ExecString biUser = new Exec.ExecStringAbstract("user", envGroup, Res.str("returns_the_current_user_name")) {
        @Override
        public String execute(final RunEnv env, final DBData from, final DBData[] args) throws JoriaDataException {
            return Env.instance().getCurrentUserName();
        }

        @Override
        public JoriaType getType(final NodeInterface[] a) {
            if (a.length == 0) {
                return getDefaultType();
            }
            return null;
        }
    };

    private static final Exec.ExecString biLocale = new Exec.ExecStringAbstract("locale", envGroup, Res.str("returns_the_current_locale")) {
        @Override
        public String execute(final RunEnv env, final DBData from, final DBData[] args) throws JoriaDataException {
            return env.getLocale().toString();
        }

        @Override
        public JoriaType getType(final NodeInterface[] a) {
            if (a.length == 0) {
                return getDefaultType();
            }
            return null;
        }
    };

    private static final Exec.ExecDate biDate = new Exec.ExecDateAbstract("date", datimGroup, Res.str("returns_the_current_date_and_time_in_a_date_object"))//trdone
    {
        public DBData execute(RunEnv env, DBData from, DBData[] args) {
            PseudoAccess pseudoAccess = new PseudoAccess(JoriaDateTime.instance(), "date");//trdone
            if (args == null || args.length == 0) {
                return new DBDateTime(pseudoAccess, System.currentTimeMillis());
            } else if (args[0] == null || args[0].isNull())
                return null;
            else if (args[0] instanceof DBInt) {
                int year = (int) ((DBInt) args[0]).getIntValue();
                int month = (int) ((DBInt) args[1]).getIntValue() - 1;
                final int day = (int) ((DBInt) args[2]).getIntValue();
                Calendar c = new GregorianCalendar(year, month, day);
                if (args.length >= 5) {
                    c.set(Calendar.HOUR_OF_DAY, (int) ((DBInt) args[3]).getIntValue());
                    c.set(Calendar.MINUTE, (int) ((DBInt) args[4]).getIntValue());
                }
                if (args.length == 6) {
                    c.set(Calendar.SECOND, (int) ((DBInt) args[5]).getIntValue());
                }
                return new DBDateTime(new PseudoAccess(JoriaDateTime.instance()), c);
            }
            throw new JoriaAssertionError("Builtin date executed but arguments are not ints");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a == null || a.length == 0)
                return JoriaDateTime.instance();
            if (a.length == 3 || a.length == 5 || a.length == 6) {
                for (NodeInterface anA : a) {
                    if (!anA.isInteger())
                        return null;
                }
                return JoriaDateTime.instance();
            }
            return null;
        }
    };
    private static final Exec.ExecStringAbstract biFormatInt = new Exec.ExecStringAbstract("formatInt", convGroup, Res.str("formats_integers_to_string_as_roman_numbers_or_characters_string_format_int_number_string_format_format_may_be_roman_text_or_TEXT"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) {
            if (args[0] == null || args[0].isNull())
                return null;
            if (args.length > 1 && (args[1] == null || args[1].isNull()))
                return null;
            if (args.length == 2 && args[0] instanceof DBInt && args[1] instanceof DBString) {
                String format = ((DBString) args[1]).getStringValue();
                long val = ((DBInt) args[0]).getIntValue();
                if ("roman".equals(format))//trdone
                {
                    return new RomanNumeral((int) val).toString();
                } else {
                    return new TextNumeral((int) val, format).toString();
                }
            } else
                throw new JoriaAssertionError("Builtin formatInt executed but arguments are not int and string");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 3)// PM temporaer wird die Locale-Erweiterung ausser Betrieb genommen.
                return null;
            if (a.length >= 2 && !a[1].isString())
                return null;
            if (a.length <= 3)
                if (a[0].isInteger() || a[0].isReal() || a[0].isDate())
                    return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecStringAbstract biFormatNumber = new Exec.ExecStringAbstract("formatNumber", convGroup, Res.str("Formats_a_number_with_Java_DecimalFormat"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) {
            if (args[0] == null || args[0].isNull())
                return null;
            if (args.length > 1 && (args[1] == null || args[1].isNull()))
                return null;
            if (args.length == 2 && (args[0] instanceof DBInt || args[0] instanceof DBReal) && args[1] instanceof DBString) {
                String format = ((DBString) args[1]).getStringValue();
                NumberFormat formatter = new DecimalFormat(format, new DecimalFormatSymbols(env.getLocale()));

                String outString;
                if (args[0] instanceof DBInt) {
                    long value = ((DBInt) args[0]).getIntValue();
                    outString = formatter.format(value);
                } else {
                    double value = ((DBReal) args[0]).getRealValue();
                    outString = formatter.format(value);
                }
                return outString;
            } else
                throw new JoriaAssertionError("Builtin format executed but arguments are not int ot real and string");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 3)// PM temporaer wird die Locale-Erweiterung ausser Betrieb genommen.
                return null;
            if (a.length >= 2 && !a[1].isString())
                return null;
            if (a.length <= 3)
                if (a[0].isInteger() || a[0].isReal() || a[0].isDate())
                    return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecStringAbstract biToString = new Exec.ExecStringAbstract("toString", convGroup, Res.str("converts_numbers_dates_objects_to_a_string"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) {
            if (args[0] == null || args[0].isNull())
                return null;
            if (args.length > 1) {
                if (args[1] == null || args[1].isNull())
                    return null;
                String format = ((DBString) args[1]).getStringValue();
                Locale locale = env.getLocale();
                if (args.length > 2) {
                    String localeName = ((DBString) args[2]).getStringValue();
                    if (localeName == null || localeName.length() == 0)
                        return null;
                    StringTokenizer tok = new StringTokenizer(localeName, "_");
                    String language = tok.nextToken();
                    String country = null;
                    if (tok.hasMoreTokens())
                        country = tok.nextToken();
                    String variant = null;
                    if (tok.hasMoreTokens())
                        variant = tok.nextToken();
                    locale = new Locale(language, country, variant);
                }
                format = Internationalisation.localize(format, locale);
                if (args[0].getAccess().getType().isDate()) {
                    final DateFormat df = new SimpleDateFormat(format, locale);
                    return df.format(((DBDateTime) args[0]).getDate());
                } else {
                    final DecimalFormat nf = new DecimalFormat(format, new DecimalFormatSymbols(locale));
                    nf.setRoundingMode(Settings.getRoundingMode());
                    if (args[0].getAccess().getType().isIntegerLiteral())
                        return nf.format(((DBInt) args[0]).getIntValue());
                    else if (args[0].getAccess().getType().isRealLiteral())
                        return nf.format(((DBReal) args[0]).getRealValue());
                }
            } else {
                final NumberFormat nf = NumberFormat.getInstance(env.getLocale());
                if (args[0].getAccess().getType().isIntegerLiteral())
                    return nf.format(((DBInt) args[0]).getIntValue());
                else if (args[0].getAccess().getType().isRealLiteral())
                    return nf.format(((DBReal) args[0]).getRealValue());
                else if (args[0].getAccess().getType().isDate()) {
                    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, env.getLocale());
                    return df.format(((DBDateTime) args[0]).getDate());
                }
            }
            throw new JoriaAssertionError("Builtin toString executed but argument is not a number");
        }

        public JoriaType getType(NodeInterface[] a) {
            try {
                return parse(a);
            } catch (OQLParseException oqlParseExcpetion) {
                throw new JoriaAssertionError("Caught syntax error when too late: " + oqlParseExcpetion.getMessage(), oqlParseExcpetion);
            }
        }

        public JoriaType parse(NodeInterface[] a) throws OQLParseException {
            if (a.length == 3)// PM temporaer wird die Locale-Erweiterung ausser Betrieb genommen.
                return null;
            if (a.length >= 2 && !a[1].isString())
                return null;
            if (a.length <= 3) {
                if (a[0].isInteger() || a[0].isReal() || a[0].isDate()) {
                    Locale locale = Env.instance().getCurrentLocale();
                    if (a.length >= 2 && a[1] instanceof StringNode) {
                        StringNode a1 = (StringNode) a[1];
                        final String format = ((StringNode) a[1]).getConstantString();
                        if (a[0].isInteger() || a[0].isReal()) {
                            try {
                                new DecimalFormat(format, new DecimalFormatSymbols(locale));
                            } catch (Exception e) {
                                throw new OQLParseException(Res.msg("Bad_number_format_0_in_toString", a1.getConstantString(), e.getMessage()));
                            }
                        } else if (a[0].isDate()) {
                            try {
                                new SimpleDateFormat(format, locale);
                            } catch (Exception e) {
                                throw new OQLParseException(Res.msg("Bad_date_format_0_in_toString", a1.getConstantString(), e.getMessage()));
                            }
                        }
                    }
                    return getDefaultType();
                } else if (a.length == 1)
                    return getDefaultType();
            }
            return null;
        }
    };
    private static final Exec.ExecStringAbstract biCase = new Exec.ExecStringAbstract("case", logicGroup, Res.str("Selects_one_string_in_a_list_depending_on_a_number_passed_as_the_first_parameter"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return null;
            long picker;
            if (args[0].getAccess().getType().isIntegerLiteral()) {
                picker = ((DBInt) args[0]).getIntValue();
            } else
                throw new JoriaDataException("Builtin case executed but first argument is not an int");
            if (picker < 0 || picker > args.length - 2) {
                Trace.logError("Builtin case executed. First argument is out of range: 0 <= " + picker + " <= " + (args.length - 2));
                return null;
            }
            return args[(int) picker + 1].toString();
        }

        public JoriaType getType(NodeInterface[] a) {
            return DefaultStringLiteral.instance();
        }
    };
    private static final Exec.ExecStringAbstract biConcat = new Exec.ExecStringAbstract("concat", strGroup, Res.str("concatenates_values_separated_by_a_string_passed_as_first_parameter"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            StringBuilder sb = new StringBuilder();
            if (args[0] != null && !args[0].getActualType().isStringLiteral())
                throw new JoriaDataException("Builtin concat executed but first argument is not an String");
            String sep = args[0] != null ? ((DBString) args[0]).getStringValue() : null;
            boolean needSep = false;
            for (int i = 1; i < args.length; i++) {
                DBData arg = args[i];
                if (arg == null || arg.isNull())
                    continue;
                if (needSep)
                    sb.append(sep);
                else
                    needSep = true;
                sb.append(arg);
            }
            return sb.toString();
        }

        public JoriaType getType(NodeInterface[] a) {
            if (!a[0].isString())
                return null;
            return DefaultStringLiteral.instance();
        }
    };
    private static final Exec.ExecStringAbstract biNewline = new Exec.ExecStringAbstract("nl", strGroup, Res.str("outputs_a_newline_character"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) {
            return "\n";
        }

        public JoriaType getType(NodeInterface[] a) {
            return DefaultStringLiteral.instance();
        }
    };
    private static final Exec.ExecDBDataAbstract biFrom = new Exec.ExecDBDataAbstract("from", envGroup, Res.str("returns_the_object_that_contains_the_current_object"))//trdone
    {
        public DBData execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            final DBData data;
            if (args != null && args.length > 0) {
                if (args[0].getAccess().getType().isIntegerLiteral()) {
                    int index = (int) ((DBInt) args[0]).getIntValue();
                    if (index > ((RunEnvImpl) env).lengthOfObjectPath())
                        throw new JoriaUserDataException("Builtin from with an argument larger then the length of the object path");
                    data = ((RunEnvImpl) env).topOfObjectPath(index);
                } else
                    throw new JoriaDataException("Builtin from executed but first argument is not an int");
            } else {
                if (((RunEnvImpl) env).lengthOfObjectPath() == 0)
                    throw new JoriaUserDataException("Builtin from with empty of the object path");
                data = ((RunEnvImpl) env).topOfObjectPath();
            }
            if (data == null || data.isNull())
                throw new JoriaDataException("beyond first from object");
            return data;
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a != null && a.length > 1)
                return null;
            if (a != null && a.length == 1 && !a[0].isInteger())
                return null;
            return JoriaClassVoid.voidType;
        }
    };
    private static final Exec.ExecStringAbstract biClassName = new Exec.ExecStringAbstract("className", envGroup, Res.str("returns_type_name_of_object"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args.length > 1)
                throw new JoriaAssertionError("Bad argument count has escaped the OQL compiler: className " + args.length);

            if (args.length == 0) {
                return from.getActualType().getName();
            }
            if (args[0] == null || args[0].isNull())
                return null;
            return args[0].getActualType().getName();
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length > 1)
                return null;
            return getDefaultType();
        }
    };

    static final Exec.ExecDBDataAbstract biPrev = new Exec.ExecDBDataAbstract("prev", collGroup, Res.str("returns_the_previous_object_in_the_current_collection_Object_prev"))//trdone
    {
        public DBData execute(RunEnv env, DBData from, DBData[] args) {
            return ((RunEnvImpl) env).topOfPrevs();
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a != null && a.length > 0)
                return null;
            return null;
        }
    };
    private static final Exec.ExecStringAbstract biSubstring = new Exec.ExecStringAbstract("substring", strGroup, Res.str("returns_a_substring_of_a_string_String_substring_String_str_int_start_int_end"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (!(args.length == 2 || args.length == 3))
                throw new JoriaAssertionError("Bad argument count has escaped the OQL compiler: substring " + args.length);
            if (args[0] == null || args[0].isNull())
                return null;
            if (!((args[0] instanceof DBString) && (args[1] instanceof DBInt) && (args.length < 3 || (args[2] instanceof DBInt))))
                throw new JoriaDataException("Builtin substring not called with proper argument types. Expected String, int, [int]. Found: " + args[0].getActualType().getName() + "," + args[1].getActualType() + (args.length == 3 ? ("," + args[2].getActualType()) : ""));
            String s = ((DBString) args[0]).getStringValue();
            int start = (int) ((DBInt) args[1]).getIntValue();
            if (s.length() <= start || start < 0)
                return null;
            if (args.length == 2) {
                return s.substring(start);
            } else {
                int to = Math.min((int) ((DBInt) args[2]).getIntValue(), s.length());
                if (to < 0)
                    return null;
                return s.substring(start, to);
            }
        }

        public JoriaType getType(NodeInterface[] a) {
            if (!(a.length == 2 || a.length == 3))
                return null;
            if (!((a[0].isString()) && (a[1].isInteger()) && (a.length < 3 || (a[2].isInteger()))))
                return null;
            return getDefaultType();
        }
    };
    static final Exec.ExecStringAbstract biLocal = new Exec.ExecStringAbstract("local", convGroup, Res.str("Localizes_the_current_string_by_looking_in_the_localisation_files_of_the_repository_String_local_String_v"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args.length != 1)
                throw new JoriaAssertionError("Bad argument count has escaped the OQL compiler: local " + args.length);
            if (args[0] == null || args[0].isNull())
                return null;
            if (!((args[0] instanceof DBString)))
                throw new JoriaDataException("Builtin local not called with proper argument types. Expected String. Found: " + args[0].getActualType().getName());
            if (Env.instance().repo() != null && Env.instance().repo().i18n != null)
                return Internationalisation2.localize(((DBString) args[0]).getStringValue(), env.getLocale());
            else
                return ((DBString) args[0]).getStringValue();
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length != 1)
                return null;
            if (!a[0].isString())
                return null;
            return getDefaultType();
        }
    };
    private static final Exec.ExecStringAbstract biFileText = new Exec.ExecStringAbstract("fileText", envGroup, Res.str("returns_the_content_of_a_text_file_String_fileText_String_filename"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args.length != 1 && args.length != 2)
                throw new JoriaAssertionError("Bad argument count has escaped the OQL compiler: fileText " + args.length);
            if (args[0] == null || args[0].isNull())
                return null;
            if (!((args[0] instanceof DBString)))
                throw new JoriaDataException("Builtin fileText not called with proper argument types. Expected String. Found: " + args[0].getActualType().getName());
            if (args.length == 2 && !(args[1] instanceof DBString))
                throw new JoriaDataException("Builtin fileText not called with proper argument types. Expected String. Found: " + args[1].getActualType().getName());
            String replacement = null;
            if (args.length == 2)
                replacement = ((DBString) args[1]).getStringValue();
            Reader r = null;
            try {
                String fileName = ((DBString) args[0]).getStringValue();
                if (fileName == null)
                    return replacement;
                r = new InputStreamReader(Env.instance().getFileService().getFileData(fileName));
                int c;
                StringBuilder b = new StringBuilder();
                while (((c = r.read())) != 0xffff && c > 0) {
                    b.append((char) c);
                }
                r.close();
                r = null;
                return b.toString();
            } catch (IOException e) {
                return replacement;
            } finally {
                if (r != null)
                    //noinspection EmptyCatchBlock
                    try {
                        r.close();
                    } catch (IOException e) {
                    }
            }
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length != 1 && a.length != 2)
                return null;
            if (!a[0].isString())
                return null;
            if (a.length == 2 && !a[1].isString())
                return null;
            return getDefaultType();
        }
    };
    private static final Exec.ExecStringAbstract biRegex = new Exec.ExecStringAbstract("replaceRegex", envGroup, Res.str("replace regular expression"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args.length != 3)
                throw new JoriaAssertionError("Bad argument count has escaped the OQL compiler: fileText " + args.length);
            if (args[0] == null || args[0].isNull() || args[1] == null || args[1].isNull() || args[2] == null || args[2].isNull())
                return null;
            if (!((args[0] instanceof DBString)))
                throw new JoriaDataException("Builtin fileText not called with proper argument types. Expected String. Found: " + args[0].getActualType().getName());
            if (!(args[1] instanceof DBString))
                throw new JoriaDataException("Builtin fileText not called with proper argument types. Expected String. Found: " + args[1].getActualType().getName());
            if (!(args[2] instanceof DBString))
                throw new JoriaDataException("Builtin fileText not called with proper argument types. Expected String. Found: " + args[2].getActualType().getName());
            String source = ((DBString) args[0]).getStringValue();
            String patternString = ((DBString) args[1]).getStringValue();
            String replacement = ((DBString) args[2]).getStringValue();
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(source);
            return matcher.replaceAll(replacement);
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length != 3)
                return null;
            if (a[0].isString() && a[1].isString() && a[2].isString())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecStringAbstract biMaxLines = new Exec.ExecStringAbstract("maxLines", envGroup, Res.str("return at most n lines"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args.length != 2)
                throw new JoriaAssertionError("Bad argument count has escaped the OQL compiler: maxLines " + args.length);
            if (args[0] == null || args[0].isNull() || args[1] == null || args[1].isNull())
                return null;
            if (!((args[0] instanceof DBString)))
                throw new JoriaDataException("Builtin maxLines not called with proper argument types. Expected String at position 1. Found: " + args[0].getActualType().getName());
            if (!(args[1] instanceof DBInt))
                throw new JoriaDataException("Builtin maxLines not called with proper argument types. Expected Int at position 2. Found: " + args[1].getActualType().getName());
            String source = ((DBString) args[0]).getStringValue();
            long lines = ((DBInt) args[1]).getIntValue();
            StringBuilder ret = new StringBuilder();
            boolean goOn = true;
            int charPos = 0;
            int lineCount = 0;
            while (goOn && charPos < source.length()) {
                if (source.charAt(charPos) == '\n')
                    goOn = ++lineCount < lines;
                ret.append(source.charAt(charPos));
                charPos++;
            }
            return ret.toString();
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length != 2)
                return null;
            if (a[0].isString() && a[1].isInteger())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecIntegerAbstract biIndexOf = new Exec.ExecIntegerAbstract("indexOf", strGroup, Res.str("returns_the_index_of_a_string_within_another_string_int_indexOf_String_search_in_String_search_for"))//trdone
    {
        public long execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args.length != 2)
                throw new JoriaAssertionError("Bad argument count has escaped the OQL compiler: indexOf " + args.length);
            if (args[0] == null || args[0].isNull() || args[1] == null || args[1].isNull())
                return DBInt.NULL;
            if (!(args[0] instanceof DBString))
                throw new JoriaDataException("Builtin indexOf not called with proper argument types. Expected String - Found: " + args[0].getActualType().getName());
            if (args[1] instanceof DBString)
                return ((DBString) args[0]).getStringValue().indexOf(((DBString) args[1]).getStringValue());
            else if (args[1] instanceof DBInt)
                return ((DBString) args[0]).getStringValue().indexOf((char) ((DBInt) args[1]).getIntValue());
            else
                throw new JoriaDataException("Builtin indexOf not called with proper argument types. Expected 1:String, 2: char or String - Found: String, " + args[1].getActualType().getName());
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length != 2 || !a[0].isString() || !a[1].isString())
                return null;
            return DefaultIntLiteral.instance();
        }
    };
    private static final Exec.ExecBooleanAbstract biContains = new Exec.ExecBooleanAbstract("containsName", strGroup, Res.str("StringContainsString"))//trdone
    {
        public boolean execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args.length != 2)
                throw new JoriaAssertionError("Bad argument count has escaped the OQL compiler: indexOf " + args.length);
            if (args[0] == null || args[0].isNull() || args[1] == null || args[1].isNull())
                return false;
            if (!(args[0] instanceof DBString))
                throw new JoriaDataException("Builtin indexOf not called with proper argument types. Expected String - Found: " + args[0].getActualType().getName());
            if (args[1] instanceof DBString)
                return ((DBString) args[0]).getStringValue().contains(((DBString) args[1]).getStringValue());
            else if (args[1] instanceof DBInt)
                return ((DBString) args[0]).getStringValue().indexOf((char) ((DBInt) args[1]).getIntValue()) >= 0;
            else
                throw new JoriaDataException("Builtin indexOf not called with proper argument types. Expected 1:String, 2: char or String - Found: String, " + args[1].getActualType().getName());
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length != 2 || !a[0].isString() || !(a[1].isString() || a[1].isBoolean()))
                return null;
            return DefaultBooleanLiteral.instance();
        }
    };
    private static final Exec.ExecIntegerAbstract biLastIndexOf = new Exec.ExecIntegerAbstract("lastIndexOf", collGroup, Res.str("returns_the_last_index_of_a_string_within_another_string_int_indexOf_String_search_in_String_search_for"))//trdone
    {
        public long execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args.length != 2)
                throw new JoriaAssertionError("Bad argument count has escaped the OQL compiler: lastIndexOf " + args.length);
            if (args[0] == null || args[0].isNull() || args[1] == null || args[1].isNull())
                return DBInt.NULL;
            if (!(args[0] instanceof DBString))
                throw new JoriaDataException("Builtin lastIndexOf not called with proper argument types. Expected String - Found: " + args[0].getActualType().getName());
            if (args[1] instanceof DBString)
                return ((DBString) args[0]).getStringValue().lastIndexOf(((DBString) args[1]).getStringValue());
            else if (args[1] instanceof DBInt)
                return ((DBString) args[0]).getStringValue().lastIndexOf((char) ((DBInt) args[1]).getIntValue());
            else
                throw new JoriaDataException("Builtin lastIndexOf not called with proper argument types. Expected 1:String, 2: char or String - Found: String, " + args[1].getActualType().getName());
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length != 2 || !a[0].isString() || !a[1].isString())
                return null;
            return DefaultIntLiteral.instance();
        }
    };
    private static final Exec.ExecDateAbstract biAddWeekToDate = new Exec.ExecDateAbstract("addWeeks", datimGroup, Res.str("adds_a_number_of_weeks_to_a_date_Date_addWeeks_Date_d"))//trdone
    {
        final PseudoAccess pseudoAccess = new PseudoAccess(JoriaDateTime.instance(), "addWeeks()");//trdone

        public DBData execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull() || args[1] == null || args[1].isNull())
                return null;
            if (args.length == 2 || args[0] instanceof DBDateTime || args[1] instanceof DBInt) {
                Calendar t = (Calendar) ((DBDateTime) args[0]).getCalendar().clone();
                int inc = (int) ((DBInt) args[1]).getIntValue();
                t.add(Calendar.WEEK_OF_YEAR, inc);
                return new DBDateTime(pseudoAccess, t);
            }
            throw new JoriaDataException("Builtin addWeeks not called with proper argument types. Expected Date");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 2 || a[0].isDate() || a[1].isInteger())
                return JoriaDateTime.instance();
            return null;
        }
    };
    private static final Exec.ExecDateAbstract biLastDayOfMonth = new Exec.ExecDateAbstract("lastDayOfMonth", datimGroup, Res.str("returns_last_day_in_the_month_defined_by_the_supplied_date_Date_lastDayOfMonth_Date_d"))//trdone
    {
        final PseudoAccess pseudoAccess = new PseudoAccess(JoriaDateTime.instance(), "lastDayOfMonth()");//trdone

        public DBData execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return null;
            if (args.length == 1 || args[0] instanceof DBDateTime) {
                Calendar t = (Calendar) ((DBDateTime) args[0]).getCalendar().clone();
                //int month = t.get(Calendar.MONTH);
                int maxday = t.getActualMaximum(Calendar.DAY_OF_MONTH);
                t.set(Calendar.DAY_OF_MONTH, maxday);
                t.set(Calendar.HOUR_OF_DAY, 12);
                t.set(Calendar.MINUTE, 0);
                t.set(Calendar.SECOND, 0);
                t.set(Calendar.MILLISECOND, 0);
                return new DBDateTime(pseudoAccess, t);
            }
            throw new JoriaDataException("Builtin lastDayOfMonth not called with proper argument types. Expected Date");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 || a[0].isDate())
                return JoriaDateTime.instance();
            return null;
        }
    };
    private static final Exec.ExecDateAbstract biLastDayOfQuarter = new Exec.ExecDateAbstract("lastDayOfQuarter", datimGroup, Res.str("returns_last_day_in_the_quarter_defined_by_the_supplied_date_Date_lastDayOfMonth_Date_d"))//trdone
    {
        final PseudoAccess pseudoAccess = new PseudoAccess(JoriaDateTime.instance(), "lastDayOfQuarter()");//trdone

        public DBData execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return null;
            if (args.length == 1 || args[0] instanceof DBDateTime) {
                Calendar t = (Calendar) ((DBDateTime) args[0]).getCalendar().clone();
                int month = t.get(Calendar.MONTH);
                int quarter = (month / 3) * 3 + 2;
                t.set(Calendar.MONTH, quarter);
                t.set(Calendar.HOUR_OF_DAY, 12);
                t.set(Calendar.MINUTE, 0);
                t.set(Calendar.SECOND, 0);
                t.set(Calendar.MILLISECOND, 0);
                t.set(Calendar.DAY_OF_MONTH, 1); // force day 1 in case the month does not have a 31 which would roll over to the next month
                int maxday = t.getActualMaximum(Calendar.DAY_OF_MONTH);
                t.set(Calendar.DAY_OF_MONTH, maxday);
                return new DBDateTime(pseudoAccess, t);
            }
            throw new JoriaDataException("Builtin lastDayOfQuarter not called with proper argument types. Expected Date");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 || a[0].isDate())
                return JoriaDateTime.instance();
            return null;
        }
    };
    private static final Exec.ExecDateAbstract biAddDayToDate = new Exec.ExecDateAbstract("addDays", datimGroup, Res.str("adds_a_number_of_days_to_a_date_Date_addDays_Date_d"))//trdone
    {
        final PseudoAccess pseudoAccess = new PseudoAccess(JoriaDateTime.instance(), "addDays()");//trdone

        public DBData execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull() || args[1] == null || args[1].isNull())
                return null;
            if (args.length == 2 && args[0] instanceof DBDateTime && args[1] instanceof DBInt) {
                Calendar t = (Calendar) ((DBDateTime) args[0]).getCalendar().clone();
                int inc = (int) ((DBInt) args[1]).getIntValue();
                t.add(Calendar.DAY_OF_YEAR, inc);
                return new DBDateTime(pseudoAccess, t);
            }
            throw new JoriaDataException("Builtin addDays not called with proper argument types. Expected Date ");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 2 || a[0].isDate() || a[1].isInteger())
                return JoriaDateTime.instance();
            return null;
        }
    };
    private static final Exec.ExecDateAbstract biRoundDate = new Exec.ExecDateAbstract("dayOf", datimGroup, Res.str("rounds_a_date_time_to_a_full_day_Date_dayOf_Date_d"))//trdone
    {
        final PseudoAccess pseudoAccess = new PseudoAccess(JoriaDateTime.instance(), "dayOf()");//trdone

        public DBData execute(RunEnv env, DBData from, DBData[] a) throws JoriaDataException {
            if (a[0] == null || a[0].isNull())
                return null;
            else if (a[0] instanceof DBDateTime) {
                //final long dl = (((DBDateTime) a[0]).getCalendar().getTimeInMillis() / 86400000L)*86400000L;
                Calendar t0 = ((DBDateTime) a[0]).getCalendar();
                //String chk0 = t0.get(Calendar.YEAR)+ "." + (t0.get(Calendar.MONTH)+1)+ "." + t0.get(Calendar.DAY_OF_MONTH)+ " "  + t0.get(Calendar.HOUR_OF_DAY);
                t0.set(Calendar.HOUR_OF_DAY, 0);
                t0.set(Calendar.SECOND, 0);
                t0.set(Calendar.MINUTE, 0);
                t0.set(Calendar.MILLISECOND, 0);
                //String chk1 = t0.get(Calendar.YEAR)+ "." + (t0.get(Calendar.MONTH)+1)+ "." + t0.get(Calendar.DAY_OF_MONTH)+ " "  + t0.get(Calendar.HOUR_OF_DAY);
                //Calendar t = Calendar.getInstance();
                //t.setTimeInMillis(dl);
                //String chk = t.get(Calendar.YEAR)+ "." + t.get(Calendar.MONTH)+ "." + t.get(Calendar.DAY_OF_MONTH)+ " "  + t.get(Calendar.HOUR_OF_DAY);
                return new DBDateTime(pseudoAccess, t0);
            }
            throw new JoriaDataException("Builtin dayOf not called with proper argument types. Expected Date ");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 && a[0].isDate())
                return JoriaDateTime.instance();
            return null;
        }
    };
    private static final Exec.ExecDateAbstract biAddMonthToDate = new Exec.ExecDateAbstract("addMonths", datimGroup, Res.str("adds_a_number_of_month_to_a_date_Date_addMonths_Date_d"))//trdone
    {
        final PseudoAccess pseudoAccess = new PseudoAccess(JoriaDateTime.instance(), "addMonths()");//trdone

        public DBData execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull() || args[1] == null || args[1].isNull())
                return null;
            if (args.length == 2 || args[0] instanceof DBDateTime || args[1] instanceof DBInt) {
                Calendar t = (Calendar) ((DBDateTime) args[0]).getCalendar().clone();
                int inc = (int) ((DBInt) args[1]).getIntValue();
                t.add(Calendar.MONTH, inc);
                return new DBDateTime(pseudoAccess, t);
            }
            throw new JoriaDataException("Builtin addMonths not called with proper argument types. Expected Date");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 2 || a[0].isDate() || a[1].isInteger())
                return JoriaDateTime.instance();
            return null;
        }
    };
    private static final Exec.ExecDateAbstract biAddYearToDate = new Exec.ExecDateAbstract("addYears", datimGroup, Res.str("adds_a_number_of_years_to_a_date_Date_addYears_Date_d"))//trdone
    {
        final PseudoAccess pseudoAccess = new PseudoAccess(JoriaDateTime.instance(), "addYears()");//trdone

        public DBData execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull() || args[1] == null || args[1].isNull())
                return null;
            if (args.length == 2 || args[0] instanceof DBDateTime || args[1] instanceof DBInt) {
                Calendar t = (Calendar) ((DBDateTime) args[0]).getCalendar().clone();
                int inc = (int) ((DBInt) args[1]).getIntValue();
                t.add(Calendar.YEAR, inc);
                return new DBDateTime(pseudoAccess, t);
            }
            throw new JoriaDataException("Builtin addYears not called with proper argument types. Expected Date");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 2 || a[0].isDate() || a[1].isInteger())
                return JoriaDateTime.instance();
            return null;
        }
    };
    private static final Exec.ExecBoolean4String biStartsWith = new Exec.ExecBoolean4String("startsWith", strGroup, Res.str("checks_whether_one_string_is_at_the_beginning_of_another_boolean_startsWith_String_search_in_String_is_beginning"))//trdone
    {
        protected boolean compare(String s1, String s2) {
            return s1.startsWith(s2);
        }
    };
    private static final Exec.ExecBoolean4String biEndsWith = new Exec.ExecBoolean4String("endsWith", strGroup, Res.str("checks_whether_one_string_is_at_the_end_of_another_boolean_endsWith_String_search_in_String_is_end"))//trdone
    {
        protected boolean compare(String s1, String s2) {
            return s1.endsWith(s2);
        }
    };
    private static final Exec.ExecIntegerAbstract biCompareToIgnoreCase = new Exec.ExecIntegerAbstract("compareToIgnoreCase", strGroup, Res.str("returns_a_number_indicating_whether_one_string_is_lecically_before_another_int_compareToIgnoreCase_String_s_String_s"))//trdone
    {
        public long execute(RunEnv env, DBData from, DBData[] args) {
            if (args[0] == null || args[0].isNull() || args[1] == null || args[1].isNull())
                return DBInt.NULL;
            return ((DBString) args[0]).getStringValue().compareToIgnoreCase(((DBString) args[1]).getStringValue());
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a != null && a.length == 2 && a[0].isString() && a[1].isString())
                return DefaultIntLiteral.instance();
            return null;
        }
    };
    private static final Exec.ExecIntegerAbstract biCharAt = new Exec.ExecIntegerAbstract("charAt", strGroup, Res.str("returns_the_character_at_specified_position_in_a_string_int_toLowerCase_String_s_int_pos"))//trdone
    {
        public long execute(RunEnv env, DBData from, DBData[] args) {
            if (args[0] == null || args[0].isNull() || args[1] == null || args[1].isNull())
                return DBInt.NULL;
            return ((DBString) args[0]).getStringValue().charAt((int) ((DBInt) args[1]).getIntValue());
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 2 && a[0].isString() && a[1].isInteger())
                return DefaultIntLiteral.instance();
            return null;
        }
    };

    private static class LenOrCount extends Exec.ExecIntegerAbstract {
        LenOrCount(String name) {
            super(name, collGroup, Res.str("returns_the_length_of_a_string_or_a_collection"));
        }

        public long execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args == null || args[0] == null)
                return 0;
            else if (args[0] instanceof DBString)
                return ((DBString) args[0]).getStringValue().length();
            else if (args[0] instanceof DBCollection)
                return ((DBCollection) args[0]).getLength();
            else
                throw new JoriaDataException("Builtin length not called with proper argument types. Expected String or Collection found " + args[0]);
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a != null && a.length == 1 && (a[0].isString() || a[0].isCollection() || a[0].isLiteralCollection()))
                return DefaultIntLiteral.instance();
            return null;
        }
    }

    private static final Exec.ExecIntegerAbstract biLength = new LenOrCount("length");//trdone
    private static final Exec.ExecIntegerAbstract biCount = new LenOrCount("count");//trdone
    private static final Exec.ExecStringAbstract biToLower = new Exec.ExecStringAbstract("toLowerCase", strGroup, Res.str("Converts_a_string_to_lower_case_letters_String_toLowerCase_String_s"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return null;
            else if (args[0] instanceof DBString)
                return ((DBString) args[0]).getStringValue().toLowerCase();
            else
                throw new JoriaDataException("Builtin toLowerCase not called with proper argument types. Expected String or Collection");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 && a[0].isString())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecStringAbstract biToUpperCase = new Exec.ExecStringAbstract("toUpperCase", strGroup, Res.str("Converts_a_string_to_upper_case"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return null;
            else if (args[0] instanceof DBString)
                return ((DBString) args[0]).getStringValue().toUpperCase();
            else
                throw new JoriaDataException("Builtin toUpperCase not called with proper argument types. Expected String or Collection");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 && a[0].isString())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecStringAbstract biConcatStringCollection = new Exec.ExecStringAbstract("concatStringCollection", collGroup, Res.str("Concatenates_all_strings_collected_by_a_selection_variable_into_a_single_string_String_concatStringCollection_Collection_variableRef_String_separator"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return null;
            if (args[0] instanceof DBLiteralCollection) {
                DBLiteralCollection coll = (DBLiteralCollection) args[0];
                if (coll.isStrings()) {
                    if (coll.getLength() == 0)
                        return null;
                    if (((JoriaLiteralCollection) coll.getActualType()).getElementLiteralType().isStringLiteral()) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < coll.getLength(); i++) {
                            if (i > 0)
                                sb.append("; ");
                            String s = coll.getStringAt(i);
                            sb.append(s);
                        }
                        return sb.toString();
                    }
                }
            } else if (args[0] instanceof DBLiteralCollectionData) {
                DBLiteralCollectionData dbLiteralCollectionData = (DBLiteralCollectionData) args[0];
                if (dbLiteralCollectionData.getLength() == 0)
                    return null;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < dbLiteralCollectionData.getLength(); i++) {
                    if (i > 0)
                        sb.append("; ");
                    String s = dbLiteralCollectionData.getValue(i, null).toString();
                    sb.append(s);
                }
                return sb.toString();
            }
            throw new JoriaDataException("Builtin concatStringCollection not called with proper argument types. Expected String-Collection");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length != 1)
                return null;
            JoriaTypedNode jtn = (JoriaTypedNode) a[0];
            JoriaType t = jtn.getType();
            if (t.isLiteralCollection()) {
                JoriaLiteralCollection jc = (JoriaLiteralCollection) t;
                if (jc.getElementLiteralType().isStringLiteral())
                    return getDefaultType();
            } else if (t.isCollection()) {
                JoriaCollection o = (JoriaCollection) t;
                JoriaClass et = o.getElementType();
                while (et instanceof ClassView) {
                    et = ((ClassView) et).getBase();
                }
                if (et instanceof LiteralCollectionClass)
                    return getDefaultType();
            }
            return null;
        }
    };
    private static final Exec.ExecStringAbstract biTrim = new Exec.ExecStringAbstract("trim", strGroup, Res.str("trims_leading_and_following_spaces_from_a_string_String_trim_String_s"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return null;
            if (args[0] instanceof DBString)
                return ((DBString) args[0]).getStringValue().trim();
            else
                throw new JoriaDataException("Builtin trim not called with proper argument types. Expected String or Collection");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 && a[0].isString())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecStringAbstract biLTrim = new Exec.ExecStringAbstract("trimLeft", strGroup, Res.str("trims_leading_spaces_from_a_string_String_trim_String_s"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return null;
            if (args[0] instanceof DBString) {
                String value = ((DBString) args[0]).getStringValue();
                int len = value.length();
                int st = 0;
                while ((st < len) && (value.charAt(st) <= ' ')) {
                    st++;
                }
                return ((st > 0) || (len < value.length())) ? value.substring(st, len) : value;
            } else
                throw new JoriaDataException("Builtin trim not called with proper argument types. Expected String or Collection");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 && a[0].isString())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecStringAbstract biRTrim = new Exec.ExecStringAbstract("trimRight", strGroup, Res.str("trims_following_spaces_from_a_string_String_trim_String_s"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return null;
            if (args[0] instanceof DBString) {
                String value = ((DBString) args[0]).getStringValue();
                int len = value.length();
                int st = 0;
                while ((st < len) && (value.charAt(len - 1) <= ' ')) {
                    len--;
                }
                return ((len < value.length())) ? value.substring(st, len) : value;
            } else
                throw new JoriaDataException("Builtin trim not called with proper argument types. Expected String or Collection");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 && a[0].isString())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecStringAbstract biSystemProperty = new Exec.ExecStringAbstract("getSystemProperty", envGroup, Res.str("gets_the_value_of_a_Java_system_property_String_getSystemProperty_String_propName"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return null;
            if (args[0] instanceof DBString)
                return System.getProperty(((DBString) args[0]).getStringValue());
            else
                throw new JoriaDataException("Builtin trim not called with proper argument types. Expected String or Collection");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 && a[0].isString())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecIntegerAbstract biParseInt = new Exec.ExecIntegerAbstract("parseInt", convGroup, Res.str("Converts_a_String_to_a_number_int_parseInt_String_s"))//trdone
    {
        public long execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return DBInt.NULL;
            if (args[0] instanceof DBString) {
                try {
                    return Integer.parseInt(((DBString) args[0]).getStringValue());
                } catch (NumberFormatException ex) {
                    //throw new JoriaUserDataException(Res.strcb("String_cannot_be_converted_to_integer") + ((DBString) args[0]).getStringValue());
                    return DBInt.NULL;
                }
            } else
                throw new JoriaDataException("Builtin parseInt not called with proper argument types. Expected String or Collection");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 && a[0].isString())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecReal biParseFloat = new Exec.ExecRealAbstract("parseFloat", convGroup, Res.str("Converts_a_String_to_a_floating_point_number_int_parseFloat_String_s"))//trdone
    {
        public double execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return DBReal.NULL;
            if (args[0] instanceof DBString) {
                try {
                    // Hier muss bei Bedarf ein Komma in einen Punkt gewandelt werden
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols(env.getLocale());
                    char dec = symbols.getDecimalSeparator();
                    char group = symbols.getGroupingSeparator();
                    String stringValue = ((DBString) args[0]).getStringValue();
                    StringBuilder val = new StringBuilder(stringValue.length());
                    for (int i = 0; i < stringValue.length(); i++) {
                        char ch = stringValue.charAt(i);
                        if (ch == dec)
                            val.append('.');
                        else if (ch != group)
                            val.append(ch);
                    }
                    return Double.parseDouble(val.toString());
                } catch (NumberFormatException ex) {
                    throw new JoriaUserDataException(Res.strp("String_cannot_be_converted_to_float", ((DBString) args[0]).getStringValue()));
                }
            } else
                throw new JoriaDataException("Builtin parseInt not called with proper argument types. Expected String or Collection");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 && a[0].isString())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecInteger biRoundFloat = new Exec.ExecIntegerAbstract("round", mathGroup, Res.str("rounds_the_floating_point_number_to_the_next_integer_int_round_float_f"))//trdone
    {
        public long execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return DBInt.NULL;
            if (args[0] instanceof DBReal) {
                return Math.round(((DBReal) args[0]).getRealValue());
            } else
                throw new JoriaDataException("Builtin round not called with proper argument types. Expected float or double");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 && a[0].isReal())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecInteger biTruncFloat = new Exec.ExecIntegerAbstract("trunc", mathGroup, Res.str("rounds_the_floating_point_number_to_the_next_integer_int_round_float_f"))//trdone
    {
        public long execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return DBInt.NULL;
            if (args[0] instanceof DBReal) {
                return (long) (((DBReal) args[0]).getRealValue());
            } else
                throw new JoriaDataException("Builtin round not called with proper argument types. Expected float or double");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 && a[0].isReal())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecReal biToFloat = new Exec.ExecRealAbstract("float", mathGroup, Res.str("converts_a_interger_to_a_float_number_float_float_int_f"))//trdone
    {
        public double execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return DBReal.NULL;
            if (args[0] instanceof DBInt) {
                return ((DBInt) args[0]).getIntValue();
            } else
                throw new JoriaDataException("Builtin round not called with proper argument types. Expected int or long");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 && a[0].isInteger())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecReal biPower = new Exec.ExecRealAbstract("power", mathGroup, Res.str("raises_the_first_argument_to_the_power_of_the_n"))//trdone
    {
        public double execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull() || args[1] == null || args[1].isNull())
                return DBReal.NULL;
            if (args[0] instanceof DBReal && args[1] instanceof DBReal) {
                return Math.pow(((DBReal) args[0]).getRealValue(), ((DBReal) args[1]).getRealValue());
            } else
                throw new JoriaDataException("Builtin round not called with proper argument types. Expected float and float");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 2 && a[0].isReal() && a[1].isReal())
                return getDefaultType();
            return null;
        }
    };
    private static final Exec.ExecBoolean biIsNaN = new Exec.ExecBooleanAbstract("isNaN", mathGroup, Res.str("returns_whether_the_floating_point_number_is_not_a_normal_number_boolean_isNaN_float_f"))//trdone
    {
        public boolean execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return true;
            if (args[0] instanceof DBReal) {
                try {
                    return Double.isNaN(((DBReal) args[0]).getRealValue());
                } catch (NumberFormatException ex) {
                    throw new JoriaUserDataException(Res.strp("String_cannot_be_converted_to_float", ((DBString) args[0]).getStringValue()));
                }
            } else if (args[0] instanceof DBInt) {
                return false;// null has been checked above
            } else
                throw new JoriaDataException("Builtin isNaN not called with proper argument types. Expected Number");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1 && a[0].isReal())
                return DefaultBooleanLiteral.instance();
            return null;
        }
    };
    private static final Exec.ExecString biReplace = new Exec.ExecStringAbstract("replace", convGroup, Res.str("replaces_a_value_with_a_selection_of_strings_string_replace_value_defaultReplacement_testValue_replacement"))//trdone
    {
        public String execute(RunEnv env, DBData from, DBData[] args) throws JoriaDataException {
            if (args[0] == null || args[0].isNull())
                return null;
            DBData value = args[0];
            if (value.getActualType().isStringLiteral() || value.getActualType().isCharacterLiteral()) {
                DBString val = (DBString) value;
                String base = val.getStringValue();
                for (int i = 2; i < args.length; i++) {
                    if (args[i] == null || args[i].isNull())
                        continue;
                    if (!args[i].getActualType().isStringLiteral() && !args[i].getActualType().isCharacterLiteral()) {
                        throw new JoriaDataException("BuiltIn replace not called with proper argument types. Expected String as " + (i + 1) + " argument");
                    }
                    DBString test = (DBString) args[i];
                    if (base.equals(test.getStringValue())) {
                        if (args[i + 1] == null || args[i + 1].isNull())
                            return null;
                        if (!args[i + 1].getActualType().isStringLiteral())
                            throw new JoriaDataException("BuiltIn replace not called with proper argument types. Expected String as " + (i + 2) + " argument");
                        return ((DBString) args[i + 1]).getStringValue();
                    }
                }
                if (args[1] == null || args[1].isNull())
                    return null;
                if (!args[1].getActualType().isStringLiteral())
                    throw new JoriaDataException("BuiltIn replace not called with proper argument types. Expected String as " + (2) + " argument");
                return ((DBString) args[1]).getStringValue();
            }
            if (value.getActualType().isIntegerLiteral()) {
                DBInt val = (DBInt) value;
                long base = val.getIntValue();
                for (int i = 2; i < args.length; i++) {
                    if (args[i] == null || args[i].isNull())
                        continue;
                    if (!args[i].getActualType().isIntegerLiteral() && !args[i].getActualType().isRealLiteral()) {
                        throw new JoriaDataException("BuiltIn replace not called with proper argument types. Expected Number as " + (i + 1) + " argument");
                    }
                    long test;
                    if (args[i].getActualType().isIntegerLiteral()) {
                        test = ((DBInt) args[i]).getIntValue();
                    } else {
                        test = Math.round(((DBReal) args[i]).getRealValue());
                    }
                    if (base == test) {
                        if (args[i + 1] == null || args[i + 1].isNull())
                            return null;
                        if (!args[i + 1].getActualType().isStringLiteral())
                            throw new JoriaDataException("BuiltIn replace not called with proper argument types. Expected String as " + (i + 2) + " argument");
                        return ((DBString) args[i + 1]).getStringValue();
                    }
                }
                if (args[1] == null || args[1].isNull())
                    return null;
                if (!args[1].getActualType().isStringLiteral())
                    throw new JoriaDataException("BuiltIn replace not called with proper argument types. Expected String as " + (2) + " argument");
                return ((DBString) args[1]).getStringValue();
            }
            if (value.getActualType().isRealLiteral()) {
                DBReal val = (DBReal) value;
                double base = val.getRealValue();
                for (int i = 2; i < args.length; i++) {
                    if (args[i] == null || args[i].isNull())
                        continue;
                    if (!args[i].getActualType().isIntegerLiteral() && !args[i].getActualType().isRealLiteral()) {
                        throw new JoriaDataException("BuiltIn replace not called with proper argument types. Expected Number as " + (i + 1) + " argument");
                    }
                    double test;
                    if (args[i].getActualType().isIntegerLiteral()) {
                        test = ((DBInt) args[i]).getIntValue();
                    } else {
                        test = ((DBReal) args[i]).getRealValue();
                    }
                    if (base == test) {
                        if (args[i + 1] == null || args[i + 1].isNull())
                            return null;
                        if (!args[i + 1].getActualType().isStringLiteral())
                            throw new JoriaDataException("BuiltIn replace not called with proper argument types. Expected String as " + (i + 2) + " argument");
                        return ((DBString) args[i + 1]).getStringValue();
                    }
                }
                if (args[1] == null || args[1].isNull())
                    return null;
                if (!args[1].getActualType().isStringLiteral())
                    throw new JoriaDataException("BuiltIn replace not called with proper argument types. Expected String as " + (2) + " argument");
                return ((DBString) args[1]).getStringValue();
            }
            throw new JoriaDataException("BuiltIn replace not called with proper argument types. Expected String or Number as " + (1) + " argument");
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length < 4 || a.length % 2 != 0)
                return null;
            NodeInterface value = a[0];
            if (value.isCharacter() || value.isString()) {
                for (int i = 2; i < a.length; i += 2) {
                    if (!a[i].isCharacter() && !a[i].isString())
                        return null;
                }
            }
            if (value.isInteger() || value.isReal()) {
                for (int i = 2; i < a.length; i += 2) {
                    if (!a[i].isInteger() && !a[i].isReal())
                        return null;
                }
            }
            for (int i = 1; i < a.length; i += 2) {
                if (!a[i].isString())
                    return null;
            }
            return DefaultStringLiteral.instance();
        }
    };
    private static final Exec.ExecString biReplaceAll = new Exec.ExecStringAbstract("replaceAll", convGroup, Res.str("replace_all_occurences_of_a_string_with_another_string"))//trdone
    {
        public String execute(final RunEnv env, final DBData from, final DBData[] args) throws JoriaDataException {
            if (args[0] instanceof DBString && args[1] instanceof DBString && args[2] instanceof DBString) {
                String value = ((DBString) args[0]).getStringValue();
                String fromStr = ((DBString) args[1]).getStringValue();
                String toStr = ((DBString) args[2]).getStringValue();
                return value.replaceAll(fromStr, toStr);
            }
            return null;
        }

        public JoriaType getType(final NodeInterface[] a) {
            if (a.length == 3) {
                if (a[0].isString() && a[1].isString() && a[2].isString()) {
                    return DefaultStringLiteral.instance();
                }
            }
            return null;
        }
    };

    private static final Exec.ExecString biPadLeft = new Exec.ExecStringAbstract("padLeft", convGroup, Res.str("pad_a_string_left_with_a_character_until_the_number_of_chracters_is_reached"))//trdone
    {
        public String execute(final RunEnv env, final DBData from, final DBData[] args) throws JoriaDataException {
            if (args[0] instanceof DBString && args[1] instanceof DBInt && args[2] instanceof DBString) {
                String value = ((DBString) args[0]).getStringValue();
                long chars = ((DBInt) args[1]).getIntValue();
                String padChar = ((DBString) args[2]).getStringValue().substring(0, 1);
                while (value.length() < chars) {
                    value = padChar + value;
                }
                return value;
            }
            return null;
        }

        public JoriaType getType(final NodeInterface[] a) {
            if (a.length == 3) {
                if (a[0].isString() && a[1].isInteger() && a[2].isString()) {
                    return DefaultStringLiteral.instance();
                }
            }
            return null;
        }
    };

    private static final Exec.ExecString biPadRight = new Exec.ExecStringAbstract("padRight", convGroup, Res.str("pad_a_string_right_with_a_character_until_the_number_of_chracters_is_reached"))//trdone
    {
        public String execute(final RunEnv env, final DBData from, final DBData[] args) throws JoriaDataException {
            if (args[0] instanceof DBString && args[1] instanceof DBInt && args[2] instanceof DBString) {
                String value = ((DBString) args[0]).getStringValue();
                long chars = ((DBInt) args[1]).getIntValue();
                String padChar = ((DBString) args[2]).getStringValue().substring(0, 1);
                while (value.length() < chars) {
                    value = value + padChar;
                }
                return value;
            }
            return null;
        }

        public JoriaType getType(final NodeInterface[] a) {
            if (a.length == 3) {
                if (a[0].isString() && a[1].isInteger() && a[2].isString()) {
                    return DefaultStringLiteral.instance();
                }
            }
            return null;
        }
    };
    private static final Exec.ExecBoolean biIsNull = new Exec.ExecBooleanAbstract("isNull", logicGroup, Res.str("checks_whether_an_object_is_null_boolean_isNull_Object_o"))//trdone
    {
        public boolean execute(RunEnv env, DBData from, DBData[] args) {
            return args[0] == null || args[0].isNull();
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 1)
                return DefaultBooleanLiteral.instance();
            return null;
        }
    };
    private static final Exec.ExecDate biAddWorkingDays = new Exec.ExecDateAbstract("addWorkingDays", datimGroup, Res.str("add_a_number_of_working_days_to_the_data_parameter"))//trdone
    {
        public DBData execute(RunEnv env, DBData from, DBData[] args) {
            Calendar date = ((DBDateTime) args[0]).getCalendar();
            long count = ((DBInt) args[1]).getIntValue();
            boolean sixDayWeek = false;
            if (args.length == 3)
                sixDayWeek = ((DBBoolean) args[2]).getBooleanValue();
            for (int i = 0; i < count; ) {
                date.add(Calendar.DAY_OF_WEEK, 1);
                int day = date.get(Calendar.DAY_OF_WEEK);
                if (day != Calendar.SUNDAY && (day != Calendar.SATURDAY || sixDayWeek))
                    i++;
            }
            return new DBDateTime(args[0].getAccess(), date);
        }

        public JoriaType getType(NodeInterface[] a) {
            if (a.length == 2) {
                if (a[0].isDate() && a[1].isInteger())
                    return JoriaDateTime.instance();
            } else if (a.length == 3) {
                if (a[0].isDate() && a[1].isInteger() && a[2].isBoolean())
                    return JoriaDateTime.instance();
            }
            return null;
        }
    };
    public static final Exec[] builtinList = {biAbs, biAddDayToDate, biAddMonthToDate, biAddWeekToDate, biAddYearToDate, biAddWorkingDays,
            biCase, biCharAt, biClassName, biCompareToIgnoreCase, biConcat, biConcatStringCollection, biContains, biCount, //
			biDate, biDateToMilliSeconds, biEndsWith, //
            biFirst, biFormatInt, biFormatNumber, biFrom, biIndexOf, biIsNaN, biIsNull, //
			biLastDayOfMonth, biLastDayOfQuarter, biLast, biLastIndexOf, biLength, biLocal, biLocale, biMaxLines,
			biPadLeft, biPadRight, biParseFloat, biParseInt, biPower, biPrev,
			biRegex, biReplace, biRoundDate, biRoundFloat, //
            biFileText, biNewline, biPage, //
            biRegex, biSecondsToDate, biSetCounter, biStartsWith, biStepCounter, biSubstring, //
            biSystemProperty, biTotalPages, biToFloat, biToLower, biToString, biToUpperCase, biTrim, biLTrim, biRTrim, biReplaceAll, biTruncFloat,
            biUser};

}
