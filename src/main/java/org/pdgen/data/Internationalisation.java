// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.env.JoriaExternalErrorWrapped;
import org.pdgen.env.Env;
import org.pdgen.env.Res;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;

public class Internationalisation implements Serializable
{
    private static final long serialVersionUID = 7L;
    String propertiesName;
	protected String directory;
	private transient HashMap<Locale, Properties> resources;
	public static final Locale NOREPLACE = new Locale(Res.asis("en"), Res.asis("US"));
	public static String defaultBundleName;
	public static final String propSuffix = Res.asis(".properties");
	private static char eosChar;

	static
	{
		String eos = System.getProperty(Res.asis("i18n.eos"));
		if (eos != null)
		{
            eosChar = eos.charAt(0);
		}
	}

	public Internationalisation(String properties, String directory)
	{
		propertiesName = properties;
        this.directory = Env.instance().repo().getStoredFileName(directory);
        defaultBundleName = properties + propSuffix;
	}


	public String get(String key, Locale loc)
	{
		if (resources == null)
			resources = new HashMap<Locale, Properties>();
		Properties prop = resources.get(loc);
		if (prop == null)
		{
			prop = makeProperties(loc);
			resources.put(loc, prop);
		}
		String str = prop.getProperty(key);
		if (str != null && str.charAt(str.length() - 1) == eosChar)
		{
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	public static boolean useInternationalisation()
	{
        return Env.instance().repo().i18n != null;
	}

	public static String localize(String s, Locale loc)
	{
        Internationalisation i18n = Env.instance().repo().i18n;
		if (i18n instanceof Internationalisation2)
			return Internationalisation2.localize(s, loc);
		if (s == null || i18n == null || s.indexOf('{') < 0 || loc == NOREPLACE)
			return s;
		StringBuffer sb = new StringBuffer();
		int at1;
		int at2 = 0;
		while ((at1 = s.indexOf('{', at2)) >= 0)
		{
			if (at1 > 0 && s.charAt(at1 - 1) == '\\')
			{
				sb.append(s, at2, at1 - 1);
				sb.append('{');
				at2 = at1 + 1;
				continue;
			}
			sb.append(s, at2, at1);
			do
			{
				at1++; // advance beyond the brace {
				at2 = s.indexOf('}', at1 + 1);
				if (at2 < 0)
					throw new JoriaExternalErrorWrapped(Res.strp("in", s));
				if (s.charAt(at2 - 1) == '\\') // closing brace escaped with a backslash
				{
					at1 = at2;
					continue;
				}
				String key = s.substring(at1, at2);
				at2++;
				String val = i18n.get(key, loc);
				if (val == null)
				{
					Env.instance().tell(Res.asis("An internationalisation key was not found: ") + key + Res.asis(" in ") + s);
					val = key;
				}
				sb.append(val);
				break;
			}
			while (true);
		}
		sb.append(s.substring(at2));
		return sb.toString();
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
			localext[0] = propertiesName + Res.asis(".properties");
			localext[1] = propertiesName + "_" + loc.getLanguage() + Res.asis(".properties");
			localext[2] = propertiesName + "_" + loc.getLanguage() + "_" + loc.getCountry() + Res.asis(".properties");
			localext[3] = propertiesName + "_" + loc.getLanguage() + "_" + loc.getCountry() + "_" + loc.getVariant() + Res.asis(".properties");
			final int tries;
			if (loc.getVariant() != null && loc.getVariant().length() > 0)
			{
				tries = 4;
			}
			else
				tries = 3;
			for (int i = 0; i < tries; i++)
			{
				InputStream is = null;
				try
				{
					File f = new File(directory, localext[i]);
					if (!Env.instance().getFileService().existsAsFile(f.getAbsolutePath()))
					{
						Trace.logDebug(Trace.init, Res.asis("Looking for localisation keys in: ") + f.getAbsolutePath());
						continue;
					}
					else
						Trace.logDebug(Trace.init, Res.asis("Found localisation keys in: ") + f.getAbsolutePath());
					is = Env.instance().getFileService().getFileData(f.getAbsolutePath());
					p.load(is);
				}
				finally
				{
					if (is != null)
						is.close();
				}
			}
		}
		catch (IOException ex)
		{
			Trace.log(ex);
		}
		return p;
	}

	public String getPropertiesName()
	{
		return propertiesName;
	}

	public String getDirectory()
	{
		return directory;
	}

	public void setDirectory(String directory)
	{
		this.directory = directory;
	}

	protected Object readResolve() throws ObjectStreamException
	{
        defaultBundleName = propertiesName + propSuffix;
		return this;
	}

	public static void collectI18nKeys(String s, HashSet<String> keySet)
	{
		Trace.logDebug(Trace.mapdir, "Collecting i18nString: " + s);
        Internationalisation i18n = Env.instance().repo().i18n;
		if (s == null || i18n == null || s.indexOf('{') < 0)
			return;
		int at1;
		int at2 = 0;
		while ((at1 = s.indexOf('{', at2)) >= 0)
		{
			if (at1 > 0 && s.charAt(at1 - 1) == '\\')
			{
				at2 = at1 + 1;
				continue;
			}
			do
			{
				at1++; // advance beyond the brace {
				at2 = s.indexOf('}', at1 + 1);
				if (at2 < 0)
					throw new JoriaExternalErrorWrapped(Res.strp("in", s));
				if (s.charAt(at2 - 1) == '\\') // closing brace escaped with a backslash
				{
					at1 = at2;
					continue;
				}
				String key = s.substring(at1, at2);
				Trace.logDebug(Trace.mapdir, "Keeping i18nString: " + s);
				keySet.add(key);
				at2++;
				break;
			}
			while (true);
		}
	}

	public static String localizeFileName(String name, Locale loc)
	{
		try
		{
			if (name.startsWith(Res.asis("file:/")))
				name = name.substring(6);
            Internationalisation i18n = Env.instance().repo().i18n;
			if (i18n == null)
			{
				return name;
			}
			int offset = name.lastIndexOf('.');
			String baseName = name;
			String extentsion = "";
			if (offset > 1)
			{
				baseName = name.substring(0, offset);
				extentsion = name.substring(offset);
			}
			if (loc == null)
			{
				loc = Env.instance().getCurrentLocale();
			}
			if (loc == null)
			{
				loc = Locale.getDefault();
			}
			if (loc == null)
			{
				return name;
			}
			String language = loc.getLanguage();
			String country = loc.getCountry();
			String variant = loc.getVariant();
			if (variant != null && variant.length() > 0)
			{
				String f = baseName + "_" + language + "_" + country + "_" + variant + extentsion;
				if (Env.instance().getFileService().existsAsFile(f))
				{
					return f;
				}
			}
			if (country != null && country.length() > 0)
			{
				String f = baseName + "_" + language + "_" + country + extentsion;
				if (Env.instance().getFileService().existsAsFile(f))
				{
					return f;
				}
			}
			if (language != null && language.length() > 0)
			{
				String f = baseName + "_" + language + extentsion;
				if (Env.instance().getFileService().existsAsFile(f))
				{
					return f;
				}
			}
			String f = name;
			if (Env.instance().getFileService().existsAsFile(f))
				return f;
			else
				return f;
		}
		catch (IOException e)
		{
			Env.instance().handle(e);
			return null;
		}
	}
}
