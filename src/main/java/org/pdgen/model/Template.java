// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.data.view.*;
import org.pdgen.model.style.CellStyle;
import org.pdgen.projection.MaDetElementAccess;


import org.pdgen.env.Env;
import org.pdgen.oql.OQLParseException;

import javax.print.attribute.standard.Media;
import java.io.Serializable;
import java.util.*;

public class Template implements Serializable, Nameable
{
	public static final int objectReport = 0;
	public static final int listReport = 1;
	public static final int masterDetailReport = 2;
    private static final long serialVersionUID = 7L;
    String name;
	String description;
	PageMaster page;
	Template nextSection;
	int reportType = objectReport;
	private String dokuFile;
	RuntimeParameter[] interactiveSequence;
	int number;
	protected boolean isDrillDown;
	protected Map<String, Object> outputConfiguration;
	protected Media firstPageMedia;
	protected Media furtherPageMedia;
	protected boolean restartNumbering;
	private boolean testReport;

	public Template duplicate(boolean newName)
	{
		String n;
		if (newName)
		{
			SortedNamedVector<Template> rp = Env.instance().repo().reports;
			int nn = 1;
			do
			{
				n = name + " (" + nn + ")";
				nn++;
			}
			while (rp.find(n) != null);
		}
		else
			n = name;
		Template ret = new Template(n, page.duplicate(new HashMap<Object, Object>()));
		ret.description = description;
		ret.reportType = reportType;
		ret.isDrillDown = isDrillDown;
		// TODO ret.outputConfiguration
		// TODO ret.interactiveSequence
		ret.firstPageMedia = firstPageMedia;
		ret.furtherPageMedia = furtherPageMedia;
		ret.restartNumbering = restartNumbering;
		if (newName)   // normal duplicate
		{
			ret.nextSection = nextSection; // next section is not duplicated but just referenced
		}
		else   // duplicate for undo
		{
			ret.nextSection = null; // next section is set to null for undo
			ret.getPage().lastEditTime = getPage().lastEditTime;
			ret.number = number;
		}
		return ret;
	}

	protected Template(String name, PageMaster page)
	{
		this.name = name;
		this.page = page;
		number = Env.instance().repo().nextReportNumber();
	}

	public int getReportType()
	{
		return reportType;
	}

	public void setReportType(int reportType)
	{
		this.reportType = reportType;
		if (reportType == masterDetailReport)
			restartNumbering = true;
		Env.repoChanged();
	}

	public String getDescription()
	{
		return description;
	}

	public String getName()
	{
		return name;
	}

	public PageMaster getPage()
	{
		return page;
	}

	public JoriaAccess getStarter()
	{
		return page.getData();
	}

	public JoriaAccess getStarterForView()
	{
		if (reportType == masterDetailReport)
		{
			final JoriaAccess data = page.getData();
			if (data instanceof AccessPath)
			{
				AccessPath accessPath = (AccessPath) data;
				final MaDetElementAccess last = (MaDetElementAccess) accessPath.getPathComponent(accessPath.getPathLength() - 1);
				return last.getBaseAccess();
			}
			else
			{
				MaDetElementAccess elax = (MaDetElementAccess) data;// may throw up: Wait and see
				return elax.getBaseAccess();
			}
		}
		else
			return page.getData();
	}

	public JoriaAccess getStarterForContinuation()
	{
		if (reportType == masterDetailReport)
		{
			final JoriaAccess data = page.getData();
			if (data instanceof AccessPath)
			{
				AccessPath accessPath = (AccessPath) data;
				return accessPath.getPathComponent(0);
			}
			else
			{
				MaDetElementAccess elax = (MaDetElementAccess) data;// may throw up: Wait and see
				return elax.getBaseAccess();
			}
		}
		else
			return page.getData();
	}

	public void setDescription(String newDescription)
	{
		description = newDescription;
		Env.repoChanged();
	}

	public void setName(String newName)
	{
		NameableTracer.notifyListenersPre(this);
		name = newName;
		Env.repoChanged();
		NameableTracer.notifyListenersPost(this);
	}

	/**
	 * ----------------------------------------------------------------------- toString
	 */
	public String toString()
	{
		return getName();
	}

	public String forType()
	{
		return getStarter().getType().getName();
	}

	public static Template createStandardReport(JoriaAccess root, String name, Template from)
	{
		Template r;
		if (from == null)
		{
			r = new Template(name, PageMaster.createStandardPage(root));
		}
		else
		{
			final JoriaAccess ja = from.getPage().getData();
			if (ja == root)
			{
				r = from.duplicate(true);
			}
			else
			{
				r = from.makeUnboundCopy();
			}
			r.setName(name);
			r.page.setData(root);
		}
		return r;
	}

	public boolean fixAccess()
	{
		return page.fixAccess();
	}

	public Set<RuntimeParameter> getVariables()
	{
		HashSet<RuntimeParameter> hashSet = new HashSet<RuntimeParameter>();
		HashSet<Object> seen = new HashSet<Object>();
		Template tarzan = this;
		while (tarzan != null)
		{
			hashSet.addAll(tarzan.page.getVariables(seen));
			tarzan = tarzan.nextSection;
		}
		return hashSet;
	}

