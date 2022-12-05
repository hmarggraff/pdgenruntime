// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import java.util.ArrayList;

public class DefaultJoriaClass implements JoriaClass {
    private static final long serialVersionUID = 7L;
    protected String myName;
    protected JoriaAccess[] members;

    public DefaultJoriaClass(String name) {
        myName = name;
    }

    public DefaultJoriaClass(String name, JoriaAccess[] members) {
        this(name);
        this.members = members;
    }

    public String getName() {
        return myName;
    }

    public String getParamString() {
        return getName();
    }

    public void setMembers(JoriaAccess[] members) {
        this.members = members;
    }

    public void setName(String newName) {
        myName = newName;
    }

    public JoriaAccess findMember(String name) {
        for (JoriaAccess member : members) {
            if (name.equals(member.getName()))
                return member;
        }
        return null;
    }

    public JoriaClass[] getBaseClasses() {
        return JoriaClass.noClasses;
    }

    public ArrayList<JoriaClass> getDerivedClasses() {
        return null;
    }

    public JoriaAccess[] getFlatMembers() {
        return members;
    }

    public JoriaAccess findMemberIncludingSuperclass(String name) {
        return findMember(name);
    }

    public JoriaAccess[] getMembers() {
        return members;
    }

    public int indexOfMember(JoriaAccess a) {
        for (int i = 0; i < members.length; i++) {
            if (a == members[i])
                return i;
        }
        return -1;
    }

    public boolean isBlob() {
        return false;
    }

    public boolean isBooleanLiteral() {
        return false;
    }

    public boolean isCharacterLiteral() {
        return false;
    }

    public boolean isClass() {
        return true;
    }

    public boolean isCollection() {
        return false;
    }

    public boolean isDate() {
        return false;
    }

    public boolean isImage() {
        return false;
    }

    public boolean isDictionary() {
        return false;
    }

    public boolean isIntegerLiteral() {
        return false;
    }

    public boolean isInternal() {
        return false;
    }

    public boolean isLiteral() {
        return false;
    }

    public boolean isRealLiteral() {
        return false;
    }

    public boolean isStringLiteral() {
        return false;
    }

    public boolean isUnknown() {
        return false;
    }

    public boolean isUserClass() {
        return false;
    }

    public boolean isView() {
        return false;
    }

    public boolean isVoid() {
        return false;
    }

    public boolean isLiteralCollection() {
        return false;
    }

    public JoriaClass getAsParent() {
        return this;
    }

    public static String uniqueName(String sample, JoriaClass parent) {
        JoriaAccess[] members = parent.getAsParent().getMembers();
        boolean exists;
        int id = 1;
        String tName = sample;
        do {
            exists = false;
            for (JoriaAccess m : members) {
                if (tName.equals(m.getName())) {
                    exists = true;
                    tName = sample + (id++);
                    break;
                }
            }
        }
        while (exists);
        return tName;
    }

    @Override
    public String toString() {
        return myName;
    }
}
