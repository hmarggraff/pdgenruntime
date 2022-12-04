// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.I18nKeyHolder;
import org.pdgen.data.Internationalisation;
import org.pdgen.data.Internationalisation2;
import org.pdgen.model.TemplateModel;

import javax.swing.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public abstract class EditableCellDef extends SimpleTextCellDef implements I18nKeyHolder
{
    private static final long serialVersionUID = 7L;

    public EditableCellDef(TemplateModel parentGrid, String s)
    {
        super(parentGrid, s);
    }

    public EditableCellDef(EditableCellDef from, TemplateModel parentGrid)
    {
        super(from, parentGrid);
    }

    public abstract JComponent getEditor(float scale);

    public abstract void keepEditorResults();

    public abstract void startEditing();

    public abstract void selectAllInEditor();
    public void collectI18nKeys(HashSet<String> keySet)
    {
        collectI18nKeysInLocalStyle(keySet);
        Internationalisation.collectI18nKeys(myText, keySet);
    }
    public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> keySet)
    {
        Internationalisation2.collectI18nKeys(myText, keySet, this);
        cascadedStyle = null;
        collectI18nKeysInLocalStyle2(keySet);
    }

    public void setI18nKey(String newVal)
    {
        myText = newVal;
        myHeight = myWidth = Float.NaN;
        grid.fireChange("New value set from I18n Key Manager");
    }
}
