// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model;

//MARKER The strings in this file shall not be translated

import java.io.Serializable;

public class GapCharList implements Serializable, CharSequence
{
    private static final long serialVersionUID = 7L;
    char[] a;
	int gapPos;
	int gapLen;
	int hash;
	private static final int step = 10;

	public GapCharList()
	{
	}

	public GapCharList(String s)
	{
		a = new char[s.length()];
		s.getChars(0, s.length(), a, 0);
		checkState();
	}

	public GapCharList(GapCharList t)
	{
		if (t.a != null)
		{
			a = t.a.clone();
			gapPos = t.gapPos;
			gapLen = t.gapLen;
			checkState();
		}
	}

	public GapCharList(int len)
	{
		a = new char[len];
		gapLen = len;
		checkState();
	}

	public GapCharList(char[] chars)
	{
		a = chars;
	}

	void prepare(int needed, int newPos)
	{
		if (a == null)
		{
			final int len = Math.max(needed, step);
			a = new char[len];
			gapLen = len;
		}
		if (needed > gapLen)
		{
			int dec = (a.length - gapLen + needed) / step;
			int newLen = (dec + 1) * step;
			int newGap = newLen - a.length + gapLen;
			char[] newChars = new char[newLen];
			if (newPos < gapPos)
			{
				int remain = a.length - gapPos - gapLen;
				//int rDest = newGap + gapPos - newPos;
				ac(a, 0, newChars, 0, newPos);// copy the chars before the new gap
				int dPos = newPos + newGap;
				ac(a, newPos, newChars, dPos, gapPos - newPos);//copy the chars before the old gap to after the new gap and leave space for additional
				ac(a, gapPos + gapLen, newChars, newLen - remain, remain);// copy the chars after the old gap to the end
			}
			else
			{
				ac(a, 0, newChars, 0, gapPos);// copy the chars before the old gap
				ac(a, gapPos + gapLen, newChars, gapPos, newPos - gapPos);// copy the chars between the old gap and the new gap to the left
				int rDest = newPos + newGap;
				int remain = a.length - gapLen - newPos;
				ac(a, newPos + gapLen, newChars, rDest, remain);// copy the chars after the new gap to the end
			}
			gapLen = newGap;
			a = newChars;
		}
		else if (newPos < gapPos)
		{
			final int len = gapPos - newPos;
			ac(a, newPos, a, gapPos + gapLen - len, len);
		}
		else if (newPos > gapPos)
			ac(a, gapPos + gapLen, a, gapPos, newPos - gapPos);
		gapPos = newPos;
		checkState();
		hash = 0;
	}

	protected void ac(char[] src, int srcPos, char[] dest, int destPos, int length)
	{
		try
		{
			System.arraycopy(src, srcPos, dest, destPos, length);
		}
		catch (Exception e)
		{
			System.err.println("srcPos = " + srcPos + " destPos = " + destPos + " src.length = " + src.length + " destlen = " + dest.length + " length = " + length);
			e.printStackTrace();
		}
	}

	public void insert(char c, int at)
	{
		if (at > length())
			throw new IllegalArgumentException("Insertion of character after end of text: length= " + length() + " at = " + at);
		prepare(1, at);
		a[at] = c;
		gapLen--;
		gapPos++;
		checkState();
	}

	public void insert(String s, int at)
	{
		if (at > length())
			throw new IllegalArgumentException("Insertion of string after end of text: length= " + length() + " at = " + at);
		prepare(s.length(), at);
		s.getChars(0, s.length(), a, gapPos);
		gapPos += s.length();
		gapLen -= s.length();
		checkState();
	}

	public void deleteRange(int from, int to)
	{
		if (from >= length() || to >= length() || from > to)
			throw new IllegalArgumentException("bad deletion range: length= " + length() + " from = " + from + " to = " + to);
		final int len = to - from;
		delete(from, len);
	}

	public void delete(int from, int len)
	{
		int to = from + len;
		if (to == gapPos)
		{
			gapPos -= len;
		}
		else if (from < gapPos)
		{
			if (to < gapPos)
			{
				ac(a, to, a, from, gapPos - to);
				gapPos -= len;
			}
			else
				gapPos = from;
		}
		else if (from > gapPos)
		{
			ac(a, gapPos + gapLen, a, gapPos, from - gapPos);
			gapPos = from;
		}
		gapLen += len;
		checkState();
		hash = 0;
	}

	public char get(int pos)
	{
		if (pos < gapPos)
			return a[pos];
		else
			return a[pos + gapLen];
	}

	public String get(int pos, int len)
	{
		if (pos <= gapPos && pos + len > gapPos)
		{
			char[] ret = new char[len];
			ac(a, pos, ret, 0, gapPos - pos);
			ac(a, gapPos + gapLen, ret, gapPos - pos, len - gapPos + pos);
			return new String(ret);
		}
		else if (pos > gapPos)
			return new String(a, pos + gapLen, len);
		else
			return new String(a, pos, len);
	}

	public char[] getChars(int pos, int len)
	{
		char[] ret = new char[len];
		if (pos <= gapPos && pos + len > gapPos)// hier liegt die Gap in dem Bereich
		{
			ac(a, pos, ret, 0, gapPos - pos);
			ac(a, gapPos + gapLen, ret, gapPos - pos, len - gapPos + pos);
		}
		else if (pos > gapPos)// hier liegt die Gap vor dem Bereich
		{
			ac(a, pos + gapLen, ret, 0, len);
		}
		else
		{
			ac(a, pos, ret, 0, len);
		}
		return ret;
	}

