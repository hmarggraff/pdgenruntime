// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import java.io.Serializable;
import java.util.Objects;

public class Length implements Serializable {
    private static final long serialVersionUID = 7L;
    private float val;
    public LengthUnit unit = LengthUnit.POINT;
    public static final float pointPerMm = 72f / 25.4f;
    public static final Length NULL = new Length();
    public static final Length TWO = new Length(2);

    public Length() {
    }

    public Length(float v) {
        val = v;
    }

    public Length(Length v) {
        val = v.val;
        unit = v.unit;
    }

    public Length(float val, LengthUnit unit) {
        this.val = val;
        this.unit = unit;
    }

    public LengthUnit getUnit() {
        return unit;
    }

    public float getVal() {
        return val;
    }

    public String toString() {
        return val + " " + unit.getName();
    }

    public String inUnits() {
        return String.valueOf(val);
    }

    public float getValInPoints() {
        return val * unit.getFactor();
    }

    public float inUnit(LengthUnit unit) {
        if (unit == this.unit)
            return val;
        else
            return val * this.unit.getFactor() / unit.getFactor();
    }

    public static Length copy(Length in) {
        if (in == null)
            return null;
        else if (NULL.equals(in))
            return NULL;
        else
            return new Length(in.val, in.unit);
    }

    public static boolean eq(Length a, Length b) {
        if (a == null)
            return b == null;
        else if (b == null)
            return false;
        return a.unit == b.unit && ((a.val - b.val) < 0.001);
    }

    public boolean equals(Object o) {
        if (o == null || o.getClass() != Length.class)
            return false;
        Length length = ((Length) o);
        return length.unit == unit && length.val - val < 0.001;
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit, val);
    }
}
