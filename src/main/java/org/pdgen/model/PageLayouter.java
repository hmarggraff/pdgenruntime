// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.data.Internationalisation;
import org.pdgen.data.JoriaAssertionError;
import org.pdgen.data.Trace;
import org.pdgen.env.JoriaUserError;
import org.pdgen.env.Res;
import org.pdgen.env.Settings;
import org.pdgen.model.style.FlexSize;
import org.pdgen.model.style.FrameStyle;
import org.pdgen.model.style.PageStyle;
import org.pdgen.model.style.SizeLimit;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class PageLayouter implements GridLayouter {
    public final int pageSeparationDistance = 30;
    public static final float displayHOffset = 7;
    public static final float displayVOffset = 7;
    ///
    protected final PageMaster page;
    private final TemplateEditorIF container;
    float bodyY;// the y coordinate where the header (+ its space after) end. I.e thats where the body begins
    float bottomEnd;
    float maxFrameWidth;// the width of the widest frame
    protected Dimension sizeForLayout;
    protected boolean layoutValid;// becomes invalid when PageStyle or contents change
    private TemplateLayouter headerLayouter;
    private TemplateLayouter footerLayouter;
    protected boolean showGrid;
    protected boolean showRepeaters;
    protected boolean showingEmptyCells;
    int pageRange = PageLevelBox.firstPage;
    protected final ArrayList<TemplateLayouter> frameLayouters = new ArrayList<TemplateLayouter>(4);
    ArrayList<PageBreak> splitPages;
    final Rectangle2D tempRect = new Rectangle2D.Float();
    protected final Line2D.Float tempLine = new Line2D.Float();
    protected final Rectangle2D.Float hr = new Rectangle2D.Float();

    public PageLayouter(PageMaster box, TemplateEditorIF container) {
        page = box;
        this.container = container;
        buildTemplateLayouters();
        calcLayoutSize(0);
        box.setLayouter(this);
    }

    public void layoutChildren(Graphics2D g) throws JoriaUserError {
        if (layoutValid)
            return;
        buildTemplateLayouters();
        PageStyle paStyle = page.getCascadedPageStyle();
        bodyY = paStyle.getTopMargin().getValInPoints();
        //bodyH = paStyle.getHeightInsideMargin();
        bottomEnd = paStyle.getHeightToBottomMargin();
        maxFrameWidth = 0;
        float leftMargin = paStyle.getLeftMargin().getValInPoints();
        float bodyPos = bodyY;
        splitPages = new ArrayList<PageBreak>(1);
        float useWidth;
        if (headerLayouter != null) {
            headerLayouter.calcWidths(g);
            useWidth = Math.max(headerLayouter.reqWidth, paStyle.getBodyWidth());
            headerLayouter.layoutHorizontal(useWidth, leftMargin, g);
            maxFrameWidth = headerLayouter.reqWidth;
            headerLayouter.layoutVertical(bodyPos, g);
            page.setHeaderHeight(pageRange, headerLayouter.constrainedHeight);
            bodyY = bodyY + headerLayouter.constrainedHeight;
            bodyPos = bodyPos + headerLayouter.getHeight();
            if (bodyY != bodyPos)
                Trace.logWarn("Ambiguous vertical body start. Please debug PageLayouter");
        }
        if (footerLayouter != null) {
            footerLayouter.calcWidths(g);
            useWidth = Math.max(footerLayouter.reqWidth, paStyle.getBodyWidth());

            footerLayouter.layoutHorizontal(useWidth, leftMargin, g);

            maxFrameWidth = Math.max(footerLayouter.reqWidth, maxFrameWidth);
            footerLayouter.layoutVertical(bottomEnd, g);
            bottomEnd -= footerLayouter.constrainedHeight;
            footerLayouter.repositionY(bottomEnd);// if there is a reflower in the footer, then constrained height is changed by layout inner and we must reposition
            page.setFooterHeight(pageRange, footerLayouter.constrainedHeight);
        }
        float maxBot = bodyY;
        float totalHeight = 0;
        //TemplateLayouter selectedTemplate = EditorFrame.templateEditor.getSel().layout;
        for (int i = 0; i < frameLayouters.size(); i++) {
            TemplateLayouter frameLayouter = frameLayouters.get(i);
            //PageLevelBox abbox = (PageLevelBox) frameLayouter.myBox;
            FrameStyle fs = frameLayouter.myBox.getCascadedFrameStyle();
            if (fs.getOnNewPage() || bodyPos > bottomEnd - 10)// approximately first line of frame should fit
            {
                float pageHeight = paStyle.getPageHeight();
                maxBot = Math.max(pageHeight - bottomEnd + maxBot, pageHeight);
                splitPages.add(new PageBreak(i, maxBot));
                totalHeight += maxBot + pageSeparationDistance;
                maxBot = bodyPos = bodyY;
            }
            frameLayouter.setPageNo(splitPages.size());
            frameLayouter.setComponentPos(totalHeight);
			/*
			if (frameLayouter == selectedTemplate)
			{
				container.setPageNo(splitPages.size());
			}
			*/
            if (fs.getYPos() != null && fs.getYPos().getUnit() != FlexSize.flex) {
                bodyPos = fs.getYPos().getVal();
            }
            float bodyWidth;
            float x;
            if (fs.getXPos() != null && fs.getXPos().getUnit() != FlexSize.flex) {
                x = fs.getXPos().getVal();
                bodyWidth = paStyle.getPageWidth() - paStyle.getRightMargin().getValInPoints() - x;
            } else {
                x = paStyle.getLeftMargin().getValInPoints();
                bodyWidth = paStyle.getBodyWidth();
            }
            if (fs.getWidth() != null && fs.getWidth().getUnit() != FlexSize.flex) {
                bodyWidth = fs.getWidth().getVal();
            }
            frameLayouter.calcWidths(g);
            useWidth = Math.max(frameLayouter.constrainedWidth, bodyWidth);
            frameLayouter.layoutHorizontal(useWidth, x, g);
            maxFrameWidth = Math.max(frameLayouter.reqWidth, maxFrameWidth);

            if (bodyPos > bottomEnd && !fs.getOnNewPage() && !fs.getBreakable()) {// occurs when an atomic frame is on the page border
                if (fs.getYPos().isExpandable()) {
                    maxBot = paStyle.getPageHeight() + maxBot - bottomEnd;
                    splitPages.add(new PageBreak(i, maxBot));
                    totalHeight += maxBot + pageSeparationDistance;
                    maxBot = bodyPos = bodyY;
                } else {
                    throw new JoriaUserError("Positioned_frame_breaks_page_which_is_not_allowed_Please_change_your_page_design");
                }
            }
            frameLayouter.layoutVertical(bodyPos, g);
            final float height = frameLayouter.getHeight();
            bodyPos = bodyPos + height;
            maxBot = Math.max(maxBot, bodyPos);
            bodyPos++;
            maxBot++;
        }
        //maxBot += paStyle.getBotMargin().getVal();
        float pageHeight = paStyle.getPageHeight();
        maxBot = Math.max(pageHeight - bottomEnd + maxBot, pageHeight);
        //maxBot = Math.max(maxBot, pageHeight);
        splitPages.add(new PageBreak(frameLayouters.size(), maxBot));
        layoutValid = true;
        totalHeight += maxBot + pageSeparationDistance;
        calcLayoutSize(totalHeight);
        if (container != null) {
            container.setPreferredSize(sizeForLayout);
            container.revalidate();
        }
    }

    public void getCellAt(Point p, CellLocation out) {
		/*
		1. Determine page click belongs to
		2. determine Frame
		3. determine cell
		4. determine column for farbelow
		*/
        final PageStyle ps = page.getCascadedPageStyle();
        //final float end = (ps.getPageHeight() + 30) * splitPages.size();
        float end = 0;
        for (PageBreak pageBreak : splitPages) {
            end += pageBreak.maxBot + pageSeparationDistance;
        }
        float py = p.y / getScale();
        if (py <= ps.getTopMargin().getValInPoints() || p.x <= -3 || /*p.x > ps.getPageWidth() ||*/ py > end) {
            out.relative = CellLocation.NOWHERE;
            return;
        }
        float pageStart = 0;
        for (int pageNo = 0; pageNo < splitPages.size(); pageNo++) {
            //out.pageNo = pageNo;
            PageBreak pageBreak = splitPages.get(pageNo);
            p.y = (int) (py - pageStart);//p.y - (int) (pageNo * (ps.getPageHeight() + 30));
            int gix = pageBreak.lix;
            int egix = 0;
            if (pageNo > 0)
                egix = splitPages.get(pageNo - 1).lix;
            TemplateLayouter l;
            for (int i = gix - 1; i >= egix; i--)// search from bottom to top, so that lowest frame has a chance of indicating farbelow
            {
                l = frameLayouters.get(i);
                l.getCellAt(p, out);
                if (out.relative != CellLocation.NOWHERE)
                    return;// done
            }
            if (headerLayouter != null) {
                headerLayouter.getCellAt(p, out);
                if (out.relative != CellLocation.NOWHERE)
                    return;
            }
            if (footerLayouter != null) {
                final int shiftFooter = (int) (pageBreak.maxBot - ps.getPageHeight());
                p.y -= shiftFooter;// transform further down if page is extended.
                footerLayouter.getCellAt(p, out);
                p.y += shiftFooter;
                if (out.relative != CellLocation.NOWHERE) {
                    return;
                }
            }
            // check if we are in the free area between the last body frame and the footer
            // return it as FARBELOW
            if (gix > 0)// Dieses ist nur dann falsch, wenn auf der ersten Seite kein Frame existiert
            {
                l = frameLayouters.get(gix - 1);
                if (p.y < bottomEnd && p.x >= l.xOff && p.x <= l.scaleToWidth) {
                    out.relative = CellLocation.FARBELOW;
                    out.model = page.getLastBody().getTemplate();
                    out.col = 0;
                    out.row = out.model.getRowCount() - 1;
                }
            }
            pageStart += pageBreak.maxBot + pageSeparationDistance;
        }
    }

    void buildTemplateLayouters() {
        Trace.log(Trace.layout, "rebuilding Template layouters " + pageRange);
        frameLayouters.clear();
        PageLevelBox hlb = null;
        PageLevelBox flb = null;
        if (pageRange == PageLevelBox.firstPage) {
            hlb = page.getFirstPageHeader();
            flb = page.getFirstPageFooter();
        } else if (pageRange == PageLevelBox.middlePage) {
            flb = page.getMiddlePagesFooter();
            hlb = page.getFurtherPagesHeader();
        } else if (pageRange == PageLevelBox.lastPage) {
            flb = page.getLastPageFooter();
            hlb = page.getFurtherPagesHeader();
        }
        if (hlb != null)
            headerLayouter = makeGridLayouter(hlb);
        else
            headerLayouter = null;
        if (flb != null)
            footerLayouter = makeGridLayouter(flb);
        else
            footerLayouter = null;
        ArrayList<PageLevelBox> bodyFrames = page.getFrames();
        for (PageLevelBox b : bodyFrames) {
            if (b.getBoxType() == TemplateBoxInterface.pageBodyBox) {
                frameLayouters.add(makeGridLayouter(b));
            } else
                throw new JoriaAssertionError("A non body appears in the list of body frames: " + b.getBoxTypeName());
        }
        layoutValid = false;
    }

    private TemplateLayouter makeGridLayouter(PageLevelBox b) {
        TemplateLayouter nl = b.getTemplate().getLayouter();
        if (nl == null || nl.container != this) {
            nl = new TemplateLayouter(this, b);
            b.getTemplate().setLayouter(nl);
        } else
            nl.invalidateLayout();
        return nl;
    }

    public float calcBoxHeight(PageLevelBox b, Graphics2D g) {
        if (b == null)
            return 0;
        TemplateLayouter l = makeGridLayouter(b);
        l.calcWidths(g);
        l.calcHeights(g);
        return l.constrainedHeight;
    }

    void calcLayoutSize(float totalHeight) {
        PageStyle ps = page.getCascadedPageStyle();
        float paW = Math.max(ps.getPageWidth(), maxFrameWidth + ps.getLeftMargin().getValInPoints());
		/*
				  int splitPageCnt = 1;
				  if (splitPages != null)
					  splitPageCnt = splitPages.size();
				  float paH = (Math.max(ps.getPageHeight(), totalHeight) + 30) * splitPageCnt;
				  */
        sizeForLayout = new Dimension(Math.round((paW + 24) * getScale()), Math.round(totalHeight * getScale()));
    }

    public void invalidateLayout() {
        Trace.logDebug(Trace.layout, "invalidateLayout Container");
        layoutValid = false;
        if (container != null)
            container.repaint();
    }

    public void layoutVertical(float y, Graphics2D g) {
    }

    public void layoutOuter(GridLayouter innerGrid, Graphics2D g) {
        Trace.logDebug(Trace.layout, "layoutOuter valid=" + layoutValid);
        //this page level is fixed, so we can go back in
        layoutChildren(g);
    }

    float paintOneGrid(Graphics2D g2, TemplateLayouter tl, float yPos) {
        if (tl == null)
            return 0;
        final float ty = yPos - tl.yOff;
        g2.translate(0, ty);
        tl.paintIt(g2);
        g2.translate(0, -ty);
        return tl.yOff + tl.envelopeHeight;
    }

    public void paintOrPrint(Graphics2D g2) {
        layoutChildren(g2);
        //Rectangle b = g2.getClipBounds();
        // Trace.logDebug(4,"PaintComponent " + g2.getClip());
        PageStyle s = page.getCascadedPageStyle();
        //final float pageHeight = s.getPageHeight();
        float tm = s.getTopMargin().getValInPoints();
        float bm = s.getBotMargin().getValInPoints();
        float lm = s.getLeftMargin().getValInPoints();
        float rm = s.getRightMargin().getValInPoints();
        float pW = s.getPageWidth();
        //int pH = Math.round(pageHeight);
        //JViewport jViewport = ((JViewport) container.getParent());
        //Rectangle b = jViewport.getViewRect();
        //Rectangle2D clipBounds = jViewport.getViewRect();
        //Rectangle2D clipBounds = g2.getClip().getBounds2D();
        //g2.setClip(null);
        g2.setBackground(Res.lightBlue);
        //g2.setClip(clipBounds);
        //g2.fill(b);
        Stroke oldStroke = g2.getStroke();
        float pagePos = displayVOffset;
        int splitAt = 0;
        for (final PageBreak pageSplitter : splitPages) {
            g2.translate(displayHOffset, pagePos);
			/*
			   g2.setColor(Color.green);
			   tempRect.setRect(7, 6, pW + 1, pageSplitter.maxBot + 2);
			   g2.fill(tempRect);
   */
            //g2.translate(-2, -2);
            g2.setColor(Color.gray);
            tempRect.setRect(7, 7, pW, pageSplitter.maxBot);
            g2.fill(tempRect);
            g2.setColor(Color.WHITE);
            tempRect.setRect(0, 0, pW, pageSplitter.maxBot);
            g2.fill(tempRect);
            g2.setColor(s.getBackground());
            g2.fill(tempRect);
            if (s.getBackgroundImageName() != null) {
                ImageIcon background = s.getBackgroundImage(Internationalisation.NOREPLACE);
                float width = background.getIconWidth();
                float scale = 0;
                FlexSize imageScale = s.getBackgroundImageTargetWidth();
                float outWith = s.getPageWidth();

                SizeLimit limit = s.getSizeLimit();
                final boolean mustScaleDown = limit == SizeLimit.AtMost && width > outWith;
                final boolean mustScaleUp = limit == SizeLimit.AtLeast && width < outWith;
                final boolean mustScale = limit == SizeLimit.Fix && width != outWith;
                if (outWith != 0 && (mustScaleDown || mustScaleUp || mustScale)) {
                    scale = outWith / width;
                }
                if (imageScale != null && !imageScale.isExpandable()) {
                    scale = imageScale.getVal() / width;
                }
                double movex = s.getBackgroundImageX().getValInPoints();
                double movey = s.getBackgroundImageY().getValInPoints();
                Graphics2D g2d = (Graphics2D) g2.create();
                g2d.translate(movex, movey);
                tempRect.setRect(0, 0, pW - movex, pageSplitter.maxBot - movey);
                g2d.setClip(tempRect);
                if (scale != 0)
                    g2d.scale(scale, scale);
                g2d.drawImage(background.getImage(), 0, 0, null);
                g2d.dispose();
            }
            if (isShowGrid()) {
                g2.setStroke(new BasicStroke(1 / getScale()));
                Color color = Color.lightGray;
                if (Settings.INSTANCE.getShowDebugColors())
                    color = Color.red;
                g2.setColor(color);
                tempLine.setLine(0, bodyY, pW, bodyY);// start of body
                g2.draw(tempLine);
                float bodyEnd;
                if (footerLayouter != null)
                    bodyEnd = pageSplitter.maxBot - s.getBotMargin().getValInPoints() - footerLayouter.constrainedHeight;
                else
                    bodyEnd = pageSplitter.maxBot - s.getBotMargin().getValInPoints();
                tempLine.setLine(0, bodyEnd, pW, bodyEnd);// end of body
                g2.draw(tempLine);
                tempLine.setLine(0, tm, pW, tm);// start of header
                g2.draw(tempLine);
                float pH = pageSplitter.maxBot;
                tempLine.setLine(0, pH - bm, pW, pH - bm);// end of footer
                g2.draw(tempLine);
                tempLine.setLine(lm, 0, lm, pH);
                g2.draw(tempLine);
                tempLine.setLine(pW - rm, 0, pW - rm, pH);
                g2.draw(tempLine);
            }
            float bot = 0;
            if (headerLayouter != null) {
                bot = paintOneGrid(g2, headerLayouter, headerLayouter.yOff);
            }
            float maxBot = Math.max(0, bot);
            int gridGroup = pageSplitter.lix;
            for (int i = splitAt; i < gridGroup; i++) {
                TemplateLayouter tl = frameLayouters.get(i);
                bot = paintOneGrid(g2, tl, tl.yOff);
                maxBot = Math.max(maxBot, bot);
            }
            splitAt = gridGroup;
            if (footerLayouter != null) {
                paintOneGrid(g2, footerLayouter, Math.max(maxBot, footerLayouter.yOff));
            }
            g2.translate(-7, -pagePos);
            g2.setStroke(oldStroke);
            pagePos += pageSplitter.maxBot + pageSeparationDistance;
        }
    }

    public void printComponent2D(Graphics2D g2, int pageIndex) {
        Trace.log(Trace.run, "Print page");
        PageBreak pageSplitter = splitPages.get(pageIndex);
        int splitAt = 0;
        if (pageIndex > 0)
            splitAt = splitPages.get(pageIndex - 1).lix;
        PageStyle s = page.getCascadedPageStyle();
        //final float pageHeight = s.getPageHeight();
        float tm = s.getTopMargin().getValInPoints();
        float bm = s.getBotMargin().getValInPoints();
        float lm = s.getLeftMargin().getValInPoints();
        float rm = s.getRightMargin().getValInPoints();
        float pW = s.getPageWidth();
        if (isShowGrid()) {
            g2.setColor(Color.lightGray);
            tempLine.setLine(0, bodyY, pW, bodyY);// start of body
            g2.draw(tempLine);
            float bodyEnd;
            if (footerLayouter != null)
                bodyEnd = pageSplitter.maxBot - s.getBotMargin().getValInPoints() - footerLayouter.constrainedHeight;
            else
                bodyEnd = pageSplitter.maxBot - s.getBotMargin().getValInPoints();
            tempLine.setLine(0, bodyEnd, pW, bodyEnd);// end of body
            g2.draw(tempLine);
            g2.setColor(Color.lightGray);
            tempLine.setLine(0, tm, pW, tm);// start of header
            g2.draw(tempLine);
            float pH = pageSplitter.maxBot;
            tempLine.setLine(0, pH - bm, pW, pH - bm);// end of footer
            g2.draw(tempLine);
            tempLine.setLine(lm, 0, lm, pH);
            g2.draw(tempLine);
            tempLine.setLine(pW - rm, 0, pW - rm, pH);
            g2.draw(tempLine);
        }
        float bot = 0;
        if (headerLayouter != null) {
            bot = paintOneGrid(g2, headerLayouter, headerLayouter.yOff);
        }
        float maxBot = Math.max(0, bot);
        int gridGroup = pageSplitter.lix;
        for (int i = splitAt; i < gridGroup; i++) {
            TemplateLayouter tl = frameLayouters.get(i);
            bot = paintOneGrid(g2, tl, tl.yOff);
            maxBot = Math.max(maxBot, bot);
        }
        if (footerLayouter != null) {
            paintOneGrid(g2, footerLayouter, Math.max(maxBot, footerLayouter.yOff));
        }
		/*
	   if (headerLayouter != null)
		   headerLayouter.paintIt(g2);
	   for (int i = gridlas.size() - 1; i >= 0; i--)
	   {
		   ((TemplateLayouter) gridlas.get(i)).paintIt(g2);
	   }
	   if (footerLayouter != null)
		   footerLayouter.paintIt(g2);
		   */
    }

    public void invalidateAll() {
        if (headerLayouter != null)
            headerLayouter.invalidateLayout();
        for (int i = frameLayouters.size() - 1; i >= 0; i--) {
            frameLayouters.get(i).invalidateLayout();
        }
        if (footerLayouter != null)
            footerLayouter.invalidateLayout();
        invalidateLayout();
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public void setShowingEmptyCells(boolean showingEmptyCells) {
        this.showingEmptyCells = showingEmptyCells;
        invalidateAll();
    }

    public void setShowRepeaters(boolean showRepeaters) {
        this.showRepeaters = showRepeaters;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public boolean isShowingEmptyCells() {
        return showingEmptyCells;
    }

    public boolean isShowRepeaters() {
        return showRepeaters;
    }

    public float getScale() {
        if (container == null)
            return 1;
        return container.getScale();
    }

    public PageLayouter getTopContainer() {
        return this;
    }

    public GridLayouter getContainer() {
        return this;
    }

    public void noteChange() {
        page.noteChange();
    }

    public boolean isLayoutValid() {
        return layoutValid;
    }

    public void setPageRange(int pageRange) {
        this.pageRange = pageRange;
        invalidateLayout();
    }

    public void repaint() {
        // TODO ok to remove? layoutChildren(oops);
        if (container != null)
            container.repaint();
    }

    public TemplateLayouter getHeaderLayouter() {
        return headerLayouter;
    }

    public TemplateLayouter getFooterLayouter() {
        return footerLayouter;
    }

    public int getSplitPageCount() {
        return splitPages.size();
    }

    static class PageBreak {
        final int lix;
        final float maxBot;

        public PageBreak(int lix, float maxBot) {
            this.lix = lix;
            this.maxBot = maxBot;
        }
    }

}

