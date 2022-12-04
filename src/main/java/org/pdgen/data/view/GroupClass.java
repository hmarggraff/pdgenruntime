// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaClass;
import org.pdgen.data.Trace;


import java.util.Map;

public class GroupClass extends ClassProjection
{
    private static final long serialVersionUID = 7L;
    protected GroupKeyAccess myGroupKeyAxs;
	protected NameableAccess myGroupValueAxs;

	public GroupClass(JoriaClass cn, String name, boolean sibling)
	{
		super(cn, name, sibling);
	}

	public MutableView dup(Map<Object,Object> alreadyCopiedViews)
	{
		final Object duplicate = alreadyCopiedViews.get(this);
		if (duplicate != null)
			return (MutableView) duplicate;
		
		GroupClass ret = new GroupClass(base, null, true);
        alreadyCopiedViews.put(this, ret);
		final NameableAccess gka = myGroupKeyAxs.dup(ret, alreadyCopiedViews);
		ret.myGroupKeyAxs =  (GroupKeyAccess) gka;
		ret.myGroupValueAxs = myGroupValueAxs.dup(ret, alreadyCopiedViews);
		ret.members = new JoriaAccess[members.length];
		for (int i = 0; i < members.length; i++)
		{
			NameableAccess member = (NameableAccess) members[i];
			if (member == myGroupKeyAxs)
				ret.members[i] = ret.myGroupKeyAxs;
			else if (member == myGroupValueAxs)
				ret.members[i] = ret.myGroupValueAxs;
			else
			{
				NameableAccess nMember = member.dup(ret, alreadyCopiedViews);
				ret.members[i] = nMember;
			}
		}
		return ret;
	}

	public void setGroupKeyAxs(GroupKeyAccess groupKeyAxs)
	{
		if (myGroupKeyAxs != null)
			replaceChild(myGroupKeyAxs, groupKeyAxs);
		else
			addChild(groupKeyAxs);
		myGroupKeyAxs = groupKeyAxs;
	}


	public void setGroupValueAxs(GroupValueBaseAccess groupValueAxs)
	{
        if(groupValueAxs.getDefiningClass() != this)
            groupValueAxs.setDefiningClass(this);
        if (myGroupValueAxs != null)
			replaceChild(myGroupValueAxs, groupValueAxs);
		else
			addChild(groupValueAxs);
		myGroupValueAxs = groupValueAxs;
	}

	public void removeChild(JoriaAccess f)
	{
		Trace.check(f != myGroupKeyAxs, "Deleting the key of a group is not possible");
		super.removeChild(f);
	}

	public GroupKeyAccess getGroupKeyAxs()
	{
		return myGroupKeyAxs;
	}

	public NameableAccess getGroupValueAxs()
	{
		return myGroupValueAxs;
	}

    public boolean isView()
    {
        return false;
    }

	public GroupClass copyReportPrivateGroupClass(final Map<Object, Object> copiedData, final JoriaAccess myBaseAccess)
	{
		final GroupClass ret = new GroupClass(base, myName, false);
		ret.myGroupValueAxs = (NameableAccess) ((GroupValueAccess)myGroupValueAxs).copyReportPrivateGroupValueAccess(copiedData, ret, (MutableCollection) myBaseAccess.getType());
		ret.myGroupKeyAxs = myGroupKeyAxs.copyReportPrivateAccess(ret, copiedData);
		ret.members = new JoriaAccess[members.length];
		for (int i = 0; i < members.length; i++)
		{
			NameableAccess member = (NameableAccess) members[i];
			if (member == myGroupKeyAxs)
				ret.members[i] = ret.myGroupKeyAxs;
			else if (member == myGroupValueAxs)
				ret.members[i] = ret.myGroupValueAxs;
			else
			{
				NameableAccess nMember = member.dup(ret, copiedData);
				ret.members[i] = nMember;
			}
		}
		return ret;
	}
}