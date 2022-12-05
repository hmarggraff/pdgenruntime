package org.pdgen.util;

import org.pdgen.data.NotYetImplementedError;
import org.pdgen.model.run.*;

public class RuntimeComponentFactory {
    public static RuntimeComponentFactory theInstance;

    public static RuntimeComponentFactory instance() {
        if (theInstance == null)
            theInstance = new RuntimeComponentFactory();
        return theInstance;
    }

    public FrameFlowSupport getFrameFlowSupport(float flowingFrameColumnWidth, FillPagedFrame fillPagedFrame) {
        return new FrameFlowSupport(fillPagedFrame);
    }

    public RunTemplate getFillRunner(RDRangeBase rdef, RVTemplate rvt, FillPagedFrame frame) {
        if (frame.getTemplate().isCrosstabFrame())
            throw new NotYetImplementedError("Crosstabs are a plus-feature. Please Contact Qint Software");
        else
            return new RunTemplate(frame, rvt, rdef, 0, frame.getTemplate());
    }

}
