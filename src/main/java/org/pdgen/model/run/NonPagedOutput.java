// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.*;
import org.pdgen.model.*;
import org.pdgen.model.cells.CellDef;

import org.pdgen.data.view.AccessPath;
import org.pdgen.data.view.AggregateDef;
import org.pdgen.data.view.IndirectAccess;
import org.pdgen.env.Env;
import org.pdgen.env.JoriaException;
import org.pdgen.model.run.OutputMode.LastRowState;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Stack;

/**
 * User: patrick
 * Date: Jul 11, 2005
 * Time: 7:27:59 AM
 */
public abstract class NonPagedOutput {
    protected int rr;
    protected Output out;
    protected AggregateCollector aggs;
    private DBData rootVal;

    protected abstract boolean addNullCellInt(RunRangeBase rr, CellDef cd, int row, int col, boolean firstRepeat) throws JoriaDataException;

    protected abstract void addSpanInt(int count) throws JoriaDataException;

    protected abstract void generateOutputInt(RunRangeBase rr, CellDef cd, int row, int c) throws JoriaDataException;

    protected abstract void processOneFrame(RDBase[][] fields, RVTemplate rvt, TemplateModel template);

    protected abstract void preprocess();

    protected abstract void postprocess();

    protected abstract boolean includeHeaderFooterFrames();

    protected abstract void startOneFrame(int frameType);

    protected abstract void endOneFrame() throws JoriaDataException;

    protected abstract void startOneRow() throws JoriaDataException;

    protected abstract void endOneRow() throws JoriaDataException;

    protected abstract boolean includeAllHeaderAndFooterFrames();

    protected RunEnvImpl env;
    public static final int CONTENT_FRAME = 0;
    public static final int FIRST_HEADER_FRAME = 1;
    public static final int FURTHER_HEADER_FRAME = 2;
    public static final int FIRST_FOOTER_FRAME = 3;
    public static final int MIDDLE_FOOTER_FRAME = 4;
    public static final int LAST_FOOTER_FRAME = 5;

    protected NonPagedOutput(RunEnvImpl env) {
        this.env = env;
    }

    protected void doOutput() throws JoriaException {
        Graphics2D savedG2D = env.getGraphics2D();
        try {
            rootVal = env.loadRootVal();
            Template template = env.startTemplate;
            aggs = new AggregateCollector(env);
            rr = 0;
            env.setGraphics2D(Env.instance().getDefaultGraphics2D());
            preprocess();
            do {
                if (env.getTemplate().getReportType() == Template.masterDetailReport) {
                    if (rootVal != null) {
                        if (!(rootVal instanceof DBCollection)) {
                            JoriaAccess axs = env.getTemplate().getPage().getData();
                            if (axs instanceof AccessPath) {
                                AccessPath path = (AccessPath) axs;
                                JoriaAccess[] elements = path.getPath();
                                JoriaAccess base = elements[0];
                                JoriaAccess[] rest = new JoriaAccess[elements.length - 1];
                                System.arraycopy(elements, 1, rest, 0, elements.length - 1);
                                JoriaAccess newPath = AccessPath.makePath(rest);
                                while (base instanceof IndirectAccess) {
                                    base = ((IndirectAccess) base).getBaseAccess();
                                }
                                env.putRuntimeParameter(base, rootVal);
                                rootVal = newPath.getValue(rootVal, newPath, env);
                                env.putRuntimeParameter(axs, rootVal);
                                env.rootVal = rootVal;
                            } else
                                throw new JoriaDataException("root value of a master/detail template is not a collection");
                        }
                        Trace.check(rootVal, DBCollection.class);
                        DBCollection collVal = (DBCollection) rootVal;
                        while (collVal.next()) {
                            rootVal = collVal.current();
                            if (rootVal != null && !rootVal.isNull()) {
                                doOneMaster(template);
                                aggs.resetTotals(AggregateDef.grand);
                            }
                        }
                    }
                } else
                    doOneMaster(template);
                template = template.getNextSection();
                if (template != null) {
                    env.nextSection();
                    rootVal = env.getRootVal();
                }
            }
            while (template != null);
            postprocess();
        } finally {
            env.setGraphics2D(savedG2D);
        }
    }

