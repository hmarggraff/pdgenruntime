// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model;

import java.text.AttributedCharacterIterator;

/**
 * User: patrick
 * Date: Nov 6, 2006
 * Time: 10:41:40 AM
 */
public class FloatStyleAttribute extends StyleAttribute {
    private static final long serialVersionUID = 7L;
    private float value;

    public FloatStyleAttribute(AttributedCharacterIterator.Attribute key, float value) {
        super(key);
        this.value = value;
    }

    public FloatStyleAttribute(AttributedCharacterIterator.Attribute key, float value, StyleAttribute next) {
        super(key, next);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public Object setValue(Object value) {
        float oldVal = this.value;
        this.value = (Float) value;
        return oldVal;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof FloatStyleAttribute))
            return false;
        FloatStyleAttribute sta = (FloatStyleAttribute) obj;
        return key.equals(sta.key) && value == sta.value;
    }
}
