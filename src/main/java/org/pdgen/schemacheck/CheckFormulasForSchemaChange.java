// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.schemacheck;

import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaClass;
import org.pdgen.data.JoriaCollection;
import org.pdgen.data.JoriaType;
import org.pdgen.data.view.IndirectAccess;
import org.pdgen.data.view.MutableAccess;
import org.pdgen.data.view.MutableView;
import org.pdgen.model.cells.CellDef;


import org.pdgen.model.PageLevelBox;
import org.pdgen.model.Repeater;
import org.pdgen.model.Template;
import org.pdgen.projection.ComputedField;
import org.pdgen.oql.OQLParser;
import org.pdgen.oql.OQLParseException;

import java.util.*;

public class CheckFormulasForSchemaChange
{
	private final StringBuffer log;
	//public static HashMap<ComputedField, Stack> allComputedFields = new HashMap<ComputedField, Stack>();
	public static HashSet<ComputedField> allComputedFields = new HashSet<>();
	public static final String erroneousFormula = "//Formula commented out, because a schema change made it unusable.";

	public CheckFormulasForSchemaChange(final StringBuffer log)
	{
		this.log = log;
	}

	public static void check(final StringBuffer log)
	{
		final CheckFormulasForSchemaChange fu = new CheckFormulasForSchemaChange(log);
		fu.updateFormulas();
	}

	void updateFormulas()
	{
		final Set<FormulaContext> oqlObjects = FormulaFinder.findAllOQLObjects();
		resurrectOrphanedFormulas(oqlObjects);

		for (FormulaContext context : oqlObjects)
		{
			Object oqlObject = context.getFormulaHolder();
			if (oqlObject instanceof PageLevelBox)
			{
				PageLevelBox box = (PageLevelBox) oqlObject;
				String expr = box.getVisibilityCondition();
				if (checkFilterString(expr, box.getRoot().getType(), context))
					box.setVisibilityCondition(newBoolExpr(expr));
			}
			else if (oqlObject instanceof ComputedField)
			{
				ComputedField cf = (ComputedField) oqlObject;
				String expr = cf.getFilter();
				if (checkFilterString(expr, cf.getDefiningClass(), context))
					cf.setExpression(newExpr(expr, cf.getType()), cf.getType());
                allComputedFields.remove(cf);
			}
			else if (oqlObject instanceof CellDef)
			{
				CellDef cd = (CellDef) oqlObject;
				String expr = cd.getVisibilityCondition();
				final JoriaType scope;
				if (cd.getRepeater() != null)
					scope = cd.getRepeater().getAccessor().getCollectionTypeAsserted().getElementType();
				else
					scope = cd.getGrid().getFrame().getRoot().getType();
				if (checkFilterString(expr, scope, context))
					cd.setVisibilityCondition(newExpr(expr));
			}
			else if (oqlObject instanceof JoriaCollection)
			{
				final JoriaCollection collection = (JoriaCollection) oqlObject;
				String expr = collection.getFilter().getOqlString();
				if (checkFilterString(expr, collection.getElementType(), context))
					collection.getFilter().setOqlString(newExpr(expr));
			}
			else if (oqlObject instanceof OqlFilterWithContext)
			{
				OqlFilterWithContext q = (OqlFilterWithContext) oqlObject;
				JoriaClass contextClass = q.getContextClass();
				String expr = q.getOqlText();
				if (checkFilterString(expr, contextClass, context))
					q.setOqlText(newExpr(expr));
			}
		}
		if (allComputedFields.size() > 0)
			System.out.println("Orphaned ComputedFields after search = " + allComputedFields.size());
		for (ComputedField fieldEntry : allComputedFields)
		{
			final ComputedField field = fieldEntry;
			try
			{
				System.out.println(field.getDefiningClass().getName() + "." + field.getName() + " --> " + field.getFilter().replace('\n', ' '));
				field.getQuery();
			}
			catch (OQLParseException e)
			{
				System.out.println(e.getMessage());
			}
		}
        allComputedFields = new HashSet<>(0); // free
	}

