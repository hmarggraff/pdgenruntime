// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.util;

public class IndCharSeq implements CharSequence
{
	int len;

	public IndCharSeq(final int len)
	{
		this.len = Math.max(0, len);
	}

	public int length()
	{
		return len;
	}

	public char charAt(int index)
	{
		return ' ';
	}

	public CharSequence subSequence(int start, int end)
	{
		return new IndCharSeq(end - start);
	}
}
