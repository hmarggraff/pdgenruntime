// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.util;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class StringUtils
{
	public static String trimNull(String s)
	{
		if (s == null)
			return null;
		s = s.trim();
		if (s.length() == 0)
			return null;
		else
			return s;
	}

	public static String trimNullRight(String s)
	{
		if (s == null)
			return null;
		int l;
		for (l = s.length() - 1; l >= 0; l--)
		{
			if (s.charAt(l) > ' ')
				break;
		}
		s = s.substring(0, l + 1);
		if (s.length() == 0)
			return null;
		else
			return s;
	}

	public static boolean isEmpty(String s)
	{
		if (s == null)
			return true;
		for (int i = 0; i < s.length(); i++)
		{
			if (!Character.isWhitespace(s.charAt(i)))
				return false;
		}
		return true;
	}

	public static int compareIgnoreCase(String s, String s1)
	{
		if (s == null)
		{
			if (s1 == null)
				return 0;
			else
				return -1;
		}
		else if (s1 == null)
			return 1;
		else
			return s.compareToIgnoreCase(s1);
	}

	public static int compare(String s, String s1)
	{
		if (s == null)
		{
			if (s1 == null)
				return 0;
			else
				return -1;
		}
		else if (s1 == null)
			return 1;
		else
			return s.compareTo(s1);
	}

	public static boolean isWord(String k)
	{
		int i = 0;
		while (i < k.length() && Character.isLetterOrDigit(k.charAt(i)))
		{
			i++;
		}
		return i == k.length();
	}

	public static boolean isJavaIdentifier(String k)
	{
		if (k == null || k.length() == 0)
			return false;
		if (!Character.isJavaIdentifierStart(k.charAt(0)))
			return false;
		int i = 1;
		while (i < k.length() && Character.isJavaIdentifierPart(k.charAt(i)))
		{
			i++;
		}
		return i == k.length();
	}

	public static boolean isJavaIdentifierWithPackage(String k)
	{
		if (k == null || k.length() == 0)
			return false;
		int i = 0;
		int p = 0;// counts packages
		while (i < k.length())
		{
			while (i < k.length() && Character.isSpaceChar(k.charAt(i)))
			{
				i++;
			}
			if (i >= k.length())
				break;
			if (p > 0 && k.charAt(i) != '.')
				break;
			else if (p > 0)
				i++;
			while (i < k.length() && Character.isSpaceChar(k.charAt(i)))
			{
				i++;
			}
			if (i >= k.length() || !Character.isJavaIdentifierStart(k.charAt(i)))
				return false;
			else
				i++;
			while (i < k.length() && Character.isJavaIdentifierPart(k.charAt(i)))
			{
				i++;
			}
			p++;
		}
		return i == k.length() && p > 0;
	}

	public static String[] split(String s, String sep)
	{
		if (s == null)
			return null;
		ArrayList<String> l = new ArrayList<String>();
		int at = 0;
		while (at < s.length())
		{
			final int pos = s.indexOf(sep, at);
			if (pos < 0)
				break;
			l.add(s.substring(at, pos));
			at = pos + 1;
		}
		return l.toArray(new String[l.size()]);
	}

	public static String[] splitLines(String s)
	{
		if (s == null)
			return null;
		ArrayList<String> l = new ArrayList<String>();
		int at = 0;
		StringBuffer sb = new StringBuffer();
		while (at < s.length())
		{
			final char c = s.charAt(at);
			int nat = at;
			boolean split = false;
			if (c == '\n')
			{
				split = true;
				if (at < s.length() - 1 && s.charAt(at + 1) == '\r')
				{
					nat++;
				}
			}
			else if (c == '\r')
				split = true;
			if (split)
			{
				l.add(sb.toString());
				sb = new StringBuffer();
				at = nat;
			}
			else
				sb.append(c);
			at++;
		}
		l.add(sb.toString());
		return l.toArray(new String[l.size()]);
	}

	public static String[] splitAndTrim(String s, String sep)
	{
		if (s == null)
			return null;
		ArrayList<String> l = new ArrayList<String>();
		int at = 0;
		while (at < s.length())
		{
			final int pos = s.indexOf(sep, at);
			if (pos < 0)
			{
				l.add(s.substring(at));
				break;
			}
			l.add(s.substring(at, pos).trim());
			at = pos + 1;
		}
		return l.toArray(new String[l.size()]);
	}

	public static String removePackagesFromName(String name)
	{
		if (name == null)
			return null;
		int ix = name.lastIndexOf('.');
		if (ix < 0)
			return null;
		else
			return name.substring(ix + 1);
	}

	public static String byteArrayToString(byte[] key)
	{
		StringBuffer sb = new StringBuffer(key.length * 2);
		for (short b : key)
		{
			if (b < 0)
				b += 0xff;
			String s = Integer.toHexString(b);
			sb.append(s);
		}
		return sb.toString();
	}

	public static String concat(String... fields)
	{
		if (fields == null)
			return null;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < fields.length; i++)
		{
			String field = fields[i];
			if (!isEmpty(field))
			{
				if (i > 0)
					sb.append(' ');
				sb.append(field);
			}
		}
		return sb.toString();
	}

	public static boolean globStart(String candidate, String pattern)
	{
		int at = 0;
		if (pattern == null || pattern.length() == 0)
			return true;
		if (candidate == null)
			return false;
		candidate = candidate.toLowerCase();
		boolean firstMustMatch = pattern.charAt(0) != ' ' && pattern.charAt(0) != '*';
		for (StringTokenizer stringTokenizer = new StringTokenizer(pattern, "* "); stringTokenizer.hasMoreTokens(); )
		{
			String s = stringTokenizer.nextToken();
			int ix = candidate.indexOf(s, at);
			if (ix < 0 || (at == 0 && ix > 0 && firstMustMatch))
				return false;
			at = ix + s.length();
			firstMustMatch = false;
		}
		return true;
	}

	public static boolean glob(String candidate, String pattern)
	{
		int at = 0;
		if (pattern == null || pattern.length() == 0)
			return true;
		if (candidate == null)
			return false;
		boolean firstMustMatch = pattern.charAt(0) != ' ' && pattern.charAt(0) != '*';
		for (StringTokenizer stringTokenizer = new StringTokenizer(pattern, "* "); stringTokenizer.hasMoreTokens(); )
		{
			String s = stringTokenizer.nextToken();
			int ix = candidate.indexOf(s, at);
			if (ix < 0 || (at == 0 && ix > 0 && firstMustMatch))
				return false;
			at = ix + s.length();
			firstMustMatch = false;
		}
		return at == candidate.length();
	}

	public static String tailName(String name)
	{
		if (name == null)
			return null;
		int dot = name.lastIndexOf('.');
		if (dot < 0)
			return name;
		String ret = name.substring(dot + 1);
		return ret;
	}

	public static String slice(String txt, String start, String end)
	{
		if (txt == null)
			return null;
		int dot = txt.indexOf(start);
		if (dot < 0)
			return null;
		int eix = txt.lastIndexOf(end);
		if (eix < 0)
		{
			String ret = txt.substring(dot + 1);
			return ret;
		}
		else
			return txt.substring(dot+1, eix);
	}

	public static boolean isInteger(String s)
	{
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}

	public static String isInteger(String s, String errorMessage)
	{
		if (isInteger(s))
			return null;
		else
			return errorMessage;
	}

	public static boolean isAllUpper(String s)
	{
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (Character.isLowerCase(c))
				return false;
		}
		return true;
	}

	public static boolean contains(String s, String[] list)
	{
		for (String s1 : list)
		{
			if (s1 == null && s == null)
				return true;
			if (s.equals(s1))
				return true;
		}
		return false;
	}
	public static boolean containsChar(String s, String charset)
	{
		if (isEmpty(s))
			return false;
		for (int i = 0; i < charset.length(); i++)
		{
			char s1 = charset.charAt(i);
			if (s.indexOf(s1) > 0)
				return true;
		}
		return false;
	}

	public static boolean containsIgnoringCase(String s, String[] list)
	{
		for (String s1 : list)
		{
			if (s1 == null && s == null)
				return true;
			if (s.equalsIgnoreCase(s1))
				return true;
		}
		return false;
	}

	public static boolean eq(String s1, String s2)
	{
		return s1 == null && s2 == null || s1 != null && s1.equals(s2);
	}

	public static String morphToJavaIdentifier(final String sb)
	{
		StringBuffer ret = new StringBuffer(sb.length());
		boolean had_underline = false;
		for (int i = 0; i < sb.length(); i++)
		{
			final char c = sb.charAt(i);
			if ((i == 0 && !Character.isJavaIdentifierStart(c)) || !Character.isJavaIdentifierPart(c))
			{
				if (!had_underline)
				{
					ret.append('_');
					had_underline = true;
				}
			}
			else
			{
				ret.append(c);
				had_underline = false;
			}
		}
		return ret.toString();
	}
}
