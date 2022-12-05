// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.*;
import org.pdgen.model.RDBase;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.run.*;
import org.pdgen.model.style.CellStyle;
import org.pdgen.util.StringUtils;

import javax.swing.plaf.basic.BasicHTML;
import java.awt.*;
import java.util.Locale;
import java.util.Stack;

public abstract class SimpleTextCellDef extends FreeCellDef {
    private static final long serialVersionUID = 7L;
    protected String myText;
    protected transient TextCellLayouter layout;

    protected SimpleTextCellDef(SimpleTextCellDef from, TemplateModel parentGrid) {
        super(from, parentGrid);
        myText = from.myText;
    }

    protected SimpleTextCellDef(TemplateModel parentGrid, String text) {
        super(parentGrid);
        myText = text;
    }

    public static String wrapText(String txt, CellStyle cs, Locale loc) {
        String prefix = Internationalisation.localize(cs.getPrefix(), loc);
        String appendix = Internationalisation.localize(cs.getSuffix(), loc);
        if (prefix == null) {
            if (appendix == null)
                return txt;
            else if (txt == null)
                return appendix;
            else
                return txt + appendix;
        } else if (appendix == null) {
            if (txt == null)
                return prefix;
            else
                return prefix + txt;
        } else if (txt == null)
            return prefix + appendix;
        else
            return prefix + txt + appendix;
    }

    protected String getWrappedText(Locale loc) {
        return wrapText(Internationalisation.localize(myText, loc), getCascadedStyle(), loc);
    }

    protected void adjustSize(Locale loc, Graphics2D g) {
        super.adjustSize(loc, g);
        initLayouter();
        layout.calcSize(loc, g);
    }

    void initLayouter() {
        if (layout != null)
            return;
        int tt = getCascadedStyle().getTextType();
        if (tt == CellStyle.vertBottomUp || tt == CellStyle.vertTopDown) {
            layout = new VerticalTextCellLayouter(this);
        } else if (tt == CellStyle.plainType) {
            if (myText == null)
                layout = new PlainTextCellLayouter(this);
            else if (BasicHTML.isHTMLString(myText))
                layout = new HtmlLayouter(this);
            else if (myText.indexOf('\n') < 0)
                layout = new PlainTextCellLayouter(this);
            else
                layout = new MultiLineTextCellLayouter(this);
        } else if (tt == CellStyle.htmlType)
            layout = new HtmlLayouter(this);
        else if (tt == CellStyle.rtfType)
            layout = new RtfLayouter(this);
        else if (tt == CellStyle.reFlowType)
            layout = new FlowingTextCellLayouter(this);
        else
            throw new NotYetImplementedError("This text type not implemented " + tt);
    }

    public String getText() {
        return myText;
    }

    public String getFormattedString(DBData from, AggregateCollector into) throws JoriaDataException {
        return wrapText(myText, getCascadedStyle(), into.getRunEnv().getLocale());
    }

    public void paint(Graphics2D p, float x0, float y0, float w, float h, Locale loc) {
        initLayouter();
        p.setFont(cascadedStyle.getStyledFont());
        p.setColor(cascadedStyle.getForeground());
        layout.paint(p, x0, y0, w, h, loc);
    }

    public void clearCachedStyle() {
        super.clearCachedStyle();//To change body of overridden methods use File | Settings | File Templates.
        layout = null;// throw away layouter in case text type has changed
    }

    public void setStyle(CellStyle sty) {
        super.setStyle(sty);
        layout = null;// throw away layouter in case text type has changed
    }

    // this is really only useful when a style requests reflowing of contents
    // the layouter is required to do the right thing. I.e use the width and set the height
    public void reFlow(float width, Locale loc, Graphics2D g) {
        myWidth = width;
        initLayouter();
        layout.calcSize(loc, g);
    }

    public float heightForWidth(float width, Locale loc, Graphics2D g) {
        reFlow(width, loc, g);
        return myHeight;
    }

    public boolean isReflowing() {
        return getCascadedStyle().getTextType().equals(CellStyle.reFlowType) || getCascadedStyle().getTextType().equals(CellStyle.htmlType) || getCascadedStyle().getTextType().equals(CellStyle.rtfType);
    }

    public static String abbrev(String name) {
		/*
		if (name.length() > 11)
            return name.substring(0, 5) + '\'' + name.substring(name.length() - 5);
            */
        return name;
    }

