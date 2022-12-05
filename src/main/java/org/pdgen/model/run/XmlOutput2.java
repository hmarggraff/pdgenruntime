// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.JoriaDataException;
import org.pdgen.data.view.AggregateDef;
import org.pdgen.data.view.MutableAccess;
import org.pdgen.env.Env;
import org.pdgen.env.JoriaException;
import org.pdgen.model.RDBase;
import org.pdgen.model.RDCrossTab;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.cells.*;

import javax.swing.plaf.basic.BasicHTML;
import java.io.IOException;
import java.io.Writer;

/**
 * User: patrick
 * Date: Jul 12, 2005
 * Time: 1:29:59 PM
 */
public class XmlOutput2 extends NonPagedOutput {
    Writer w;

    public static void writeXml(Writer output, RunEnvImpl env) throws JoriaException {
        XmlOutput2 out = new XmlOutput2(output, env);
        out.doOutput();
    }

    public XmlOutput2(Writer output, RunEnvImpl env) {
        super(env);
        w = output;
    }

    protected boolean addNullCellInt(RunRangeBase rr, CellDef cd, int row, int col, boolean firstRepeat) throws JoriaDataException {
        // not used here
        return false;
    }

    protected void addSpanInt(int count) throws JoriaDataException {
        // not used here
    }

    protected void generateOutputInt(RunRangeBase rr, CellDef cd, int row, int c) throws JoriaDataException {
        // not used here
    }

    protected void processOneFrame(RDBase[][] fields, RVTemplate rvt, TemplateModel template) {
        if (template != null)
            processOneLevel(fields, rvt, 0, 0);
    }

    protected void preprocess() {
        println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        //out.println("<!DOCTYPE simpleexport SYSTEM \"simpleexport.dtd\">");
        print("<pdgensimpleexport src=\"");
        printescaped(env.getTemplate().getName());
        println("\">");
    }

    protected void postprocess() {
        println("</pdgensimpleexport>");
        try {
            w.flush();
        } catch (IOException e) {
            Env.instance().handle(e);
        }
    }

    protected boolean includeHeaderFooterFrames() {
        return false;
    }

    protected void startOneFrame(int frameType) {
        // not used here
    }

    protected void endOneFrame() {
        // not used here
    }

    protected void startOneRow() {
        // not used here
    }

    protected void endOneRow() throws JoriaDataException {
        // not used here
    }

    protected boolean includeAllHeaderAndFooterFrames() {
        return false;
    }

    private void printindent(int level) {
        for (int i = 0; i < level; i++)
            print(" ");
    }