	public String getAccessName()
	{
		JoriaAccess st = getStarter();
		if (st == null)
			return null;
		return st.getName();
	}

	public String getRootClassName()
	{
		JoriaAccess st = getStarter();
		if (st == null)
			return null;
		return st.getType().getName();
	}

	public boolean isCollectionReport()
	{
		JoriaAccess st = getStarter();
		if (st == null)
			return false;
		while (st instanceof IndirectAccess)
		{
			st = ((IndirectAccess) st).getBaseAccess();
		}
		JoriaType t = st.getType();
		while (t instanceof ClassView)
		{
			t = ((ClassView) t).getBase();
		}
		return t instanceof CollectionWrapperClass || t.isCollection();
	}

	public String getPhysicalRootTypeName()
	{
		JoriaAccess st = getStarter();
		if (st == null)
			return null;
		while (st instanceof IndirectAccess)
		{
			st = ((IndirectAccess) st).getBaseAccess();
		}
		JoriaType t = st.getType();
		while (t instanceof ClassView)
		{
			t = ((ClassView) t).getBase();
		}
		if (t instanceof CollectionWrapperClass)
		{
			CollectionWrapperClass cwc = (CollectionWrapperClass) t;
			t = cwc.getCollection().getCollectionTypeAsserted().getElementType();
		}
		while (t instanceof ClassView)
		{
			t = ((ClassView) t).getBase();
		}
		if (t instanceof JoriaPhysicalClass)
			return ((JoriaPhysicalClass) t).getPhysicalClassName();
		else
			return t.getName();
	}

	public String getDokuFile()
	{
		return dokuFile;
	}

	public void setDokuFile(String dokuFile)
	{
		this.dokuFile = dokuFile;
	}

	public RuntimeParameter[] getInterviewSequence()
	{
		return interactiveSequence;
	}

	public void setInteractiveSequence(RuntimeParameter[] interactiveSequence)
	{
		this.interactiveSequence = interactiveSequence;
		Env.repoChanged();
	}

	public Template makeUnboundCopy()
	{
		SortedNamedVector<Template> rp = Env.instance().repo().reports;
		int nn = 1;
		String n = "Neutralized Copy of " + name;
		while (rp.find(n) != null)
		{
			n = "Neutralized Copy " + (nn++) + " of " + name;
		}
		Template ret = new Template(n, page.duplicate(new HashMap<Object, Object>()));
		ret.description = description;
		ret.reportType = reportType;
		ret.restartNumbering = restartNumbering;
		ret.getPage().makeUnbound();
		return ret;
	}

	public void makeUnbound()
	{
		getPage().makeUnbound();
	}

	public void collectI28nKeys2(HashMap<String, List<I18nKeyHolder>> keySet)
	{
		Internationalisation2.collectI18nKeys(name, keySet, new MyI18nKeyHolder());
	}

	public void setPreviewIcon(byte[] bytes)
	{
		//this.previewIcon = bytes;
	}

	public byte[] getPreviewIcon()
	{
		//return previewIcon;
		return null;
	}

	public void fixVisibiltyCondition()
	{
		page.fixVisibiltyCondition();
	}

	public int testCellStyleForSpanCollision(CellStyle cs, int newHSpan, int newVSpan)
	{
		return page.testCellStyleForSpanCollision(cs, newHSpan, newVSpan);
	}

	public void makeFilenameRelative(JoriaFileService fs)
	{
		dokuFile = fs.makeFilenameRelative(dokuFile);
		page.makeFilenameRelative(fs);
	}

	public void makeFilenameAbsolute(JoriaFileService fs)
	{
		dokuFile = fs.makeFilenameAbsolute(dokuFile);
		page.makeFilenameAbsolute(fs);
	}

	public void getAllReferencedFiles(HashSet<String> ret)
	{
		if (dokuFile != null)
		{
			if (dokuFile.startsWith("file:/"))
				ret.add(dokuFile.substring(6));
			else
				ret.add(dokuFile);
		}
		page.getAllReferencedFiles(ret);
	}

	public boolean visitCells(CellVisitor cellVisitor)
	{
		return page.visitCells(cellVisitor);
	}

	public boolean visitRepeaters(RepeaterVisitor repVisitor)
	{
		return page.visitRepeater(repVisitor);
	}

	public boolean visitFrames(FrameVisitor frameVisitor)
	{
		return page.visitFrames(frameVisitor);
	}

	public void setNextSection(Template template)
	{
		nextSection = template;
		Env.repoChanged();
	}

	public boolean isTestReport()
	{
		return testReport;
	}

	public void setTestReport(boolean testReport)
	{
		this.testReport = testReport;
	}

	class MyI18nKeyHolder implements I18nKeyHolder
	{
		public void setI18nKey(String newVal)
		{
			setName(newVal);
		}
	}


