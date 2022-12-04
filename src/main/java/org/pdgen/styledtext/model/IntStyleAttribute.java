// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model;

import java.text.AttributedCharacterIterator;

/**
 * User: patrick
 * Date: Jan 8, 2007
 * Time: 12:16:28 PM
 */
public class IntStyleAttribute extends StyleAttribute
{
    private static final long serialVersionUID = 7L;
    private int value;

	public IntStyleAttribute(AttributedCharacterIterator.Attribute key, int value)
	{
		super(key);
		this.value = value;
	}

	public IntStyleAttribute(AttributedCharacterIterator.Attribute key, int value, StyleAttribute next)
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
		float oldVal = this.value;
		this.value = (Integer) value;
		return oldVal;
	}

	public boolean equals(Object obj)
	{
		if (!(obj instanceof IntStyleAttribute))
			return false;
		IntStyleAttribute sta = (IntStyleAttribute) obj;
		return key.equals(sta.key) && value == sta.value;
	}
}
