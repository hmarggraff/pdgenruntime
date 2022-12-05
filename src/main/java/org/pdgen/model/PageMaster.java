// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

/*
 * PageMaster.java
 *
 * Created on October 6, 1999, 6:44 PM
 */

import org.pdgen.data.*;
import org.pdgen.data.view.AggregateDef;
import org.pdgen.data.view.ClassProjection;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.env.Env;
import org.pdgen.env.Res;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.TextCellDef;
import org.pdgen.model.style.*;
import org.pdgen.oql.OQLParseException;

import java.awt.*;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;
import java.util.*;

public class PageMaster implements Serializable {
    private static final long serialVersionUID = 7L;
    protected PageStyle pageStyle = PredefinedStyles.instance().thePageStyleDefaultPageStyle;
    protected float firstHeaderHeight = Float.NaN;// caches the start of the body frames
    protected float furtherHeaderHeight = Float.NaN;// caches the start of the body frames
    protected float firstFooterHeight = Float.NaN;// caches the end of the body frames
    protected float middleFooterHeight = Float.NaN;// caches the start of the body frames
    protected float lastFooterHeight = Float.NaN;// caches the start of the body frames
    private ArrayList<PageLevelBox> frames = new ArrayList<PageLevelBox>();
    protected JoriaAccess myData;
    protected ArrayList<AggregateDef> aggregates = new ArrayList<AggregateDef>();// of AggregateDef
    protected PageLevelBox firstPageHeader;
    protected PageLevelBox furtherPagesHeader;
    protected PageLevelBox firstPageFooter;
    protected PageLevelBox middlePagesFooter;
    protected PageLevelBox lastPageFooter;
    protected transient PageStyle cascadedPageStyle;
    protected transient PageLayouter myLayouter;
    transient ClassProjection formulaView;
    protected long lastEditTime;

    /**
     * @param data the root for the report
     */
    public PageMaster(JoriaAccess data) {
        myData = data;
    }

    public PageMaster duplicate(Map<Object, Object> copiedData) {
        PageMaster pm = new PageMaster(myData);
        //HashMap<CellDef, CellDef> duplicatedCellRelationLinker = new HashMap<CellDef, CellDef>();
        copyTo(pm, copiedData);
        return pm;
    }

    private void copyTo(final PageMaster pm, Map<Object, Object> copiedData) {
        pm.frames = new ArrayList<PageLevelBox>(frames.size());
        pm.firstFooterHeight = firstFooterHeight;
        pm.firstHeaderHeight = firstHeaderHeight;
        pm.middleFooterHeight = middleFooterHeight;
        pm.furtherHeaderHeight = furtherHeaderHeight;
        pm.lastFooterHeight = lastFooterHeight;
        if (firstPageHeader != null)
            pm.firstPageHeader = firstPageHeader.duplicate(pm, copiedData);
        if (furtherPagesHeader == firstPageHeader)
            pm.furtherPagesHeader = pm.firstPageHeader;
        else if (furtherPagesHeader != null)
            pm.furtherPagesHeader = furtherPagesHeader.duplicate(pm, copiedData);
        if (firstPageFooter != null)
            pm.firstPageFooter = firstPageFooter.duplicate(pm, copiedData);
        if (middlePagesFooter == firstPageFooter)
            pm.middlePagesFooter = pm.firstPageFooter;
        else if (middlePagesFooter != null)
            pm.middlePagesFooter = middlePagesFooter.duplicate(pm, copiedData);
        if (lastPageFooter == middlePagesFooter)
            pm.lastPageFooter = pm.middlePagesFooter;
        else if (lastPageFooter != null)
            pm.lastPageFooter = lastPageFooter.duplicate(pm, copiedData);
        for (int i = 0; i < frames.size(); i++) {
            PageLevelBox box = frames.get(i);
            pm.frames.add(box.duplicate(pm, copiedData));
        }
        if (aggregates != null) {
            pm.aggregates = new ArrayList<AggregateDef>(aggregates);// this presumes that aggregatedefs are immutable and need not be copied
        }
        pm.pageStyle = PageStyle.duplicateLocal(pageStyle);
        pm.lastEditTime = System.currentTimeMillis();
    }

    protected void checkLayout() {
        if (Float.isNaN(firstHeaderHeight) || Float.isNaN(furtherHeaderHeight) || Float.isNaN(firstFooterHeight) || Float.isNaN(middleFooterHeight) || Float.isNaN(lastFooterHeight)) {
            if (myLayouter == null)
                myLayouter = new PageLayouter(this, null);// no container required  in this mode
            Graphics2D defaultGraphics2D = Env.instance().getDefaultGraphics2D();
            setHeaderHeight(PageLevelBox.firstPage, myLayouter.calcBoxHeight(firstPageHeader, defaultGraphics2D));
            setHeaderHeight(PageLevelBox.middlePage, myLayouter.calcBoxHeight(furtherPagesHeader, defaultGraphics2D));
            setFooterHeight(PageLevelBox.firstPage, myLayouter.calcBoxHeight(firstPageFooter, defaultGraphics2D));
            setFooterHeight(PageLevelBox.middlePage, myLayouter.calcBoxHeight(middlePagesFooter, defaultGraphics2D));
            setFooterHeight(PageLevelBox.lastPage, myLayouter.calcBoxHeight(lastPageFooter, defaultGraphics2D));
        }
    }

    public float getFooterHeight(int pageRange) {
        checkLayout();
        if (pageRange == PageLevelBox.firstPage)
            return firstFooterHeight;
        else if (pageRange == PageLevelBox.middlePage)
            return Math.max(middleFooterHeight, lastFooterHeight);// the last header may not be higher than the middle header; otherwise the last page can not be determined reliably
        else
            return lastFooterHeight;
    }