    private void printescaped(String s) {
        if (s == null) {
            print("(null)");
            return;
        }
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = (s.charAt(i));
            switch (c) {
                case '<':
                    buf.append("&lt;");
                    break;
                case '>':
                    buf.append("&gt;");
                    break;
                case '&':
                    buf.append("&amp;");
                    break;
                case '\'':
                    buf.append("&apos;");
                    break;
                case '"':
                    buf.append("&quot;");
                    break;
                default:
                    buf.append(c);
            }
        }
        print(buf.toString());
    }

    private void println(String s) {
        print(s + "\n");
    }

    private void print(String s) {
        try {
            w.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processOneLevel(RDBase[][] fields, RVTemplate rvt, int index, int level) {
        if (out.template.isCrosstabFrame()) {
            RDCrossTab ct = out.template.getCrosstab();
            printindent(level);
            print("<crosstab src=\"");
            printescaped(ct.getAccess().getName());
            print("\">");
            for (int hr = 0; hr < ct.getHeaderRows(); hr++) {
                print("<headerrow src=\"");
                CrosstabLabelCell cd = (CrosstabLabelCell) fields[hr][ct.getHeaderCols()];
                printescaped(cd.getAccessorName());
                print("\">");
                RVAny[] data = rvt.subs[hr];
                for (int col = ct.getHeaderCols(); col < data.length; col++) {
                    outputOneData(fields[hr][ct.getHeaderCols()], rvt.subs[hr][col], index, level);
                }
                println("</headerrow>");
            }
            for (int row = 0; row < rvt.elementCount; row++) {
                for (int agg = 0; agg < ct.getDataRows(); agg++) {
                    print("<row src=\"");
                    CrosstabValueCellDef cd = (CrosstabValueCellDef) fields[ct.getHeaderRows() + agg][ct.getHeaderCols()];
                    printescaped(cd.getAccessorName());
                    println("\">");
                    RVStringCol[] data = (RVStringCol[]) rvt.subs[agg + ct.getHeaderRows()];
                    int startCol = agg == 0 ? 0 : ct.getHeaderCols();
                    for (int col = startCol; col < data.length; col++) {
                        if (col < ct.getHeaderCols() && data[col].get(row) != null) {
                            print("<headcolumn src=\"");
                            CrosstabLabelCell cdi = (CrosstabLabelCell) fields[ct.getHeaderRows()][col];
                            printescaped(cdi.getAccessorName());
                            print("\">");
                            outputOneData(fields[ct.getHeaderRows()][col], data[col], row, level);
                            println("</headercolumn");
                        } else if (data[col].get(row) != null) {
                            outputOneData(fields[ct.getHeaderRows() + agg][ct.getHeaderCols()], data[col], row, level);
                        } else
                            println("<empty/>");
                    }
                    println("</row>");
                }
            }
            System.out.println("todo");//todo
            println("</crosstab>");
        } else {
            for (int row = 0; row < fields.length; row++) {
                rr++;
                for (int col = 0; col < fields[row].length; col++) {
                    RDBase field = fields[row][col];
                    RVAny data = rvt.get(row, col);
                    outputOneData(field, data, index, level);
                }
            }
        }
    }

    private void outputOneData(RDBase field, RVAny data, int index, int level) {
        if (field instanceof DataCellDef || field instanceof DateCell || field instanceof UserNameCell || field instanceof SummaryCell || field instanceof MasterCountCell || field instanceof CrosstabValueCellDef) {
            if (field instanceof SummaryCell) {
                SummaryCell sum = (SummaryCell) field;
                if (sum.getWhere() == AggregateDef.page || sum.getWhere() == AggregateDef.running) {
                    return;
                }
            }

            CellDef cd = (CellDef) field;
            if (data instanceof RVSupressHeader)
                return;
            RValue val = (RValue) data;
            if (val instanceof RVStringCol)
                ((RVStringCol) val).buildFormattedStrings(cd, aggs.getRunEnv().getLocale());
            if (val == null)
                return;
            String str = val.get(index);
            String name = null;
            if (field instanceof DataCellDef) {
                if (((DataCellDef) field).getAccessor() instanceof MutableAccess) {
                    MutableAccess ma = (MutableAccess) ((DataCellDef) field).getAccessor();
                    name = ma.getXmlTag();
                } else {
                    name = ((DataCellDef) field).getAccessor().getName();
                }
            } else if (field instanceof DateCell) {
                name = ((DateCell) field).getText();
            } else if (field instanceof UserNameCell) {
                name = ((UserNameCell) field).getText();
            } else if (field instanceof MasterCountCell) {
                name = ((MasterCountCell) field).getText();
            } else if (field instanceof SummaryCell) {
                SummaryCell sum = (SummaryCell) field;
                String axsName;
                if (sum.getAggregateDef().getAggregatedAccess() instanceof MutableAccess) {
                    MutableAccess ma = (MutableAccess) sum.getAggregateDef().getAggregatedAccess();
                    axsName = ma.getXmlTag();
                } else {
                    axsName = sum.getAggregateDef().getAggregatedAccess().getName();
                }
                name = axsName + ":" + AggregateDef.tagStrings[sum.getWhere()];
            }
            if (BasicHTML.isHTMLString(str)) {
                str = str.substring(str.indexOf('>') + 1);
            }
            printindent(level);
            print("<data");
            if (name != null) {
                print(" src=\"");
                printescaped(name);
                print("\"");
            }
            print(">");
            printescaped(str);
            println("</data>");
        } else if (field instanceof RDRepeater) {

            if (data instanceof RVTemplate) {
                RVTemplate rvtSub = (RVTemplate) data;
                RDBase[][] fieldsSub = ((RDRepeater) field).getFields();
                String name = ((RDRepeater) field).getAccess().getName();
                printindent(level);
                print("<coll src=\"");
                printescaped(name);
                println("\">");
                for (int indexSub = 0; indexSub < rvtSub.getElementCount(); indexSub++) {
                    printindent(level);
                    println("<elem>");
                    processOneLevel(fieldsSub, rvtSub, indexSub, level + 1);
                    printindent(level);
                    println("</elem>");
                }
                printindent(level);
                println("</coll>");
            } else if (data instanceof RVObjects) {
                RVObjects objects = (RVObjects) data;
                RVTemplate rvtSub1 = objects.elems[index];
                RDBase[][] fieldsSub = ((RDRepeater) field).getFields();
                String name = ((RDRepeater) field).getAccess().getName();
                printindent(level);
                print("<coll src=\"");
                printescaped(name);
                println("\">");
                for (int indexSub = 0; rvtSub1 != null && indexSub < rvtSub1.getElementCount(); indexSub++) {
                    printindent(level);
                    println("<elem>");
                    processOneLevel(fieldsSub, rvtSub1, indexSub, level + 1);
                    printindent(level);
                    println("</elem>");
                }
                printindent(level);
                println("</coll>");
            } else {
                System.out.println();
            }
        } else if (field instanceof RDRepeaterNext && data != null) {
            System.out.println();
        }
    }
}
