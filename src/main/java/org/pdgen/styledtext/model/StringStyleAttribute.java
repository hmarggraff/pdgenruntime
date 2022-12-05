// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model;

import java.text.AttributedCharacterIterator;
import java.util.Objects;

/**
 * User: patrick
 * Date: Nov 6, 2006
 * Time: 10:40:00 AM
 */
public class StringStyleAttribute extends StyleAttribute {
    private static final long serialVersionUID = 7L;
    private String value;

    public StringStyleAttribute(AttributedCharacterIterator.Attribute key, String value) {
        super(key);
        this.value = value;
    }

    public StringStyleAttribute(AttributedCharacterIterator.Attribute key, String value, StyleAttribute next) {
        super(key, next);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public Object setValue(Object value) {
        String oldVal = this.value;
        this.value = (String) value;
        return oldVal;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof StringStyleAttribute))
            return false;
        StringStyleAttribute sta = (StringStyleAttribute) obj;
        return key.equals(sta.key) && value.equals(sta.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
