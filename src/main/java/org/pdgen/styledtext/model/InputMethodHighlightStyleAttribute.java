// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model;

import java.awt.im.InputMethodHighlight;
import java.text.AttributedCharacterIterator;

/**
 * User: patrick
 * Date: Nov 6, 2006
 * Time: 10:40:00 AM
 */
public class InputMethodHighlightStyleAttribute extends StyleAttribute
{
    private static final long serialVersionUID = 7L;
    private transient InputMethodHighlight value;

	public InputMethodHighlightStyleAttribute(AttributedCharacterIterator.Attribute key, InputMethodHighlight value)
	{
		super(key);
		this.value = value;
	}

	public InputMethodHighlightStyleAttribute(AttributedCharacterIterator.Attribute key, InputMethodHighlight value, StyleAttribute next)
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
		InputMethodHighlight oldVal = this.value;
		this.value = (InputMethodHighlight) value;
		return oldVal;
	}

	public boolean equals(Object obj)
	{
		if (!(obj instanceof InputMethodHighlightStyleAttribute))
			return false;
		InputMethodHighlightStyleAttribute sta = (InputMethodHighlightStyleAttribute) obj;
		return key.equals(sta.key) && value == sta.value;
	}
}