    public void setFooterHeight(int pageRange, float footerHeight) {
        if (pageRange == PageLevelBox.firstPage) {
            firstFooterHeight = footerHeight;
            if (middlePagesFooter == firstPageFooter)
                middleFooterHeight = footerHeight;
            if (lastPageFooter == firstPageFooter)
                lastFooterHeight = footerHeight;
        } else if (pageRange == PageLevelBox.middlePage) {
            middleFooterHeight = footerHeight;
            if (middlePagesFooter == firstPageFooter)
                firstFooterHeight = footerHeight;
            if (lastPageFooter == middlePagesFooter)
                lastFooterHeight = footerHeight;
        } else {
            lastFooterHeight = footerHeight;
            if (lastPageFooter == middlePagesFooter)
                middleFooterHeight = footerHeight;
            if (lastPageFooter == firstPageFooter)
                firstFooterHeight = footerHeight;
        }
    }

    public float getHeaderHeight(int pageRange) {
        checkLayout();
        if (pageRange == PageLevelBox.firstPage)
            return firstHeaderHeight;
        else
            return furtherHeaderHeight;
    }

    public void setHeaderHeight(int pageRange, float headerHeight) {
        if (firstPageHeader == furtherPagesHeader)
            firstHeaderHeight = furtherHeaderHeight = headerHeight;
        else if (pageRange == PageLevelBox.firstPage)
            firstHeaderHeight = headerHeight;
        else
            furtherHeaderHeight = headerHeight;
    }

    /**
     * @return page style after the defaults have been merged in
     */
    public PageStyle getCascadedPageStyle() {
        if (cascadedPageStyle == null) {
            if (pageStyle != null) {
                cascadedPageStyle = new PageStyle(pageStyle);
                if (pageStyle != PredefinedStyles.instance().thePageStyleDefaultPageStyle)
                    cascadedPageStyle.mergePageStyle(PredefinedStyles.instance().thePageStyleDefaultPageStyle);
            } else
                cascadedPageStyle = new PageStyle(PredefinedStyles.instance().thePageStyleDefaultPageStyle);
            cascadedPageStyle.mergeDefaults();
        }
        return cascadedPageStyle;
    }

    public JoriaAccess getData() {
        return myData;
    }

    /**
     * @return the style used fot this page or null if the default shall be used
     */
    public PageStyle getPageStyle() {
        return pageStyle;
    }

    public void setData(JoriaAccess newMyData) {
        myData = newMyData;
    }

    /**
     * sets the data scope for the report and all its matching frames.
     *
     * @param newRoot the new root to use
     */
    public void setDataAll(final JoriaAccess newRoot) {
        final PageMaster.AllFramesIterator framesIterator = new AllFramesIterator();
        for (PageLevelBox f : framesIterator) {
            if (myData == f.getRoot())
                f.setRoot(newRoot);
        }
        myData = newRoot;
    }


    public void setPageStyle(PageStyle newPageStyle) {
        Trace.check(newPageStyle);
        pageStyle = newPageStyle;
        cascadedPageStyle = null;
        clearCachedStyles();
        if (myLayouter != null)
            myLayouter.invalidateAll();
    }

    public void clearCachedStyles() {
        for (PageLevelBox frame : frames) {
            frame.clearCachedStyles();
        }
        clearHFStyle(firstPageHeader);
        clearHFStyle(firstPageFooter);
        clearHFStyle(furtherPagesHeader);
        clearHFStyle(middlePagesFooter);
        clearHFStyle(lastPageFooter);
        firstHeaderHeight = furtherHeaderHeight = firstFooterHeight = middleFooterHeight = lastFooterHeight = Float.NaN;
    }

    protected void clearHFStyle(PageLevelBox b) {
        if (b != null)
            b.clearCachedStyles();
    }

    public void setAggregates(ArrayList<AggregateDef> newAggregates) {
        aggregates = newAggregates;
    }

    /**
     * @return the list of aggregates
     */
    public ArrayList<AggregateDef> getAggregates() {
        return aggregates;
    }

