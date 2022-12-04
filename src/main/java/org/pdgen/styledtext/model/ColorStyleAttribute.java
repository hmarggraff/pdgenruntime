// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model;

import java.awt.Color;
import java.text.AttributedCharacterIterator;

/**
 * User: patrick
 * Date: Nov 6, 2006
 * Time: 10:40:00 AM
 */
public class ColorStyleAttribute extends StyleAttribute
{
    private static final long serialVersionUID = 7L;
    private Color value;

	public ColorStyleAttribute(AttributedCharacterIterator.Attribute key, Color value)
	{
		super(key);
		this.value = value;
	}

	public ColorStyleAttribute(AttributedCharacterIterator.Attribute key, Color value, StyleAttribute next)
	{
		super(key, next);
		this.value = value;
	}

	public Object getValue()
	{
		return value;
	}

	public Object setValue(Object value)
	{
		Color oldVal = this.value;
		this.value = (Color) value;
		return oldVal;
	}

	public boolean equals(Object obj)
	{
		if (!(obj instanceof ColorStyleAttribute))
			return false;
		ColorStyleAttribute sta = (ColorStyleAttribute) obj;
		return key.equals(sta.key) && value == sta.value;
	}
}