	/**
	 * there are computed fields, that are not properly anchored in a cell or a view any more: trying to repair them
	 * @param oqlObjects
	 */
	private void resurrectOrphanedFormulas(final Set<FormulaContext> oqlObjects)
	{
		for (FormulaContext context : oqlObjects)
		{
			Object oqlObject = context.getFormulaHolder();
			if (oqlObject instanceof ComputedField)
			{
				ComputedField cf = (ComputedField) oqlObject;
                allComputedFields.remove(cf);
			}
		}		// this leaves the orphans
		//System.out.println("allComputedFields.size() = " + allComputedFields.size());
		for (ComputedField fieldEntry : allComputedFields)
		{
			final ComputedField field = fieldEntry;
			final JoriaAccess[] accesses = field.getDefiningClass().getMembers();
			boolean inParent = false;
			boolean sameName = false;
			for (JoriaAccess access : accesses)
			{

				if (access == field)
				{
					inParent = true;
				}
				else if (access.getName().equals(field.getName()))
				{
					if (access instanceof IndirectAccess)
					{
						IndirectAccess ia = (IndirectAccess) access;
						if (ia.getBaseAccess() == field)
						{
							inParent = true;
						}
					}
					sameName = true;
				}
			}

			if (field.getDefiningClass() instanceof MutableView)
			{
				if (!inParent)
				{
					MutableView parent = (MutableView) field.getDefiningClass();
					if (!sameName)
					{
							System.out.println("Resurrected field " + field.getName());
					}
					else
					{
						final String s = MutableAccess.uniqueName(field.getName() + "_orphaned", parent);
						System.out.println("Resurrected field " + field.getName() + " under new name " + s);
						field.setName(s);
					}
					parent.addChild(field);
				}
				else
					System.out.println("Orphaned fields exists in parent! " + field.getName() + " --> parent is also orphaned.");
			}
			else if (field.getDefiningClass().isUserClass())
			{
				System.out.println("Orphaned fields in named parent! " + field.getName() + " parent= " + field.getDefiningClass().getName() + " of class " + field.getDefiningClass().getName() + " user Class: " + field.getDefiningClass().isUserClass());

			}
			// else it is a formula in the template, that is attached to its physical class: ignore
		}
	}

	private String newExpr(final String expr, JoriaType t)
	{
		if (t.isBooleanLiteral())
			return newBoolExpr(expr);
		else
			return newExpr(expr);
	}
	private String newExpr(final String expr)
	{
		return "null\n" + erroneousFormula + "\n/*\n" + expr + "\n*/";
	}
	private String newBoolExpr(final String expr)
	{
		return "true\n" + erroneousFormula + "\n/*\n" + expr + "\n*/";
	}

	boolean checkFilterString(String expr, JoriaType scope, FormulaContext ctx)
	{
		try
		{
			OQLParser.parse(expr, scope, true);
			return false;
		}
		catch (OQLParseException e)
		{
			log.append("Formula deactivated: ").append(ctx.explanation).append(" ");
			if (ctx.context instanceof CellContext)
			{
				CellContext cc = (CellContext) ctx.context;
				log.append("in Template \"").append(cc.template).append('"');
				log.append(" row ").append(cc.r).append(" col ").append(cc.c).append('.');
			}
			else if (ctx.getFormulaHolder() instanceof JoriaCollection)
			{
				log.append(ctx.context);
			}
			else if (ctx.getFormulaHolder() instanceof Repeater)
			{
				Template template = (Template) ctx.context;
				log.append(" in Template \"").append(template).append("\".");
			}
			else if (ctx.getFormulaHolder() instanceof PageLevelBox)
			{
				PageLevelBox box = (PageLevelBox) ctx.getFormulaHolder();
				Template r = (Template) ctx.context;
				log.append(" in Template \"").append(r).append('"');
			}
			else if (ctx.getFormulaHolder() instanceof ComputedField)
			{
				if (ctx.context instanceof Template)
				{
					Template template = (Template) ctx.context;
					log.append(" in Template \"").append(template).append("\".");
				}
				else
					log.append(ctx.context);
			}
			else
				log.append(ctx.context);
			log.append(" Reason: ");
			log.append(e.getMessage());
			log.append('\n');

			return true;
		}
	}
}
