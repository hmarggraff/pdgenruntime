// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.metafilegraphics;
//MARKER The strings in this file shall not be translated

import java.io.StreamTokenizer;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

class DebugMetaFileReader implements MetaFileReader
{
	StreamTokenizer st;
	int line;
	int tokNum;
	protected FileReader in;

	DebugMetaFileReader()
	{
		reset();
	}

	public byte nextCmd()
	{
		try
		{
			int t = st.nextToken();
			if (t != StreamTokenizer.TT_EOL)
			{
				throw new Error("Newline expected at line: " + line);

			}
			line++;
			t = st.nextToken();
			if (t != StreamTokenizer.TT_NUMBER)
			{
				throw new Error("Number expected at line: " + line);
			}
			byte ret = (byte) st.nval;
			return ret;
		}
		catch (IOException e)
		{
			System.out.println("IO Error at line: " + line);
			e.printStackTrace();
			return -1;
		}
	}

	public int readInt() throws IOException
	{
		try
		{
			int t = st.nextToken();
			if (t != StreamTokenizer.TT_NUMBER)
			{
				throw new Error("Number expected at line: " + line);
			}
			int ret = (int) st.nval;
			return ret;
		}
		catch (IOException e)
		{
			System.out.println("IO Error at line: " + line);
			e.printStackTrace();
			return -1;
		}

	}

	public byte readByte() throws IOException
	{
		try
		{
			int t = st.nextToken();
			if (t != StreamTokenizer.TT_NUMBER)
			{
				throw new Error("Number expected at line: " + line);
			}
			byte ret = (byte) st.nval;
			return ret;
		}
		catch (IOException e)
		{
			System.out.println("IO Error at line: " + line);
			e.printStackTrace();
			return -1;
		}

	}

	public float readFloat() throws IOException
	{
		try
		{
			int t = st.nextToken();
			if (t != StreamTokenizer.TT_NUMBER)
			{
				throw new Error("Number expected at line: " + line);
			}
			float ret = (float) st.nval;
			return ret;
		}
		catch (IOException e)
		{
			System.out.println("IO Error at line: " + line);
			e.printStackTrace();
			return -1;
		}

	}

	public double readDouble() throws IOException
	{
		try
		{
			int t = st.nextToken();
			if (t != StreamTokenizer.TT_NUMBER)
			{
				throw new Error("Number expected at line: " + line);
			}
			return st.nval;
		}
		catch (IOException e)
		{
			System.out.println("IO Error at line: " + line);
			e.printStackTrace();
			return -1;
		}

	}

	public void close()
	{
		try
		{
			in.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

	}

	public void reset()
	{
		try
		{
			in = new FileReader("metafile.txt");
			st = new StreamTokenizer(in);
			st.parseNumbers();
			st.eolIsSignificant(true);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
	}
}