	public String toString()
	{
		if (a == null)
			return "";
		return get(0, a.length - gapLen);
	}

	public void compact()
	{
		char[] ret = new char[a.length - gapLen];
		ac(a, 0, ret, 0, gapPos);
		ac(a, gapPos + gapLen, ret, gapPos, ret.length - gapPos);
		gapPos = 0;
		gapLen = 0;
		a = ret;
		checkState();
	}

	public int length()
	{
		if (a == null)
			return 0;
		return a.length - gapLen;
	}

	public char charAt(int index)
	{
		return get(index);
	}

	public String subSequence(int start, int end)
	{
		return get(start, end - start);
	}

	public void clear()
	{
		gapPos = 0;
		if (a != null)
			gapLen = a.length;
		checkState();
		hash = 0;
	}

	public void append(char c)
	{
		insert(c, length() - 1);
	}

	public void set(String newText)
	{
		gapPos = 0;
		gapLen = 0;
		a = newText.toCharArray();
		checkState();
		hash = 0;
	}

	public int wordForward(int from)
	{
		int p = from;
		char ws = 'x';
		while (p < length() && Character.isWhitespace(ws = get(p)))
		{
			p++;
		}
		if (Character.isWhitespace(get(from)) || p >= length())// we started with whitespace: stop at first non-whitespace
			return p;
		if (Character.isLetterOrDigit(ws))
		{
			while (p < length() && Character.isLetterOrDigit(get(p)))
			{
				p++;
			}
		}
		else
		{
			while (p < length() && !Character.isLetterOrDigit(get(p)) && !Character.isWhitespace(get(p)))
			{
				p++;
			}
		}
		while (p < length() && Character.isWhitespace(get(p)))
		{
			p++;
		}
		return p;
	}

	public int wordBackward(int from)
	{
		if (from == 0)
			return 0;
		int p = from - 1;
		char c = 'x';
		while (p > 0 && Character.isWhitespace(c = get(p)))
		{
			p--;
		}
		if (p == 0)
			return 0;
		if (Character.isLetterOrDigit(c))
		{
			c = get(p);
			while (Character.isLetterOrDigit(c))
			{
				p--;
				if (p <= 0)
					return p;
				c = get(p);
			}
		}
		else
		{
			c = get(p);
			while (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c))
			{
				p--;
				if (p == 0)
					return p;
				c = get(p);
			}
		}
		return p + 1;
	}

	public void append(GapCharList text)
	{
		prepare(text.length(), length());
		ac(text.a, 0, a, gapPos, text.gapPos);
		int rlen = text.a.length - text.gapLen - text.gapPos;
		//System.out.println("rlen = " + rlen);
		ac(text.a, text.gapPos + text.gapLen, a, gapPos + text.gapPos, rlen);
		gapPos += text.length();
		gapLen -= text.length();
		checkState();
	}

	void checkState()
	{
		if (gapPos < 0)
			throw new RuntimeException("Gapcharlist out of order: gapPos < 0");
		else if (gapPos > a.length)
			throw new RuntimeException("Gapcharlist out of order: gapPos > length");
		else if ((gapPos == a.length && gapLen != 0))
			throw new RuntimeException("Gapcharlist out of order: gapPos == length && gapLen > 0");
		else if (gapLen < 0)
			throw new RuntimeException("Gapcharlist out of order: gapLen < 0");
		else if (gapLen > a.length)
			throw new RuntimeException("Gapcharlist out of order: gapLen > length");
	}

	public int hashCode()
	{
		int h = hash;
		if (h == 0)
		{
			int off = 0;
			int len = length();
			for (int i = 0; i < len; i++)
			{
				h = 31 * h + get(off++);
			}
			hash = h;
		}
		return h;
	}

	public boolean hasText(final String text)
	{
		final char[] chars = getChars(0, length());
		for (int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			chars[i] = Character.toLowerCase(c);
		}
		String s = new String(chars);
		boolean ret = s.contains(text);
		return ret;
	}

	public boolean containsChar(char c)
	{
		return indexOf(c, 0) >= 0;
	}

	public int indexOf(final char c, int at)
	{
		for (int i = at; i < length(); i++)
		{
			final char c1 = get(i);
			if (c == c1)
				return i;
		}
		return -1;
	}

	public int indexOf(String target, int fromIndex)
	{
		int targetCount = target.length();
		int sourceCount = length();
		if (fromIndex >= sourceCount)
		{
			return (targetCount == 0 ? sourceCount : -1);
		}
		if (fromIndex < 0)
		{
			fromIndex = 0;
		}
		if (targetCount == 0)
		{
			return fromIndex;
		}
		char first = target.charAt(0);
		int max = (sourceCount - targetCount);
		for (int i = fromIndex; i <= max; i++)
		{
			/* Look for first character. */
			if (get(i) != first)
			{
				if (++i <= max && get(i) != first) {
					do i++;
					while (i <= max && get(i) != first);
				}
			}
			/* Found first character, now look at the rest of v2 */
			if (i <= max)
			{
				int j = i + 1;
				int end = j + targetCount - 1;
				int k = 1;
				while (j < end && get(j) == target.charAt(k)) {
					j++;
					k++;
				}
				if (j == end)
				{
					/* Found whole string. */
					return i;
				}
			}
		}
		return -1;
	}
}
