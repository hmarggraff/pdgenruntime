// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.*;
import org.pdgen.model.*;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.DeferredTotalPagesCell;
import org.pdgen.model.cells.PagestotalCell;
import org.pdgen.model.cells.StyledTextCellDef;
import org.pdgen.model.style.*;
import org.pdgen.styledtext.model.StyledParagraph;
import org.pdgen.styledtext.model.StyledParagraphList;

import org.pdgen.data.view.AccessPath;
import org.pdgen.data.view.AggregateDef;
import org.pdgen.env.Env;
import org.pdgen.env.Res;
import org.pdgen.env.Settings;
import org.pdgen.model.run.OutputMode.LastRowState;

import org.pdgen.env.JoriaException;
import org.pdgen.env.JoriaUserError;
import org.pdgen.env.JoriaUserException;
import org.pdgen.util.Send;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.Size2DSyntax;
import javax.print.attribute.standard.DialogTypeSelection;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.*;
import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

//import javax.print.attribute.standard.DialogTypeSelection;


public class PageEnv2 extends AggregateCollector {
    private boolean isVirgin = true;// need initializing true for each section
    private FillPagedFrame currBodyFiller;// Header or Footer fillers
    private FillPagedFrame firstHeaderFiller;
    private FillPagedFrame furtherHeaderFiller;
    private FillPagedFrame firstFooterFiller;
    private FillPagedFrame middleFooterFiller;
    private FillPagedFrame lastFooterFiller;
    private int atPage = -1;// current page
    private boolean endOfLastSectionSeen;// all of the sections of the template have been build
    private ArrayList<DisplayList> pages;// already rendered pages
    private final ArrayList<Object> bodies = new ArrayList<>();// either the Filler for a body frame or
    // in case of a TOC frame the PageLevelBox
    private ArrayList<Rectangle2D.Float> framesOnPage;// List of all Frames on the current page to
    // detect overlapping frames.
    private int bodyCount = 1;// index of the current body frame to be rendered
    private int pageCnt;// current page counter. Needed when pages is null
    private final GrelViewer painter;// display object
    private int destBodyStart = -1;// where on the display list, starts the current page
    private float spaceInMargin;// net available height on the current page
    private boolean buildAllPages;// if either total pages or table of contents is used
    private boolean hasPagesTotal;// if total pages is used
    private boolean singleMasterMode;// email output each template or master is a seperate email
    private final boolean holdPages;// if pages are to be stored (for the display) to be reused
    protected SimplePageGenerator generator;//
    private DisplayList lastPage;// to set the displayed page number in the viewer, the last page is stored here
    private int setTotalsNumber;// to use totalPages from a formula
    private int setPageNo = -1;// to use the current page number with the totalPages from a formula
    private int displayPageCount = -1;// current page number as used in headers or footers
    private ArrayList<DisplayList> pagesForNumbering;// DisplayList of pages to be used for numbering (TotalPages)
    private boolean isNumberingVirgin = true;// we have to search for TOC and totalPages

    protected PageEnv2(boolean holdPages, RunEnvImpl env) {
        super(env);
        this.holdPages = holdPages;
        if (holdPages)
            pages = new ArrayList<>();
        Template r = env.getStartTemplate();
        if (r.getReportType() == Template.masterDetailReport)
            generator = new MaDetPageGenerator();
        else
            generator = new SimplePageGenerator();
        float ph = 0;
        float pw = 0;
        while (r != null) {
            PageStyle ps = r.getPage().getCascadedPageStyle();
            pw = Math.max(ps.getPageWidth(), pw);
            ph = Math.max(ps.getPageHeight(), ph);
            r = r.getNextSection();
        }
        painter = new GrelViewer(Math.round(pw), Math.round(ph));// Build a viewer for the biggest page size
    }

    /**
     * load (generate) page to be displayed
     *
     * @param pageNo the number of the page to be processed;
     * @return the current page number after loading
     * @throws JoriaException in case of problems
     */
    public int loadPage(final int pageNo) throws JoriaException {
        if ((atPage == pageNo) || (endOfLastSectionSeen && (pageNo >= pageCnt && atPage == pageCnt - 1)))        // this is the current page or the current page is the last page available
        {
            return atPage;// do not go anywhere
        }        // this page or all pages have already been rendered
        if (pages != null && (endOfLastSectionSeen || pageNo < pages.size())) {            // display the requested or the last page, which ever comes first
            if (pageNo < pages.size())
                atPage = pageNo;
            else
                atPage = pages.size() - 1;
            painter.setPageContents(pages.get(atPage));
        } else {            // render the requested page in the background (where possible)
            final ObjectHolder<JoriaException> exception = new ObjectHolder<>();
            Env.instance().runInBackground(() -> {
                try {                        // Search for totalPages and TOCs in this case we have to build all pages
                    searchReportForTotalCells();
                    if (buildAllPages)// we have to build all pages
                    {
                        fillModel(Integer.MAX_VALUE);// force loading of all pages
                        if (pages.size() == 0) {
                            final PageMaster page = env.getStartTemplate().getPage();
                            PageStyle cascadedPageStyle = page.getCascadedPageStyle();
                            DisplayList dest = new DisplayList(cascadedPageStyle.getPageWidth(), cascadedPageStyle.getPageHeight(), env.getStartTemplate());
                            pages.add(dest);
                        }
                        if (pageNo < pages.size())// select the selected page or the last page, whatever comes first
                            atPage = pageNo;
                        else
                            atPage = pages.size() - 1;
                        buildTotalPagesAndToc();// build the last totalPages and toc
                        painter.setPageContents(pages.get(atPage));
                    } else
                        fillModel(pageNo);// juts build the requested pages
                } catch (JoriaException e) {
                    exception.value = e;
                }
            }, env.getLocale());
            if (exception.value != null)
                throw exception.value;
        }
        return atPage;
    }

    /**
     * fix the number of total pages and build the toc
     *
     * @throws JoriaDataException in case of trouble
     */
    private void buildTotalPagesAndToc() throws JoriaDataException {
        if (hasPagesTotal)// we use the totals cells
        {
            try {
                setTotalsNumber = getPagesForPageNumbers().size();  // Formulas access the current page number from this page env -- we have to mimic the current page number
                for (int i = 0; i < setTotalsNumber; i++)// for all pages in this run of reports
                {
                    final DisplayList list = getPagesForPageNumbers().get(i);
                    Vector<GrahElPostprocess> dl = list.getTotalPages();// all cells which use totalPage
                    if (dl == null || dl.size() == 0)
                        continue;
                    for (GrahElPostprocess elPostprocess : dl) {
                        setPageNo = i;
                        final DeferredTotalPagesCell sourceCell = elPostprocess.getPostprocessSource();
                        sourceCell.postProcess(elPostprocess, env, i + 1, getPagesForPageNumbers().size());
                    }
                }
            } finally {
                setPageNo = -1;
                setTotalsNumber = 0;
            }
        }
        if (pagesForNumbering != null)
            pagesForNumbering.clear();
    }

