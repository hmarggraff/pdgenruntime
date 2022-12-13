// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.view.*;
import org.pdgen.env.Env;
import org.pdgen.model.run.RunEnv;

import java.util.*;

public class JoriaUnknownType implements JoriaClass, JoriaLiteral, MutableCollection {
    private static final long serialVersionUID = 7L;
    protected String name;
    protected JoriaClass elementType;

    private JoriaUnknownType(String name) {
        Trace.logWarn("JoriaUnknownType " + name);
        this.name = name;
    }

    public JoriaAccess findMember(String n) {
        return null;
    }

    public JoriaClass[] getBaseClasses() {
        return noClasses;
    }

    public ArrayList<JoriaClass> getDerivedClasses() {
        return null;
    }

    public JoriaAccess[] getFlatMembers() {
        return noMembers;
    }

    public JoriaAccess findMemberIncludingSuperclass(String name) {
        return null;
    }

    public JoriaAccess[] getMembers() {
        return noMembers;
    }

    public String getName() {
        return name;
    }

    public String getParamString() {
        return "JoriaUnknownType[" + name + "]";
    }

    public int indexOfMember(JoriaAccess a) {
        return -1;
    }

    public boolean isBlob() {
        return false;
    }

    public boolean isClass() {
        return false;
    }

    public boolean isCollection() {
        return true;
    }

    public boolean isDictionary() {
        return false;
    }

    public boolean isInternal() {
        return false;
    }

    public boolean isLiteral() {
        return false;
    }

    public boolean isUnknown() {
        return true;
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

    public String toString() {
        return getName();
    }

    public boolean isBooleanLiteral() {
        return false;
    }

    public boolean isCharacterLiteral() {
        return false;
    }

    public boolean isIntegerLiteral() {
        return false;
    }

    public boolean isRealLiteral() {
        return false;
    }

    public boolean isStringLiteral() {
        return false;
    }

    public boolean isDate() {
        return false;
    }

    public boolean isImage() {
        return false;
    }

    public boolean isLarge() {
        return false;
    }

    public JoriaClass getElementType() {
        if (elementType == null) {
            elementType = new JoriaUnknownType(stripOuterName(name));
        }
        return elementType;
    }

    private static String stripOuterName(String name) {
        int index = name.indexOf("<");
        if (index != -1) {
            name = name.substring(index + 1, name.length() - 1);
        }
        return name;
    }

    public JoriaClass getElementMatchType() {
        return null;
    }

    public SortOrder[] getSorting() {
        return null;
    }

    public Filter getFilter() {
        return null;
    }

    public String getElementXmlTag() {
        return "JoriaUnknownType " + name;
    }

    public void setFilter(Filter f) {
    }

    public boolean isLiteralCollection() {
        return false;
    }

    // JoriaMutableCollection
    public void addChild(JoriaAccess f) {
    }

    public void collectVariables(Set<RuntimeParameter> s, Set<Object> seen) {
    }

    public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> s, Set<Object> seen) {
    }

    public MutableView dup(Map<Object, Object> alreadyCopiedViews) {
        return this;
    }

    public void collectViewUsage(Map<MutableView, Set<Object>> viewUsage, Set<MutableView> visitedViews) {
    }

    public void collectUsedViews(Set<MutableView> s) {
    }

    public boolean fixAccess() {
        return false;
    }

    public JoriaClass getAsParent() {
        return null;
    }

    public JoriaCollection getBaseCollection() {
        return null;
    }

    public JoriaType getOriginalType() {
        return null;
    }

    public void removeChild(JoriaAccess f) {
    }

    public void replaceChild(JoriaAccess f, JoriaAccess fNew) {
    }

    public void setBase(JoriaClass c) {
    }

    public void setElementType(JoriaClass newElementMatchType) {
    }

    public void setName(String name) {
    }

    public void setSorting(SortOrder[] so) {
    }

    public int getTopN() {
        return 0;
    }

    public void setTopN(int topN) {
    }

    public RuntimeParameterLiteral getTopNVariable() {
        return null;
    }

    public void setTopNVariable(RuntimeParameter param) {
        // nothing
    }

    public MutableCollection copyReportPrivate(final Map<Object, Object> copiedData) {
        return new JoriaUnknownType(name);
    }

    public void collectVisiblePickersInScope(List<JoriaAccess[]> collection, Set<RuntimeParameter> visible, Stack<JoriaAccess> pathStack, Set<Object> seen) {
    }

    protected Object readResolve() {
        Named a = Env.schemaInstance.findClass(name);
        if (a != null) {
            Trace.logWarn("Previously missing type from schema change reconnected: " + name);

            Env.repoChanged();
            return a;
        }
        Trace.logWarn("Placeholder for unknown type in save file: " + name);
        return this;
    }

    public int getMinTopN(RunEnv env) {
        return 0;
    }

    public boolean hasFilterOrSortingOrTopN() {
        return false;
    }

    public boolean hasName() {
        return name != null;
    }

    public void unbind() {
    }

    public boolean unbound() {
        return false;
    }

    public boolean bindableTo(JoriaAccess newBinding, JoriaAccess newParentBinding) {
        return false;
    }

    public void rebind(JoriaAccess newBinding, JoriaAccess newParentBinding) {
    }

    public static JoriaClass createJoriaUnknownType(String name) {
        return new JoriaUnknownType(name);
    }

    public void sortMembers() {
    }
}
