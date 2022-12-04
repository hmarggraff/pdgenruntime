// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;

import org.pdgen.env.Env;
import org.pdgen.env.RepoLoader;
import org.pdgen.schemacheck.ViewChecker;
import org.pdgen.env.Res;

import java.util.*;

import static org.pdgen.data.Trace.logDebug;

public class ClassProjection implements ClassView, PushOnStack, Nameable
{
    private static final long serialVersionUID = 7L;
    protected String myName;
	protected JoriaAccess[] members = JoriaClass.noMembers;
	protected JoriaClass base;

	public ClassProjection(JoriaClass base)
	{
		Trace.check(base, "base may not be null to create a projection from it");
		this.base = base;
	}

	public ClassProjection(JoriaClass base, String name, boolean createSibling)
	{
		if (createSibling && base.isView())
			this.base = ((ClassView) base).getBase();
		else
			this.base = base;
		myName = name;
		if (name != null)
		{
			JoriaClass physical = this.base;
			while (physical instanceof ClassProjection)
				physical = ((ClassProjection) physical).getPhysicalClass();
			Env.instance().repo().classProjections.add(this);
		}
	}

	public MutableView dup(Map<Object, Object> alreadyCopiedViews)
	{
		final Object duplicate = alreadyCopiedViews.get(this);
		if (duplicate != null)
			return (MutableView) duplicate;
		ClassProjection ret = new ClassProjection(this, null, true);
		alreadyCopiedViews.put(this, ret);
		dup(ret, alreadyCopiedViews);
		return ret;
	}

	public void dup(ClassProjection ret, Map<Object, Object> alreadyCopiedViews)
	{
		ret.members = new JoriaAccess[members.length];
		for (int i = 0; i < members.length; i++)
		{
			NameableAccess member = (NameableAccess) members[i];
			NameableAccess nMember = member.dup(ret, alreadyCopiedViews);
			ret.members[i] = nMember;
		}
	}

	public void collectUsedViews(Set<MutableView> s)
	{
		logDebug(Trace.copy, "collectUsedViews classProjection1: " + getName());
		if (s.contains(this))
		{
			logDebug(Trace.copy, "collectUsedViews found");
			return;
		}
		s.add(this);
		for (JoriaAccess member : members)
		{
			if (member instanceof MutableAccess)
				((MutableAccess) member).collectUsedViews(s);
		}
		if (base instanceof MutableView)
			((MutableView) base).collectUsedViews(s);
	}

	public void collectViewUsage(Map<MutableView, Set<Object>> viewUsage, Set<MutableView> visitedViews)
	{
		logDebug(Trace.copy, "collectUsedViews classProjection1: " + getName());
		if (visitedViews.contains(this))
		{
			logDebug(Trace.copy, "collectUsedViews found");
			return;
		}
		visitedViews.add(this);
		for (JoriaAccess member : members)
		{
			if (member instanceof MutableAccess)
				((MutableAccess) member).collectViewUsage(viewUsage, visitedViews);
		}
		if (base instanceof MutableView)
			((MutableView) base).collectViewUsage(viewUsage, visitedViews);
	}

	public boolean hasName()
	{
		return myName != null;
	}

	/**
	 * Add a Fieldaccess to this ClassProjection (ClassView)
	 * Child is added at the end.
	 */
	public void addChild(JoriaAccess f)
	{
		if (f.getDefiningClass() != null && f.getDefiningClass() != this)
			throw new JoriaAssertionError("while adding " + f.getName() + " to " + getName() + " the defining class is " + f.getDefiningClass().getName());
		JoriaAccess[] tMembers = new JoriaAccess[members.length + 1];
		int j = 0;
		while (j < members.length)
		{
			JoriaAccess m = members[j];
			if (m.getName().compareToIgnoreCase(f.getName()) < 0)
				tMembers[j] = m;
			else
				break;
			j++;
		}
		tMembers[j] = f;
		if (j <= members.length-1)
			System.arraycopy(members, j, tMembers, j + 1, members.length - j);
		members = tMembers;
	}

	public void setMembers(JoriaAccess[] members)
	{
		this.members = members;
	}

	public int shiftChild(JoriaAccess f, int at)
	{
		Trace.check(at <= members.length);
		for (int i = 0; i < members.length; i++)
		{
			if (members[i] == f)
			{
				if (i == at)
					return at;// nothing to do
				else if (i < at)
					System.arraycopy(members, i + 1, members, i, at - i);
				else
					System.arraycopy(members, at, members, at + 1, i - at);
				members[at] = f;
				Env.repoChanged();
				return i;
			}
		}
		throw new JoriaAssertionError("Field " + f.getLongName() + Res.asis(" not found for shift"));
	}