    /**
     * Builds the FilePageFrames for a Template
     *
     * @param val    root or current value for a master-detail-template
     * @param runEnv Environment
     * @throws JoriaDataException in case of problems
     */
    private void buildFillers(DBData val, RunEnv runEnv) throws JoriaDataException {
        searchReportForTotalCells();
        PageMaster p = template.getPage();
        ArrayList<PageLevelBox> frames = p.getFrames();
        final PageStyle pageStyle = p.getCascadedPageStyle();
        spaceInMargin = pageStyle.getHeightToBottomMargin();
        firstHeaderFiller = firstFooterFiller = furtherHeaderFiller = middleFooterFiller = lastFooterFiller = null;
        if (p.getFirstPageHeader() != null && p.getFirstPageHeader().isVisible(runEnv, val)) {            //float height = p.getHeaderHeight(PageLevelBox.firstPage);
            firstHeaderFiller = new FillPagedFrame(p.getFirstPageHeader().getTemplate(), val, this, true);
        }
        if (p.getFurtherPagesHeader() != null) {
            if (p.getFurtherPagesHeader() != p.getFirstPageHeader() && p.getFurtherPagesHeader().isVisible(runEnv, val)) {                //exp 05-12-11 remove dependency from desgner based layout.				//float height = p.getHeaderHeight(PageLevelBox.middlePage);
                furtherHeaderFiller = new FillPagedFrame(p.getFurtherPagesHeader().getTemplate(), val, this, true);
            } else
                furtherHeaderFiller = firstHeaderFiller;
        }
        if (p.getFirstPageFooter() != null && p.getFirstPageFooter().isVisible(runEnv, val)) {
            firstFooterFiller = new FillPagedFrame(p.getFirstPageFooter().getTemplate(), val, this, true);
        }
        if (p.getMiddlePagesFooter() != null) {
            if (p.getMiddlePagesFooter() != p.getFirstPageFooter() && p.getMiddlePagesFooter().isVisible(runEnv, val)) {
                middleFooterFiller = new FillPagedFrame(p.getMiddlePagesFooter().getTemplate(), val, this, true);
            } else
                middleFooterFiller = firstFooterFiller;
        }
        if (p.getLastPageFooter() != null) {
            if (p.getLastPageFooter() != p.getMiddlePagesFooter() && p.getLastPageFooter().isVisible(runEnv, val)) {
                lastFooterFiller = new FillPagedFrame(p.getLastPageFooter().getTemplate(), val, this, true);
            } else
                lastFooterFiller = middleFooterFiller;
        }
        bodies.clear();
        for (PageLevelBox frame : frames) {
            if (frame.isVisible(runEnv, val)) {
                FillPagedFrame bf = new FillPagedFrame(frame.getTemplate(), val, this, false);
                searchTotalCells(frame.getTemplate());
                bodies.add(bf);
            }
        }
        if (bodies.size() > 0)
            currBodyFiller = (FillPagedFrame) bodies.get(0);
        isVirgin = false;
    }

    private void searchReportForTotalCells() {
        if (isNumberingVirgin) {
            buildAllPages = false;
            hasPagesTotal = false;
            for (Template r = template; r != null && (r == template || !r.isRestartNumbering()); r = r.getNextSection()) {
                PageMaster p = r.getPage();
                if (p.getFirstPageHeader() != null) {
                    searchTotalCells(p.getFirstPageHeader().getTemplate());
                }
                if (p.getFurtherPagesHeader() != null && p.getFirstPageHeader() != p.getFurtherPagesHeader()) {
                    searchTotalCells(p.getFurtherPagesHeader().getTemplate());
                }
                if (p.getFirstPageFooter() != null) {
                    searchTotalCells(p.getFirstPageFooter().getTemplate());
                }
                if (p.getMiddlePagesFooter() != null && p.getFirstPageFooter() != p.getMiddlePagesFooter()) {
                    searchTotalCells(p.getMiddlePagesFooter().getTemplate());
                }
                if (p.getLastPageFooter() != null && p.getFirstPageFooter() != p.getLastPageFooter() && p.getMiddlePagesFooter() != p.getLastPageFooter()) {
                    searchTotalCells(p.getLastPageFooter().getTemplate());
                }
                for (int i = 0; i < p.getFrames().size(); i++) {
                    PageLevelBox pageLevelBox = p.getFrames().get(i);
                    searchTotalCells(pageLevelBox.getTemplate());
                }
            }
            displayPageCount = -1;
        }
        isNumberingVirgin = false;
    }

    private void searchTotalCells(TemplateModel template) {
        if (hasPagesTotal)
            return;
        for (int i = 0; i < template.getRowCount(); i++) {
            ArrayList<CellDef> row = template.getRow(i);
            for (CellDef o : row) {
                if (o instanceof PagestotalCell) {
                    buildAllPages = true;
                    hasPagesTotal = true;
                    if (pages == null)
                        pages = new ArrayList<>();
                    return;
                } else if (o instanceof DeferredTotalPagesCell) {
                    if (((DeferredTotalPagesCell) o).hasTotalPages()) {
                        buildAllPages = true;
                        hasPagesTotal = true;
                        if (pages == null)
                            pages = new ArrayList<>();
                        return;
                    }
                }
            }
        }
    }

    private void fillModel(int pageNo) throws JoriaException {
        do {
            if (!generator.fillModel(pageNo))
                return;
            if (endOfLastSectionSeen)
                break;
        }
        while (atPage < pageNo);
    }

