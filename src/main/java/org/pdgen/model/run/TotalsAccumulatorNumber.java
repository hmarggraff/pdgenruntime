// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.JoriaAssertionError;
import org.pdgen.data.Trace;
import org.pdgen.data.view.AggregateDef;

public class TotalsAccumulatorNumber implements TotalsAccumulator {

    protected double[] val = {Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
    protected double tVal = Double.NaN;
    protected long[] avgCnt = new long[5];
    protected int function;
    protected boolean first = true;
    protected boolean groupTop;
    protected boolean pageTop;

    public TotalsAccumulatorNumber(int function) {
        this.function = function;
    }

    /* ----------------------------------------------------------------------- rowComplete */
    public void rowComplete() {
        if (Double.isNaN(tVal))
            return;
        if (first) {
            java.util.Arrays.fill(val, 0, AggregateDef.lastRunning, tVal);
            java.util.Arrays.fill(avgCnt, 0, AggregateDef.lastRunning, 1);
            first = false;
        } else {
            switch (function) {
                case AggregateDef.avg:
                    for (int i = 0; i < 3; i++)
                        avgCnt[i]++;
                    Trace.logDebug(Trace.fill, "Average counted " + avgCnt[AggregateDef.group] + " " + avgCnt[AggregateDef.page]);

                    // no break fall through
                case AggregateDef.sum:
                    if (groupTop)
                        val[AggregateDef.group] = tVal;
                    else
                        val[AggregateDef.group] += tVal;
                    val[AggregateDef.grand] += tVal;
                    val[AggregateDef.running] += tVal;
                    if (pageTop)
                        val[AggregateDef.page] = tVal;
                    else
                        val[AggregateDef.page] += tVal;
                    break;
                case AggregateDef.min:
                    if (groupTop)
                        val[AggregateDef.group] = tVal;
                    else
                        val[AggregateDef.group] = Math.min(val[AggregateDef.group], tVal);
                    val[AggregateDef.grand] = Math.min(val[AggregateDef.grand], tVal);
                    val[AggregateDef.running] = Math.min(val[AggregateDef.grand], tVal);
                    if (pageTop)
                        val[AggregateDef.page] = tVal;
                    else
                        val[AggregateDef.page] = Math.min(val[AggregateDef.page], tVal);
                    break;
                case AggregateDef.max:
                    if (groupTop)
                        val[AggregateDef.group] = tVal;
                    else
                        val[AggregateDef.group] = Math.max(val[AggregateDef.group], tVal);
                    val[AggregateDef.grand] = Math.max(val[AggregateDef.grand], tVal);
                    val[AggregateDef.running] = Math.max(val[AggregateDef.grand], tVal);
                    if (pageTop)
                        val[AggregateDef.page] = tVal;
                    else
                        val[AggregateDef.page] = Math.max(val[AggregateDef.page], tVal);
                    break;
            }
        }
        pageTop = false;
        groupTop = false;
        tVal = Double.NaN;
    }

    /* ----------------------------------------------------------------------- reset */
    public void reset(int scope) {
        tVal = Double.NaN;
        if (scope == AggregateDef.grand) {
            for (int i = 0; i < val.length; i++) {
                val[i] = Double.NaN;
                avgCnt[i] = 0;
            }
            first = groupTop = pageTop = true;
        } else {
            if (scope == AggregateDef.page) {
                val[AggregateDef.lastRunning] = val[AggregateDef.running];
                avgCnt[AggregateDef.lastRunning] = avgCnt[AggregateDef.running];
            }
            val[scope] = Double.NaN;
            avgCnt[scope] = 0;
            if (scope == AggregateDef.group)
                groupTop = true;
            if (scope == AggregateDef.page)
                pageTop = true;
        }
    }

    /* ----------------------------------------------------------------------- getStringVal */
    public String getStringVal(int scope) {
        throw new JoriaAssertionError("Not possible for this subclass");
    }

    /* ----------------------------------------------------------------------- getLongVal */
    public long getLongVal(int scope) {
        if (Double.isNaN(val[scope]))
            return 0;
        if (Double.isInfinite(val[scope]))
            if (function == AggregateDef.min)
                return Long.MIN_VALUE;
            else
                return Long.MAX_VALUE;
        else
            return Math.round(val[scope]);
    }

    /* ----------------------------------------------------------------------- getDoubleVal */
    public double getDoubleVal(int scope) {
        if (function == AggregateDef.avg) {
            if (avgCnt[scope] == 0)
                return Double.NaN;
            else
                return val[scope] / avgCnt[scope];
        } else {
            return val[scope];
        }
    }

    /* ----------------------------------------------------------------------- add */
    public void add(double nVal) {
        Trace.check(Double.isNaN(tVal));
        tVal = nVal;
    }

    /* ----------------------------------------------------------------------- add */
    public void add(String nVal) {
        throw new JoriaAssertionError("Not possible for this subclass");
    }

    /* ----------------------------------------------------------------------- redoRow */
    public void redoRow() {
        tVal = Double.NaN;
    }
}
