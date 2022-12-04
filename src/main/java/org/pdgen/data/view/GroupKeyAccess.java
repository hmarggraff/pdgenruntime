// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

import org.pdgen.env.Res;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Map;

public class GroupKeyAccess extends AbstractMember implements AccessPlus, JoriaAccess {
    private static final long serialVersionUID = 7L;
    protected JoriaAccess myGroupingField;
	boolean isNameSet;
	boolean exportingAsXmlAttribute = true;
	String xmlTag;
	String formatString;
	protected transient boolean definedInView;

	public GroupKeyAccess(JoriaClass parent, JoriaAccess groupingField)
	{
		super(parent, groupingField.getName() + Res.asis("_key"));
		myGroupingField = groupingField;
		makeLongName();
		xmlTag = name;
	}

	private GroupKeyAccess(JoriaClass parent, String name)
	{
		super(parent, name);
	}

	public NameableAccess dup(JoriaClass newParent, Map<Object,Object> alreadyCopied)
	{
		final Object duplicate = alreadyCopied.get(this);
		if (duplicate != null)
			return (NameableAccess) duplicate;
		GroupKeyAccess ret = new GroupKeyAccess(newParent, myGroupingField);
		alreadyCopied.put(this, ret);
		ret.isNameSet = isNameSet;
		ret.exportingAsXmlAttribute = exportingAsXmlAttribute;
		ret.xmlTag = xmlTag;
		ret.formatString = formatString;
		return ret;
	}
	public GroupKeyAccess copyReportPrivateAccess(JoriaClass newParent, Map<Object,Object> alreadyCopied)
	{
		final Object duplicate = alreadyCopied.get(this);
		if (duplicate != null)
			return (GroupKeyAccess) duplicate;
		GroupKeyAccess ret = new GroupKeyAccess(newParent, name);
		alreadyCopied.put(this, ret);
		ret.myGroupingField = myGroupingField;
		ret.name = name;
		ret.longName = longName;
		ret.isNameSet = isNameSet;
		ret.exportingAsXmlAttribute = exportingAsXmlAttribute;
		ret.xmlTag = xmlTag;
		ret.formatString = formatString;
		return ret;
	}

	public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException
	{
		Trace.check(from, DBGroup.class);
		DBGroup gf = (DBGroup) from;
		return gf.getKeyValue();
	}

	public JoriaType getType()
	{
		return myGroupingField.getType();
	}

	public JoriaType getSourceTypeForChildren()
	{
		return myGroupingField.getType();
	}

	public void makeName()
	{
		if (isNameSet)
			makeLongName();
		else
			makeNames(myGroupingField.getName(), Res.asis("key"));
	}

	public void setExportingAsXmlAttribute(boolean newValue)
	{
		exportingAsXmlAttribute = newValue;
	}

	public void setName(String newName)
	{
		NameableTracer.notifyListenersPre(this);
		name = newName;
		isNameSet = true;
		makeName();
		NameableTracer.notifyListenersPost(this);
	}

	public void setNameInternally(String newName)
	{
		if (isNameSet)
			return;
		NameableTracer.notifyListenersPre(this);
		name = newName;
		makeName();
		NameableTracer.notifyListenersPost(this);
	}

	public void setXmlTag(String newTag)
	{
		xmlTag = newTag;
	}

	public JoriaAccess getBaseAccess()
	{
		return myGroupingField;
	}

	public void unbind()
	{
		myGroupingField = new UnboundAccessSentinel(myGroupingField);
	}

	public boolean unbound()
	{
		return myGroupingField instanceof UnboundAccessSentinel;
	}

	public boolean bindableTo(JoriaAccess newBinding, JoriaAccess newParentBinding)
	{
		return ((UnboundAccessSentinel) myGroupingField).isBindable(newBinding);
	}

	public void rebind(JoriaAccess newBinding, JoriaAccess newParentBinding)
	{
		Trace.check(((UnboundAccessSentinel) myGroupingField).isBindable(newBinding));
		myGroupingField = newBinding;
		((GroupingAccess) newParentBinding).modifyGroupingKey(newBinding);
	}

    public void collectVariables(Set<RuntimeParameter> s, Set<Object> seen)
	{
	}

	public void collectI18nKeys2(HashMap<String,List<I18nKeyHolder>> s, Set<Object> seen)
	{
	}

	public String getFormatString()
	{
		return formatString;
	}

	public void setFormatString(String formatString)
	{
		this.formatString = formatString;
	}

	public void collectVisiblePickersInScope(List<JoriaAccess[]> collection, Set<RuntimeParameter> visible, Stack<JoriaAccess> pathStack, Set<Object> seen)
	{
		// nothing to do
	}

	public void modifyBase(JoriaAccess newKey)
	{
		myGroupingField = newKey;
		makeName();
		xmlTag = name;
	}

    public JoriaAccess getRootAccess()
    {
        return getBaseAccess();
    }
    
    public boolean visitAllAccesses(AccessVisitor visitor, Set<JoriaAccess> seen)
    {
        if(seen.contains(this))
            return true;
        seen.add(this);
        if(!visitor.visit(myGroupingField))
            return false;
        if(myGroupingField instanceof VisitableAccess)
        {
			return ((VisitableAccess) myGroupingField).visitAllAccesses(visitor, seen);
        }
        return true;
    }
	public boolean isDefinedInView()
	{
		return definedInView;
	}

	public void setDefinedInView(final boolean definedInView)
	{
		this.definedInView = definedInView;
	}

}
