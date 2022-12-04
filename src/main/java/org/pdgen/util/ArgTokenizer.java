// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.util;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class ArgTokenizer
{

	Reader in;
	char[] unread = new char[256];
	char[] inbuf = new char[1024];
	int inbufLevel = inbuf.length;
	int inbufPos = inbufLevel;
	int unreadCnt;
	int line = 1;
	int charPos = 1;

	public ArgTokenizer(Reader in)
	{
		this.in = in;
	}

	void pushback(char c)
	{
		unread[unreadCnt++] = c;
	}

	char next()
	{
		if (unreadCnt > 0)
		{
			return unread[--unreadCnt];
		}
		try
		{
			char c;
			if (inbufPos >= inbufLevel)
			{
				inbufLevel = in.read(inbuf);
				if (inbufLevel < 0)
					return (char) -1;
				inbufPos = 0;
			}
			c = inbuf[inbufPos++];
			if (c == '\n')
			{
				line++;
				charPos = 0;
			}
			else
				charPos++;
			return c;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			System.exit(1);
		}
		return (char) -1;
	}

	public String nextAttrName() throws Exception
	{
		return scanName();
	}

	public String nextAttrValue() throws Exception
	{
		skipWhitespace();
		char c = next1();
		if (c != '=')
			throw new Exception("= expected at " + charPos);
		return scanString();
	}

	public String nextContent() throws Exception
	{
		char c = next1();
		StringBuffer sb = new StringBuffer();
		while (c != '<')
		{
			if (c == '&')
				c = escape();
			sb.append(c);
			c = next1();
		}
		pushback(c);
		return sb.toString();
	}

	protected String scanName() throws Exception
	{
		skipWhitespace();
		char c = next1();
		if (!Character.isJavaIdentifierStart(c))
		{
			pushback(c);
			return null;
		}
		StringBuffer b = new StringBuffer();
		for (; ;)
		{
			if (Character.isJavaIdentifierPart(c) || c == '.' || c == ':')
			{
				b.append(c);
			}
			else
			{
				pushback(c);
				break;
			}
			c = next1();
		}
        return b.toString();
	}

	void skipWhitespace() throws Exception
	{
		char c;
		do
		{
			c = next1();
		}
		while (Character.isWhitespace(c));
		pushback(c);
	}

	String scanString() throws Exception
	{
		skipWhitespace();
		StringBuffer sb = new StringBuffer();
		char c;
		char first = next();
		if (first != '"' && first != '\'')
			throw new Exception("\" expected " + charPos);
		do
		{
			c = next1();
			if (c == first)
				break;
			if (c == '&')
				c = escape();
			sb.append(c);
		}
		while (true);
		return sb.toString();
	}

	char escape() throws Exception
	{
		char c = next1();
		if (c == 'q' && next1() == 'u' && next1() == 'o' && next1() == 't' && next1() == ';')
			return '"';
		if (c == 'l' && next1() == 't' && next1() == ';')
			return '<';
		if (c == 'g' && next1() == 't' && next1() == ';')
			return '>';
		if (c == 'a')
		{
			c = next1();
			if (c == 'p' && next1() == 'o' && next1() == 's' && next1() == ';')
				return '\'';
			else if (c == 'm' && next1() == 'p' && next1() == ';')
				return '&';
		}
		throw new Exception("Unknown escape sequence (not all are implemented) " + charPos);
	}

	char next1() throws Exception
	{
		char c = next();
		if (c < 0)
			throw new Exception("Unexpected EOF " + charPos);
		return c;
	}

	public int getLine()
	{
		return line;
	}

	public int getCharPos()
	{
		return charPos;
	}

	public Properties getAttributes() throws Exception
	{
		Properties p = new Properties();
		String aName;
		while ((aName = nextAttrName()) != null)
		{
			p.setProperty(aName, nextAttrValue());
		}
		return p;
	}

	public String nextWord() throws Exception
	{
		return scanName();
	}

	public boolean isEof()
	{
		char c = next();
		if (c < 0)
			return true;
		pushback(c);
		return false;
	}

	public void close() throws IOException
	{
		in.close();
	}
}
