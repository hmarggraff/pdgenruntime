// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import java.awt.*;
import java.io.Serializable;
import java.util.Objects;

// Diese Klasse wird mit dem neuen Borderkonzept entsorgt.
public class TableBorder implements Serializable
{
    private static final long serialVersionUID = 7L;
    /// -> Border Ueber der Tabelle.
 	public float top;
    /// -> Border zwischen Header und Tabelle.
    public float neck;
    /// -> Border unter Tabelle.
    public float bottom;
    /// -> Border zwischen Headerspalten.
    public float vInsideHeader;
    /// -> Border zwischen Headerzeilen.
    public float hInsideHeader;
    /// -> Border links vom Header.
    public float leftHeader;
    /// -> Border rechts vom Header.
    public float rightHeader;
    /// -> Border zwischen Datenspalten.
    public float vInsideBody;
    /// -> Border zwischen Datenzeilen.
    public float hInsideBody;
    /// -> Border links von den Daten.
    public float leftBody;
    /// -> Border rechts von den Daten.
    public float rightBody;

    public int topStyle = JoriaBorder.NONE;
	public int neckStyle = JoriaBorder.NONE;
	public int bottomStyle = JoriaBorder.NONE;
	public int rightHeaderStyle = JoriaBorder.NONE;
	public int leftHeaderStyle = JoriaBorder.NONE;
	public int vInsideHeaderStyle = JoriaBorder.NONE;
	public int hInsideHeaderStyle = JoriaBorder.NONE;
	public int rightBodyStyle = JoriaBorder.NONE;
	public int leftBodyStyle = JoriaBorder.NONE;
	public int vInsideBodyStyle = JoriaBorder.NONE;
	public int hInsideBodyStyle = JoriaBorder.NONE;

	public Color topColor = Color.black;
	public Color neckColor = Color.black;
	public Color bottomColor = Color.black;
	public Color rightHeaderColor = Color.black;
	public Color leftHeaderColor = Color.black;
	public Color vInsideHeaderColor = Color.black;
	public Color hInsideHeaderColor = Color.black;
	public Color rightBodyColor = Color.black;
	public Color leftBodyColor = Color.black;
	public Color vInsideBodyColor = Color.black;
	public Color hInsideBodyColor = Color.black;
	public static TableBorder zeroBorder = new TableBorder();

	public TableBorder()
	{
	}

	public TableBorder(TableBorder from)
	{
		top = from.top;
		neck = from.neck;
		bottom = from.bottom;
		vInsideHeader = from.vInsideHeader;
		hInsideHeader = from.hInsideHeader;
		leftHeader = from.leftHeader;
		rightHeader = from.rightHeader;
		vInsideBody = from.vInsideBody;
		hInsideBody = from.hInsideBody;
		leftBody = from.leftBody;
		rightBody = from.rightBody;
		topStyle = from.topStyle;
		neckStyle = from.neckStyle;
		bottomStyle = from.bottomStyle;
		rightHeaderStyle = from.rightHeaderStyle;
		leftHeaderStyle = from.leftHeaderStyle;
		vInsideHeaderStyle = from.vInsideHeaderStyle;
		hInsideHeaderStyle = from.hInsideHeaderStyle;
		rightBodyStyle = from.rightBodyStyle;
		leftBodyStyle = from.leftBodyStyle;
		vInsideBodyStyle = from.vInsideBodyStyle;
		hInsideBodyStyle = from.hInsideBodyStyle;
		topColor = from.topColor;
		neckColor = from.neckColor;
		bottomColor = from.bottomColor;
		rightHeaderColor = from.rightHeaderColor;
		leftHeaderColor = from.leftHeaderColor;
		vInsideHeaderColor = from.vInsideHeaderColor;
		hInsideHeaderColor = from.hInsideHeaderColor;
		rightBodyColor = from.rightBodyColor;
		leftBodyColor = from.leftBodyColor;
		vInsideBodyColor = from.vInsideBodyColor;
		hInsideBodyColor = from.hInsideBodyColor;
		/*
		topColor;
		neckColor;
		bottomColor;
		rightHeaderColor;
		leftHeaderColor;
		vInsideHeaderColor;
		hInsideHeaderColor;
		rightBodyColor;
		leftBodyColor;
		vInsideBodyColor;
		hInsideBodyColor;
		*/
	}

    public boolean equals(Object o)
    {
        if(!(o instanceof TableBorder))
            return false;
        TableBorder t = (TableBorder) o;
        return top == t.top && neck == t.neck && bottom == t.bottom && vInsideHeader == t.vInsideHeader &&
               hInsideHeader == t.hInsideHeader && leftHeader == t.leftHeader && rightHeader == t.rightHeader &&
               vInsideBody == t.vInsideBody && hInsideBody == t.hInsideBody && leftBody == t.leftBody &&
               rightBody == t.rightBody && topStyle == t.topStyle && neckStyle == t.neckStyle &&
               bottomStyle == t.bottomStyle && rightHeaderStyle == t.rightHeaderStyle &&
               leftHeaderStyle == t.leftHeaderStyle && vInsideHeaderStyle == t.vInsideHeaderStyle &&
               hInsideHeaderStyle == t.hInsideHeaderStyle && rightBodyStyle == t.rightBodyStyle &&
               leftBodyStyle == t.leftBodyStyle && vInsideBodyStyle == t.vInsideBodyStyle &&
               hInsideBodyStyle == t.hInsideBodyStyle && eq(topColor, t.topColor) && eq(neckColor, t.neckColor) &&
                eq(bottomColor, t.bottomColor) && eq(rightHeaderColor, t.rightHeaderColor) &&
                eq(leftHeaderColor, t.leftHeaderColor) && eq(vInsideHeaderColor, t.vInsideHeaderColor) &&
                eq(hInsideHeaderColor, t.hInsideHeaderColor) && eq(rightBodyColor, t.rightBodyColor) &&
                eq(leftBodyColor, t.leftBodyColor) && eq(vInsideHeaderColor, t.vInsideHeaderColor) &&
                eq(hInsideBodyColor, t.hInsideBodyColor);
    }

	@Override
	public int hashCode() {
		return Objects.hash(top,neck,bottom,vInsideHeader,hInsideHeader, leftHeader, rightHeader, vInsideBody, hInsideBody, leftBody, rightBody, topStyle, neckStyle, bottomStyle, rightHeaderStyle, leftHeaderStyle, vInsideHeaderStyle,
				hInsideHeaderStyle, rightBodyStyle, leftBodyStyle, vInsideBodyStyle, hInsideBodyStyle, topColor, neckColor, bottomColor, rightHeaderColor, leftHeaderColor, vInsideHeaderColor, hInsideHeaderColor, rightBodyColor, leftBodyColor,
				vInsideHeaderColor, hInsideBodyColor);
	}

	public static boolean eq(Object s, Object t)
    {
        if (s == null)
            return t == null;
        else
        {
            if (t == null)
                return false;
            return s.equals(t);
        }
    }
}
