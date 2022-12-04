// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model;

import java.text.AttributedCharacterIterator;

/**
 * User: patrick
 * Date: Nov 6, 2006
 * Time: 10:43:27 AM
 */
public class BooleanStyleAttribute extends StyleAttribute
{
    private static final long serialVersionUID = 7L;
    private boolean value;

	public BooleanStyleAttribute(AttributedCharacterIterator.Attribute key, boolean value)
	{
		super(key);
		this.value = value;
	}

	public BooleanStyleAttribute(AttributedCharacterIterator.Attribute key, boolean value, StyleAttribute next)
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
		boolean oldVal = this.value;
		this.value = (Boolean) value;
		return oldVal;
	}

	public boolean equals(Object obj)
	{
		if (!(obj instanceof BooleanStyleAttribute))
			return false;
		BooleanStyleAttribute sta = (BooleanStyleAttribute) obj;
		return key.equals(sta.key) && value == sta.value;
	}
}
