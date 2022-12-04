// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;
//MARKER The strings in this file shall not be translated

import java.io.Serializable;

// wird nur noch zum Einlesen benutzt.
public class CellBorder implements Serializable
{
    private static final long serialVersionUID = 7L;
    private JoriaBorder top = JoriaBorder.zeroBorder;
	private JoriaBorder left = JoriaBorder.zeroBorder;
	private JoriaBorder right = JoriaBorder.zeroBorder;
	private JoriaBorder bottom = JoriaBorder.zeroBorder;
    public static final CellBorder defaultHeaderBorder;
	public static final CellBorder defaultFooterBorder;
	public static final CellBorder plainBorder;
	public static final CellBorder spaceBorder;
	public static final CellBorder zeroBorder = new CellBorder();
	public static final CellBorder insetBorder;

	static
	{
		defaultFooterBorder = new CellBorder();
        defaultFooterBorder.top = new JoriaBorder(JoriaBorder.SOLID, 2f, java.awt.Color.black, 6f, 12f);
		defaultHeaderBorder = new CellBorder();
        defaultHeaderBorder.bottom = new JoriaBorder(JoriaBorder.SOLID, 2f, java.awt.Color.black, 6f, 12f);
		plainBorder = new CellBorder();
        plainBorder.top = new JoriaBorder(JoriaBorder.SOLID, 1f, java.awt.Color.black, 6f, 12f);
        plainBorder.bottom = new JoriaBorder(JoriaBorder.SOLID, 1f, java.awt.Color.black, 6f, 12f);
        plainBorder.left = new JoriaBorder(JoriaBorder.SOLID, 1f, java.awt.Color.black, 6f, 12f);
        plainBorder.right = new JoriaBorder(JoriaBorder.SOLID, 1f, java.awt.Color.black, 6f, 12f);
		spaceBorder = new CellBorder();
        spaceBorder.top = new JoriaBorder(JoriaBorder.SOLID, 1f, java.awt.Color.black, 6f, 12f);
        spaceBorder.bottom = new JoriaBorder(JoriaBorder.SOLID, 1f, java.awt.Color.black, 6f, 12f);
        spaceBorder.left = new JoriaBorder(JoriaBorder.SOLID, 1f, java.awt.Color.black, 6f, 12f);
        spaceBorder.right = new JoriaBorder(JoriaBorder.SOLID, 1f, java.awt.Color.black, 6f, 12f);
		insetBorder = new CellBorder();
        insetBorder.left = new JoriaBorder(JoriaBorder.SOLID, 0f, java.awt.Color.black, 0, 3);
        insetBorder.right = new JoriaBorder(JoriaBorder.SOLID, 0f, java.awt.Color.black, 0, 3);
	}

	/** ----------------------------------------------------------------------- CellBorder */
	public CellBorder()
	{
	}

	/** ----------------------------------------------------------------------- CellBorder */
	public CellBorder(CellBorder from)
	{
		if (from == null)
			return;
		top = JoriaBorder.newFrom(from.top);
		left = JoriaBorder.newFrom(from.left);
		right = JoriaBorder.newFrom(from.right);
		bottom = JoriaBorder.newFrom(from.bottom);
	}

	/** ----------------------------------------------------------------------- getBottom */
	public JoriaBorder getBottom()
	{
		return bottom;
	}

    /** ----------------------------------------------------------------------- getLeft */
    public JoriaBorder getLeft()
    {
        return left;
    }

	/** ----------------------------------------------------------------------- getRight */
	public JoriaBorder getRight()
	{
		return right;
	}

	/** ----------------------------------------------------------------------- getTop */
	public JoriaBorder getTop()
	{
		return top;
	}

    /** ----------------------------------------------------------------------- newFrom */
	public boolean equals(Object parm1)
	{
		if (parm1 == null || (parm1.getClass() != CellBorder.class))
			return false;
		else
		{
			CellBorder b = (CellBorder)parm1;
			return StyleBase.eq(left, b.left) && StyleBase.eq(right, b.right) && StyleBase.eq(top, b.top) && StyleBase.eq(bottom, b.bottom);
		}
	}

	protected Object readResolve() throws java.io.ObjectStreamException
	{
		if (isZero())
			return zeroBorder;
		else
			return this;
	}

	public boolean isZero()
	{
		return left == JoriaBorder.zeroBorder && right == JoriaBorder.zeroBorder && top == JoriaBorder.zeroBorder && bottom == JoriaBorder.zeroBorder;
	}

	public String toString()
	{
		return "CellBorder(top(" + top +"), right(" + right +"), bottom(" + bottom +"), left(" + left +"))";
	}
}