    protected void doOneMaster(Template template) throws JoriaException {
        startSection(template);
        PageMaster p = template.getPage();
        if (includeHeaderFooterFrames() && !includeAllHeaderAndFooterFrames()) {
            PageLevelBox box = p.getFirstPageHeader();
            if (box != null) {
                buildOneFrame(box, rootVal);
                runOneFrame(box.getTemplate(), FIRST_HEADER_FRAME);
            }
        }
        if (includeAllHeaderAndFooterFrames()) {
            PageLevelBox box = p.getFirstPageHeader();
            if (box != null) {
                buildOneFrame(box, rootVal);
                runOneFrame(box.getTemplate(), FIRST_HEADER_FRAME);
            }
            box = p.getFirstPageFooter();
            if (box != null) {
                buildOneFrame(box, rootVal);
                runOneFrame(box.getTemplate(), FIRST_FOOTER_FRAME);
            }
            box = p.getFurtherPagesHeader();
            if (box != null) {
                buildOneFrame(box, rootVal);
                runOneFrame(box.getTemplate(), FURTHER_HEADER_FRAME);
            }
            box = p.getMiddlePagesFooter();
            if (box != null) {
                buildOneFrame(box, rootVal);
                runOneFrame(box.getTemplate(), MIDDLE_FOOTER_FRAME);
            }
            box = p.getLastPageFooter();
            if (box != null) {
                buildOneFrame(box, rootVal);
                runOneFrame(box.getTemplate(), LAST_FOOTER_FRAME);
            }
        }
        ArrayList<PageLevelBox> l = p.getFrames();
        ArrayList<Object> outFrames = new ArrayList<Object>(l.size());
        for (PageLevelBox box : l) {
            if (box.isVisible(env, rootVal)) {
                buildOneFrame(box, rootVal);
                outFrames.add(out);
            }
        }
        for (int i = 0; i < outFrames.size(); i++) {
            Object o = outFrames.get(i);
            if (o instanceof PageLevelBox) {
                PageLevelBox box = (PageLevelBox) o;
                buildOneFrame(box, rootVal);
                outFrames.set(i, out);
            }
        }
        for (Object outFrame : outFrames) {
            out = (Output) outFrame;
            runOneFrame(out.getTemplate(), CONTENT_FRAME);
        }
        if (includeHeaderFooterFrames() || !includeAllHeaderAndFooterFrames()) {
            PageLevelBox box = p.getLastPageFooter();
            if (box != null) {
                buildOneFrame(box, rootVal);
                runOneFrame(box.getTemplate(), LAST_FOOTER_FRAME);
            }
        }
        endSection();
    }

    protected void endSection() {
    }

    protected void startSection(Template template) {
    }

    private void buildOneFrame(PageLevelBox box, DBData rootVal) throws JoriaDataException {
        TemplateModel template = box.getTemplate();
        RDRangeBase rdef = template.getColumnDefs();
        out = new Output(template, rootVal, rdef);
    }

