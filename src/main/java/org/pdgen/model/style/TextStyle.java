// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.I18nKeyHolder;
import org.pdgen.data.Internationalisation;
import org.pdgen.data.Internationalisation2;
import org.pdgen.data.JoriaFileService;

import org.pdgen.env.Env;
import org.pdgen.env.Res;
import org.pdgen.env.Settings;
import org.pdgen.model.run.RVImage;

import javax.swing.*;
import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public abstract class TextStyle extends StyleBase implements Serializable
{
    private static final long serialVersionUID = 7L;
    protected static String stringMeter;
	static final String defaultFloatPattern = new DecimalFormat().toPattern();
	static final String defaultIntPattern = ((DecimalFormat) NumberFormat.getNumberInstance()).toPattern();
	static final String defaultDatePattern = new SimpleDateFormat().toPattern();


	static
	{
		char[] ca = new char[128 - 32];
		for (char c = 32; c < 128; c++)
		{
			ca[c - 32] = c;
		}
        stringMeter = new String(ca);
	}

	protected String font;
	protected PointSize size;
	protected Boolean bold;
	protected Boolean italic;
	protected Boolean underlined;
	protected Color background;
	protected Color foreground;
	protected String backgroundImageName; // attention this attribute will be merged by the concret class
    protected FlexSize backgroundImageTargetWidth; // attention this attribute will be merged by the concret class
	protected String intPattern;
	protected String floatPattern;
	protected String datePattern;
    protected Float lineSpacingNumber;

	transient Font guiFont;
	transient float baseLine;
	transient float lineSpacing;
	protected transient ImageIcon backgroundImageCache;
	public static Color transparent = new Color(255, 255, 255, 0);

	protected TextStyle()//required for deserialisation
	{
	}

	protected TextStyle(String name)
	{
		super(name);
	}

	protected TextStyle(TextStyle b)
	{
		copyTextStyle(b);
	}

	public Font getStyledFont()
	{
		if (guiFont == null)
		{
			Hashtable<TextAttribute, Object> fa = new Hashtable<TextAttribute, Object>();
			if (bold)
				fa.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
			if (italic)
				fa.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
			fa.put(TextAttribute.SIZE, size.getValInPoints());
			fa.put(TextAttribute.FAMILY, font);
			guiFont = new Font(fa);
			LineMetrics lm = guiFont.getLineMetrics(stringMeter, Env.instance().getFontRenderContext());
			lineSpacing = lm.getHeight();
			baseLine = lm.getAscent();
		}
		return guiFont;
	}

	public void copyTextStyle(TextStyle s)
	{
		font = s.font;
		background = s.background;
		foreground = s.foreground;
		size = PointSize.newPointSize(s.size);
		bold = s.bold;
		italic = s.italic;
		underlined = s.underlined;
        backgroundImageName = s.backgroundImageName;
        backgroundImageTargetWidth = FlexSize.newFlexSize(s.backgroundImageTargetWidth);
		intPattern = s.intPattern;
		floatPattern = s.floatPattern;
		datePattern = s.datePattern;
        lineSpacingNumber = s.lineSpacingNumber;
	}

	public boolean textStyleEquals(TextStyle s)
	{
		//noinspection SimplifiableIfStatement
		if (s == null)
			return false;
		return StyleBase.eq(font, s.font) && StyleBase.eq(background, s.background) && StyleBase.eq(foreground, s.foreground)
				&& StyleBase.eq(size, s.size) && StyleBase.eq(bold, s.bold) && StyleBase.eq(italic, s.italic) && StyleBase.eq(underlined, s.underlined)
				&& StyleBase.eq(backgroundImageName, s.backgroundImageName) && FlexSize.eq(backgroundImageTargetWidth, s.backgroundImageTargetWidth)
				&& StyleBase.eq(datePattern, s.datePattern) && StyleBase.eq(floatPattern, s.floatPattern) && StyleBase.eq(intPattern, s.intPattern) && StyleBase.eq(lineSpacingNumber, s.lineSpacingNumber);
	}

	protected static void fillDefaultTextStyle4Page(TextStyle s)
	{
		s.font = getDefaultFontFamily();
		s.background = Color.white;
		s.foreground = Color.black;
		s.size = new PointSize(11);
		s.bold = Boolean.FALSE;
		s.italic = Boolean.FALSE;
		s.underlined = Boolean.FALSE;
	}

	protected void mergeTextStyleDefaults()
	{
		if (font == null || "Default".endsWith(font))
			font = getDefaultFontFamily();
		if (background == null)
			background = transparent;
		if (foreground == null)
			foreground = Color.black;
		if (size == null)
			size = new PointSize(11);
		if (bold == null)
			bold = Boolean.FALSE;
		if (italic == null)
			italic = Boolean.FALSE;
		if (underlined == null)
			underlined = Boolean.FALSE;
		if (intPattern == null)
			intPattern = defaultIntPattern;
		if (floatPattern == null)
			floatPattern = defaultFloatPattern;
		if (datePattern == null)
			datePattern = defaultDatePattern;

	}

	public Color getBackground()
	{
		return background;
	}

	public Boolean getBold()
	{
		return bold;
	}

	public String getFont()
	{
		return font;
	}

	public Color getForeground()
	{
		return foreground;
	}

	public Boolean getItalic()
	{
		return italic;
	}

	public PointSize getSize()
	{
		return size;
	}

	public Boolean getUnderlined()
	{
		return underlined;
	}

	public void mergeTextStyle(TextStyle s)
	{
		if (background == null)
			background = s.background;
		if (font == null)
			font = s.font;
		if (foreground == null)
			foreground = s.foreground;
		if (size == null)
			size = PointSize.newPointSize(s.size);
		if (bold == null)
			bold = s.bold;
		if (italic == null)
			italic = s.italic;
		if (underlined == null)
			underlined = s.underlined;
		if (intPattern == null)
			intPattern = s.intPattern;
		if (floatPattern == null)
			floatPattern = s.floatPattern;
		if (datePattern == null)
			datePattern = s.datePattern;
        if (lineSpacingNumber == null)
            lineSpacingNumber = s.lineSpacingNumber;

	}

	public void mergeTextOverrides(TextStyle s)
	{
		if (s.font != null)
			font = s.font;
		if (s.background != null)
			background = s.background;
		if (s.foreground != null)
			foreground = s.foreground;
		if (s.size != null)
			size = PointSize.newPointSize(s.size);
		if (s.bold != null)
			bold = s.bold;
		if (s.italic != null)
			italic = s.italic;
		if (s.underlined != null)
			underlined = s.underlined;
		if (s.datePattern != null)
			datePattern = s.datePattern;
		if (s.floatPattern != null)
			floatPattern = s.floatPattern;
		if (s.intPattern != null)
			intPattern = s.intPattern;
        if (lineSpacingNumber == null)
            lineSpacingNumber = s.lineSpacingNumber;

	}

	public void setBackground(Color newBackground)
	{
		background = newBackground;
	}

	public void setBold(Boolean newBold)
	{
		bold = newBold;
		guiFont = null;
	}

	public void setFont(String newFont)
	{
		font = newFont;
		guiFont = null;
		// Trace.logDebug(4,"TextStyle new base font: " + id);
	}

	public void setForeground(Color newForeground)
	{
		foreground = newForeground;
	}

	public void setItalic(Boolean newItalic)
	{
		italic = newItalic;
		guiFont = null;
	}

	public void setSize(PointSize newSize)
	{
		size = newSize;
		guiFont = null;
		// Trace.logDebug(4,"TextStyle size " + id + ": " + newSize);
	}

	public void setUnderlined(Boolean newUnderlined)
	{
		underlined = newUnderlined;
	}

	public float getLineSpacing()
	{
		getStyledFont();
		return lineSpacing;
	}

	public float getBaseLine()
	{
		getStyledFont();
		return baseLine;
	}

	public String getBackgroundImageName()
	{
		return backgroundImageName;
	}

	public void setBackgroundImageName(final String backgroundImageName)
	{
		this.backgroundImageName = backgroundImageName;
        backgroundImageCache = null;
    }

	public ImageIcon getBackgroundImage(Locale loc)
	{
        if(backgroundImageName == null)
            return null;
        if (backgroundImageCache == null || backgroundImageCache == Res.missingImageIcon)
		{
			backgroundImageCache = RVImage.buildImageFromFileName(backgroundImageName, loc, backgroundImageCache == null);
		}
		return backgroundImageCache;
	}

	public void setIntPattern(String newIntPattern)
	{
		intPattern = newIntPattern;
	}

	public String getIntPattern()
	{
		return intPattern;
	}

	public void setFloatPattern(String newFloatPattern)
	{
		floatPattern = newFloatPattern;
	}

	public String getFloatPattern()
	{
		return floatPattern;
	}

	public void setDatePattern(String newDatePattern)
	{
		datePattern = newDatePattern;
	}

	public String getDatePattern()
	{
		return datePattern;
	}


	public void makeFileName(final JoriaFileService fs, final boolean relative)
	{
		if (relative)
			backgroundImageName = fs.makeFilenameRelative(backgroundImageName);
		else
			backgroundImageName = fs.makeFilenameAbsolute(backgroundImageName);
	}

    public FlexSize getBackgroundImageTargetWidth()
    {
        return backgroundImageTargetWidth;
    }

    public void setBackgroundImageTargetWidth(FlexSize backgroundImageTargetWidth)
    {
        this.backgroundImageTargetWidth = backgroundImageTargetWidth;
    }

	public void collectI18nKeys(HashSet<String> keySet)
	{
		Internationalisation.collectI18nKeys(intPattern, keySet);
		Internationalisation.collectI18nKeys(floatPattern, keySet);
		Internationalisation.collectI18nKeys(datePattern, keySet);
	}

	public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> keySet)
	{
		if (this == PredefinedStyles.instance().thePageStyleDefaultPageStyle)
			return;
		Internationalisation2.collectI18nKeys(intPattern, keySet, new I18nKeyHolder()
		{
			public void setI18nKey(String newVal)
			{
				intPattern = newVal;
			}
		});
		Internationalisation2.collectI18nKeys(floatPattern, keySet, new I18nKeyHolder()
		{
			public void setI18nKey(String newVal)
			{
				floatPattern = newVal;
			}
		});
		Internationalisation2.collectI18nKeys(datePattern, keySet, new I18nKeyHolder()
		{
			public void setI18nKey(String newVal)
			{
				datePattern = newVal;
			}
		});
	}

    public void setLineSpacingNumber(Float lineSpacingNumber)
    {
        this.lineSpacingNumber = lineSpacingNumber;
    }

    public Float getLineSpacingNumber()
    {
        return lineSpacingNumber;
    }

    public static String getDefaultFontFamily()
    {
        String fontDefault = Settings.get("DefaultFontFamily");
		return Objects.requireNonNullElse(fontDefault, "Default");
    }
}
