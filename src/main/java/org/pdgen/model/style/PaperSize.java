// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import org.pdgen.data.Nameable;
import org.pdgen.data.NameableTracer;
import org.pdgen.data.SortedNamedVector;
import org.pdgen.env.Res;

import java.io.Serializable;
import java.util.Objects;

public class PaperSize implements Serializable, Nameable {
    private static final long serialVersionUID = 7L;
    private String name;
    private Length height;
    private Length width;
    private int rows;
    private int columns;
    public static String A3Name = Res.str("A3");
    public static String A4Name = Res.str("A4");
    public static String A5Name = Res.str("A5");
    public static String LetterName = Res.str("Letter");
    public static String SmallName = Res.str("Small");
    protected transient PaperSize localOverlay;
    protected transient boolean isCentral;
    public static final LengthUnit[] rowUnits = {LengthUnit.MM, LengthUnit.CM, LengthUnit.INCH, LengthUnit.POINT, LengthUnit.ROWS};
    public static final LengthUnit[] colUnits = {LengthUnit.MM, LengthUnit.CM, LengthUnit.INCH, LengthUnit.POINT, LengthUnit.COLS};
    public static final LengthUnit[] fontUnits = {LengthUnit.POINT, LengthUnit.MM, LengthUnit.CM, LengthUnit.INCH,};

    public PaperSize() {
        name = A4Name;
        width = new Length(210, LengthUnit.MM);
        height = new Length(297, LengthUnit.MM);
    }

    public PaperSize(String name) {
        this.name = name;
        width = new Length(210, LengthUnit.MM);
        height = new Length(210, LengthUnit.MM);
    }

    public PaperSize(String n, Length w, Length h) {
        name = n;
        width = w;
        height = h;
    }

    public PaperSize(Length w, Length h) {
        width = w;
        height = h;
        name = height + "*" + width;
    }

    public PaperSize(PaperSize s) {
        name = s.name;
        width = s.width;
        height = s.height;
    }

    public static void init(SortedNamedVector<PaperSize> l) {
        PredefinedStyles stat = PredefinedStyles.instance();
        stat.thePaperSizeA4 = new PaperSize();
        stat.thePaperSizeLetter = new PaperSize(LetterName, new Length(8.5f, LengthUnit.INCH), new Length(11, LengthUnit.INCH));
        stat.thePaperSizeA5 = new PaperSize(A5Name, new Length(148.5f, LengthUnit.MM), new Length(210, LengthUnit.MM));
        stat.thePaperSizeA3 = new PaperSize(A3Name, new Length(297, LengthUnit.MM), new Length(420, LengthUnit.MM));
        l.add(stat.thePaperSizeA4);
        l.add(stat.thePaperSizeA5);
        l.add(stat.thePaperSizeLetter);
        l.add(stat.thePaperSizeA3);
    }

    public boolean equals(Object o) {
        if (!(o instanceof PaperSize))
            return false;
        PaperSize t = (PaperSize) o;
        return (name != null && (name.equals(t.name)) || (name == null && t.name == null)) && (height != null && (height.equals(t.height)) || (height == null && t.height == null)) && (width != null && (width.equals(t.width)) || (width == null && t.width == null));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, height, width);
    }

    public Length getHeight() {
        if (localOverlay != null)
            return localOverlay.height;
        return height;
    }

    public String getName() {
        return name;
    }

    public Length getWidth() {
        if (localOverlay != null)
            return localOverlay.width;
        return width;
    }

    public void setHeight(Length newHeight) {
        height = newHeight;
        //name = String.valueOf(height) + "*" + String.valueOf(width);
    }

    public void setWidth(Length newWidth) {
        width = newWidth;
        //name = String.valueOf(height) + "*" + String.valueOf(width);
    }

    public String toString() {
        return getName();
    }

    protected Object readResolve() throws java.io.ObjectStreamException {
        PredefinedStyles stat = PredefinedStyles.instance();
        if (name == null)
            name = height + "*" + width;
        if (A4Name.equals(name))
            stat.thePaperSizeA4 = this;
        else if (A5Name.equals(name))
            stat.thePaperSizeA5 = this;
        else if (LetterName.equals(name))
            stat.thePaperSizeLetter = this;
        else if (A3Name.equals(name))
            stat.thePaperSizeA3 = this;
        return this;
    }

    public void setName(String newName) {
        NameableTracer.notifyListenersPre(this);
        name = newName;
        NameableTracer.notifyListenersPost(this);
    }

    public boolean hasName() {
        return true;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public PaperSize theLarger(PaperSize other) {
        if (other.equals(this))
            return this;
        float length = Math.max(width.getValInPoints(), height.getValInPoints());
        float otherLength = Math.max(other.width.getValInPoints(), other.height.getValInPoints());
        if (otherLength > length)
            return other;
        else
            return this;
    }
}
