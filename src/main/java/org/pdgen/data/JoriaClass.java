// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import java.util.ArrayList;

public interface JoriaClass extends JoriaType
{
	JoriaAccess[] noMembers = new JoriaAccess[0];
	JoriaClass[] noClasses = new JoriaClass[0];

	JoriaAccess findMember(String name);

	JoriaClass[] getBaseClasses();

	ArrayList<JoriaClass> getDerivedClasses();

	int indexOfMember(JoriaAccess a);

	JoriaAccess[] getMembers();

	JoriaAccess[] getFlatMembers();

    JoriaAccess findMemberIncludingSuperclass(String name);

}
