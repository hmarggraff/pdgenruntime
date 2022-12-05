// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model;

import java.io.Serializable;
import java.text.AttributedCharacterIterator;
import java.util.Map;

//* User: hmf   Date: 29.10.2006 Time: 09:13:47
public abstract class StyleAttribute implements Map.Entry<AttributedCharacterIterator.Attribute, Object>, Serializable {
    private static final long serialVersionUID = 7L;
    AttributedCharacterIterator.Attribute key;
    StyleAttribute next;

    protected StyleAttribute(AttributedCharacterIterator.Attribute key) {
        this.key = key;
    }

    protected StyleAttribute(AttributedCharacterIterator.Attribute key, StyleAttribute next) {
        this.key = key;
        this.next = next;
    }

    public void append(StyleAttribute a) {
        a.next = next;
        next = a;
    }

    public AttributedCharacterIterator.Attribute getKey() {
        return key;
    }

    public abstract Object getValue();

    public abstract Object setValue(Object value);

    public boolean equals(Object obj) {
        if (!(obj instanceof StyleAttribute))
            return false;
        StyleAttribute sta = (StyleAttribute) obj;
        return key.equals(sta.key);
    }
}