    private boolean fillFrames() throws JoriaDataException {
        final PageMaster page = template.getPage();
        PageStyle cascadedPageStyle = page.getCascadedPageStyle();
        DisplayList dest = new DisplayList(cascadedPageStyle.getPageWidth(), cascadedPageStyle.getPageHeight(), template);        // Page Background
        if (cascadedPageStyle.getBackground().getAlpha() != 0 || cascadedPageStyle.getBackgroundImageName() != null) {
            GraphElContent background;
            if (cascadedPageStyle.getBackgroundImageName() != null) {
                ImageIcon image = cascadedPageStyle.getBackgroundImage(env.getLocale());
                float scale = Float.NaN;
                float width = image.getIconWidth();
                float heigth = image.getIconHeight();
                float outWith = cascadedPageStyle.getPageWidth();

                SizeLimit limit = cascadedPageStyle.getSizeLimit();
                final boolean mustScaleDown = limit == SizeLimit.AtMost && width > outWith;
                final boolean mustScaleUp = limit == SizeLimit.AtLeast && width < outWith;
                final boolean mustScale = limit == SizeLimit.Fix && width != outWith;
                if (outWith != 0 && (mustScaleDown || mustScaleUp || mustScale)) {
                    scale = outWith / width;
                    width = outWith;
                    heigth = heigth * scale;
                }
                if (cascadedPageStyle.getBackgroundImageTargetWidth() != null && !cascadedPageStyle.getBackgroundImageTargetWidth().isExpandable()) {
                    scale = cascadedPageStyle.getBackgroundImageTargetWidth().getVal() / width;
                    width = cascadedPageStyle.getBackgroundImageTargetWidth().getVal();
                    heigth = heigth * scale;
                }
                background = new GraphElPicture(cascadedPageStyle.getBackgroundImageName(), image, cascadedPageStyle.getBackground(), scale, null, false, null);
                background.xContent = cascadedPageStyle.getBackgroundImageX().getValInPoints();
                background.yContent = cascadedPageStyle.getBackgroundImageY().getValInPoints();
                background.wContent = Math.min(width, cascadedPageStyle.getPageWidth() - background.xContent);
                background.hContent = Math.min(heigth, cascadedPageStyle.getPageHeight() - background.yContent);
                background.xEnvelope = background.xContent;
                background.yEnvelope = background.yContent;
                background.wEnvelope = background.wContent;
                background.hEnvelope = background.hContent;
            } else {
                background = new GraphicElementRect(cascadedPageStyle.getBackground(), null, null);
            }
            background.x = 0;
            background.y = 0;
            background.width = cascadedPageStyle.getPageWidth();
            background.height = cascadedPageStyle.getPageHeight();
            dest.add(background);
        }
        framesOnPage = new ArrayList<>();
        destBodyStart = -1;        // header
        Trace.logDebug(Trace.fill, "Filling header of page " + atPage);
        float bodyPos = cascadedPageStyle.getTopMargin().getValInPoints();
        final float footerHeight;
        if (isFirstPage()) {
            bodyPos = doFirstHeaderFiller(bodyPos, dest);
            DisplayList firstFooterList = new DisplayList();
            footerHeight = buildFooterFrame(firstFooterFiller, spaceInMargin, TemplateBoxInterface.firstPageFooter, firstFooterList);
        } else {
            bodyPos = doFurtherHeaderFiller(bodyPos, dest);
            DisplayList middleFooterList = new DisplayList();
            float middleFooterHeight = buildFooterFrame(middleFooterFiller, spaceInMargin, TemplateBoxInterface.middlePagesFooter, middleFooterList);
            DisplayList lastFooterList = new DisplayList();
            float lastFooterHeight = buildFooterFrame(lastFooterFiller, spaceInMargin, TemplateBoxInterface.lastPageFooter, lastFooterList);
            footerHeight = Math.max(middleFooterHeight, lastFooterHeight);
        }
        Trace.logDebug(Trace.run, "Filling bodies of page " + atPage + " pos " + bodyPos);
        destBodyStart = dest.size();
        boolean firstBodyOnPage = true;
        boolean spaceLeft = true;
        nextFrame:
        while (spaceLeft && currBodyFiller != null) {
            FrameStyle fs = currBodyFiller.getTemplate().getFrame().getCascadedFrameStyle();
            if (fs.getOnNewPage() && !firstBodyOnPage) {
                spaceLeft = false;
                break;
            }
            Rectangle2D.Float frameRect = checkAbsPos(fs, bodyPos);
            if (frameRect == null) {
                spaceLeft = false;
                break;
            }
            float remainingHeight = currBodyFiller.hFill(spaceInMargin - frameRect.y - footerHeight, frameRect.y, dest, firstBodyOnPage);
            Trace.logDebug(Trace.run, "Filled body " + bodyCount + " pos " + bodyPos);
            final LastRowState endState = currBodyFiller.getEndState();
            if (endState == LastRowState.endOfData) {
                firstBodyOnPage = false;
                if (bodyCount >= bodies.size())
                    break;
                PageStyle ps = page.getCascadedPageStyle();
                if (fs.getXPos() != null && ps.getBodyWidth() + ps.getLeftMargin().getValInPoints() < fs.getXPos().getVal()) {                    // frame right of content
                    if (fs.getHeight().isExpandable())
                        frameRect.height = spaceInMargin - footerHeight - remainingHeight - frameRect.y;
                    else
                        frameRect.height = fs.getHeight().getVal();
                } else if (fs.getXPos() != null && !fs.getWidth().isExpandable() && fs.getXPos().getVal() + fs.getWidth().getVal() < ps.getLeftMargin().getValInPoints()) {                    // frame left of content;
                    if (fs.getHeight().isExpandable())
                        frameRect.height = spaceInMargin - footerHeight - remainingHeight - frameRect.y;
                    else
                        frameRect.height = fs.getHeight().getVal();
                } else {
                    if (fs.getYPos() != null)
                        bodyPos = fs.getYPos().getVal();
                    if (fs.getHeight().isExpandable()) {
                        bodyPos = spaceInMargin - footerHeight - remainingHeight;
                    } else
                        bodyPos += fs.getHeight().getVal();// falsch hier fehlt der Anfang
                    frameRect.height = bodyPos - frameRect.y;
                }
                framesOnPage.add(frameRect);
                for (; ; ) {
                    currBodyFiller = (FillPagedFrame) bodies.get(bodyCount++);
                    if (currBodyFiller.getTemplate().getFrame().getPageLevelParent().isVisibleDeferred(env))
                        continue nextFrame;
                    if (bodyCount < bodies.size())
                        break nextFrame;
                }
            } else if (endState == LastRowState.donePartial || endState == LastRowState.doneRedo || endState == LastRowState.doneComplete) {
                firstBodyOnPage = false;
                if ((!currBodyFiller.template.getFrame().getCascadedFrameStyle().getBreakable() || !currBodyFiller.template.getFrame().getCascadedFrameStyle().getHeight().isExpandable())// check if frame must be kept together
                        && destBodyStart != currBodyFiller.getDestStart())// only reset if we gain space because we there is output above us, which only happens on first keep
                    currBodyFiller.resetToBeginning();
                spaceLeft = false;
            } else
                throw new JoriaAssertionError("Unhandled end state after filling a body frame: " + endState);
        }
        Trace.logDebug(Trace.run, "Filling bodies full. ");
        if (isFirstPage()) {
            DisplayList footerList = new DisplayList();
            buildFooterFrame(firstFooterFiller, spaceInMargin, TemplateBoxInterface.firstPageFooter, footerList);
            dest.add(footerList);
        } else if (!spaceLeft) {
            DisplayList footerList = new DisplayList();
            buildFooterFrame(middleFooterFiller, spaceInMargin, TemplateBoxInterface.middlePagesFooter, footerList);
            dest.add(footerList);
        } else {
            DisplayList footerList = new DisplayList();
            buildFooterFrame(lastFooterFiller, spaceInMargin, TemplateBoxInterface.lastPageFooter, footerList);
            dest.add(footerList);
        }
        completePage(dest);
        return spaceLeft;
    }

