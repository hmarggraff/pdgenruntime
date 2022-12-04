// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;
//MARKER The strings in this file shall not be translated
import org.pdgen.model.TemplateModel;

import java.util.Map;


public class MetaValueCell extends SimpleTextCellDef
{
    private static final long serialVersionUID = 7L;
    CellDef definition;
   /** ----------------------------------------------------------------------- MetaValueCell */

   public MetaValueCell(TemplateModel parentGrid, String txt, SimpleTextCellDef definition)
   {
	  super(parentGrid, txt);
	  myStyle       = definition.getStyle();
	  cascadedStyle = definition.cascadedStyle;
	  this.definition = definition;
   }

   public MetaValueCell(SimpleTextCellDef from, TemplateModel parentGrid)
   {
	  super(from, parentGrid);
   }


	public CellDef duplicate(TemplateModel newContainerGrid, Map<Object,Object> copiedReferences)
	{
		return new MetaValueCell(this, newContainerGrid);
	}

	public String toString()
   {
	  return "MetaValueCell(" + myText + ")";
   }
}