	public static ClassProjection copyLiterals(JoriaClass c)
	{
		ClassProjection myProjection = new ClassProjection(c);
		JoriaAccess[] ms = c.getFlatMembers();
		ArrayList<JoriaAccess> l = new ArrayList<JoriaAccess>();
		for (JoriaAccess m : ms)
		{
			if (m.getType().isLiteral() || m.getType().isDate())
			{
				MutableAccess pm = new MutableAccess(myProjection, m);
				l.add(pm);
			}
		}
		myProjection.members = new JoriaAccess[l.size()];
		l.toArray(myProjection.members);
		return myProjection;
	}

	public JoriaAccess findMember(String name)
	{
		JoriaAccess[] mm = getMembers();
		for (final JoriaAccess mmi : mm)
		{
			if (mmi != null && mmi.getName().equals(name))
			{
				return mmi;
			}
		}
		return null;
	}

	public JoriaClass getBase()
	{
		return base;
	}

	public JoriaType getOriginalType()
	{
		return base;
	}

	public JoriaClass[] getBaseClasses()
	{
		return JoriaClass.noClasses;
	}

	public ArrayList<JoriaClass> getDerivedClasses()
	{
		return null;
	}

	public JoriaAccess[] getFlatMembers()
	{
		return getMembers();
	}

	public JoriaAccess findMemberIncludingSuperclass(String name)
	{
		return findMember(name);
	}

	public JoriaAccess[] getMembers()
	{
		return members;
	}

	public String getName()
	{
		if (myName == null)
			return Res.asis("V_") + base.getName();
		return myName;
	}

	public String getParamString()
	{
		return getClass().toString() + "[" + myName + ", " + base.getParamString() + "]";
	}

	public int indexOfMember(JoriaAccess a)
	{
		for (int i = 0; i < members.length; i++)
		{
			if (a == members[i])
				return i;
		}
		return -1;
	}

	public boolean isBlob()
	{
		return false;
	}

	public boolean isClass()
	{
		return true;
	}

	public boolean isCollection()
	{
		return false;
	}

	public boolean isDictionary()
	{
		return getPhysicalClass().isDictionary();
	}

	public boolean isInternal()
	{
		return false;
	}

	public boolean isJavaPrimitive()
	{
		return false;
	}

	public boolean isLiteral()
	{
		return false;
	}

	public boolean isUnknown()
	{
		return getPhysicalClass().isUnknown();
	}

	public boolean isUserClass()
	{
		return true;
	}

	public boolean isView()
	{
		return true;
	}

	public boolean isVoid()
	{
		return true;
	}

	public void removeChild(JoriaAccess f)
	{
		JoriaAccess[] tMembers = new JoriaAccess[members.length - 1];
		int tIndex = 0;
		for (JoriaAccess member : members)
		{
			if (member != f)
			{
				tMembers[tIndex] = member;
				tIndex++;
			}
		}
		members = tMembers;
		Env.repoChanged();
	}

	public void replaceChild(JoriaAccess f, JoriaAccess fNew)
	{
		for (int i = 0; i < members.length; i++)
		{
			JoriaAccess member = members[i];
			if (member == f)
			{
				members[i] = fNew;
				Env.repoChanged();
				return;
			}
		}
		throw new JoriaAssertionError("ClassProjection.replaceChild did not find member " + f.getLongName());
	}

	public void setName(String name)
	{
		NameableTracer.notifyListenersPre(this);
		myName = name;
		NameableTracer.notifyListenersPost(this);
		Env.repoChanged();
	}

	public String toString()
	{
		if (getName() == null)
			return base.toString();
		return getName();
	}

	public JoriaClass getAsParent()
	{
		return this;
	}

	public void setBase(JoriaClass c)
	{
		base = c;
	}

	public boolean isBooleanLiteral()
	{
		return false;
	}

	public boolean isCharacterLiteral()
	{
		return false;
	}

	public boolean isIntegerLiteral()
	{
		return false;
	}

	public boolean isRealLiteral()
	{
		return false;
	}

	public boolean isStringLiteral()
	{
		return false;
	}

	public boolean isDate()
	{
		return base != null && base.isDate();
	}

	public boolean isImage()
	{
		return false;
	}

	public boolean fixAccess()
	{
		int nulledCnt = 0;
		for (int i = 0; i < members.length; i++)
		{
			JoriaAccess m = members[i];
			if (m == null)
				return false;
			if (m instanceof JoriaPlaceHolderAccess)
			{
				nulledCnt++;
				members[i] = null;
				Env.instance().repo().logFix(Res.asis("View"), m, Res.asis("Changed Member removed from view"));
				Env.repoChanged();
				// forget this member
			}
			else
			{
				JoriaAccess fixed = m.getPlaceHolderIfNeeded();
				if (fixed instanceof JoriaPlaceHolderAccess)
				{
					Env.instance().repo().logFix(Res.asis("View"), m, Res.asis("Changed Member removed from view"));
					nulledCnt++;
					members[i] = null;
					// forget this member
					Env.repoChanged();
				}
				else if (fixed != null)
				{
					Env.repoChanged();
					members[i] = fixed;
				}
			}
		}
		if (nulledCnt > 0)
		{
			JoriaAccess[] newMembers = new JoriaAccess[members.length - nulledCnt];
			int newIx = 0;
			for (JoriaAccess m : members)
			{
				if (m != null)
				{
					newMembers[newIx] = m;
					newIx++;
				}
			}
			members = newMembers;
		}
		return false;// still usable (somewhat)
	}