    private Rectangle2D.Float checkAbsPos(FrameStyle fs, float bodyPos) {
        final float frameX;
        final PageStyle ps = template.getPage().getCascadedPageStyle();
        if (fs.getXPos() == null || fs.getXPos().isExpandable())
            frameX = ps.getLeftMargin().getValInPoints();
        else
            frameX = fs.getXPos().getVal();
        final float frameTopY;
        if (fs.getYPos() != null && !fs.getYPos().isExpandable()) {
            frameTopY = fs.getYPos().getVal();
        } else {
            frameTopY = bodyPos + 0.0001f;
        }
        final float frameWidth;
        if (fs.getWidth() != null && !fs.getWidth().isExpandable())
            frameWidth = fs.getWidth().getVal();
        else
            frameWidth = ps.getPageWidth() - frameX - ps.getRightMargin().getValInPoints();
        final float frameHeight;
        if (fs.getHeight() != null && !fs.getHeight().isExpandable())
            frameHeight = fs.getHeight().getVal();
        else
            frameHeight = ps.getHeightToBottomMargin() - frameTopY;
        Rectangle2D.Float fr = new Rectangle2D.Float(frameX, frameTopY, frameWidth, frameHeight);
        for (Rectangle2D.Float pr : framesOnPage) {
            if (fr.intersects(pr))
                return null;
        }
        return fr;
    }

    private float doFirstHeaderFiller(float bodyPos, DisplayList dest) throws JoriaDataException {
        float bodySpace = spaceInMargin - bodyPos;// - template.getPage().getFooterHeight(PageLevelBox.firstPage);
        if (firstHeaderFiller != null) {
            firstHeaderFiller.reset();
            firstHeaderFiller.reinit();
            float remainingHeight = firstHeaderFiller.hFill(bodySpace, bodyPos, dest, true);
            if (firstHeaderFiller.getEndState() != LastRowState.endOfData && firstHeaderFiller.getEndState() != LastRowState.doneComplete)
                throw new JoriaUserError(Res.str("Header_is_larger_than_page_please_change_your_layout"));
            FrameStyle fs = firstHeaderFiller.getTemplate().getFrame().getCascadedFrameStyle();
            if (fs.getHeight().isExpandable())
                bodyPos += bodySpace - remainingHeight;
            else
                bodyPos += fs.getHeight().getVal();
        }
        return bodyPos;
    }

    private float doFurtherHeaderFiller(float bodyPos, DisplayList dest) throws JoriaDataException {
        if (furtherHeaderFiller == null)
            return bodyPos;
        float bodySpace = spaceInMargin - bodyPos;// - template.getPage().getFooterHeight((PageLevelBox.middlePage));
        furtherHeaderFiller.reset();
        furtherHeaderFiller.reinit();
        float remainingHeight = furtherHeaderFiller.hFill(bodySpace, bodyPos, dest, true);
        if (furtherHeaderFiller.getEndState() != LastRowState.endOfData && firstHeaderFiller.getEndState() != LastRowState.doneComplete)
            throw new JoriaUserError(Res.str("Header_is_larger_than_page_please_change_your_layout"));
        FrameStyle hfs = furtherHeaderFiller.getTemplate().getFrame().getCascadedFrameStyle();
        if (hfs.getHeight().isExpandable())
            return bodyPos + bodySpace - remainingHeight;
        else
            return bodyPos + hfs.getHeight().getVal();
    }

    private void completePage(DisplayList dest) {
        resetTotals(AggregateDef.page);
        painter.setPageContents(dest);        //dest.dump(); // dump display list for debugging purposes
        if (pages != null) {
            pages.add(dest);
            if (pages != getPagesForPageNumbers())
                getPagesForPageNumbers().add(dest);
        }
        pageCnt++;
        lastPage = dest;
    }

    private boolean isFirstPage() {
        return generator.isFirstPage();
    }

    private float buildFooterFrame(FillPagedFrame b, float remainingHeight, int type, DisplayList pma) throws JoriaDataException {
        if (b == null)
            return 0;
        b.reset();
        b.reinit();
        Trace.logDebug(Trace.run, "Filling " + TemplateBoxInterface.boxNames[type] + " of page " + atPage);
        float usedHeight = remainingHeight - b.hFill(remainingHeight, 0, pma, false);
        final float offsety = remainingHeight - usedHeight;
        pma.translate(0, offsety);
        return usedHeight;
    }

    public boolean nextPage() throws JoriaException {
        int wasPage = atPage;
        return wasPage != loadPage(atPage + 1);
    }

    public void printPage(JoriaPrinter graphics) throws IOException {
        painter.print(graphics);
    }

    public boolean prevPage() throws JoriaException {
        int wasPage = atPage;
        return atPage != 0 && wasPage != loadPage(atPage - 1);
    }

