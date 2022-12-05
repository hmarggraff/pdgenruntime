// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.CharacterIterator;
import java.util.*;
import java.util.Map.Entry;


//@SuppressWarnings({"ForLoopReplaceableByForEach"})
public class StyledParagraph extends GapCharList {
    public static final int alignLeft = 0;
    public static final int alignCenter = 1;
    public static final int alignRight = 2;
    public static final int alignJustified = 3;
    private static final long serialVersionUID = 7L;
    ArrayList<AttributeRun> styleRuns;
    int alignment = alignLeft;
    float spaceBelow;
    float rowSpacing = 1;
    boolean displayOnly;

    public StyledParagraph() {
        styleRuns = new ArrayList<AttributeRun>(2);
        styleRuns.add(new AttributeRun(0));
    }

    public StyledParagraph(StyledParagraph from, boolean displayOnly) {
        super(from);
        alignment = from.alignment;
        spaceBelow = from.spaceBelow;
        rowSpacing = from.rowSpacing;
        styleRuns = new ArrayList<AttributeRun>(from.styleRuns.size());
        for (AttributeRun styleRun : from.styleRuns) {
            AttributeRun nar = styleRun.dup(styleRun.start);
            styleRuns.add(nar);
        }
		/*
		  if (displayOnly)
		  {
			  this.displayOnly = displayOnly;
			  int beginIndex = length() - 1;
			  int beginRunIndex = ensureRunBreak(beginIndex, true);
			  int endIndex = length();
			  int endRunIndex = ensureRunBreak(endIndex, true);
			  for (int i = beginRunIndex; i < endRunIndex; i++)
			  {
				  final AttributeRun ar = styleRuns.get(i);
				  ar.removeAttribute(TextAttribute.UNDERLINE);
			  }
			  checkStyleRunState();
		  }
  */
    }

    public StyledParagraph(String s) {
        super(s);
        styleRuns = new ArrayList<AttributeRun>(2);
        styleRuns.add(new AttributeRun(0));
    }

    public StyledParagraph(StyledParagraph from) {
        this(from, false);
    }

    public StyledParagraph(StyledParagraph from, int pos) {
        super(from.getChars(pos, from.length() - pos));
        alignment = from.alignment;
        spaceBelow = from.spaceBelow;
        rowSpacing = from.rowSpacing;
        styleRuns = new ArrayList<AttributeRun>();
        for (int i = from.styleRuns.size() - 1; i >= 0; i--) {
            AttributeRun styleRun = from.styleRuns.get(i);
            AttributeRun nar = styleRun.dup(styleRun.start - pos);
            styleRuns.add(nar);
            if (styleRun.start <= pos)// copy including the last before pos
            {
                nar.start = 0;
                break;
            }
        }
        Collections.reverse(styleRuns);
    }

    public StyledParagraph(String newText, Map<AttributedCharacterIterator.Attribute, Object> attributes) {
        if (newText == null || attributes == null) {
            throw new NullPointerException();
        }
        set(newText);
        styleRuns = new ArrayList<AttributeRun>(1);
        styleRuns.add(new AttributeRun(0, attributes));
    }

    public StyledParagraph(final String s, final AttributeRun attributeRun) {
        super(s);
        styleRuns = new ArrayList<AttributeRun>(2);
        styleRuns.add(attributeRun.dup(0));
    }

    public ArrayList<AttributeRun> getStyleRuns() {
        return styleRuns;
    }

    private synchronized Object attributeValue(AttributedCharacterIterator.Attribute attribute, int runIndex) {
        if (styleRuns == null || runIndex == -1)
            return null;
        final AttributeRun attributeRun = styleRuns.get(runIndex);
        return attributeRun.getValueFor(attribute);
    }

    private static boolean valuesMatch(Object value1, Object value2) {
        if (value1 == null) {
            return value2 == null;
        } else {
            return value1.equals(value2);
        }
    }