	public void collectViewUsage(Map<MutableView, Set<Object>> viewUsageMap, Set<MutableView> visitedViews)
	{
		if (page.getData() instanceof CollectUsedViewsAccess)
			((CollectUsedViewsAccess) page.getData()).collectViewUsage(viewUsageMap, visitedViews);
		if (page.getFirstPageHeader() != null)
			page.getFirstPageHeader().collectViewUsage(viewUsageMap, visitedViews);
		if (page.getFurtherPagesHeader() != null && page.getFirstPageHeader() != page.getFurtherPagesHeader())
			page.getFurtherPagesHeader().collectViewUsage(viewUsageMap, visitedViews);
		if (page.getFirstPageFooter() != null)
			page.getFirstPageFooter().collectViewUsage(viewUsageMap, visitedViews);
		if (page.getMiddlePagesFooter() != null && page.getFirstPageFooter() != page.getMiddlePagesFooter())
			page.getMiddlePagesFooter().collectViewUsage(viewUsageMap, visitedViews);
		if (page.getLastPageFooter() != null && page.getFirstPageFooter() != page.getLastPageFooter() && page.getMiddlePagesFooter() != page.getLastPageFooter())
			page.getLastPageFooter().collectViewUsage(viewUsageMap, visitedViews);
		ArrayList<PageLevelBox> frames = page.getFrames();
		for (PageLevelBox frame : frames)
		{
			frame.collectViewUsage(viewUsageMap, visitedViews);
		}
	}

	public void collectExternalFiles(ExternalFileUsage results)
	{
		if (dokuFile != null)
		{
			results.add(dokuFile, ExternalFileUsage.doc, getName());
		}
		page.collectExternalFiles(results, this);
	}

	public boolean isDrillDown()
	{
		return isDrillDown;
	}

	public void setDrillDown(boolean drillDown)
	{
		isDrillDown = drillDown;
	}

	public JoriaAccess getData()
	{
		return getPage().getData();
	}

	public JoriaAccess getRootAccess()
	{
		JoriaAccess access = getPage().getData();
		while (access instanceof RootGetableAccess)
		{
			access = ((RootGetableAccess) access).getRootAccess();
		}
		return access;
	}

	public Template getNextSection()
	{
		return nextSection;
	}

	public Object getOutputConfiguration(String key)
	{
		if (outputConfiguration == null)
			return null;
		else
			return outputConfiguration.get(key);
	}

	public void putOutputConfiguration(String key, Object value)
	{
		if (outputConfiguration == null)
			outputConfiguration = new HashMap<String, Object>();
		outputConfiguration.put(key, value);
	}

	public void removeOutputConfiguration(String key)
	{
		if (outputConfiguration != null)
			outputConfiguration.remove(key);
	}

	public Set<JoriaAccess> getUsedAccessors() throws OQLParseException
	{
		Set<JoriaAccess> ret = new HashSet<JoriaAccess>();
		page.getUsedAccessors(ret);
		return ret;
	}

	public synchronized HashMap<JoriaClass, List<JoriaAccess>> getPhysicalAccessors()
	{
		//		if (physicalAccessors == null)
		//		{
		if (getData() == null)// neutralized report has no physical accessors
		{
			return new HashMap<JoriaClass, List<JoriaAccess>>();
		}
		final Set<JoriaAccess> physicalAxss = new HashSet<JoriaAccess>();
		HashSet<JoriaAccess> seen = new HashSet<JoriaAccess>();
		Template r = this;
		do
		{
			r.page.visitAllAccessors(new AccessVisitor()
			{
				public boolean visit(JoriaAccess access)
				{
					if (access instanceof PhysicalAccess)
						physicalAxss.add(access);
					return true;
				}

				public boolean stopAccessSearchOnError()
				{
					return true;
					//throw new JoriaAssertionError("Error in formula found when it is too late. Template cannot be run.");
				}
			}, seen);
			r = r.nextSection;
		}
		while (r != null);
		HashMap<JoriaClass, List<JoriaAccess>> physicalAccessors = new HashMap<JoriaClass, List<JoriaAccess>>();
		for (JoriaAccess joriaAccess : physicalAxss)
		{
			JoriaClass cls = joriaAccess.getDefiningClass();
			List<JoriaAccess> forClassList = physicalAccessors.get(cls);
			if (forClassList == null)
			{
				forClassList = new ArrayList<JoriaAccess>();
				physicalAccessors.put(cls, forClassList);
			}
			forClassList.add(joriaAccess);
		}
		//        }
		return physicalAccessors;
	}

	public int getNumber()
	{
		return number;
	}

	public void setNumber(int number)
	{
		this.number = number;
	}

	public Media getFirstPageMedia()
	{
		return firstPageMedia;
	}

	public void setFirstPageMedia(Media firstPageMedia)
	{
		this.firstPageMedia = firstPageMedia;
		Env.repoChanged();
	}

	public Media getFurtherPageMedia()
	{
		return furtherPageMedia;
	}

	public void setFurtherPageMedia(Media furtherPageMedia)
	{
		this.furtherPageMedia = furtherPageMedia;
		Env.repoChanged();
	}

	public boolean isRestartNumbering()
	{
		return restartNumbering;
	}

	public void setRestartNumbering(boolean restartNumbering)
	{
		this.restartNumbering = restartNumbering;
	}
}

