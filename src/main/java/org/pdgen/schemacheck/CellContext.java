// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.schemacheck;

import org.pdgen.model.Template;
import org.pdgen.model.TemplateBoxInterface;

public class CellContext
{
	Template template;
	TemplateBoxInterface frame;
	int r, c;

	public CellContext(final Template template, final TemplateBoxInterface frame, final int r, final int c)
	{
		this.template = template;
		this.frame = frame;
		this.r = r;
		this.c = c;
	}
}