    // returns whether all specified attributes have equal values in the runs with the given indices
    private boolean attributeValuesMatch(Set<? extends AttributedCharacterIterator.Attribute> attributes, int runIndex1, int runIndex2) {
        for (AttributedCharacterIterator.Attribute attribute : attributes) {
            if (!valuesMatch(attributeValue(attribute, runIndex1), attributeValue(attribute, runIndex2))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates an AttributedCharacterIterator instance that provides access to the entire contents of
     * this string.
     *
     * @return An iterator providing access to the text and its attributes.
     */
    public AttributedCharacterIterator getIterator() {
        return getIterator(null, 0, length());
    }

    /**
     * Creates an AttributedCharacterIterator instance that provides access to
     * selected contents of this string.
     * Information about attributes not listed in attributes that the
     * implementor may have need not be made accessible through the iterator.
     * If the list is null, all available attribute information should be made
     * accessible.
     *
     * @param attributes a list of attributes that the client is interested in
     * @return an iterator providing access to the entire text and its selected attributes
     */
    public AttributedCharacterIterator getIterator(AttributedCharacterIterator.Attribute[] attributes) {
        return getIterator(attributes, 0, length());
    }

    /**
     * Creates an AttributedCharacterIterator instance that provides access to
     * selected contents of this string.
     * Information about attributes not listed in attributes that the
     * implementor may have need not be made accessible through the iterator.
     * If the list is null, all available attribute information should be made
     * accessible.
     *
     * @param attributes a list of attributes that the client is interested in
     * @param beginIndex the index of the first character
     * @param endIndex   the index of the character following the last character
     * @return an iterator providing access to the text and its attributes
     * @throws IllegalArgumentException if beginIndex is less then 0,
     *                                  endIndex is greater than the length of the string, or beginIndex is
     *                                  greater than endIndex.
     */
    public AttributedCharacterIterator getIterator(AttributedCharacterIterator.Attribute[] attributes, int beginIndex, int endIndex) {
        return new AttributedStringIterator(beginIndex, endIndex);
    }

    public int runCount() {
        return styleRuns.size();
    }

    public int runStart(int runIndex) {
        return styleRuns.get(runIndex).start;
    }

    public String[] getUsedFontFamilies() {
        HashSet<String> families = new HashSet<String>();
        for (AttributeRun styleRun : styleRuns) {
            for (Map.Entry<AttributedCharacterIterator.Attribute, Object> entry : styleRun.entrySet()) {
                if (entry.getKey() == TextAttribute.FAMILY) {
                    families.add((String) entry.getValue());
                }
            }
        }
        return families.toArray(new String[families.size()]);
    }

    public void getUsedFontFamilies(Set<String> families) {
        for (AttributeRun styleRun : styleRuns) {
            for (Map.Entry<AttributedCharacterIterator.Attribute, Object> entry : styleRun.entrySet()) {
                if (entry.getKey() == TextAttribute.FAMILY) {
                    families.add((String) entry.getValue());
                }
            }
        }
    }

    public void getUsedColors(final HashSet<Color> colors) {
        for (AttributeRun styleRun : styleRuns) {
            for (Entry<Attribute, Object> entry : styleRun.entrySet()) {
                if (entry.getKey() == TextAttribute.BACKGROUND) {
                    colors.add((Color) entry.getValue());
                }
                if (entry.getKey() == TextAttribute.FOREGROUND) {
                    colors.add((Color) entry.getValue());
                }
            }
        }
    }

    public float getSpaceBelow() {
        return spaceBelow;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public void setSpaceBelow(float spaceBelow) {
        this.spaceBelow = spaceBelow;
    }

    public int getAlignment() {
        return alignment;
    }

    protected void setInitialAttributes(String fontfamily, Float size, boolean bold, boolean italic, boolean underlined) {
        addAttribute(TextAttribute.SIZE, size);
        addAttribute(TextAttribute.FAMILY, fontfamily);
        //addAttribute(TextAttribute.WIDTH, TextAttribute.WIDTH_EXTENDED);
        if (bold)
            addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        else
            removeAttribute(TextAttribute.WEIGHT);
        if (italic)
            addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        else
            removeAttribute(TextAttribute.POSTURE);
        if (underlined)
            addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        else
            removeAttribute(TextAttribute.UNDERLINE);
    }

    public FloatDim unbrokenWidth(FontRenderContext frc) {
        TextLayout l = new TextLayout(getIterator(), frc);
        return new FloatDim(l.getAdvance(), l.getAscent() + l.getDescent() + l.getLeading());
    }

    public void insert(char c, int at) {
        super.insert(c, at);
        for (int i = 1; i < styleRuns.size(); i++) {
            AttributeRun ar = styleRuns.get(i);
            if (ar.start >= at)
                ar.start++;
        }
        checkStyleRunState();
    }

    public void insert(String txt, int at) {
        super.insert(txt, at);
        int len = txt.length();
        for (AttributeRun ar : styleRuns) {
            if (ar.start > at && ar.start > 0)
                ar.start += len;
        }
        checkStyleRunState();
    }

    public void delete(int at, int len) {
        if (len == 0)
            return;
        if (len < 0)
            throw new IllegalArgumentException("Len must be greater 0: len=" + len);
        int endSpan = length() - 1;
        super.delete(at, len);
        //final int end = at + len;
        //int firstDisappear = -1;
        final int end = at + len;
        final int last = runCount() - 1;
        AttributeRun first = styleRuns.get(0);
        for (int i = last; i >= 0; i--) {
            final AttributeRun ar = styleRuns.get(i);
            if (ar.start < at)// davor endend
                break;
            if (ar.start >= at && endSpan <= end)// ganz darin liegend
            {
                endSpan = ar.start;
                styleRuns.remove(i);
            } else
            // dahinter oder darüber liegend
            {
                endSpan = ar.start;
                first = ar;
                if (ar.start < end && ar.start >= at)// darüber liegend
                    ar.start = at;
                else
                    ar.start -= len;
            }
        }
        if (styleRuns.size() == 0)
            styleRuns.add(first);
        checkStyleRunState();
    }

    public void replaceInStyle(int at, int to, String newText) {
        int len = to - at;
        if (len == 0)
            return;
        if (len < 0)
            throw new IllegalArgumentException("Len must be greater 0: len=" + len);
        int endSpan = length() - 1;
        super.delete(at, len);
        super.insert(newText, at);
        //final int end = at + len;
        //int firstDisappear = -1;
        final int end = at + len;
        final int last = runCount() - 1;
        //AttributeRun first = styleRuns.get(0);
        int delta = len - newText.length();
        for (int i = last; i >= 0; i--) {
            final AttributeRun ar = styleRuns.get(i);
            if (ar.start < at)// davor endend
                break;
            // hier beginnt der Span mit dem Austauchbereich und entweder
            // es wird Text eingefügt oder das ist der erste Spann oder der Span geht hinten
            // aus dem Ersatzbereich, dann behalten wir den Spann
            if (ar.start == at && (newText.length() > 0 || at == 0 && last == 0 || endSpan > end))// style where replacement occurs is retained
                continue;
            if (ar.start >= at && endSpan <= end)// ganz darin liegend
            {
                endSpan = ar.start;
                styleRuns.remove(i);
            } else
            // dahinter oder darüber liegend
            {
                endSpan = ar.start;
                //first = ar;
                if (ar.start < end && ar.start >= at)// darüber liegend
                    ar.start = end;
                else
                    ar.start -= delta;
            }
        }
        //		if (styleRuns.size() == 0)
        //			styleRuns.add(first);
        checkStyleRunState();
    }

    public int replace(int from, int to, ArrayList<String> newText, ArrayList<StyledParagraph> outputParagraphs) {
        final AttributedCharacterIterator iter = getIterator();
        iter.setIndex(from);
        final Map<AttributedCharacterIterator.Attribute, Object> attributes = iter.getAttributes();
        final StyledParagraph endPar = split(to);
        replaceInStyle(from, to, newText.get(0));
        if (outputParagraphs.size() == 0 || outputParagraphs.get(outputParagraphs.size() - 1) != this) {
            outputParagraphs.add(this);
        }
        for (int i = 1; i < newText.size() - 1; i++) {
            StyledParagraph p = new StyledParagraph(newText.get(i), attributes);
            outputParagraphs.add(p);
        }
        endPar.insert(newText.get(newText.size() - 1), 0);
        outputParagraphs.add(endPar);
        checkStyleRunState();
        return 0;
    }

    public void replace(int from, int to, String newText) {
        final AttributeRun attributeRun = styleAt(from);
        deleteRange(from, to);
        if (newText != null && newText.length() > 0) {
            insert(newText, from);
            if (from > 0) {
                for (int i = 0; i < styleRuns.size(); i++) {
                    AttributeRun r = styleRuns.get(i);
                    if (r.getStart() > from) {
                        styleRuns.add(i, attributeRun);
                        break;
                    }
                }
            }
        }
		/*
		  int len = newText.length();
		  for (int i = 0; i < styleRuns.size(); i++)
		  {
			  AttributeRun runStart = styleRuns.get(i);
			  if (runStart.start > from)
				  runStart.start += len;
		  }
  */
        checkStyleRunState();
    }

    protected StyledParagraph split(int pos) {
        StyledParagraph ret = new StyledParagraph(this, pos);
        delete(pos, length() - pos - 1);
        ret.checkStyleRunState();
        checkStyleRunState();
        return ret;
    }

    protected boolean removeAttribute(AttributedCharacterIterator.Attribute attribute, int beginIndex, int endIndex) {
        // break up runs if necessary
        int beginRunIndex = ensureRunBreak(beginIndex, false);
        int endRunIndex = ensureRunBreak(endIndex, false);
        boolean ret = false;
        for (int i = beginRunIndex; i < endRunIndex; i++) {
            final AttributeRun ar = styleRuns.get(i);
            ret |= ar.removeAttribute(attribute);
        }
        for (int i = styleRuns.size() - 2; i >= 0; i--) {
            final AttributeRun lastAr = styleRuns.get(i);
            final AttributeRun nextAr = styleRuns.get(i + 1);
            if (lastAr.set.equals(nextAr.set))
                styleRuns.remove(i + 1);
        }
        checkStyleRunState();
        return ret;
    }

    protected boolean hasAttribute(AttributedCharacterIterator.Attribute attribute) {
        if (styleRuns == null)
            return false;
        for (AttributeRun run : styleRuns) {
            if (run.getValueFor(attribute) != null)
                return true;
        }
        return false;
    }

    public void merge(StyledParagraph text) {
        super.delete(length() - 1, 1);
        int oldLength = length();
        append(text);
        if (oldLength == 0) {
            styleRuns = text.styleRuns;
        } else {
            for (int i = 1; i < text.styleRuns.size(); i++) {
                AttributeRun ar = text.styleRuns.get(i);
                ar.start += oldLength;
                styleRuns.add(ar);
            }
        }
        text.gapPos = a.length + 1;
        text.styleRuns = null;// this ensures that an errorneous later use of the merged par gets caught
        checkStyleRunState();
    }

    private int ensureRunBreak(int pos, boolean atLastCharacterAllowed) {
        for (int i = 0; i < styleRuns.size(); i++) {
            AttributeRun run = styleRuns.get(i);
            if (run.start == pos)
                return i;
            if (run.start > pos) {
                AttributeRun nar;
                if (i == 0)
                    nar = new AttributeRun(pos);
                else
                    nar = styleRuns.get(i - 1).dup(pos);
                styleRuns.add(i, nar);
                return i;
            }
        }
        if (pos >= length() - 1 && !atLastCharacterAllowed)// no style break at last (pseudo) character
            return runCount();
        AttributeRun nar = styleRuns.get(styleRuns.size() - 1).dup(pos);
        styleRuns.add(nar);
        return styleRuns.size() - 1;
    }

    public boolean removeAttribute(AttributedCharacterIterator.Attribute attribute) {
        if (styleRuns == null)
            return false;
        int len = length();
        return removeAttribute(attribute, 0, len);
    }

    public char charAt(int ix) {
        return get(ix);
    }

    /**
     * Adds an attribute to the entire string.
     *
     * @param attribute the attribute key
     * @param value     the value of the attribute; may be null
     * @throws NullPointerException     if <code>attribute</code> is null.
     * @throws IllegalArgumentException if the AttributedString has length 0
     *                                  (attributes cannot be applied to a 0-length range).
     */
    public void addAttribute(AttributedCharacterIterator.Attribute attribute, Object value) {
        if (attribute == null) {
            throw new NullPointerException();
        }
        int len = length();
        addAttributeImpl(attribute, value, 0, len);
    }

    /**
     * Adds an attribute to a subrange of the string.
     *
     * @param attribute  the attribute key
     * @param value      The value of the attribute. May be null.
     * @param beginIndex Index of the first character of the range.
     * @param endIndex   Index of the character following the last character of the range.
     * @throws NullPointerException     if <code>attribute</code> is null.
     * @throws IllegalArgumentException if beginIndex is less then 0, endIndex is
     *                                  greater than the length of the string, or beginIndex and endIndex together don't
     *                                  define a non-empty subrange of the string.
     */
    public void addAttribute(AttributedCharacterIterator.Attribute attribute, Object value, int beginIndex, int endIndex) {
        if (attribute == null) {
            throw new NullPointerException();
        }
        if (beginIndex < 0 || endIndex > length() || beginIndex >= endIndex) {
            throw new IllegalArgumentException("Invalid substring range");
        }
        addAttributeImpl(attribute, value, beginIndex, endIndex);
    }

    private synchronized void addAttributeImpl(AttributedCharacterIterator.Attribute attribute, Object value, int beginIndex, int endIndex) {
        // break up runs if necessary
        int beginRunIndex = ensureRunBreak(beginIndex, false);
        int endRunIndex = ensureRunBreak(endIndex, false);
        for (int j = beginRunIndex; j < endRunIndex; j++) {
            final AttributeRun ar = styleRuns.get(j);
            ar.setAttribute(attribute, value);
        }
        checkStyleRunState();
    }

    public AttributeRun styleAt(final int at) {
        for (int i = styleRuns.size() - 1; i >= 0; i--) {
            AttributeRun attributeRun = styleRuns.get(i);
            if (attributeRun.start <= at)
                return attributeRun;
        }
        return styleRuns.get(0);
    }

    public void shiftStyle(int at, int len, AttributedCharacterIterator.Attribute attribute) {
        for (int i = 0; i < styleRuns.size(); i++) {
            AttributeRun ar = styleRuns.get(i);
            if (ar.start == at) {
                AttributeRun nar = ar.dup(at + len);
                styleRuns.add(i + 1, nar);
                ar.removeAttribute(attribute);
            }
        }
        checkStyleRunState();
    }

    public float getRowSpacing() {
        return rowSpacing;
    }

    public void setRowSpacing(final float rowSpacing) {
        this.rowSpacing = rowSpacing;
    }

    private final class AttributedStringIterator implements AttributedCharacterIterator {
        // note on synchronization:
        // we don't synchronize on the iterator, assuming that an iterator is only used in one thread.
        // we do synchronize access to the AttributedString however, since it's more likely to be shared between threads.
        // start and end index for our iteration
        private final int beginIndex;
        private final int endIndex;
        // the current index for our iteration
        // invariant: beginIndex <= currentIndex <= endIndex
        private int currentIndex;
        // information about the run that includes currentIndex
        private int currentRunIndex;
        private int currentRunStart;
        private int currentRunLimit;

        AttributedStringIterator(int beginIndex, int endIndex) {
            if (beginIndex < 0 || beginIndex > endIndex || endIndex > length()) {
                throw new IllegalArgumentException("Invalid substring range");
            }
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
            currentIndex = beginIndex;
            updateRunInfo();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof StyledParagraph.AttributedStringIterator)) {
                return false;
            }
            StyledParagraph.AttributedStringIterator that = (StyledParagraph.AttributedStringIterator) obj;
            if (StyledParagraph.this != that.getStyledParagraph())
                return false;
            //noinspection RedundantIfStatement
            if (currentIndex != that.currentIndex || beginIndex != that.beginIndex || endIndex != that.endIndex)
                return false;
            return true;
        }

        public int hashCode() {
            return StyledParagraph.this.hashCode() ^ currentIndex ^ beginIndex ^ endIndex;
        }

        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                throw new InternalError();
            }
        }

        // CharacterIterator methods. See documentation in that interface.

        public char first() {
            return internalSetIndex(beginIndex);
        }

        public char last() {
            if (endIndex == beginIndex) {
                return internalSetIndex(endIndex);
            } else {
                return internalSetIndex(endIndex - 1);
            }
        }

        public char current() {
            if (currentIndex == endIndex) {
                return CharacterIterator.DONE;
            } else {
                return get(currentIndex);
            }
        }

        public char next() {
            if (currentIndex < endIndex) {
                return internalSetIndex(currentIndex + 1);
            } else {
                return CharacterIterator.DONE;
            }
        }

        public char previous() {
            if (currentIndex > beginIndex) {
                return internalSetIndex(currentIndex - 1);
            } else {
                return CharacterIterator.DONE;
            }
        }

        public char setIndex(int position) {
            if (position < beginIndex || position > endIndex)
                throw new IllegalArgumentException("Invalid index");
            return internalSetIndex(position);
        }

        public int getBeginIndex() {
            return beginIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public int getIndex() {
            return currentIndex;
        }

        // AttributedCharacterIterator methods. See documentation in that interface.

        public int getRunStart() {
            return currentRunStart;
        }

        public int getRunStart(Attribute attribute) {
            if (currentRunStart == beginIndex || currentRunIndex == -1) {
                return currentRunStart;
            } else {
                Object value = getAttribute(attribute);
                int runStart = currentRunStart;
                int runIndex = currentRunIndex;
                while (runStart > beginIndex && valuesMatch(value, attributeValue(attribute, runIndex - 1))) {
                    runIndex--;
                    runStart = styleRuns.get(runIndex).start;
                }
                if (runStart < beginIndex) {
                    runStart = beginIndex;
                }
                return runStart;
            }
        }

        public int getRunStart(Set<? extends Attribute> attributes) {
            if (currentRunStart == beginIndex || currentRunIndex == -1) {
                return currentRunStart;
            } else {
                int runStart = currentRunStart;
                int runIndex = currentRunIndex;
                while (runStart > beginIndex && attributeValuesMatch(attributes, currentRunIndex, runIndex - 1)) {
                    runIndex--;
                    runStart = styleRuns.get(runIndex).start;
                }
                if (runStart < beginIndex) {
                    runStart = beginIndex;
                }
                return runStart;
            }
        }

        public int getRunLimit() {
            return currentRunLimit;
        }

        public int getRunLimit(Attribute attribute) {
            if (currentRunLimit == endIndex || currentRunIndex == -1) {
                return currentRunLimit;
            } else {
                Object value = getAttribute(attribute);
                int runLimit = currentRunLimit;
                int runIndex = currentRunIndex;
                while (runLimit < endIndex && valuesMatch(value, attributeValue(attribute, runIndex + 1))) {
                    runIndex++;
                    runLimit = runIndex < styleRuns.size() - 1 ? styleRuns.get(runIndex + 1).start : endIndex;
                }
                if (runLimit > endIndex) {
                    runLimit = endIndex;
                }
                return runLimit;
            }
        }

        public int getRunLimit(Set<? extends AttributedCharacterIterator.Attribute> attributes) {
            if (currentRunLimit == endIndex || currentRunIndex == -1) {
                return currentRunLimit;
            } else {
                int runLimit = currentRunLimit;
                int runIndex = currentRunIndex;
                while (runLimit < endIndex && attributeValuesMatch(attributes, currentRunIndex, runIndex + 1)) {
                    runIndex++;
                    runLimit = runIndex < styleRuns.size() - 1 ? styleRuns.get(runIndex + 1).start : endIndex;
                }
                if (runLimit > endIndex) {
                    runLimit = endIndex;
                }
                return runLimit;
            }
        }

        public Map<Attribute, Object> getAttributes() {
            if (styleRuns == null) {
                return new Hashtable<Attribute, Object>(0);
            }
            return new AbstractMap<AttributedCharacterIterator.Attribute, Object>() {
                final int index = currentRunIndex;

                public Set<Entry<Attribute, Object>> entrySet() {
                    return styleRuns.get(index).entrySet();
                }
            };
        }

        public Set<AttributedCharacterIterator.Attribute> getAllAttributeKeys() {
            if (styleRuns == null) {
                return new HashSet<AttributedCharacterIterator.Attribute>(0);
            }
            synchronized (StyledParagraph.this) {
                Set<AttributedCharacterIterator.Attribute> keys = new HashSet<AttributedCharacterIterator.Attribute>();
                for (AttributeRun styleRun : styleRuns) {
                    for (Map.Entry<Attribute, Object> entry : styleRun.entrySet()) {
                        keys.add(entry.getKey());
                    }
                }
                return keys;
            }
        }

        public Object getAttribute(Attribute attribute) {
            return attributeValue(attribute, currentRunIndex);
        }

        // internally used methods

        private StyledParagraph getStyledParagraph() {
            return StyledParagraph.this;
        }

        // set the current index, update information about the current run if necessary,
        // return the character at the current index
        private char internalSetIndex(int position) {
            currentIndex = position;
            if (position < currentRunStart || position >= currentRunLimit) {
                updateRunInfo();
            }
            if (currentIndex == endIndex) {
                return CharacterIterator.DONE;
            } else {
                return get(position);
            }
        }

        // update the information about the current run
        private void updateRunInfo() {
            if (currentIndex == endIndex) {
                currentRunStart = currentRunLimit = endIndex;
                currentRunIndex = -1;
            } else {
                synchronized (StyledParagraph.this) {
                    int runIndex = -1;
                    while (runIndex < styleRuns.size() - 1 && styleRuns.get(runIndex + 1).start <= currentIndex) {
                        runIndex++;
                    }
                    currentRunIndex = runIndex;
                    if (runIndex >= 0) {
                        currentRunStart = styleRuns.get(runIndex).start;
                        if (currentRunStart < beginIndex)
                            currentRunStart = beginIndex;
                    } else {
                        currentRunStart = beginIndex;
                    }
                    if (runIndex < styleRuns.size() - 1) {
                        currentRunLimit = styleRuns.get(runIndex + 1).start;
                        if (currentRunLimit > endIndex)
                            currentRunLimit = endIndex;
                    } else {
                        currentRunLimit = endIndex;
                    }
                }
            }
        }
    }

    void checkStyleRunState() {
        if (styleRuns == null)
            throw new RuntimeException("Null Style runs");
        if (styleRuns.size() == 0)
            throw new RuntimeException("Zero Style runs");
        int lastStart = -1;
        for (AttributeRun styleRun : styleRuns) {
            if (styleRun.entrySet().size() == 0)
                throw new RuntimeException("Zero size style runs.");
            if (styleRun.start > 0 && styleRun.start >= length() - (displayOnly ? -1 : 1))
                throw new RuntimeException("Style run after end.");
            if (styleRun.start < 0)
                throw new RuntimeException("Style run start -1.");
            if (styleRun.start <= lastStart)
                throw new RuntimeException("Style run back jump");
            lastStart = styleRun.start;
        }
    }

    public void cleanStyleRuns() {
        if (styleRuns == null) {
            styleRuns = new ArrayList<AttributeRun>(2);
        }
        if (styleRuns.size() == 0) {
            final AttributeRun a = new AttributeRun(0);
            a.setAttribute(TextAttribute.SIZE, 11f);
            a.setAttribute(TextAttribute.FAMILY, "Default");
            styleRuns.add(a);
            return; //throw new RuntimeException("Null Style runs");
        }
        int lastStart = -1;
        final Iterator<AttributeRun> iterator = styleRuns.iterator();
        while (iterator.hasNext()) {
            AttributeRun styleRun = iterator.next();
            if (styleRun.entrySet().size() == 0)
                iterator.remove();
            if (styleRun.start > 0 && styleRun.start >= length() - (displayOnly ? -1 : 1))
                iterator.remove();
            if (styleRun.start < 0)
                iterator.remove();
            if (styleRun.start <= lastStart)
                iterator.remove();
            lastStart = styleRun.start;
        }
    }

    protected Object readResolve() {
        if (rowSpacing == 0)//upgrade old styled paragraphs
            rowSpacing = 1;
        cleanStyleRuns();
        return this;
    }
}
