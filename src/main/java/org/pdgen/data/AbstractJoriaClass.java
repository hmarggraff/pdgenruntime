// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;
//MARKER The strings in this file shall not be translated

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Set;

public abstract class AbstractJoriaClass implements JoriaClass
{
    private static final long serialVersionUID = 7L;
    protected transient JoriaAccess[] members;
	protected transient JoriaClass[] baseClasses = JoriaClass.noClasses;
	protected transient ArrayList<JoriaClass> derivedClasses;
	protected transient JoriaAccess[] flat_members;

	public AbstractJoriaClass()
	{
	}

	public AbstractJoriaClass(JoriaAccess[] members)
	{
		this();
		this.members = members;
	}

	public JoriaAccess findMember(String name)
	{
		if (members == null)
			return null;
		for (JoriaAccess member : members)
		{
			if (member.getName().equals(name))
			{
				return member;
			}
		}
		return null;
	}

	public JoriaAccess findMemberIncludingSuperclass(String name)
	{
		JoriaAccess[] fm = getFlatMembers();
		for (JoriaAccess joriaAccess : fm)
		{
			if (joriaAccess.getName().equals(name))
				return joriaAccess;
		}
		return null;
	}

	public static JoriaAccess[] flattenMembers(JoriaClass c)
	{
		Hashtable<String, JoriaAccess> vec = new Hashtable<String, JoriaAccess>();
        flattenMembers1(c, vec);
		JoriaAccess[] ret = new JoriaAccess[vec.size()];
		vec.values().toArray(ret);
		Arrays.sort(ret, new NamedComparator(false));
		return ret;
	}

	public static void flattenMembers1(JoriaClass c, Hashtable<String, JoriaAccess> vec)
	{
		JoriaClass[] bc = c.getBaseClasses();
		for (JoriaClass aBc : bc)
		{
            flattenMembers1(aBc, vec);
		}
		JoriaAccess[] mm = c.getMembers();
		if (mm == null)
			return;
		for (JoriaAccess aMm : mm)
		{
			vec.put(aMm.getName(), aMm);
		}
	}

	public static SortedNamedVector<JoriaClass> getAllBaseClasses(JoriaClass c)
	{
		SortedNamedVector<JoriaClass> v = new SortedNamedVector<JoriaClass>();
        getAllBases1(c, v);
		return v;
	}

	protected static void getAllBases1(JoriaClass c, SortedNamedVector<JoriaClass> v)
	{
		JoriaClass[] a;
		try
		{
			a = c.getBaseClasses();
		}
		catch (Exception ex)
		{
			System.out.println("Exception when building classes " + c);
			return;
		}
		v.addOrReplaceAll(a);
		for (JoriaClass anA : a)
		{
            getAllBases1(anA, v);
		}
	}

	protected static void getAllDerived1(JoriaClass c, SortedNamedVector<JoriaClass> v)
	{
		ArrayList<JoriaClass> a = c.getDerivedClasses();
		if (a == null)
			return;
		v.addAll(a);
		for (JoriaClass anA : a)
		{
            getAllDerived1(anA, v);
		}
	}

	public static SortedNamedVector<JoriaClass> getAllDerivedClasses(JoriaClass c)
	{
		SortedNamedVector<JoriaClass> v = new SortedNamedVector<JoriaClass>();
        getAllDerived1(c, v);
		return v;
	}

	public JoriaClass[] getBaseClasses()
	{
		return baseClasses;
	}

	public ArrayList<JoriaClass> getDerivedClasses()
	{
		return derivedClasses;
	}

	public JoriaAccess[] getFlatMembers()
	{
		if (flat_members == null)
		{
			flat_members = flattenMembers(this);
		}
		return flat_members;
	}

	/**
	 * ----------------------------------------------------------------------- getMembers
	 */
	public JoriaAccess[] getMembers()
	{
		return members;
	}

	/**
	 * ----------------------------------------------------------------------- indexOfMember
	 */
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

	/**
	 * ----------------------------------------------------------------------- isBlob
	 */
	public boolean isBlob()
	{
		return false;
	}

	/**
	 * ----------------------------------------------------------------------- isClass
	 */
	public boolean isClass()
	{
		return true;
	}

	/**
	 * ----------------------------------------------------------------------- isCollection
	 */
	public boolean isCollection()
	{
		return false;
	}

	/**
	 * ----------------------------------------------------------------------- isDictionary
	 */
	public boolean isDictionary()
	{
		return false;
	}

	/**
	 * ----------------------------------------------------------------------- isInternal
	 */
	public boolean isInternal()
	{
		return true;
	}

	/**
	 * ----------------------------------------------------------------------- isLiteral
	 */
	public boolean isLiteral()
	{
		return false;
	}

	/**
	 * ----------------------------------------------------------------------- isUnknown
	 */
	public boolean isUnknown()
	{
		return false;
	}

	/**
	 * ----------------------------------------------------------------------- isUserClass
	 */
	public boolean isUserClass()
	{
		return false;
	}

	/**
	 * ----------------------------------------------------------------------- isView
	 */
	public boolean isView()
	{
		return false;
	}

	/**
	 * ----------------------------------------------------------------------- isVoid
	 */
	public boolean isVoid()
	{
		return false;
	}

	/**
	 * ----------------------------------------------------------------------- toString
	 */
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

	public void setDerivedClasses(ArrayList<JoriaClass> subs)
	{
		derivedClasses = subs;
	}

	public boolean isLiteralCollection()
	{
		return false;
	}

	public JoriaClass getAsParent()
	{
		return this;
	}

	public static void buildBaseClasses(Set<JoriaClass> baseClassesCollector, JoriaClass child)
	{
		final JoriaClass[] classes = child.getBaseClasses();
		if (classes == null || classes.length == 0)
			return;
		for (JoriaClass c : classes)
		{
			baseClassesCollector.add(c);
            buildBaseClasses(baseClassesCollector, c);
		}
	}

	public static JoriaClass findBestBaseClass(Set<JoriaClass> baseClassesCollector, JoriaClass child)
	{
		final JoriaClass[] classes = child.getBaseClasses();
		if (classes == null || classes.length == 0)
			return null;
		for (JoriaClass c : classes)
		{
			if (baseClassesCollector.contains(c))
				return c;
		}
		for (JoriaClass c : classes)
		{
			final JoriaClass baseClass = findBestBaseClass(baseClassesCollector, c);
			if (baseClass != null)
				return baseClass;
		}
		return null;
	}
}

