// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.util.StringUtils;
import org.pdgen.env.Env;
import org.pdgen.env.Res;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.*;

public class Internationalisation2 extends Internationalisation
{
    private static final long serialVersionUID = 7L;

    public Internationalisation2(String properties, String directory)
	{
		super(properties, directory);
	}

	protected Properties makeProperties(Locale loc)
	{
		Properties p = new Properties();
		try
		{
			if (loc == null)
				loc = Env.instance().getCurrentLocale();
			if (loc == null)
				loc = Locale.getDefault();
			String[] localext = new String[4];
			final String pn = Res.asis(".properties");
			localext[0] = propertiesName + pn;
			localext[1] = propertiesName + "_" + loc.getLanguage() + pn;
			final int tries;
			if (loc.getVariant() != null && loc.getVariant().length() > 0)
			{
				tries = 4;
				localext[3] = propertiesName + "_" + loc.getLanguage() + "_" + loc.getCountry() + "_" + loc.getVariant() + pn;
                localext[2] = propertiesName + "_" + loc.getLanguage() + "_" + loc.getCountry() + pn;
			}
			else if (loc.getCountry() != null && loc.getCountry().length() > 0)
			{
				tries = 3;
				localext[2] = propertiesName + "_" + loc.getLanguage() + "_" + loc.getCountry() + pn;
			}
			else
				tries = 2;
			for (int i = 0; i < tries; i++)
			{
                System.out.println("I18n2.makeProperties: try "+i+" filename "+localext[i]+" for "+loc);
                StringBuffer errs = null;
				File f = null;
				try
				{
					f = new File(directory, localext[i]);
					if (!Env.instance().getFileService().existsAsFile(f.getPath()))
					{
						Trace.logDebug(Trace.init, Res.asis("Looking for localisation keys in: ") + f.getAbsolutePath());
						continue;
					}
					else
						Trace.logDebug(Trace.init, Res.asis("Found localisation keys in: ") + f.getAbsolutePath());
					errs = readProperties(f, p, errs);
				}
				finally
				{
					if (errs != null)
						Env.instance().tell(Res.msg("Reading_the_localisation_file__0_produced_the_following_syntax_errors", f.getAbsolutePath()), errs.toString());
				}
			}
		}
		catch (IOException ex)
		{
			Trace.log(ex);
			//ex.printStackTrace();
		}
		return p;
	}

	public static StringBuffer readProperties(File f, Properties p, StringBuffer errs) throws IOException
	{
		BufferedReader r = new BufferedReader(new InputStreamReader(Env.instance().getFileService().getFileData(f.getPath())));
		String l;
		Parser pars = new Parser();
		int lc = 1;
		while ((l = r.readLine()) != null)
		{
			try
			{
				if (pars.parse(l))
					p.setProperty(pars.key, pars.val);
			}
			catch (ParseException ex)
			{
				if (errs == null)
					errs = new StringBuffer();
				errs.append(ex.getMessage()).append(Res.str("at_line")).append(lc).append(Res.str("pos")).append(ex.getErrorOffset()).append("\n");
			}
			lc++;
		}
		return errs;
	}

	public static String localize(String s, Locale loc)
	{
        Internationalisation2 i18n = (Internationalisation2) Env.instance().repo().i18n;
		if (s == null || i18n == null || loc == Internationalisation.NOREPLACE)
			return s;
		String val = i18n.get(s, loc);
		if (val == null)
			return s;
		return val;
	}

	public static void collectI18nKeys(String s, HashMap<String, List<I18nKeyHolder>> map, I18nKeyHolder src)
	{
		Trace.logDebug(Trace.mapdir, "Collecting strings: " + s);
        Internationalisation i18n = Env.instance().repo().i18n;
		if (StringUtils.isEmpty(s) || i18n == null)
			return;
		List<I18nKeyHolder> l = map.get(s);
		if (l == null)
		{
			l = new ArrayList<I18nKeyHolder>();
			map.put(s, l);
		}
		l.add(src);
	}

	static class Parser
	{
		String s;
		int pos;
		String key;
		String val;

		boolean parse(String l) throws ParseException
		{
			s = l;
			pos = 0;
			key = null;
			val = null;
			char c = skipws();
			if (c == '#' || c == 0)
				return false;
			if (c == '"')
			{
				key = readString();
				if (StringUtils.isEmpty(key))
					throw new ParseException(Res.str("Empty_string_is_not_allowed_as_key"), pos);
			}
			else
				key = readUnquoted();
			c = skipws();
			if (c != ':' && c != '=')
				throw new ParseException(Res.str("Expected_or_between_key_and_value"), pos);
			pos++;
			c = skipws();
			if (c == '"')
				val = readString();
			else
			{
				val = readUnquoted();
			}
			if (pos < s.length() && s.charAt(pos) != '#')
				throw new ParseException(Res.str("Unexpected_text_after_value"), pos);
			return key != null;
		}

		private String readUnquoted() throws ParseException
		{
			if (pos == s.length())
				throw new ParseException(Res.str("Empty_string_must_be_denoted_by"), pos);
			StringBuffer b = new StringBuffer();
			while (pos < s.length())
			{
				char c = s.charAt(pos);
				if (!Character.isLetterOrDigit(c))
					break;
				b.append(c);
				pos++;
			}
			return b.toString();
		}

		private String readString() throws ParseException
		{
			pos++;
			StringBuffer b = new StringBuffer();
			while (pos < s.length())
			{
				char c = s.charAt(pos);
				if (c == '\\')
				{
					if (pos == s.length())
						throw new ParseException(Res.str("Unexpected_end_of_string"), pos);
					pos++;
					char c2 = s.charAt(pos);
					if (c2 == 'n')
						b.append('\n');
					else
						b.append(c2);
				}
				else if (c == '"')
				{
					pos++;
					return b.toString();
				}
				else
					b.append(c);
				pos++;
			}
			throw new ParseException(Res.str("String_incomplete"), pos);
		}

		char skipws()
		{
			while (pos < s.length())
			{
				char c = s.charAt(pos);
				if (!Character.isSpaceChar(c))
				{
					return c;
				}
				pos++;
			}
			return 0;
		}
	}
}