    public void namedFrameStyleChanged(FrameStyle f) {
        if (firstPageHeader != null)
            firstPageHeader.namedFrameStyleChanged(f);
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader)
            furtherPagesHeader.namedFrameStyleChanged(f);
        if (firstPageFooter != null)
            firstPageFooter.namedFrameStyleChanged(f);
        if (middlePagesFooter != firstPageFooter && middlePagesFooter != null)
            middlePagesFooter.namedFrameStyleChanged(f);
        if (lastPageFooter == middlePagesFooter && lastPageFooter != null)
            lastPageFooter.namedFrameStyleChanged(f);
        for (PageLevelBox frame : frames) {
            frame.namedFrameStyleChanged(f);
        }
        if (myLayouter != null)
            myLayouter.invalidateAll();
        firstHeaderHeight = furtherHeaderHeight = firstFooterHeight = middleFooterHeight = lastFooterHeight = Float.NaN;
    }

    /*
     * remove aggregates, that have their owning cells deleted
     * as of V4.2 this only applies to page and running totals
     */
    public void removeObsoleteAggregates(Collection<? extends CellDef> cells, Collection<AggregateDef> obsoleteAggregates) {
        for (PageLevelBox frame : frames) {
            frame.getTemplate().clearCellList(cells);
        }
        if (firstPageHeader != null)
            firstPageHeader.getTemplate().clearCellList(cells);
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader)
            furtherPagesHeader.getTemplate().clearCellList(cells);
        if (firstPageFooter != null)
            firstPageFooter.getTemplate().clearCellList(cells);
        if (middlePagesFooter != firstPageFooter && middlePagesFooter != null)
            middlePagesFooter.getTemplate().clearCellList(cells);
        if (lastPageFooter == middlePagesFooter && lastPageFooter != null)
            lastPageFooter.getTemplate().clearCellList(cells);
        aggregates.removeAll(obsoleteAggregates);
    }

    public void namedPageStyleChanged(PageStyle o) {
        if (pageStyle != null) {
            if (pageStyle.getName() == null || pageStyle.getName().equals(o.getName())) {
                cascadedPageStyle = null;
                clearCachedStyles();
            } else
                return;
        }
        if (myLayouter != null)
            myLayouter.invalidateLayout();
    }

    public void namedTableStyleChanged(TableStyle b) {
        for (int i = frames.size() - 1; i >= 0; i--) {
            frames.get(i).getTemplate().namedTableStyleChanged(b);
        }
        if (firstPageHeader != null)
            firstPageHeader.getTemplate().namedTableStyleChanged(b);
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader)
            furtherPagesHeader.getTemplate().namedTableStyleChanged(b);
        if (firstPageFooter != null)
            firstPageFooter.getTemplate().namedTableStyleChanged(b);
        if (middlePagesFooter != firstPageFooter && middlePagesFooter != null)
            middlePagesFooter.getTemplate().namedTableStyleChanged(b);
        if (lastPageFooter == middlePagesFooter && lastPageFooter != null)
            lastPageFooter.getTemplate().namedTableStyleChanged(b);
        firstHeaderHeight = furtherHeaderHeight = firstFooterHeight = middleFooterHeight = lastFooterHeight = Float.NaN;
    }

    public void namedCellStyleChanged(CellStyle b) {
        for (int i = frames.size() - 1; i >= 0; i--) {
            frames.get(i).getTemplate().namedStyleChanged(b);
        }
        if (firstPageHeader != null)
            firstPageHeader.getTemplate().namedStyleChanged(b);
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader)
            furtherPagesHeader.getTemplate().namedStyleChanged(b);
        if (firstPageFooter != null)
            firstPageFooter.getTemplate().namedStyleChanged(b);
        if (middlePagesFooter != firstPageFooter && middlePagesFooter != null)
            middlePagesFooter.getTemplate().namedStyleChanged(b);
        if (lastPageFooter == middlePagesFooter && lastPageFooter != null)
            lastPageFooter.getTemplate().namedStyleChanged(b);
        firstHeaderHeight = furtherHeaderHeight = firstFooterHeight = middleFooterHeight = lastFooterHeight = Float.NaN;
    }

    public PageLayouter getLayouter() {
        return myLayouter;
    }

    public void setLayouter(PageLayouter layouter) {
        myLayouter = layouter;
    }

    public ArrayList<PageLevelBox> getFrames() {
        return frames;
    }

    /**
     * find the footer Box
     * create one if there is none
     *
     * @return the footer box; never null
     */
    public PageLevelBox needFooterBox() {
        if (firstPageFooter == null) {
            firstPageFooter = new PageLevelBox(this, getData(), TemplateBoxInterface.firstPageFooter);
            TemplateModel tm = new TemplateModel(1, 1, firstPageFooter);
            firstPageFooter.setTemplate(tm);
            firstPageFooter.setFrameStyle(PredefinedStyles.instance().theFrameStyleDefaultFooterStyle);
            if (myLayouter != null)
                myLayouter.invalidateLayout();
        }
        return firstPageFooter;
    }

    public void insertBodyFrame(PageLevelBox b) {
        frames.add(0, b);
        if (myLayouter != null)
            myLayouter.invalidateLayout();
    }

    public void appendFrame(PageLevelBox b) {
        frames.add(b);
        if (myLayouter != null)
            myLayouter.invalidateLayout();
    }

    public void appendFrame(PageLevelBox b, PageLevelBox at) {
        if (myLayouter != null)
            myLayouter.invalidateLayout();
        for (int i = 0; i < frames.size(); i++) {
            PageLevelBox box = frames.get(i);
            if (box == at) {
                frames.add(i + 1, b);
                return;
            }
        }
        frames.add(b);
    }

    public void insertFrame(PageLevelBox b, PageLevelBox at) {
        if (myLayouter != null)
            myLayouter.invalidateLayout();
        for (int i = 0; i < frames.size(); i++) {
            PageLevelBox box = frames.get(i);
            if (box == at) {
                frames.add(i, b);
                return;
            }
        }
        frames.add(0, b);
    }

    public PageLevelBox getLastBody() {
        return frames.get(frames.size() - 1);
    }

    public PageLevelBox newSimpleBodyBox() {
        PageLevelBox ret = new PageLevelBox(this, getData(), TemplateBoxInterface.pageBodyBox);
        new TemplateModel(1, 1, ret);
        return ret;
    }

    public void syncFramesFromHeaderFooterConfiguration(int firstHeader, int furtherHeader, int firstFooter, int middleFooter, int lastFooter) {
        if (firstHeader == 0) {
            if (firstPageHeader == null) {
                firstPageHeader = new PageLevelBox(this, getData(), TemplateBoxInterface.firstPageHeader, 1, 3);
                firstPageHeader.setFrameStyle(PredefinedStyles.instance().theFrameStyleDefaultHeaderStyle);
            }
        } else
            firstPageHeader = null;
        if (firstFooter == 0) {
            if (firstPageFooter == null) {
                firstPageFooter = new PageLevelBox(this, getData(), TemplateBoxInterface.firstPageFooter, 1, 3);
                firstPageFooter.setFrameStyle(PredefinedStyles.instance().theFrameStyleDefaultFooterStyle);
            }
        } else
            firstPageFooter = null;
        if (furtherHeader == 0) {
            if (furtherPagesHeader == null || furtherPagesHeader == firstPageHeader) {
                furtherPagesHeader = new PageLevelBox(this, getData(), TemplateBoxInterface.furtherPagesHeader, 1, 3);
                furtherPagesHeader.setFrameStyle(PredefinedStyles.instance().theFrameStyleDefaultHeaderStyle);
            }
        } else if (furtherHeader == 2)
            furtherPagesHeader = firstPageHeader;
        else
            furtherPagesHeader = null;
        if (middleFooter == 0) {
            if (middlePagesFooter == null || middlePagesFooter == firstPageFooter) {
                middlePagesFooter = new PageLevelBox(this, getData(), TemplateBoxInterface.middlePagesFooter, 1, 3);
                middlePagesFooter.setFrameStyle(PredefinedStyles.instance().theFrameStyleDefaultFooterStyle);
            }
        } else if (middleFooter == 2)
            middlePagesFooter = firstPageFooter;
        else
            middlePagesFooter = null;
        if (lastFooter == 0) {
            if (lastPageFooter == null || lastPageFooter == middlePagesFooter || lastPageFooter == firstPageFooter) {
                lastPageFooter = new PageLevelBox(this, getData(), TemplateBoxInterface.lastPageFooter, 1, 3);
                lastPageFooter.setFrameStyle(PredefinedStyles.instance().theFrameStyleDefaultFooterStyle);
            }
        } else if (lastFooter == 2)
            lastPageFooter = middlePagesFooter;
        else
            lastPageFooter = null;
        Env.repoChanged();
        myLayouter.invalidateLayout();
        myLayouter.repaint();
    }

    public PageLevelBox getMiddlePagesFooter() {
        return middlePagesFooter;
    }

    public PageLevelBox getLastPageFooter() {
        return lastPageFooter;
    }

    public PageLevelBox getFurtherPagesHeader() {
        return furtherPagesHeader;
    }

    public PageLevelBox getFirstPageHeader() {
        return firstPageHeader;
    }

    public PageLevelBox getFirstPageFooter() {
        return firstPageFooter;
    }

    protected Object readResolve() throws ObjectStreamException {
        if (lastEditTime == 0)
            lastEditTime = System.currentTimeMillis();
        return this;
    }

    public void addHeaderFooterFrame(PageLevelBox box, int boxType) {
        if (boxType == TemplateBoxInterface.firstPageHeader)
            firstPageHeader = box;
        else if (boxType == TemplateBoxInterface.firstPageFooter)
            firstPageFooter = box;
        else if (boxType == TemplateBoxInterface.furtherPagesHeader)
            furtherPagesHeader = box;
        else if (boxType == TemplateBoxInterface.middlePagesFooter)
            middlePagesFooter = box;
        else if (boxType == TemplateBoxInterface.lastPageFooter)
            lastPageFooter = box;
        else
            throw new JoriaAssertionError("Not adding a header or footer frame: " + box.getBoxTypeName());
        if (myLayouter != null)
            myLayouter.invalidateAll();
    }

    public void setHeaderFooterFrame(PageLevelBox box) {
        int boxType = box.getBoxType();
        if (boxType == TemplateBoxInterface.firstPageHeader) {
            if (furtherPagesHeader == firstPageHeader)
                furtherPagesHeader = box;
            firstPageHeader = box;
        } else if (boxType == TemplateBoxInterface.furtherPagesHeader)
            furtherPagesHeader = box;
        else if (boxType == TemplateBoxInterface.firstPageFooter) {
            if (firstPageFooter == lastPageFooter)
                lastPageFooter = box;
            if (firstPageFooter == middlePagesFooter)
                middlePagesFooter = box;
            firstPageFooter = box;
        } else if (boxType == TemplateBoxInterface.middlePagesFooter) {
            if (middlePagesFooter == lastPageFooter)
                lastPageFooter = box;
            middlePagesFooter = box;
        } else if (boxType == TemplateBoxInterface.lastPageFooter)
            lastPageFooter = box;
        else
            throw new JoriaAssertionError("Not adding a header or footer frame: " + box.getBoxTypeName());
        if (myLayouter != null)
            myLayouter.invalidateAll();
    }

    public void setHeaderFooterFrame(PageLevelBox box, int slot) {
        if (slot == TemplateBoxInterface.firstPageHeader) {
            firstPageHeader = box;
        } else if (slot == TemplateBoxInterface.furtherPagesHeader)
            furtherPagesHeader = box;
        else if (slot == TemplateBoxInterface.firstPageFooter) {
            firstPageFooter = box;
        } else if (slot == TemplateBoxInterface.middlePagesFooter) {
            middlePagesFooter = box;
        } else if (slot == TemplateBoxInterface.lastPageFooter)
            lastPageFooter = box;
        if (myLayouter != null)
            myLayouter.invalidateAll();
    }

    public boolean fixAccess() {
        if (myData != null) {
            JoriaAccess fixed = myData.getPlaceHolderIfNeeded();
            if (fixed != null) {
                myData = fixed;
            }
            if (myData instanceof JoriaPlaceHolderAccess) {
                return false;
            }
            for (int i = 0; i < aggregates.size(); i++) {
                AggregateDef ad = aggregates.get(i);
                if (!ad.fixAccess()) {
                    aggregates.remove(i);
                    i--;
                }
            }
        }
        if (firstPageHeader != null)
            firstPageHeader.fixAccess();
        if (firstPageFooter != null)
            firstPageFooter.fixAccess();
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader)
            furtherPagesHeader.fixAccess();
        if (middlePagesFooter != null && middlePagesFooter != firstPageFooter)
            middlePagesFooter.fixAccess();
        if (lastPageFooter != null && lastPageFooter != middlePagesFooter)
            lastPageFooter.fixAccess();
        for (PageLevelBox box : frames) {
            box.fixAccess();
        }
        return true;
    }

    public Set<RuntimeParameter> getVariables(Set<Object> seen) {
        Set<RuntimeParameter> variables = new HashSet<RuntimeParameter>();
        if (myData == null)// neutralized reports should have no data variables
            return variables;
        if (firstPageHeader != null)
            firstPageHeader.collectVariables(variables, seen);
        if (firstPageFooter != null)
            firstPageFooter.collectVariables(variables, seen);
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader)
            furtherPagesHeader.collectVariables(variables, seen);
        if (middlePagesFooter != null && middlePagesFooter != firstPageFooter)
            middlePagesFooter.collectVariables(variables, seen);
        if (lastPageFooter != null && lastPageFooter != middlePagesFooter)
            lastPageFooter.collectVariables(variables, seen);
        for (PageLevelBox box : frames) {
            box.collectVariables(variables, seen);
        }
        if (myData instanceof VariableProvider)
            ((VariableProvider) myData).collectVariables(variables, new HashSet<Object>());
        return variables;
    }

    public void removeFrame(PageLevelBox plb) {
        if (plb == firstPageHeader) {
            firstPageHeader = null;
            firstHeaderHeight = 0;
            if (plb == furtherPagesHeader) {
                furtherPagesHeader = null;
                furtherHeaderHeight = 0;
            }
        } else if (plb == firstPageFooter) {
            firstPageFooter = null;
            firstFooterHeight = 0;
            if (plb == middlePagesFooter) {
                middleFooterHeight = 0;
                middlePagesFooter = null;
            }
            if (plb == lastPageFooter) {
                lastFooterHeight = 0;
                lastPageFooter = null;
            }
        } else if (plb == furtherPagesHeader) {
            furtherPagesHeader = null;
            furtherHeaderHeight = 0;
        } else if (plb == middlePagesFooter) {
            middlePagesFooter = null;
            middleFooterHeight = 0;
            if (plb == lastPageFooter) {
                lastPageFooter = null;
                lastFooterHeight = 0;
            }
        } else if (plb == lastPageFooter) {
            lastPageFooter = null;
            lastFooterHeight = 0;
        } else {
            frames.remove(plb);
            if (frames.size() == 0)
                frames.add(newSimpleBodyBox());
        }
        if (myLayouter != null)
            myLayouter.invalidateAll();
    }

    public Set<RuntimeParameter> getVariables(Set<RuntimeParameter> variables, Set<Object> seen) {
        if (myData == null)// neutralized reports should have no data variables
            return variables;
        if (firstPageHeader != null)
            firstPageHeader.collectVariables(variables, seen);
        if (firstPageFooter != null)
            firstPageFooter.collectVariables(variables, seen);
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader)
            furtherPagesHeader.collectVariables(variables, seen);
        if (middlePagesFooter != null && middlePagesFooter != firstPageFooter)
            middlePagesFooter.collectVariables(variables, seen);
        if (lastPageFooter != null && lastPageFooter != middlePagesFooter)
            lastPageFooter.collectVariables(variables, seen);
        for (PageLevelBox box : frames) {
            box.collectVariables(variables, seen);
        }
        if (myData instanceof VariableProvider)
            ((VariableProvider) myData).collectVariables(variables, new HashSet<Object>());
        return variables;
    }

    public static PageMaster createStandardPage(JoriaAccess data) {
        PageMaster pm = new PageMaster(data);
        PageLevelBox titleBox = new PageLevelBox(pm, data, TemplateBoxInterface.pageBodyBox);
        TemplateModel tm = new TemplateModel(1, 1, titleBox);
        TextCellDef title = new TextCellDef(tm, Res.str("Click_here_to_edit_the_title"));
        title.setStyle(PredefinedStyles.instance().theCellStyleDefaultTitleStyle);
        tm.setCellAt(title, 0, 0);
        pm.insertBodyFrame(titleBox);
        //
        PageLevelBox headerBox = new PageLevelBox(pm, data, TemplateBoxInterface.firstPageHeader, 1, 3);
        headerBox.setFrameStyle(PredefinedStyles.instance().theFrameStyleDefaultHeaderStyle);
        PageLevelBox bodyBox = pm.newSimpleBodyBox();
        bodyBox.setFrameStyle(PredefinedStyles.instance().theFrameStyleDefaultBodyStyle);
        pm.frames.add(bodyBox);
        PageLevelBox footerBox = new PageLevelBox(pm, data, TemplateBoxInterface.firstPageFooter, 1, 3);
        footerBox.setFrameStyle(PredefinedStyles.instance().theFrameStyleDefaultFooterStyle);
        pm.furtherPagesHeader = pm.firstPageHeader = headerBox;
        pm.lastPageFooter = pm.middlePagesFooter = pm.firstPageFooter = footerBox;
        return pm;
    }

    protected void reloadStyle(PageLevelBox b) {
        if (b == null)
            return;
        FrameStyle fs = b.getFrameStyle();
        if (fs == null || fs.getName() == null)
            return;
        b.setFrameStyle(Env.instance().repo().frameStyles.find(fs.getName()));
        RepeaterList rr = b.getTemplate().getRepeaterList();
        rr.reloadStyles();
        b.getTemplate().reloadStyles();
    }

    public void reloadStyles() {
        if (pageStyle != null && pageStyle.getName() != null)
            setPageStyle(Env.instance().repo().pageStyles.find(pageStyle.getName()));
        for (PageLevelBox f : frames) {
            reloadStyle(f);
        }
        reloadStyle(firstPageHeader);
        reloadStyle(firstPageFooter);
        reloadStyle(middlePagesFooter);
        reloadStyle(furtherPagesHeader);
        reloadStyle(lastPageFooter);
    }

    public void makeUnbound() {
        for (PageLevelBox f : frames) {
            f.getTemplate().makeUnbound();
        }
        if (firstPageHeader != null)
            firstPageHeader.getTemplate().makeUnbound();
        if (firstPageFooter != null)
            firstPageFooter.getTemplate().makeUnbound();
        if (middlePagesFooter != null)
            middlePagesFooter.getTemplate().makeUnbound();
        if (furtherPagesHeader != null)
            furtherPagesHeader.getTemplate().makeUnbound();
        if (lastPageFooter != null)
            lastPageFooter.getTemplate().makeUnbound();
        myData = null;
    }

    public void collectI18nKeys(HashSet<String> keySet) {
        for (PageLevelBox f : frames) {
            f.getTemplate().collectI18nKeys(keySet);
        }
        if (firstPageHeader != null)
            firstPageHeader.getTemplate().collectI18nKeys(keySet);
        if (firstPageFooter != null)
            firstPageFooter.getTemplate().collectI18nKeys(keySet);
        if (middlePagesFooter != null)
            middlePagesFooter.getTemplate().collectI18nKeys(keySet);
        if (furtherPagesHeader != null)
            furtherPagesHeader.getTemplate().collectI18nKeys(keySet);
        if (lastPageFooter != null)
            lastPageFooter.getTemplate().collectI18nKeys(keySet);
    }

    public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> keySet) {
        for (PageLevelBox f : frames) {
            f.getTemplate().collectI18nKeys2(keySet);
        }
        if (firstPageHeader != null)
            firstPageHeader.getTemplate().collectI18nKeys2(keySet);
        if (firstPageFooter != null)
            firstPageFooter.getTemplate().collectI18nKeys2(keySet);
        if (middlePagesFooter != null)
            middlePagesFooter.getTemplate().collectI18nKeys2(keySet);
        if (furtherPagesHeader != null)
            furtherPagesHeader.getTemplate().collectI18nKeys2(keySet);
        if (lastPageFooter != null)
            lastPageFooter.getTemplate().collectI18nKeys2(keySet);
    }

    public void collectExternalFiles(ExternalFileUsage results, Template r) {
        for (PageLevelBox f : frames) {
            f.getTemplate().collectExternalFiles(results, r);
        }
        if (firstPageHeader != null)
            firstPageHeader.getTemplate().collectExternalFiles(results, r);
        if (firstPageFooter != null)
            firstPageFooter.getTemplate().collectExternalFiles(results, r);
        if (middlePagesFooter != null)
            middlePagesFooter.getTemplate().collectExternalFiles(results, r);
        if (furtherPagesHeader != null)
            furtherPagesHeader.getTemplate().collectExternalFiles(results, r);
        if (lastPageFooter != null)
            lastPageFooter.getTemplate().collectExternalFiles(results, r);
    }

    public void namedStyleChanged(StyleBase style) {
        if (style instanceof CellStyle) {
            CellStyle cellStyle = (CellStyle) style;
            namedCellStyleChanged(cellStyle);
        } else if (style instanceof FrameStyle) {
            FrameStyle frameStyle = (FrameStyle) style;
            namedFrameStyleChanged(frameStyle);
        } else if (style instanceof PageStyle) {
            PageStyle pageStyle1 = (PageStyle) style;
            namedPageStyleChanged(pageStyle1);
        } else if (style instanceof TableStyle) {
            TableStyle tableStyle = (TableStyle) style;
            namedTableStyleChanged(tableStyle);
        }
    }

    public void fixVisibiltyCondition() {
        if (firstPageHeader != null)
            firstPageHeader.fixVisibiltyCondition();
        if (firstPageFooter != null)
            firstPageFooter.fixVisibiltyCondition();
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader)
            furtherPagesHeader.fixVisibiltyCondition();
        if (middlePagesFooter != null && middlePagesFooter != firstPageFooter)
            middlePagesFooter.fixVisibiltyCondition();
        if (lastPageFooter != null && lastPageFooter != middlePagesFooter)
            lastPageFooter.fixVisibiltyCondition();
        for (PageLevelBox box : frames) {
            box.fixVisibiltyCondition();
        }
    }

    public int testCellStyleForSpanCollision(CellStyle cs, int newHSpan, int newVSpan) {
        int coll = TemplateModel.NO_COLLISION;
        if (firstPageHeader != null)
            coll = firstPageHeader.getTemplate().testSpanCollision(cs, newHSpan, newVSpan);
        int maxColl = Math.max(TemplateModel.NO_COLLISION, coll);
        if (maxColl > TemplateModel.NON_EMPTY_CELL_IN_SPAN)
            return maxColl;
        if (firstPageFooter != null)
            coll = firstPageFooter.getTemplate().testSpanCollision(cs, newHSpan, newVSpan);
        maxColl = Math.max(maxColl, coll);
        if (maxColl > TemplateModel.NON_EMPTY_CELL_IN_SPAN)
            return maxColl;
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader)
            coll = furtherPagesHeader.getTemplate().testSpanCollision(cs, newHSpan, newVSpan);
        maxColl = Math.max(maxColl, coll);
        if (maxColl > TemplateModel.NON_EMPTY_CELL_IN_SPAN)
            return maxColl;
        if (middlePagesFooter != null && middlePagesFooter != firstPageFooter)
            coll = middlePagesFooter.getTemplate().testSpanCollision(cs, newHSpan, newVSpan);
        maxColl = Math.max(maxColl, coll);
        if (maxColl > TemplateModel.NON_EMPTY_CELL_IN_SPAN)
            return maxColl;
        if (lastPageFooter != null && lastPageFooter != middlePagesFooter)
            coll = lastPageFooter.getTemplate().testSpanCollision(cs, newHSpan, newVSpan);
        maxColl = Math.max(maxColl, coll);
        if (maxColl > TemplateModel.NON_EMPTY_CELL_IN_SPAN)
            return maxColl;
        for (PageLevelBox box : frames) {
            coll = box.getTemplate().testSpanCollision(cs, newHSpan, newVSpan);
            maxColl = Math.max(maxColl, coll);
            if (maxColl > TemplateModel.NON_EMPTY_CELL_IN_SPAN)
                return maxColl;
        }
        return maxColl;
    }

    public void makeFilenameRelative(JoriaFileService fs) {
        if (pageStyle != null)
            pageStyle.makeFileName(fs, true);
        if (firstPageHeader != null)
            firstPageHeader.makeFilenameRelative(fs);
        if (firstPageFooter != null)
            firstPageFooter.makeFilenameRelative(fs);
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader)
            furtherPagesHeader.makeFilenameRelative(fs);
        if (middlePagesFooter != null && middlePagesFooter != firstPageFooter)
            middlePagesFooter.makeFilenameRelative(fs);
        if (lastPageFooter != null && lastPageFooter != middlePagesFooter)
            lastPageFooter.makeFilenameRelative(fs);
        for (PageLevelBox box : frames) {
            box.makeFilenameRelative(fs);
        }
    }

    public void makeFilenameAbsolute(JoriaFileService fs) {
        if (pageStyle != null)
            pageStyle.makeFileName(fs, false);
        if (firstPageHeader != null)
            firstPageHeader.makeFilenameAbsolute(fs);
        if (firstPageFooter != null)
            firstPageFooter.makeFilenameAbsolute(fs);
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader)
            furtherPagesHeader.makeFilenameAbsolute(fs);
        if (middlePagesFooter != null && middlePagesFooter != firstPageFooter)
            middlePagesFooter.makeFilenameAbsolute(fs);
        if (lastPageFooter != null && lastPageFooter != middlePagesFooter)
            lastPageFooter.makeFilenameAbsolute(fs);
        for (PageLevelBox box : frames) {
            box.makeFilenameAbsolute(fs);
        }
    }

    public void getUsedAccessors(Set<JoriaAccess> ret) throws OQLParseException {
        ret.add(myData);
        if (firstPageHeader != null)
            firstPageHeader.getUsedAccessors(ret);
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader)
            furtherPagesHeader.getUsedAccessors(ret);
        if (firstPageFooter != null)
            firstPageFooter.getUsedAccessors(ret);
        if (middlePagesFooter != null && middlePagesFooter != firstPageFooter)
            middlePagesFooter.getUsedAccessors(ret);
        if (lastPageFooter != null && lastPageFooter != middlePagesFooter)
            lastPageFooter.getUsedAccessors(ret);
        for (PageLevelBox frame : frames) {
            frame.getUsedAccessors(ret);
        }
    }

    public void getAllReferencedFiles(HashSet<String> ret) {
        if (firstPageHeader != null)
            firstPageHeader.getTemplate().getAllReferencedFiles(ret);
        if (firstPageFooter != null)
            firstPageFooter.getTemplate().getAllReferencedFiles(ret);
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader)
            furtherPagesHeader.getTemplate().getAllReferencedFiles(ret);
        if (middlePagesFooter != null && middlePagesFooter != firstPageFooter)
            middlePagesFooter.getTemplate().getAllReferencedFiles(ret);
        if (lastPageFooter != null && lastPageFooter != middlePagesFooter)
            lastPageFooter.getTemplate().getAllReferencedFiles(ret);
        for (PageLevelBox box : frames) {
            box.getTemplate().getAllReferencedFiles(ret);
        }
    }

    public boolean visitCells(CellVisitor cellVisitor) {
        if (firstPageHeader != null) {
            if (!firstPageHeader.getTemplate().visitCells(cellVisitor)) {
                return false;
            }
        }
        if (firstPageFooter != null) {
            if (!firstPageFooter.getTemplate().visitCells(cellVisitor)) {
                return false;
            }
        }
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader) {
            if (!furtherPagesHeader.getTemplate().visitCells(cellVisitor)) {
                return false;
            }
        }
        if (middlePagesFooter != null && middlePagesFooter != firstPageFooter) {
            if (!middlePagesFooter.getTemplate().visitCells(cellVisitor)) {
                return false;
            }
        }
        if (lastPageFooter != null && lastPageFooter != middlePagesFooter) {
            if (!lastPageFooter.getTemplate().visitCells(cellVisitor)) {
                return false;
            }
        }
        for (PageLevelBox box : frames) {
            if (!box.getTemplate().visitCells(cellVisitor))
                return false;
        }
        return true;
    }

    public boolean visitRepeater(RepeaterVisitor repVisitor) {
        if (firstPageHeader != null) {
            if (!firstPageHeader.getTemplate().visitRepeaters(repVisitor)) {
                return false;
            }
        }
        if (firstPageFooter != null) {
            if (!firstPageFooter.getTemplate().visitRepeaters(repVisitor)) {
                return false;
            }
        }
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader) {
            if (!furtherPagesHeader.getTemplate().visitRepeaters(repVisitor)) {
                return false;
            }
        }
        if (middlePagesFooter != null && middlePagesFooter != firstPageFooter) {
            if (!middlePagesFooter.getTemplate().visitRepeaters(repVisitor)) {
                return false;
            }
        }
        if (lastPageFooter != null && lastPageFooter != middlePagesFooter) {
            if (!lastPageFooter.getTemplate().visitRepeaters(repVisitor)) {
                return false;
            }
        }
        for (PageLevelBox box : frames) {
            if (!box.getTemplate().visitRepeaters(repVisitor))
                return false;
        }
        return true;
    }

    public boolean visitFrames(FrameVisitor frameVisitor) {
        if (firstPageHeader != null) {
            if (!firstPageHeader.visit(frameVisitor)) {
                return false;
            }
        }
        if (firstPageFooter != null) {
            if (!firstPageFooter.visit(frameVisitor)) {
                return false;
            }
        }
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader) {
            if (!furtherPagesHeader.visit(frameVisitor)) {
                return false;
            }
        }
        if (middlePagesFooter != null && middlePagesFooter != firstPageFooter) {
            if (!middlePagesFooter.visit(frameVisitor)) {
                return false;
            }
        }
        if (lastPageFooter != null && lastPageFooter != middlePagesFooter) {
            if (!lastPageFooter.visit(frameVisitor)) {
                return false;
            }
        }
        for (PageLevelBox box : frames) {
            if (!box.visit(frameVisitor))
                return false;
        }
        return true;
    }

    public ClassProjection createFormulaView() {
        if (myData.getType().isClass())
            formulaView = new ClassProjection(myData.getClassTypeAsserted());
        else if (myData.getType().isCollection()) {
            formulaView = new ClassProjection(JoriaClassVoid.voidType);

        } else
            throw new JoriaAssertionError("class or collection expected: " + myData.getType());


        Set<JoriaAccess> members = new HashSet<JoriaAccess>();
        FormulaCollector formulaCollector = new FormulaCollector(members);
        visitCells(formulaCollector);
        JoriaAccess[] joriaAccesses = members.toArray(new JoriaAccess[members.size()]);
        formulaView.setMembers(joriaAccesses);
        return formulaView;
    }

    public ClassProjection getFormulaView() {
        return formulaView;
    }

    public void noteChange() {
        setLastEditTime(System.currentTimeMillis());
    }

    public long getLastEditTime() {
        return lastEditTime;
    }

    public void setLastEditTime(final long lastEditTime) {
        this.lastEditTime = lastEditTime;
    }

    public boolean visitAllAccessors(AccessVisitor visitor, Set<JoriaAccess> seen) {
        if (!visitor.visit(myData))
            return false;
        if (myData instanceof VisitableAccess) {
            if (!((VisitableAccess) myData).visitAllAccesses(visitor, seen))
                return false;
        }
        if (firstPageHeader != null) {
            if (!firstPageHeader.visitAccess(visitor, seen)) {
                return false;
            }
        }
        if (firstPageFooter != null) {
            if (!firstPageFooter.visitAccess(visitor, seen)) {
                return false;
            }
        }
        if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader) {
            if (!furtherPagesHeader.visitAccess(visitor, seen)) {
                return false;
            }
        }
        if (middlePagesFooter != null && middlePagesFooter != firstPageFooter) {
            if (!middlePagesFooter.visitAccess(visitor, seen)) {
                return false;
            }
        }
        if (lastPageFooter != null && lastPageFooter != middlePagesFooter) {
            if (!lastPageFooter.visitAccess(visitor, seen)) {
                return false;
            }
        }
        for (PageLevelBox box : frames) {
            if (!box.visitAccess(visitor, seen))
                return false;
        }
        return true;
    }

    public PageLevelBox getHeaderFooter(final int boxType) {
        if (boxType == TemplateBoxInterface.firstPageHeader)
            return firstPageHeader;
        if (boxType == TemplateBoxInterface.furtherPagesHeader)
            return furtherPagesHeader;
        if (boxType == TemplateBoxInterface.firstPageFooter)
            return firstPageFooter;
        if (boxType == TemplateBoxInterface.middlePagesFooter)
            return middlePagesFooter;
        if (boxType == TemplateBoxInterface.lastPageFooter)
            return lastPageFooter;
        throw new JoriaAssertionError("Bad Header/FooterKey " + boxType);
    }

    public class AllFramesIterator implements Iterator<PageLevelBox>, Iterable<PageLevelBox> {
        int at;

        public boolean hasNext() {
            return at < frames.size() + 5;
        }

        public PageLevelBox next() {
            if (at >= frames.size() + 5)
                return null;
            if (at == 0) {
                at++;
                if (firstPageHeader != null) {
                    return firstPageHeader;
                }
            }
            if (at == 1) {
                at++;
                if (furtherPagesHeader != null && furtherPagesHeader != firstPageHeader) {
                    return furtherPagesHeader;
                }
            }
            if (at == 2) {
                at++;
                if (firstPageFooter != null) {
                    return firstPageFooter;
                }
            }
            if (at == 3) {
                at++;
                if (middlePagesFooter != null && middlePagesFooter != firstPageFooter) {
                    return middlePagesFooter;
                }
            }
            if (at == 4) {
                at++;
                if (lastPageFooter != null && middlePagesFooter != lastPageFooter) {
                    return lastPageFooter;
                }
            }
            final PageLevelBox ret = frames.get(at - 5);
            at++;
            return ret;

        }

        public void remove() {
            throw new JoriaAssertionError("Cannot remove from frames via iterator");
        }

        public Iterator<PageLevelBox> iterator() {
            return this;
        }
    }

    public AllFramesIterator iterateAllFrames() {
        return new AllFramesIterator();
    }


}