    private int printPage(JoriaPrinter graphics, int pageNo) throws JoriaException {
        int ret = loadPage(pageNo);
        if (ret != pageNo) {
            return Printable.NO_SUCH_PAGE;
        }
        try {
            graphics.startPage();
            painter.print(graphics);
            graphics.endPage();
        } catch (IOException e) {
            throw new JoriaDataExceptionWrapped(e.getMessage(), e);
        }
        return Printable.PAGE_EXISTS;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isLast(int pageno) {
        return endOfLastSectionSeen && pageno >= pageCnt - 1;
    }

    public boolean isOk() {
        boolean ret = false;
        if (currBodyFiller != null)// error in initialisation
            ret = currBodyFiller.getEndState() != LastRowState.errorOccured;
        if (firstHeaderFiller != null)
            ret &= firstHeaderFiller.getEndState() != LastRowState.errorOccured;
        if (furtherHeaderFiller != null)
            ret &= furtherHeaderFiller.getEndState() != LastRowState.errorOccured;
        if (firstFooterFiller != null)
            ret &= firstFooterFiller.getEndState() != LastRowState.errorOccured;
        if (middleFooterFiller != null)
            ret &= middleFooterFiller.getEndState() != LastRowState.errorOccured;
        if (lastFooterFiller != null)
            ret &= lastFooterFiller.getEndState() != LastRowState.errorOccured;
        return ret;
    }

    @SuppressWarnings("UnusedDeclaration")
    public FillPagedFrame getBodyFiller() {
        return currBodyFiller;
    }

    public void endReport() {
        if (env.getGraphics2D() != null) {
            env.getGraphics2D().dispose();
            env.setGraphics2D(null);
        }
        try {
            Env.instance().runInBackground(() -> env.endReport(template), env.locale);
        } catch (JoriaDataException e) {
            Env.instance().handle(e);
        }
    }

    public void setScale(float scale) {
        painter.setScale(scale);
    }

    @SuppressWarnings("UnusedDeclaration")
    public PageLayouter getCurrentLayouter() {
        return null;
    }

    public int getDisplayPageNo() {
        if (setPageNo != -1)
            return setPageNo;
        return displayPageCount;
    }

    public static PageEnv2 makePageEnv(boolean holdPages, RunEnvImpl env) {
        return new PageEnv2(holdPages, env);
    }

    public Dimension getPreferredSizeUnscaled() {
        return painter.getPreferredSizeUnscaled();
    }

    private void exportToEmailInternal(EMailConfigurationData config) throws JoriaException {
        long t0 = System.currentTimeMillis();
        setSingleMasterMode();
        Template def = RunEnvImpl.cri(getRunEnv().template);
        Trace.logDebug(Trace.action, "Email export of " + def.getName());
        PageStyle pa = def.getPage().getCascadedPageStyle();
        PageFormat paf = pa.getPageFormat();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(50 * 1024);
        int totalPageNos = 0;
        int mails = 0;
        String server = Settings.get("MailServer");//trdone
        String sender = config.getSender();
        String fileName = config.getReportFileName();
        Graphics2D save = getRunEnv().getGraphics2D();
        Graphics2D graphics2D = null;
        try {
            graphics2D = (Graphics2D) Env.instance().getDefaultGraphics2D().create();
            graphics2D.scale(8.0, 8.0);
            getRunEnv().setGraphics2D(graphics2D);
            do {
                baos.reset();
                DBData obj = currentObject();
                String recepient = config.getAddress(obj, env);
                String subject = config.getSubject(obj, env);
                String letter = config.getLetter(obj, env);
                PDFPrinter printer = new PDFPrinter(baos, paf, getRunEnv().startTemplate.getName(), getStartingReport(), env.getGraphics2D(), Env.instance().getCurrentUserName());
                int pageNo = 0;
                int res = Printable.PAGE_EXISTS;
                while (res == Printable.PAGE_EXISTS) {
                    res = printPage(printer, pageNo++);                    //Trace.logGC("Pdf page " + pageNo); //trdone
                }
                printer.end(false);
                pageNo--;
                baos.close();
                Send s = new Send(server, recepient, sender, subject, letter);
                s.attachContent(baos.toByteArray(), fileName, "application/pdf");//trdone
                s.send();
                System.out.println("pdf size = " + baos.size());//trdone
                totalPageNos += pageNo;
                mails++;
                atPage = -1;
                pageCnt = 0;
            }
            while (nextMaster());
        } catch (Exception pex) {
            throw new JoriaDataExceptionWrapped("An error occured when sending a email pdf file. See details", pex);
        } finally {
            getRunEnv().setGraphics2D(save);
            if (graphics2D != null)
                graphics2D.dispose();
            t0 = System.currentTimeMillis() - t0;
            if (t0 > 0)
                System.out.println("emails = " + mails + " pdf pages = " + totalPageNos + " t= " + t0 + " ppm= " + totalPageNos * 60000 / t0);//trdone
            else
                System.out.println("emails = " + mails + " pdf pages = " + totalPageNos + " t= " + t0 + " ppm= too fast");//trdone
            env.endReport(env.startTemplate);
        }
    }

    public void exportToEmail(final EMailConfigurationData config) throws JoriaException {
        export(() -> copy().exportToEmailInternal(config));
    }

    public Dimension getDimension() {
        return painter.getDimension();
    }

    private interface ExportRunnable {
        void run() throws JoriaException;
    }

    private void export(final ExportRunnable r) throws JoriaException {
        final ObjectHolder<JoriaException> exception = new ObjectHolder<>();
        Env.instance().runInBackground(() -> {
            try {
                r.run();
            } catch (JoriaException e) {
                Trace.log(e);
                exception.value = e;
            }
        }, env.getLocale());
        if (exception.value != null)
            throw exception.value;
    }

    public void exportToRtf(final OutputStream out) throws JoriaException {
        export(() -> {
            long t0 = System.currentTimeMillis();
            env.resetCounters();
            Template def = RunEnvImpl.cri(getRunEnv().startTemplate);
            Trace.logDebug(Trace.action, "rtf export of " + def.getName());
            final ArrayList<CellStyle> cellStyles = new ArrayList<>(1000);
            final HashSet<String> additionalFonts = new HashSet<>(1000);
            final HashSet<Color> additionalColors = new HashSet<>(1000);
            final ArrayList<FrameStyle> frameStyles = new ArrayList<>(20);
            final ArrayList<TableStyle> tableStyles = new ArrayList<>(40);
            final ArrayList<JoriaSimpleBorder> cellBorders = new ArrayList<>(4000);
            Template current = def;
            boolean multiSection = true;
            while (current != null) {
                if (current.getReportType() == Template.masterDetailReport)
                    multiSection = true;
                current.visitCells((cd, r, c) -> {
                    if (cd != null)
                        cellStyles.add(cd.getCascadedStyle());
                    if (cd instanceof StyledTextCellDef) {
                        StyledParagraphList list = ((StyledTextCellDef) cd).getParagraphs();
                        for (int i = 0; i < list.length(); i++) {
                            StyledParagraph t = list.get(i);
                            t.getUsedFontFamilies(additionalFonts);
                            t.getUsedColors(additionalColors);
                        }
                    }
                    return true;
                });
                current.visitFrames(frame -> {
                    frameStyles.add(frame.getCascadedFrameStyle());
                    cellStyles.add(frame.getCascadedCellStyle());
                    TemplateModel model = frame.getTemplate();
                    for (int r = 0; r < model.getRowCount(); r++) {
                        for (int c = 0; c < model.getColCount(); c++) {
                            JoriaSimpleBorder border = model.getBorderAt(r, c, true, true);
                            if (!JoriaSimpleBorder.isNull(border))
                                cellBorders.add(border);
                            border = model.getBorderAt(r, c, false, true);
                            if (!JoriaSimpleBorder.isNull(border))
                                cellBorders.add(border);
                            if (r == model.getRowCount() - 1) {
                                border = model.getBorderAt(r, c, true, false);
                                if (!JoriaSimpleBorder.isNull(border))
                                    cellBorders.add(border);
                            }
                            if (c == model.getColCount() - 1) {
                                border = model.getBorderAt(r, c, false, false);
                                if (!JoriaSimpleBorder.isNull(border))
                                    cellBorders.add(border);
                            }
                        }
                    }
                    return true;
                });
                current.visitRepeaters(r -> {
                    tableStyles.add(r.getCascadedTableStyle());
                    return true;
                });
                current = current.getNextSection();
                if (current != null)
                    multiSection = true;
            }
            Graphics2D savedGraphics = env.getGraphics2D();
            try {
                env.setGraphics2D(Env.instance().getDefaultGraphics2D());
//noinspection ConstantConditions
                RtfOutput output = new RtfOutput(env, cellStyles, frameStyles, tableStyles, cellBorders, additionalFonts, additionalColors, out, multiSection);
                output.doOutput();
            } catch (UnsupportedEncodingException e) {
                throw new JoriaAssertionError("Cp1252 encoding unsupported");
            } finally {
                env.setGraphics2D(savedGraphics);
            }
            t0 = System.currentTimeMillis() - t0;
            System.out.println("rtf export = " + t0);//trdone
        });
    }

    public void exportToPdf(final OutputStream out) throws JoriaException {
        if (!isVirgin) {
            copy().exportToPdf(out);
            return;
        }
        export(() -> {
            long t0 = System.currentTimeMillis();
            env.resetCounters();
            Template def = RunEnvImpl.cri(getRunEnv().startTemplate);

            Trace.log(Trace.run, DateFormat.getDateInstance().format(new Date()) + "Pdf export of " + def.getName());
            PageStyle pa = def.getPage().getCascadedPageStyle();
            PageFormat paf = pa.getPageFormat();
            int oldPageNo = atPage;
            getRunEnv().startReport(getRunEnv().template);
            Graphics2D save = getRunEnv().getGraphics2D();
            Graphics2D graphics2D = null;
            try {
                graphics2D = (Graphics2D) Env.instance().getDefaultGraphics2D().create();
                graphics2D.scale(8.0, 8.0);
                getRunEnv().setGraphics2D(graphics2D);
                PDFPrinter printer = new PDFPrinter(out, paf, getRunEnv().startTemplate.getName(), getStartingReport(), env.getGraphics2D(), Env.instance().getCurrentUserName());
                int pageNo = 0;
                int res = Printable.PAGE_EXISTS;
                while (res == Printable.PAGE_EXISTS) {
                    res = printPage(printer, pageNo++);
                    //Trace.logGC("Pdf page " + pageNo); //trdone
                }
                printer.end(false);
                t0 = System.currentTimeMillis() - t0;
                pageNo--;
                if (t0 > 0)
                    System.out.println("pdf pages = " + pageNo + " t= " + t0 + " ppm= " + pageNo * 60000 / t0);//trdone
                else
                    System.out.println("pdf pages = " + pageNo + " t= " + t0 + " ppm= too fast");//trdone
                if (oldPageNo >= 0)
                    loadPage(oldPageNo);
            } catch (IOException pex) {
                throw new JoriaUserException(Res.str("An_error_occured_when_writing_a_pdf_file_See_details"), pex);
            } finally {
                getRunEnv().setGraphics2D(save);
                if (graphics2D != null)
                    graphics2D.dispose();
            }
        });
    }

    public void exportToXml(final Writer out) throws JoriaException {
        export(() -> {
            Template def = RunEnvImpl.cri(getRunEnv().template);
            Trace.logDebug(Trace.action, "Xml export of " + def.getName());
            XmlOutput2.writeXml(out, getRunEnv());
        });
    }

    public void printToPrinter(boolean withDialog) {
        if (!isVirgin) {
            copy().printToPrinter(withDialog);
            return;
        }
        try {
            Template def = RunEnvImpl.cri(getRunEnv().startTemplate);
            doPrint(def, null, withDialog);
        } catch (JoriaException e) {
            Env.instance().handle(e);
        }
    }

    public void doPrint(final Template def, PrintService ps, boolean withDialog) throws JoriaException {
        Graphics2D saved = env.getGraphics2D();
        int oldPageNo = atPage;
        PrinterJob pj = null;
        final PrintRequestAttributeSet set = new HashPrintRequestAttributeSet();
        if (withDialog) {
            pj = PrinterJob.getPrinterJob();
            if (ps != null) {
                try {
                    pj.setPrintService(ps);
                } catch (PrinterException e) {
                    Env.instance().handle(e);
                }
            }
            set.add(DialogTypeSelection.NATIVE);
            boolean doIt = pj.printDialog(set);
            if (!doIt)
                return;
            ps = pj.getPrintService();
        } else if (ps == null && PrintServiceLookup.lookupDefaultPrintService() == null) {
            String msg = Res.str("there_is_no_default_printer", env.getLocale());
            throw new JoriaDataException(msg);
        }
        if (ps == null)
            ps = PrintServiceLookup.lookupDefaultPrintService();
        final PrintService fps = ps;
        final PrinterJob fpj = pj;
        PaperSize largest = def.getPage().getCascadedPageStyle().getPaperSize();
        for (Template r = def; r != null; r = r.getNextSection()) {
            largest = largest.theLarger(r.getPage().getCascadedPageStyle().getPaperSize());
        }
        MediaSizeName media = null;
        if (largest.getHeight().unit == LengthUnit.MM) {
            media = MediaSize.findMedia(largest.getWidth().inUnit(LengthUnit.MM), largest.getHeight().inUnit(LengthUnit.MM), Size2DSyntax.MM);
        } else if (largest.getHeight().unit == LengthUnit.INCH) {
            media = MediaSize.findMedia(largest.getWidth().inUnit(LengthUnit.INCH), largest.getHeight().inUnit(LengthUnit.INCH), Size2DSyntax.INCH);
        }
        final boolean scaleToA4 = media != null && !ps.isAttributeValueSupported(media, DocFlavor.SERVICE_FORMATTED.PAGEABLE, set) && media == MediaSizeName.ISO_A3 && ps.isAttributeValueSupported(MediaSizeName.ISO_A4, DocFlavor.SERVICE_FORMATTED.PAGEABLE, set);
        try {
            Env.instance().runInBackground(() -> {
                try {
                    Trace.logDebug(Trace.action, "Print of " + def.getName());
                    PageStyle pa = def.getPage().getCascadedPageStyle();
                    PageFormat paf = pa.getPageFormatUnrotated(scaleToA4);
                    double scale = pa.getScale(scaleToA4);
                    Graphics2DPrinter printer = new Graphics2DPrinter();
                    PageEnvPrintable printable = new PageEnvPrintable(printer, scale);
                    PageEnvPageable pageable = null;
                    long t0 = System.currentTimeMillis();
                    if (def.getFirstPageMedia() != def.getFurtherPageMedia()) {
                        pageable = new PageEnvPageable(printable, scaleToA4);
                        pageable.firstPageOnly = true;
                        Media m = (Media) set.get(Media.class);
                        if (def.getFirstPageMedia() != null)
                            set.add(def.getFirstPageMedia());
                        doOnePrintJob(pageable, printable, paf, fps, def, set, fpj);
                        if (def.getFurtherPageMedia() != null)
                            set.add(def.getFurtherPageMedia());
                        else
                            set.add(m);
                        pageable.firstPageOnly = false;
                        pageable.furtherPagesOnly = true;
                        doOnePrintJob(pageable, printable, paf, fps, def, set, fpj);
                    } else if (def.getNextSection() != null) {
                        pageable = new PageEnvPageable(printable, scaleToA4);
                        doOnePrintJob(pageable, printable, paf, fps, def, set, fpj);
                    } else
//noinspection ConstantConditions
                        doOnePrintJob(pageable, printable, paf, fps, def, set, fpj);
                    t0 = System.currentTimeMillis() - t0;
                    System.out.println("printed pages = " + atPage + " t= " + t0 + " ppm= " + atPage * 60000 / t0);//trdone
                } catch (PrinterException e) {
                    Env.instance().handle(e);
                }
            }, env.getLocale());
        } finally {
            env.setGraphics2D(saved);
            if (oldPageNo >= 0)
                loadPage(oldPageNo);
        }
    }

    private void doOnePrintJob(Pageable pageable, Printable printable, PageFormat paf, PrintService fps, Template def, PrintRequestAttributeSet set, PrinterJob pj) throws PrinterException {
        if (pj == null)
            pj = PrinterJob.getPrinterJob();
        if (fps != null)
            pj.setPrintService(fps);
        if (pageable == null) {
            pj.setPrintable(printable, paf);
        } else {
            pj.setPageable(pageable);
        }
        pj.setJobName(def.getName() + " - " + Res.productName());
        pj.print(set);
    }

    public int getDestBodyStart() {
        return destBodyStart;
    }

    public GraphElContent getGraphelAt(Point point) {
        return painter.getDrillDownAt(point);
    }

    public boolean isAtLastTotalPage() {
        return endOfLastSectionSeen && atPage >= pageCnt - 1;
    }

    public String getDisplayPageNumberText() {
        if (pages != null)
            return pages.get(atPage).pageNumberText;
        else
            return Integer.toString(atPage + 1);
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getDisplayPageNumber() {
        return atPage;
    }

    private ArrayList<DisplayList> getPagesForPageNumbers() {
        if (pagesForNumbering == null)
            pagesForNumbering = new ArrayList<>();
        return pagesForNumbering;
    }

    public int getTotalPagesNumber() {
        return setTotalsNumber;
    }

    private void setSingleMasterMode() {
        singleMasterMode = true;
    }

    private DBData currentObject() {
        return generator.currentObject();
    }

    private boolean nextMaster() throws JoriaException {
        if (generator.nextMaster())
            return true;
        if (template.getNextSection() != null) {
            startNextSection();
            if (template.getReportType() == Template.masterDetailReport)
                generator = new MaDetPageGenerator();
            else
                generator = new SimplePageGenerator();
            bodyCount = 1;
            if (pages != null) {
                for (DisplayList displayList : pages) {
                    if (template == displayList.currentTemplate)
                        throw new JoriaAssertionError("Template " + template.getName() + " used multiple times in definition of template " + getStartingReport().getName());
                }
            }
            buildFillers(generator.currentObject(), env);
            endOfLastSectionSeen = false;
            return true;
        }
        return false;
    }

    private void startNextSection() throws JoriaException {
        isVirgin = true;
        env.nextSection();
        template = template.getNextSection();
        if (template.isRestartNumbering())
            isNumberingVirgin = true;
    }

    protected PageEnv2 copy() {
        return new PageEnv2(holdPages, env);
    }

    private class PageEnvPageable implements Pageable {
        private boolean firstPageOnly;
        private boolean furtherPagesOnly;
        private final PageEnvPrintable printable;
        private final boolean scaleToA4;

        PageEnvPageable(PageEnvPrintable printable, boolean scaleToA4) {
            this.printable = printable;
            this.scaleToA4 = scaleToA4;
        }

        public int getNumberOfPages() {
            if (firstPageOnly)
                return 1;
            else
                return Pageable.UNKNOWN_NUMBER_OF_PAGES;
        }

        public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
            Template def = getRunEnv().template;
            PageStyle pa = def.getPage().getCascadedPageStyle();
            PageFormat paf = pa.getPageFormatUnrotated(scaleToA4);
            printable.scale = pa.getScale(scaleToA4);
            return paf;
        }

        public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
            if (furtherPagesOnly)
                printable.furtherPagesOnly = true;
            return printable;
        }
    }