    public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException {
        if (repeater == null) {
            if (!isVisible(outMode, from))
                return RVSupressHeader.instance;
            else
                return new RVString(getFormattedString(from, outMode.getRunEnv().getPager()), getCascadedStyle(), g);
        } else {
            DBCollection source = (DBCollection) from;
            final int length = Math.max(source.getLength(), RVStringCol.startSize);
            RVStringCol col;
            col = new RVStringCol(length);
            return col;
        }
    }

    public boolean makeGraphicElement(TableBorderRequirements tblReq, int iter, FillPagedFrame out) throws JoriaDataException {
        String s = getGraphElemString(tblReq, iter, out);
        if (tblReq.value == RVSupressHeader.instance) {
            return out.makeNull(tblReq);
        }
        if (!StringUtils.isEmpty(s)) {
            if (getCascadedStyle().getTextType().equals(CellStyle.htmlType) || BasicHTML.isHTMLString(s))
                return out.makeTextHtmlGraphEl(tblReq, s);
            if (getCascadedStyle().getTextType().equals(CellStyle.rtfType))
                return out.makeTextRtfGraphEl(tblReq, s);
            else
                return out.makeTextGraphEl(tblReq, s);
        } else
            return out.makeEmptyGrel(tblReq);
    }

    public float getMaxWidth(RVAny values, Locale loc, Graphics2D g) {
        if (values == null)
            return 0;
        else if (values instanceof RVStringCol) {
            ((RVStringCol) values).buildFormattedStrings(this, loc);
            String[] strings = ((RVStringCol) values).get();
            float w = 0;
            CellStyle cs = getCascadedStyle();
            for (String string : strings) {
                if (string != null) {
                    w = Math.max(w, cs.getWidth(string, g));
                }
            }
            return w;
        } else if (values instanceof RVString) {
            return getMaxWidthSimple(values, loc);
        } else if (values instanceof RVSupressHeader) {
            //RVSupressHeader sh = (RVSupressHeader) values;
            return 0;
        } else
            throw new JoriaAssertionError("Unhandled data value: " + values.getClass());
    }

    public float getMaxWidthSimple(RVAny value, @SuppressWarnings("UnusedParameters") Locale loc) {
        if (value == null || value == RVSupressHeader.instance)
            return 0;
        else if (value instanceof RVString) {
            return ((RVString) value).getWidth();
        } else
            throw new JoriaAssertionError("SimpleTextCellDef not a RVStringl: " + value.getClass().getName());
    }

    protected String getGraphElemString(TableBorderRequirements tblReq, int iter, FillPagedFrame out) {
        String s;
        if (tblReq.value != null && tblReq.value instanceof RValue)// must check if value is an RValue in case the user has put a table into the header of another table
        {
            final RValue rValue = (RValue) tblReq.value;
            s = rValue.get(iter);
        } else {
            s = null;
        }
        return s;
    }

    public boolean hasText(final String text) {
        if (super.hasText(text))
            return true;
        //noinspection UnnecessaryLocalVariable
        final boolean ret = myText != null && myText.toLowerCase().contains(text);
        return ret;
    }

//	public void createPngImageForRtf(OutputStream out, String text) throws IOException
//	{
//		/*        float width = 400;
//				if (presetWidth.getUnit() > FlexSize.flex && presetWidth.getUnit() < FlexSize.min)
//				{
//					width = FlexSize.getPoints(presetWidth.getUnit(), presetWidth.getVal());
//				}
//				float height = width / 0.71f;
//				if (presetHeight.getUnit() > FlexSize.flex && presetHeight.getUnit() < FlexSize.min)
//				{
//					height = FlexSize.getPoints(presetHeight.getUnit(), presetHeight.getVal());
//				}
//				else if (presetHeight.getUnit() == FlexSize.wfactor)
//				{
//					height = width * presetHeight.getVal();
//				}
//				ch.setBounds(0, 0, Math.round(width), Math.round(height));
//				BufferedImage bi = new BufferedImage(Math.round(width), Math.round(height), BufferedImage.TYPE_4BYTE_ABGR);
//				Graphics g = bi.getGraphics();
//				g.setColor(getCascadedStyle().getBackground());
//				g.fillRect(0, 0, Math.round(width), Math.round(height));
//				ch.validate(g);
//				ch.render(g);
//				g.dispose();
//				byte[] pngbytes = ConvertImageToByteArray.convertImage(bi);
//				if (pngbytes == null)
//				{
//					Trace.logError("Null image");
//				}
//				else
//				{
//					out.write(pngbytes);
//				}
//				out.close();*/
//	}
}