    private void runOneFrame(TemplateModel template, int frameType) throws JoriaDataException {
        processOneFrame(out.rdef.fields, out.rvt, template);
        out.rdef.format(out.rvt, aggs.env.getLocale());
        if (template != null && template.isCrosstabFrame())
            out.fill = new RunCrosstab(out, out.rvt, out.rdef, 0, template);
        else if (template != null)
            out.fill = new RunTemplate(out, out.rvt, out.rdef, 0, template);
        int cc = template == null ? 0 : template.getColCount();
        int rc = template == null ? 0 : template.getRowCount();
        if (out.lastRowState == OutputMode.LastRowState.errorOccured)
            throw new JoriaAssertionError("HtmlOutput.run called when in error state");
        if (rc == 0 || cc == 0) {
            if (template != null)
                Trace.logDebug(Trace.fill, "Skipped empty model: " + template.getFrame().getBoxTypeName());
            else {
                startOneFrame(frameType);
                endOneFrame();
            }
        } else {
            startOneFrame(frameType);
            while (out.lastRowState == LastRowState.proceed) {
                rr++;
                startOneRow();
                out.colInCurrRow = 0;
                out.fill.row();
                aggs.completeRowAccus();
                endOneRow();
                Trace.logDebug(Trace.fill, out.ind + "Done outRow " + out.outRow);
                out.outRow++;
                out.lastRowState = out.fill.advance();// returns notYetReached, breakAfterObject, endOfData
                if (out.lastRowState != OutputMode.LastRowState.proceed) {
                    break;
                }
            }
            if (out.lastRowState != OutputMode.LastRowState.endOfData)
                throw new JoriaAssertionError("Bad row state in NonPagedOutput" + out.lastRowState);
            endOneFrame();
        }
    }

    public String getPageNumberInt() {
        return "1";
    }

    public String getTotalPageNumberPlaceHolderInt() {
        return "1";
    }

    protected class Output extends OutputMode {
        public RVTemplate rvt;
        public RDRangeBase rdef;

        public Output(TemplateModel template, DBData rootVal, RDRangeBase rdef) throws JoriaDataException {
            super(template, rootVal);
            out = this;
            this.rdef = rdef;
            rvt = (RVTemplate) rdef.buildRunValue(rootVal, out, new Stack<RDBase>(), new Stack<RVAny>(), env.getGraphics2D());
        }

        /**
         * for inner frame
         *
         * @param template inner template
         * @param rootVal  rootVal
         * @param rdef     cell definitions
         * @param subVals  nested vals that have been computed
         */
        public Output(TemplateModel template, DBData rootVal, RDRangeBase rdef, RVTemplate subVals) {
            super(template, rootVal);
            out = this;
            this.rdef = rdef;
            rvt = subVals;
        }

        public void addSpan(int cols) throws JoriaDataException {
            addSpanInt(cols);
        }

        public RunEnvImpl getRunEnv() {
            return env;
        }

        public boolean addNullCell(RunRangeBase rr, int templateRow, int templateCol) throws JoriaDataException {
            return addNullCellInt(rr, null, templateRow, templateCol, false);
        }

        public boolean generateOutput(RunRangeBase rr, CellDef cd, int row, int c) throws JoriaDataException {
            generateOutputInt(rr, cd, row, c);
            return false;
        }

        public boolean generateOutputRepeatedCell(RunRangeBase rr, CellDef cd, int row, int col, boolean firstRepeat, boolean lastRepeat) throws JoriaDataException {
            addNullCellInt(rr, cd, row, col, firstRepeat);
            return false;
        }

        public void startRepeater(Repeater r) {
        }

        public void makeSavePoint() {
        }

        public void releaseSavePoint() {
        }

        public void steppedRepeater(Repeater fromRepeater) {
        }

        public String getPageNumber() {
            return getPageNumberInt();
        }

        public String getTotalPageNumberPlaceHolder() {
            return getTotalPageNumberPlaceHolderInt();
        }

        public void checkSavePoints(boolean resetRepeater) {
            // nothing to do
        }

        public boolean isFirstBodyFrame() {
            return true;
        }

        public AggregateCollector getAggregateCollector() {
            return aggs;
        }

        public boolean isPageOutput() {
            return false;
        }

        public void addSpaceForTableBorderAtPageBottom(float spaceNeeded) {
        }

        public void recalcSpaceForTableBorderAtPageBottom() {
        }

        public OutputMode createInner(final TemplateModel innerModel, final RDRange defs, final RVTemplate subVals, final int col) {
            NonPagedOutput.Output ret = new Output(innerModel, rootVal, defs, subVals);
            return ret;
        }

        public float fillInner(RunRangeBase rr, final RDRange defs, final RVTemplate subVals, int row, int col) throws JoriaDataException {
            return 0;
        }
    }
}
