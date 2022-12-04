// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;

import org.pdgen.env.JoriaViewRoot;
import org.pdgen.env.Res;

import java.util.Map;

public class MutableAccess extends DefaultAccess implements AccessPlus, JoriaViewRoot
{
    private static final long serialVersionUID = 7L;
    boolean isNameSet;
	String xmlTag;
	boolean xmlInline;
	String formatString;
	protected transient boolean definedInView;

	public MutableAccess(JoriaAccess access)
	{
		super(null, access.getType(), access);
		makeName();
		xmlTag = escape(getName());
		xmlInline = access.getType().isLiteral();
	}

	public MutableAccess(JoriaClass parent, JoriaAccess access)
	{
		super(parent, access.getType(), access);
		makeName();
		xmlTag = escape(getName());
		xmlInline = access.getType().isLiteral();
	}

	public MutableAccess(JoriaClass parent, JoriaAccess access, String uName)
	{
		super(parent, access.getType(), access);
		name = uName;
		makeLongName();
		xmlTag = escape(getName());
		xmlInline = access.getType().isLiteral();
	}

	public MutableAccess(JoriaClass parent, JoriaType type, JoriaAccess access)
	{
		super(parent, type, access);
		makeName();
		xmlTag = escape(getName());
		xmlInline = access.getType().isLiteral();
	}

	public MutableAccess(JoriaClass parent, JoriaType type, JoriaAccess access, String uName)
	{
		super(parent, type, access);
		name = uName;
		makeName();
		xmlTag = escape(getName());
		xmlInline = access.getType().isLiteral();
	}

	public MutableAccess(JoriaAccess oldAccess, JoriaType newType)
	{
		this(null, newType, oldAccess);
	}

	/**
	 * constructor only for the copy of reports. The base access will be set later by setBase
	 *
	 * @param parent class for this access (if null, root or special access)
	 * @param name   the name must be passed here because the base access is not available now
	 */
	protected MutableAccess(JoriaClass parent, String name)
	{
		super(parent, name);
		isNameSet = true;
	}

    public static String uniqueName(String sample, MutableView parent)
    {
        JoriaAccess[] members = parent.getAsParent().getMembers();
        boolean exists;
        int id = 1;
        String tName = sample;
        do
        {
            exists = false;
            for (JoriaAccess m : members)
            {
                if (tName.equals(m.getName()))
                {
                    exists = true;
                    tName = sample + (id++);
                    break;
                }
            }
        }
        while (exists);
        return tName;
    }

    /**
	 * complete the access during the copy of reports.
	 *
	 * @param baseAccess the real access
	 * @param type	   the type. If not overriden, may be null.
	 */
	protected void setBase(JoriaAccess baseAccess, JoriaType type)
	{
		if (type != null)
			super.setType(type);
		else
			super.setType(baseAccess.getType());
		myBaseAccess = baseAccess;
	}

	public NameableAccess dup(JoriaClass newParent, Map<Object,Object> alreadyCopied)
	{
		final Object duplicate = alreadyCopied.get(this);
		if (duplicate != null)
			return (NameableAccess) duplicate;

		MutableAccess ret = new MutableAccess(newParent, dupBaseAccess(newParent, alreadyCopied));
		alreadyCopied.put(this, ret);
		fillDup(ret, alreadyCopied);
		return ret;
	}

	public void fillDup(MutableAccess newAxs, Map<Object, Object> alreadyCopiedViews)
	{
		newAxs.setNameInternally(name);
		newAxs.isNameSet = isNameSet;
		newAxs.xmlInline = xmlInline;
		newAxs.xmlTag = xmlTag;
		newAxs.formatString = formatString;
		if (type instanceof ClassProjection)
		{
			dupType(newAxs, alreadyCopiedViews);
		}
	}

	protected void dupType(MutableAccess newAxs, Map<Object, Object> alreadyCopiedViews)
	{
		MutableView newType = (MutableView) alreadyCopiedViews.get(type);
		if (newType == null)
		{
			newType = ((MutableView) type).dup(alreadyCopiedViews);
		}
		newAxs.setType(newType);
		newAxs.makeLongName();
	}

