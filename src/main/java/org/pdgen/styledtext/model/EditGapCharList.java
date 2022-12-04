// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model;

class EditGapCharList extends GapCharList
{
    private static final long serialVersionUID = 7L;

    EditGapCharList(GapCharList t) // in editing mode each text has a blank appended, which allows to select the ond of a paragraph. For rendering purposes this blank is removed again
	{
		if (t == null || t.a == null)
		{
			a = new char[]{' '};
		}
		else
		{
			final int tlen = t.a.length;
			a = new char[tlen];
			System.arraycopy(t.a, 0, a, 0, tlen);
			gapPos = t.gapPos;
			gapLen = t.gapLen;
			if (!(t instanceof EditGapCharList))
				append(' ');
			checkState();
		}
	}

	public EditGapCharList()
	{
		a = new char[1];
		a[0] = ' ';
	}

	public EditGapCharList(String s)
	{
		super(s);
		append(' ');
	}

	public EditGapCharList(int len)
	{
		super(len);
	}

	public EditGapCharList(char[] chars)
	{
		super(chars);
	}

	public String toString()
	{
		if (a == null)
			return "";
		return new String(getChars(0, a.length - gapLen - 1));
	}

	public GapCharList getNoEditing()
	{
		char[] ret = getChars(0, a.length - gapLen - 1);
		return new GapCharList(ret);
	}
}