	public void collectVisiblePickersInScope(List<JoriaAccess[]> collection, Set<RuntimeParameter> visible, Stack<JoriaAccess> pathStack, Set<Object> seen)
	{
		if (seen.contains(this))
			return;
		else
			seen.add(this);
		for (JoriaAccess member : members)
		{
			if (member instanceof VariableProvider)
			{
				pathStack.push(member);
				((VariableProvider) member).collectVisiblePickersInScope(collection, visible, pathStack, seen);
				pathStack.pop();
			}
		}
	}

	public void collectVariables(Set<RuntimeParameter> r, Set<Object> seen)
	{
		if (seen.contains(this))
			return;
		seen.add(this);
		for (JoriaAccess access : members)
		{
			if (access instanceof VariableProvider)
			{
				((VariableProvider) access).collectVariables(r, seen);
			}
		}
	}

	public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> s, Set<Object> seen)
	{
		if (seen.contains(this))
			return;
		seen.add(this);
		for (JoriaAccess access : members)
		{
			if (access instanceof VariableProvider)
			{
				((VariableProvider) access).collectI18nKeys2(s, seen);
			}
		}
	}

	public boolean isLiteralCollection()
	{
		return false;
	}

	public void checkMemberLinks()
	{
		for (int i = 0; i < members.length; i++)
		{
			JoriaAccess m = members[i];
			if (m.getDefiningClass() == null)
			{
				if (m instanceof AbstractMember)
				{
					Trace.logError("Member reparented: " + getName() + "." + m.getName() + " " + m.getClass());
					((AbstractMember) m).reparent(this);
				}
				else
					Trace.logError("Member unparented: " + getName() + "." + m.getName() + " " + m.getClass());
			}
			else if (m.getDefiningClass() != this)
			{
				Trace.logError("Member stolen: " + getName() + "." + m.getName() + " " + m.getClass() + Res.asis(" from ") + m.getDefiningClass().getName());
				members[i] = JoriaModifiedAccess.createJoriaModifiedAccess(m.getName(), m.getType(), this, JoriaModifiedAccess.mutipleParents, m, m);
			}
		}
	}

	private void makeMembersUnique()
	{
		ArrayList<JoriaAccess> uniqueChildren = new ArrayList<JoriaAccess>(members.length);
		for (JoriaAccess o : members)
		{
			if (uniqueChildren.contains(o))
				Trace.logError("Info: Fixing duplicate member in ClassProjection: " + myName + " member: " + o);
			else if (o.getDefiningClass() != null && o.getDefiningClass() != this)
			{
				Trace.logError("Info: Skipping member belonging to different parent in: " + myName + " member: " + o + " from " + o.getDefiningClass().getName());
			}
			else
				uniqueChildren.add(o);
		}
		members = new JoriaAccess[uniqueChildren.size()];
		uniqueChildren.toArray(members);
	}

	public int getMemberCount()
	{
		return members.length;
	}

	public JoriaClass getPhysicalClass()
	{
		if (base != null && base.isView())
			return ((ClassView) base).getPhysicalClass();
		return base;
	}

	protected Object readResolve()
	{
		RepoLoader.addProjection(this);
		ViewChecker.add(this);
		makeMembersUnique();
		return this;
	}

	public void sortMembers()
	{
		Arrays.sort(members, NamedComparator.caseInsensitiveComparator);
	}

	public void unbind()
	{
		base = new UnboundClassSentinel(base);
		for (int i = 0; i < members.length; i++)
		{
			JoriaAccess member = members[i];
			if (member instanceof Rebindable)
			{
				Rebindable rebindable = (Rebindable) member;
				rebindable.unbind();
			}
			else
				members[i] = new UnboundAccessSentinel(member);
		}
	}

	public boolean bindableTo(JoriaAccess newBinding, JoriaAccess newParentBinding)
	{
		return newBinding.getType().isClass();
	}

	public void rebind(JoriaAccess newBinding, JoriaAccess newParentBinding)
	{
		base = (JoriaClass) newBinding.getType();
	}

	public boolean unbound()
	{
		return base instanceof UnboundClassSentinel;
	}

	public void rebindType(JoriaCollection collectionTypeAsserted)
	{
		base = collectionTypeAsserted.getElementType();
	}

}
