// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

//MARKER The strings in this file shall not be translated

import java.util.ArrayList;

public class ClassWrap implements JoriaClass
{
	//protected static final JoriaClass[] noClasses = JoriaClass.noClasses;
	protected transient JoriaAccess[] members;
	protected transient JoriaClass wrappedClass;
	public static final long serialVersionUID = -5436789366742419348L;

	public ClassWrap(JoriaClass wrappedClass)
	{
		this.wrappedClass = wrappedClass;
	}

	public void setMembers(JoriaAccess[] members)
	{
		this.members = members;
	}

	public JoriaAccess findMember(String name)
	{
		for (JoriaAccess member : members)
		{
			if (member.getName().equals(name))
			{
				return member;
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

	public boolean isLiteralCollection()
	{
		return false;
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
		return members;
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
		return "IP<" + wrappedClass.getName() + ">";
	}

	public String getParamString()
	{
		return getClass().toString() + "[" + wrappedClass.getName() + "]";
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
		return wrappedClass.isCollection();
	}

	public boolean isDictionary()
	{
		return wrappedClass.isDictionary();
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

	public String toString()
	{
		return getName();
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

	public JoriaClass getAsParent()
	{
		return this;
	}
}
