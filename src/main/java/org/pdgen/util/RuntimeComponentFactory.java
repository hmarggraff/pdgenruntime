// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.util;

import org.pdgen.model.run.FillPagedFrame;
import org.pdgen.model.run.FrameFlowSupport;
import org.pdgen.model.run.MultiColFrameSupport;

public class RuntimeComponentFactory {
    public static RuntimeComponentFactory instance;
    public FrameFlowSupport getFrameFlowSupport(float flowingFrameColumnWidth, FillPagedFrame fillPagedFrame) {
        if (!fillPagedFrame.getTemplate().getFrame().getCascadedFrameStyle().hasFlowingColumns())// no flowing columns: dont set up support
            return new FrameFlowSupport(fillPagedFrame);
        else
            return new MultiColFrameSupport(fillPagedFrame, flowingFrameColumnWidth);
    }
}
