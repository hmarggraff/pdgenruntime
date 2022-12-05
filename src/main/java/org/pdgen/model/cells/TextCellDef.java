// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.DBData;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.Trace;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.run.AggregateCollector;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class TextCellDef extends EditableCellDef {
    private static final long serialVersionUID = 7L;
    transient JTextField labelEditor;

    public TextCellDef(TemplateModel parentGrid) {
        super(parentGrid, null);
    }

    public TextCellDef(TemplateModel parentGrid, String s) {
        super(parentGrid, s);
    }

    public TextCellDef(TextCellDef from, TemplateModel parentGrid) {
        super(from, parentGrid);
    }

    public CellDef duplicate(TemplateModel newContainerGrid, Map<Object, Object> copiedReferences) {
        return new TextCellDef(this, newContainerGrid);
    }

    public JComponent getEditor(float scale) {
        Trace.logDebug(Trace.template, "getEditor");
        if (labelEditor == null)
            labelEditor = new JTextField();
        return labelEditor;
    }

    public void keepEditorResults() {
        Trace.check(labelEditor);
        myText = labelEditor.getText();
        Trace.action("keepEditorResults(\"" + myText + "\")");
        labelEditor = null;
        myWidth = Float.NaN;
        myHeight = Float.NaN;
        grid.fireChange("Text cell edited");
    }

    public String toString() {
        return "TextCellDef(" + myText + ")";
    }

    public void selectAllInEditor() {
        labelEditor.selectAll();
    }

    public void startEditing() {
        labelEditor.setText(myText);
    }

    public String getFormattedString(DBData from, AggregateCollector into) throws JoriaDataException {
        return getWrappedText(into.getRunEnv().getLocale());
        //		return Internationalisation.localize(getWrappedText(), into.getRunEnv().getLocale());
    }

    static class ScaleableTextField extends JTextField {
        float scale;

        public ScaleableTextField(float scale) {
            this.scale = scale;
        }

        public Graphics getGraphics() {
            Graphics2D g2 = (Graphics2D) super.getGraphics();
            g2.scale(scale, scale);
            return g2;
        }
    }
}