    private class PageEnvPrintable implements Printable {
        private final Graphics2DPrinter printer;
        private boolean furtherPagesOnly;
        private double scale;

        PageEnvPrintable(Graphics2DPrinter printer, double scale) {
            this.scale = scale;
            this.printer = printer;
        }

        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            if (furtherPagesOnly)
                pageIndex++;
            try {
                Graphics2D g2d = (Graphics2D) graphics;
                if (g2d.getTransform().getScaleX() != 1 / 01.2) {
                    FontRenderContext frc = g2d.getFontRenderContext();
                    AffineTransform xform = new AffineTransform();
                    xform.scale(1 / 0.12, 1 / 0.12);
                    g2d = new LayeredGraphics2D(g2d, new FontRenderContext(xform, frc.isAntiAliased(), frc.usesFractionalMetrics()));
                }
                env.setGraphics2D(g2d);
                if (scale != 1)
                    g2d.scale(scale, scale);
                printer.setGraphics2D(g2d);
                return printPage(printer, pageIndex);
            } catch (UserAbortError e) {
                return Printable.NO_SUCH_PAGE;
            } catch (Throwable pex) {
                Env.instance().handle(pex);
                return Printable.NO_SUCH_PAGE;
            }
        }
    }

    private void checkNextSection() throws JoriaException {
        if (template.getNextSection() != null && !singleMasterMode) {
            if (template.getNextSection().isRestartNumbering()) {
                buildTotalPagesAndToc();
                displayPageCount = -1;
            }
            startNextSection();
            if (template.getReportType() == Template.masterDetailReport)
                generator = new MaDetPageGenerator();
            else
                generator = new SimplePageGenerator();
            bodyCount = 1;
            if (pages != null) {
                for (DisplayList displayList : pages) {
                    if (template == displayList.currentTemplate)
                        throw new JoriaAssertionError("Template " + template.getName() + " used multiple times in definition of template " + getStartingReport().getName());
                }
            }            //buildFillers(generator.currentObject(), env);
            endOfLastSectionSeen = false;
        } else {
            endOfLastSectionSeen = true;
        }
    }

    public int getMasterIndex()// the default is not to use master/detail logic so we are always the first master
    {
        return generator.getMasterIndex();
    }

    private class SimplePageGenerator {
        protected boolean fillModel(int pageNo) throws JoriaException {
            atPage = pageCnt - 1;
            if (isVirgin) {
                try {
                    DBData rootVal = env.loadRootVal();
                    if (rootVal == null)
                        return false;
                    buildFillers(rootVal, env);
                    if (template.getNextSection() != null && template.getNextSection().getRootAccess() != template.getRootAccess())
                        env.freeRootVal();
                    isVirgin = false;
                } catch (Error | RuntimeException e) {
                    Env.instance().handle(e);
                    throw e;
                }
            }
            while (atPage < pageNo) {
                atPage++;
                displayPageCount++;
                if (fillFrames()) {
                    lastPage.pageNumberText = Integer.toString(atPage + 1);
                    checkNextSection();
                    break;
                }
                lastPage.pageNumberText = Integer.toString(atPage + 1);
            }
            return true;
        }

        public DBData currentObject() {
            return env.getRootVal();
        }

        public boolean nextMaster() throws JoriaException {
            return false;
        }

        public int getMasterIndex() {
            return 0;
        }

        public boolean isFirstPage() {
            return atPage == 0;
        }
    }

    private class MaDetPageGenerator extends SimplePageGenerator {
        int masterIndex;
        int detailPage = -1;
        DBData currVal;
        boolean firstDetailPage = true;
        boolean allBodies = true;

        MaDetPageGenerator() {
        }

        protected boolean fillModel(int pageNo) throws JoriaException {
            try {
                atPage = pageCnt - 1;
                DBCollection collVal;// = null;
                if (isVirgin) {
                    DBData root = env.loadRootVal();
                    if (!(root instanceof DBCollection)) {
                        JoriaAccess axs = env.getTemplate().getPage().getData();
                        if (axs instanceof AccessPath) {
                            AccessPath path = (AccessPath) axs;
                            JoriaAccess[] elements = path.getPath();
                            JoriaAccess base = elements[0];
                            JoriaAccess[] rest = new JoriaAccess[elements.length - 1];
                            System.arraycopy(elements, 1, rest, 0, elements.length - 1);
                            JoriaAccess newPath = AccessPath.makePath(rest);
                            while (base instanceof RootGetableAccess) {
                                base = ((RootGetableAccess) base).getRootAccess();
                            }
                            env.putRuntimeParameter(base, root);
                            root = newPath.getValue(root, newPath, env);
                            env.putRuntimeParameter(axs, root);
                            env.rootVal = root;
                        } else
                            throw new JoriaDataException("root value of a master/detail template is not a collection");
                    }
                    collVal = (DBCollection) root;
                    if (collVal == null)
                        return false;
                    isVirgin = false;
                    Trace.log(Trace.run, "Master Collection elements: " + collVal.getLength());
                    collVal.reset();
                    masterIndex = -1;
                    while (collVal.next()) {
                        currVal = collVal.current();
                        if (currVal != null) {
                            masterIndex = 0;
                            break;
                        }
                    }
                    if (masterIndex == -1) {
                        checkNextSection();
                        return true;
                    }
                } else {
                    final DBData rootval1 = env.getRootVal();
                    collVal = (DBCollection) rootval1;
                }
                while (atPage < pageNo) {
                    if (allBodies)
                        buildFillers(currVal, env);
                    atPage++;
                    detailPage++;
                    displayPageCount++;
                    allBodies = fillFrames();
                    firstDetailPage = false;
                    String atPageText = Integer.toString(atPage + 1);
                    String masterIndexText = Integer.toString(masterIndex + 1);
                    String detailPageText = Integer.toString(detailPage + 1);
                    lastPage.pageNumberText = (atPageText + " (" + masterIndexText + "." + detailPageText + ")");
                    if (allBodies) {
                        detailPage = -1;
                        if (template.isRestartNumbering()) {
                            buildTotalPagesAndToc();
                            displayPageCount = -1;
                        }
                        bodyCount = 1;
                        destBodyStart = -1;
                        resetTotals(AggregateDef.grand);
                        if (singleMasterMode) {
                            endOfLastSectionSeen = true;
                        } else {
                            if (masterIndex >= collVal.getLength() && collVal.getLength() != -1) {
                                endOfLastSectionSeen = true;
                            } else {
                                boolean isNull = true;
                                while (collVal.next()) {
                                    currVal = collVal.current();
                                    if (currVal != null) {
                                        isNull = false;
                                        masterIndex++;
                                        break;
                                    }
                                }
                                if (isNull) {
                                    endOfLastSectionSeen = true;
                                    if (template.getNextSection() != null && template.getNextSection().getRootAccess() != template.getRootAccess())
                                        env.freeRootVal();
                                }
                                firstDetailPage = true;
                            }
                        }
                        if (endOfLastSectionSeen) {
                            checkNextSection();
                            break;
                        }
                    }
                }
            } catch (Error | RuntimeException e) {
                Env.instance().handle(e);
                throw e;
            }
            return true;
        }

        public boolean isFirstPage() {
            return firstDetailPage;
        }

        public int getMasterIndex() {
            return masterIndex;
        }

        public DBData currentObject() {
            return currVal;
        }

        public boolean nextMaster() throws JoriaException // TODO multi file pdf
        {
            if (singleMasterMode) {
                DBCollection collVal = (DBCollection) env.getRootVal();
                if (masterIndex >= collVal.getLength() && collVal.getLength() != -1) {
                    endOfLastSectionSeen = true;
                    if (template.getNextSection() != null && template.getNextSection().getRootAccess() != template.getRootAccess())
                        env.freeRootVal();
                    return false;
                } else {
                    boolean isNull = true;
                    while (collVal.next()) {
                        currVal = collVal.current();
                        if (currVal != null) {
                            isNull = false;
                            masterIndex++;
                            break;
                        }
                    }
                    if (isNull) {
                        endOfLastSectionSeen = true;
                        if (template.getNextSection() != null && template.getNextSection().getRootAccess() != template.getRootAccess())
                            env.freeRootVal();
                        return false;
                    }
                    firstDetailPage = true;
                    endOfLastSectionSeen = false;
                    return true;
                }
            } else
                return false;
        }
    }

    public int getAtPage() {
        return atPage;
    }
}

