// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.data.*;
import org.pdgen.data.view.AggregateDef;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.run.RunEnv;
import org.pdgen.model.style.CellStyle;
import org.pdgen.model.style.FrameStyle;

import org.pdgen.oql.OQLParseException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

public interface TemplateBoxInterface extends Serializable, ConditionalVisibility
{
	int nestedBox = 1;
	int placedBox = 2;
	int pageBodyBox = 3;
	int firstPageHeader = 4;
	int furtherPagesHeader = 5;
	int firstPageFooter = 6;
	int middlePagesFooter = 7;
	int lastPageFooter = 8;
	String[] boxNames = {"unknownFirst", "nested", "posel", "*Body", "*HeaderFirst", "*HeaderFollowing", "*FooterFirst", "*FooterMiddle", "*FooterLast", "unknownLast"};

	PageLevelBox getPageLevelParent();

	TemplateBoxInterface getTopLevelParent();

	FrameStyle getCascadedFrameStyle();

	void cascadeCellStyle(CellStyle aCellStyle);

	FrameStyle getFrameStyle();

	int getBoxType();

	String getBoxTypeName();

	TemplateModel getTemplate();

	void setFrameStyle(FrameStyle newFrameStyle);

	void namedFrameStyleChanged(FrameStyle f);

	void clearCachedStyles();

	void setTemplate(TemplateModel newtemplate);

	void removeObsoleteAggregates(Collection<? extends CellDef> cells, Collection<AggregateDef> obsoleteAggregates);

	CellStyle getCascadedCellStyle();

	void fixAccess();

	void collectVariables(Set<RuntimeParameter> variables, Set<Object> seen);

	void fixVisibiltyCondition();

	boolean isVisible(RunEnv env, DBData from) throws JoriaDataException;

	boolean isVisibleDeferred(RunEnv env) throws JoriaDataException;

	void getUsedAccessors(Set<JoriaAccess> ret) throws OQLParseException;

	boolean visit(FrameVisitor frameVisitor) throws IOException;

	boolean isHeaderOrFooter();

	void prerun(RunEnv env);

	void postrun(RunEnv env);

	boolean isFooter();

	boolean isHeader();

	void makeFilenameRelative(JoriaFileService fs);

	void makeFilenameAbsolute(JoriaFileService fs);

	boolean visitAccess(AccessVisitor visitor, Set<JoriaAccess> seen)  throws OQLParseException;

	JoriaAccess getRoot();
}
