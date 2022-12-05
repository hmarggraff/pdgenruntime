// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.*;
import org.pdgen.model.run.RunEnv;

import java.util.List;
import java.util.Set;
import java.util.Stack;

public class DefaultJoriaCollection implements JoriaCollection {
    private static final long serialVersionUID = 7L;
    protected String name;
    protected JoriaClass elementType;

    public DefaultJoriaCollection(JoriaClass elementType) {
        this.elementType = elementType;
    }

    public DefaultJoriaCollection(JoriaClass elementType, String tag) {
        this.elementType = elementType;
        //noinspection OverriddenMethodCallInConstructor
        name = tag + "_" + getElementType();
    }

    public DefaultJoriaCollection() {
    }

    protected void makeName() {
        if (getElementType() == null)
            name = getTag() + "_Unknown";
        else
            name = getTag() + "_" + getElementType().getName();
    }

    public void setElementType(JoriaClass aType) {
        elementType = aType;
        makeName();
    }

    public String getParamString() {
        return getClass().toString() + "[" + name + "," + elementType.getParamString() + "]";
    }

    public void setName(String aName) {
        name = aName;
    }

    public String getElementXmlTag() {
        return MutableAccess.escape(getElementMatchType().getName());
    }

    public boolean isLiteralCollection() {
        return false;
    }

    public boolean isLarge() {
        return false;
    }

    //why is this not used?
    public void collectVisiblePickersInScope(List<JoriaAccess[]> collection, Set<RuntimeParameter> visible, Stack<JoriaAccess> pathStack, Set<Object> seen) {
        if (elementType instanceof VariableProvider) {
            VariableProvider provider = (VariableProvider) elementType;
            provider.collectVisiblePickersInScope(collection, visible, pathStack, seen);
        }
    }

    public JoriaClass getElementMatchType() {
        return null;
    }

    public JoriaClass getElementType() {
        return elementType;
    }

    public String getName() {
        return name;
    }

    protected String getTag() {
        return "DefaultJoriaCollection";
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
        return false;
    }

    public boolean isCollection() {
        return true;
    }

    public boolean isDictionary() {
        return false;
    }

    public boolean isIntegerLiteral() {
        return false;
    }

    public boolean isInternal() {
        return true;
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
        return true;
    }

    public boolean isView() {
        return false;
    }

    public boolean isVoid() {
        return false;
    }

    public boolean isDate() {
        return false;
    }

    public boolean isImage() {
        return false;
    }

    public SortOrder[] getSorting() {
        return null;
    }

    public Filter getFilter() {
        return null;
    }

    public void setFilter(Filter f) {
    }

    public int getMinTopN(RunEnv env) throws JoriaDataException {
        return 0;
    }

    public boolean hasFilterOrSortingOrTopN() {
        return false;
    }

    public void unbind() {
        if (elementType instanceof Rebindable) {
            Trace.check(elementType, ClassProjection.class);
            Rebindable rebindable = (Rebindable) elementType;
            rebindable.unbind();
        } else {
            Trace.logWarn("DefaultJoriaCollection.unbind elementType not rebindable: " + elementType.getParamString());
            elementType = new UnboundClassSentinel(elementType);
        }
    }

    public void rebind(JoriaAccess newBinding, JoriaAccess newParentBinding) {
        if (elementType instanceof ClassProjection) {
            ClassProjection rebindable = (ClassProjection) elementType;
            rebindable.rebindType(newBinding.getCollectionTypeAsserted());
        } else {
            Trace.logWarn("DefaultJoriaCollection.unbind elementType not rebindable: " + elementType.getParamString());
            elementType = new UnboundClassSentinel(elementType);
        }
    }

    public boolean bindableTo(JoriaAccess newBinding, JoriaAccess newParentBinding) {
        return newBinding.getType().isCollection();
    }

    public boolean unbound() {
        if (elementType instanceof Rebindable)
            return ((Rebindable) elementType).unbound();
        else
            return elementType instanceof UnboundClassSentinel;
    }

    public JoriaClass getAsParent() {
        return elementType.getAsParent();
    }
}
