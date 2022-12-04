// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

//MARKER The strings in this file shall not be translated

import org.pdgen.env.Env;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Arrays;

public class CollectionWrapperClass implements JoriaClass
{
    private static final long serialVersionUID = 7L;
    protected transient JoriaAccess[] members;
	public java.lang.String name = "Wrapper<not initialized>";

	public CollectionWrapperClass()
	{
		members = JoriaClass.noMembers;

	}
	public void initElementAccess(JoriaAccess elemAxxess)
	{
		if (members == null || members == JoriaClass.noMembers)
			members = new JoriaAccess[2];
		members[1] = elemAxxess;
		members[0] = new CollectionLengthMember(this, elemAxxess);
		name = "Wrapper<" + ((JoriaAccess) elemAxxess).getType().getName() + ">";
	}

	public JoriaAccess findMember(String name)
	{
		for (int i = 0; members != null && i < members.length; i++)
		{
			if (members[i].getName().equals(name))
			{
				return members[i];
			}
		}
		return null;
	}

	public int indexOfMember(JoriaAccess a)
	{
		JoriaAccess[] m = getFlatMembers();
		for (int i = 0; i < m.length; i++)
		{
			if (m[i] == a)
				return i;
		}
		return -1;
	}

	public void setMemberAccesses(JoriaAccess len, JoriaAccess elems)
	{
		members = new JoriaAccess[2];
		members[0] = len;
		members[1] = elems;
		Arrays.sort(members);
	}

	public JoriaClass[] getBaseClasses()
	{
		return JoriaClass.noClasses;
	}

	public JoriaAccess getCollection()
	{
		for (JoriaAccess member : members)
		{
			if (member.getType().isCollection())
			{
				return member;
			}
		}
		throw new JoriaAssertionError("No collection member in CollectionWrapperClass");
	}

	public ArrayList<JoriaClass> getDerivedClasses()
	{
		return null;
	}

	public JoriaAccess[] getFlatMembers()
	{
		return members;
	}

	public JoriaAccess findMemberIncludingSuperclass(String name)
	{
		return findMember(name);
	}

	public JoriaAccess getLength()
	{
		return members[0];
	}

	public JoriaAccess[] getMembers()
	{
		return members;
	}

	public String getName()
	{
		return name;
	}

	public String getParamString()
	{
		return "VirtualCollectionRootClass[" + getName() + "]";
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
		return false;
	}

	public boolean isInternal()
	{
		return true;
	}

	public boolean isLiteral()
	{
		return false;
	}

	public boolean isUnknown()
	{
		return false;
	}

	public boolean isUserClass()
	{
		return false;
	}

	public boolean isView()
	{
		return false;
	}

	public boolean isVoid()
	{
		return false;
	}

	public void setLength(JoriaAccess newLength)
	{
		members[0] = newLength;
	}

	public String toString()
	{
		return getName();
	}

	public void setMembers(JoriaAccess[] newMembers)
	{
		members = newMembers;
	}

	protected Object readResolve() throws ObjectStreamException
	{
        Named jc = Env.schemaInstance.findInternalType(name);
		if (jc == null)
		{
            jc = Env.schemaInstance.findInternalType(name.replace('.', '_'));
		}
		if (jc instanceof CollectionWrapperClass)
			return jc;

		return JoriaUnknownType.createJoriaUnknownType(name);
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
		return false;
	}

	public boolean isImage()
	{
		return false;
	}

	public boolean isLiteralCollection()
	{
		return false;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public JoriaClass getAsParent()
	{
		return this;
	}
}
