// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.util.BucketList;


public class RVTemplate implements RVAny {
    RVAny[][] subs;
    BucketList<DrillDownLink> drillDownKeys;
    int elementCount;

    public RVTemplate(RVAny[][] subs) {
        this.subs = subs;
    }


    public int getElementCount() {
        return elementCount;
    }

    public RVAny get(int row, int col) {
        return subs[row][col];
    }

    public DrillDownLink getDrillDownKey(int at) {
        if (drillDownKeys != null) {
            if (drillDownKeys.size() == 0 && at == 0)
                return null;
            return drillDownKeys.getObject(at);
        }
        return null;
    }

    public void setDrillDownKeys(BucketList<DrillDownLink> drillDownKeys) {
        this.drillDownKeys = drillDownKeys;
    }

    public void setElementCount(int elementCount) {
        this.elementCount = elementCount;
    }

}
