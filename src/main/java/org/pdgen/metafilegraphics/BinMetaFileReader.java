// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.metafilegraphics;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class BinMetaFileReader implements MetaFileReader
{
	private DataInputStream in;
	private ByteArrayInputStream bin;
	byte[] data;

	public BinMetaFileReader(byte[] data)
	{
		this.data = data;
		reset();
	}

	public void close() throws IOException
	{
		in.close();
		bin.close();
		bin = null;
		in = null;
	}

	public byte nextCmd() throws IOException
	{
		return in.readByte();
	}

	public byte readByte() throws IOException
	{
		return in.readByte();
	}

	public double readDouble() throws IOException
	{
		return in.readDouble();
	}

	public float readFloat() throws IOException
	{
		return in.readFloat();
	}

	public int readInt() throws IOException
	{
		return in.readInt();
	}

	public void reset()
	{
		bin = new ByteArrayInputStream(data);
		in = new DataInputStream(bin);
	}

}
