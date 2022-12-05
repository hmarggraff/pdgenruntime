// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model;

import org.pdgen.data.JoriaAssertionError;

import java.awt.*;
import java.io.Serializable;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

//* User: hmf   Date: 29.10.2006 Time: 09:19:07
public class AttributeRun implements Serializable {
    private static final long serialVersionUID = 7L;
    int start;
    AttributeRunSet set;


    public AttributeRun(int start, StyleAttribute head) {
        this.start = start;
        set = new AttributeRunSet(head);
    }

    public AttributeRun(int start) {
        this.start = start;
        set = new AttributeRunSet();
    }

    public AttributeRun() {
        set = new AttributeRunSet();
    }

    public AttributeRun(int start, Map<AttributedCharacterIterator.Attribute, Object> attributes) {
        this.start = start;
        set = new AttributeRunSet(attributes);
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStart() {
        return start;
    }

    /**
     * Finds entry for key and sets it to value.
     * If key does not exist yet add (prepend)
     *
     * @param key   Key of the attribute
     * @param value new Value
     */
    public void setAttribute(AttributedCharacterIterator.Attribute key, Object value) {
        if (value == null)
            throw new JoriaAssertionError("unexpected attribute " + key.toString() + " null value");
        StyleAttribute t = set.head;
        while (t != null) {
            if (t.key.equals(key)) {
                t.setValue(value);
                return;
            }
            t = t.next;
        }
        set.head = makeStyleAttribute(key, value, set.head);
    }

    public AttributeRun dup(int newStart) {
        final AttributeRun ret = new AttributeRun(newStart);
        StyleAttribute t = set.head;
        while (t != null) {
            ret.set.head = makeStyleAttribute(t.key, t.getValue(), ret.set.head);
            t = t.next;
        }
        return ret;
    }

    public Set<Map.Entry<AttributedCharacterIterator.Attribute, Object>> entrySet() {
        return set;
    }

    public Object getValueFor(AttributedCharacterIterator.Attribute o) {
        StyleAttribute t = set.head;
        while (t != null) {
            if (t.key.equals(o)) {
                return t.getValue();
            }
            t = t.next;
        }
        return null;
    }

    public boolean removeAttribute(AttributedCharacterIterator.Attribute attribute) {
        if (set.head == null)
            return false;
        if (set.head.key.equals(attribute)) {
            set.head = set.head.next;
            return true;
        }
        StyleAttribute t = set.head;
        while (t.next != null) {
            if (t.next.key.equals(attribute)) {
                t.next = t.next.next;
                return true;
            }
            t = t.next;
        }
        return false;
    }

    public String toString() {
        return super.toString() + " " + start;
    }

    private static StyleAttribute makeStyleAttribute(AttributedCharacterIterator.Attribute key, Object value, StyleAttribute next) {
        if (value instanceof String)
            return new StringStyleAttribute(key, (String) value, next);
        else if (value instanceof Boolean)
            return new BooleanStyleAttribute(key, (Boolean) value, next);
        else if (value instanceof Float)
            return new FloatStyleAttribute(key, (Float) value, next);
        else if (value instanceof Integer)
            return new IntStyleAttribute(key, (Integer) value, next);
        else if (value instanceof Color)
            return new ColorStyleAttribute(key, (Color) value, next);
        else
            throw new JoriaAssertionError("unexpected value type " + value.getClass().getName());
    }

    public static class AttributeRunSet extends AbstractSet<Map.Entry<AttributedCharacterIterator.Attribute, Object>> implements Serializable {
        StyleAttribute head;
        private static final long serialVersionUID = 7L;


        public AttributeRunSet(StyleAttribute head) {
            this.head = head;
        }

        public AttributeRunSet() {
        }

        public AttributeRunSet(Map<AttributedCharacterIterator.Attribute, Object> attributes) {
            for (Map.Entry<AttributedCharacterIterator.Attribute, Object> entry : attributes.entrySet()) {
                StyleAttribute a = makeStyleAttribute(entry.getKey(), entry.getValue(), head);
                head = a;
            }
        }

        public int size() {
            int ret = 0;
            StyleAttribute t = head;
            while (t != null) {
                t = t.next;
                ret++;
            }
            return ret;
        }

        public boolean isEmpty() {
            return head == null;
        }

        public boolean contains(Object o) {
            StyleAttribute t = head;
            while (t != null) {
                if (t.equals(o))
                    return true;
                t = t.next;
            }
            return false;
        }

        public boolean equals(final Object o) {
            if (!(o instanceof AttributeRunSet)) {
                return false;
            }
            AttributeRunSet run = (AttributeRunSet) o;
            final int otherSize = run.size();
            final int mySize = size();
            if (otherSize != mySize)
                return false;
            final Iterator<Entry<Attribute, Object>> it = iterator();
            while (it.hasNext()) {
                Entry<Attribute, Object> e = it.next();
                if (!run.contains(e))
                    return false;
            }
            return true;
        }

        public Iterator<Map.Entry<AttributedCharacterIterator.Attribute, Object>> iterator() {
            return new AttributeRunInterator();
        }

        public boolean remove(Object obj) {
            StyleAttribute t = head;
            while (t != null && t.next.equals(obj)) {
                t = t.next;
            }
            if (t != null) {
                t.next = t.next.next;
                return true;
            }
            return false;
        }

        public boolean add(StyleAttribute o) {
            StyleAttribute t = head;
            while (t != null) {
                if (t.key.equals(o.key)) {
                    t.setValue(o.getValue());
                    return false;
                }
                t = t.next;
            }
            o.next = head;
            head = o;
            return true;
        }

        public void clear() {
            head = null;
        }

        public boolean add(Map.Entry<AttributedCharacterIterator.Attribute, Object> o) {
            if (o instanceof StyleAttribute)
                return add((StyleAttribute) o);
            else
                return super.add(o);
        }

        class AttributeRunInterator implements Iterator<Map.Entry<AttributedCharacterIterator.Attribute, Object>> {
            StyleAttribute current;
            StyleAttribute next = head;// holding next

            public boolean hasNext() {
                return next != null;
            }

            public StyleAttribute next() {
                current = next;
                if (next != null) {
                    next = next.next;
                }
                return current;
            }

            /**
             * slow impl because remove is much rarer than normal iteration
             */
            public void remove() {
                if (current == null)
                    return;
                StyleAttribute t = head;
                while (t != null && t.next != current) {
                    t = t.next;
                }
                if (t != null) {
                    t.next = t.next.next;
                }
            }
        }
    }

    protected Object readResolve() {
        StyleAttribute run = set.head;
        StyleAttribute previous = null;
        while (run != null) {
            if (run instanceof InputMethodHighlightStyleAttribute) {
                if (previous == null) {
                    set.head = run.next;
                } else
                    previous.next = run.next;
            } else
                previous = run;
            run = run.next;
        }
        return this;
    }


}
