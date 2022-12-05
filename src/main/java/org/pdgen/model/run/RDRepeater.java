// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.*;
import org.pdgen.env.Env;
import org.pdgen.model.RDBase;
import org.pdgen.model.Repeater;
import org.pdgen.model.Template;
import org.pdgen.model.cells.CellDef;
import org.pdgen.util.BucketList;

import java.awt.*;
import java.util.Stack;

public class RDRepeater extends RDRangeBase {

    private static final long serialVersionUID = 7L;

    public RDRepeater(Repeater repeater, RDBase[][] fields) {
        super(repeater, fields);
    }

    public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException {
        DBCollection source = (DBCollection) myModel.getAccessor().getValue(from, myModel.getAccessor(), outMode.getRunEnv());
        if (source == null || source.isNull()) {
            for (int i = defs.size() - 1; i > 0; i--) {
                //final RDRangeBase rdrb = (RDRangeBase) defs.peek();
                //RVTemplate rvt = (RVTemplate) outerVals.get(i);
                //final RDBase[][] fields = rdrb.getFields();
                Trace.logDebug(Trace.run, "empty coll");
            }
            return null;
        }
        source.reset();
        outMode.getRunEnv().pushToObjectPath(from);
        RVAny[][] fill = new RVAny[myModel.getRowCount()][];
        RVTemplate ret = new RVTemplate(fill);
        defs.push(this);
        outerVals.push(ret);
        for (int r = 0; r < fill.length; r++) {
            RDBase[] rDef = fields[r];
            RVAny[] row = new RVAny[rDef.length];
            fill[r] = row;
            for (int c = 0; c < row.length; c++) // allocate columns
            {
                RDBase rdb = rDef[c];
                if (rdb == null) {
                } else if (rdb instanceof RDRepeater)
                    row[c] = new RVObjects(new RVTemplate[source.getLength()]);
                else
                    row[c] = rdb.buildRunValue(source, outMode, defs, outerVals, g);
            }
        }

        int rank = 0;
        final Repeater myRepeater = (Repeater) myModel;
        Template drilldownTarget = ((Repeater) myModel).getDrillDown();
        boolean hasDrillDown = drilldownTarget != null;
        final BucketList<DrillDownLink> drillDownKeys;
        if (hasDrillDown)
            drillDownKeys = new BucketList<DrillDownLink>();
        else
            drillDownKeys = null;
        boolean none = true;
        while (source.next()) {
            if (Env.instance().isCancelPressed())
                throw new UserAbortError();
            DBObject o = source.current();
            none = false;
            if (hasDrillDown)
                drillDownKeys.addObject(new DrillDownLink(o, drilldownTarget, outMode.getRunEnv().getObjectPath()));
            for (int r = 0; r < fill.length; r++) {
                RDBase[] rDef = fields[r];
                RVAny[] row = fill[r];
                for (int c = 0; c < row.length; c++) //fill columns
                {
                    if (Env.instance().isCancelPressed())
                        throw new UserAbortError();
                    RDBase rdb = rDef[c];
                    if (rdb == null)
                        continue;
                    if (rdb instanceof CellDef && !((CellDef) rdb).isVisible(outMode, o))
                        continue;
                    if (row[c] instanceof RVCol && rdb instanceof CellDef) {
                        RVCol rvc = (RVCol) row[c];
                        rvc.add(rank, o, (CellDef) rdb, outMode);
                    } else if (rdb instanceof RDRepeater) {
                        RDRepeater inner = (RDRepeater) rdb;
                        RVAny val = inner.buildRunValue(o, outMode, defs, outerVals, g);
                        ((RVObjects) row[c]).put(rank, (RVTemplate) val);
                    }
                    // else RDRepeaterNext do nothing: values already built
                }
            }
            rank++;
            outMode.getRunEnv().nextToPrevs(o); // this is the prev object for the next iteration
        }

        if (none && myRepeater.getCascadedTableStyle().getSuppressEmpty()) {
            for (int i = defs.size() - 2; i >= 0; i--) {
                final RDRangeBase rdrb = (RDRangeBase) defs.get(i);
                RVTemplate rvt = (RVTemplate) outerVals.get(i);
                final RDBase[][] fields = rdrb.getFields();
                nextlevel:
                for (int r = 0; r < fields.length; r++) {
                    RDBase[] fieldRow = fields[r];
                    for (int c = 0; c < fieldRow.length; c++) {
                        RDBase rdBase = fieldRow[c];
                        if (rdBase instanceof CellDef) {
                            CellDef cd = (CellDef) rdBase;
                            final Repeater hr = cd.getHeadedRepeater();
                            if (isFamily(myRepeater, hr)) {
                                if (rvt.subs[r] == null)
                                    break nextlevel;
                                rvt.subs[r][c] = RVSupressHeader.instance; // the header value of an empty repeater is being supressed
                            }
                        }
                    }
                }
                Trace.logDebug(Trace.run, "empty coll");
            }
        }

        source.reset();
        outMode.getRunEnv().popFromObjectPath();
        ret.setElementCount(rank);

        ret.setDrillDownKeys(drillDownKeys);
        defs.pop();
        outerVals.pop();
        return ret;
    }

    private boolean isFamily(Repeater p, Repeater c) {
        while (c != null) {
            if (p == c)
                return true;
            c = c.getOuterRepeater();

        }
        return false;
    }
}