	public static JoriaType getCastType(JoriaAccess c)
	{
		JoriaType t;
		while (true)
		{
			if (c instanceof CastAccess && ((CastAccess) c).getCastType() != null)
			{
				t = ((CastAccess) c).getCastType();
				break;
			}
			else if (c instanceof IndirectAccess)
				c = ((IndirectAccess) c).getBaseAccess();
			else
			{
				t = c.getType();
				break;
			}
		}
		while (t.isView())
		{
			ClassView v = (ClassView) t;
			t = v.getBase();
		}
		return t;
	}

	public void setName(String newName)
	{
		setNameInternally(newName);
		isNameSet = true;
	}

	/**
	 * Changes name without setting isNameSet
	 *
	 * @param newName the new name
	 */
	public void setNameInternally(String newName)
	{
		newName = newName.replace('<', '_');
		newName = newName.replace('>', '_');
		Trace.log(Trace.serialize, getClass().getName() + " " + getLongName() + Res.asis(" renamed to ") + newName);
		NameableTracer.notifyListenersPre(this);
		name = newName;
		makeLongName();
		NameableTracer.notifyListenersPost(this);
	}

	public void setParent(JoriaClass p)
	{
		Trace.check(p);
		definingClass = p;
	}

	public void makeName()
	{
		if (!isNameSet)
			name = myBaseAccess.getName();
		makeLongName();
		//longName = name + ": " + type.getName();
	}

	protected void makeName(String tag)
	{
		if (!isNameSet)
		{
			String ts = tag != null ? "_" + tag : "";
			name = myBaseAccess.getName() + ts;
		}
		makeLongName();
		//longName = name + ": " + type.getName();
	}

	public boolean isPlain()
	{
		boolean ret = name.equals(myBaseAccess.getName());
		ret = ret && type == myBaseAccess.getType();
		return ret;
	}

	public JoriaType getSourceTypeForChildren()
	{
		return getBaseAccess().getType();
	}

	public String getXmlTag()
	{
		return xmlTag;
	}

	public void setXmlTag(String xmlTag)
	{
		this.xmlTag = xmlTag;
	}

	public boolean isExportingAsXmlAttribute()
	{
		return xmlInline;
	}

    public void setExportingAsXmlAttribute(boolean exportingAsXmlAttribute)
	{
		xmlInline = exportingAsXmlAttribute;
	}

	public static String escape(String n)
	{
		Trace.check(n);
		Trace.check(n.length() > 0);
		StringBuffer b = new StringBuffer(n.length());
		char c = n.charAt(0);
		if (Character.isUnicodeIdentifierStart(c) || c == '_' || c == ':')
			b.append(c);
		else
			b.append('_');
		for (int i = 1; i < n.length(); i++)
		{
			c = n.charAt(i);
			if (Character.isUnicodeIdentifierPart(c) || c == '_' || c == ':' || c == '.' || c == '-')
				b.append(c);
			else
				b.append('_');
		}
		return b.toString();
	}

	public String getFormatString()
	{
		return formatString;
	}

	public void setFormatString(String formatString)
	{
		this.formatString = formatString;
	}

	public String getPhysicalRootName()
	{
		JoriaAccess st = this;
		while (st instanceof IndirectAccess)
			st = ((IndirectAccess) st).getBaseAccess();
		return st.getName();
	}

	public String getPhysicalRootTypeName()
	{
		JoriaAccess st = this;
		while (st instanceof IndirectAccess)
			st = ((IndirectAccess) st).getBaseAccess();
		JoriaType t = st.getType();
		while (t instanceof ClassView)
			t = ((ClassView) t).getBase();
		if (t instanceof CollectionWrapperClass)
		{
			CollectionWrapperClass cwc = (CollectionWrapperClass) t;
			t = cwc.getCollection().getCollectionTypeAsserted().getElementType();
		}
		while (t instanceof ClassView)
			t = ((ClassView) t).getBase();
		if (t instanceof JoriaPhysicalClass)
			return ((JoriaPhysicalClass) t).getPhysicalClassName();
		else
			return t.getName();
	}

	public JoriaType getType()
	{
		JoriaType ret = super.getType();
        if(ret == null)
            return null;
        if ((ret.isLiteral() || ret.isDate()) && getClass() == MutableAccess.class)
			ret = myBaseAccess.getType();
		return ret;
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
