// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.metafilegraphics;
//MARKER The strings in this file shall not be translated

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

class DebugMetaFileWriter implements MetaFileWriter
{
	protected PrintStream ps;

	DebugMetaFileWriter()
	{
		try
		{
			ps = new PrintStream(new FileOutputStream("metafile.txt"));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public void writeByte(byte b)
	{
		ps.print(b);
		ps.print(' ');

	}
	public void writeCmd(byte b)
	{
		ps.println();
		ps.print(b);
		ps.print(' ');

	}

	public void writeInt(int b)
	{
		ps.print(b);
		ps.print(' ');

	}

	public void writeFloat(float b)
	{
		ps.print(b);
		ps.print(' ');

	}

	public void writeDouble(double b)
	{
		ps.print(b);
		ps.print(' ');
	}

	public void close()
	{
		ps.close();
	}
}
