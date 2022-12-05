// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

public abstract class AbstractJoriaLiteral implements JoriaLiteral {
    private static final long serialVersionUID = 7L;

    public boolean isBlob() {
        return false;
    }

    /**
     * ----------------------------------------------------------------------- isBooleanLiteral
     */
    public boolean isBooleanLiteral() {
        return false;
    }

    /**
     * ----------------------------------------------------------------------- isCharacterLiteral
     */
    public boolean isCharacterLiteral() {
        return false;
    }

    /**
     * ----------------------------------------------------------------------- isClass
     */
    public boolean isClass() {
        return false;
    }

    /**
     * ----------------------------------------------------------------------- isCollection
     */
    public boolean isCollection() {
        return false;
    }

    /**
     * ----------------------------------------------------------------------- isDictionary
     */
    public boolean isDictionary() {
        return false;
    }

    /**
     * ----------------------------------------------------------------------- isIntegerLiteral
     */
    public boolean isIntegerLiteral() {
        return false;
    }

    /**
     * ----------------------------------------------------------------------- isInternal
     */
    public boolean isInternal() {
        return true;
    }

    /**
     * ----------------------------------------------------------------------- isLiteral
     */
    public boolean isLiteral() {
        return false;
    }

    /**
     * ----------------------------------------------------------------------- isRealLiteral
     */
    public boolean isRealLiteral() {
        return false;
    }

    /**
     * ----------------------------------------------------------------------- isStringLiteral
     */
    public boolean isStringLiteral() {
        return false;
    }

    /**
     * ----------------------------------------------------------------------- isUnknown
     */
    public boolean isUnknown() {
        return false;
    }

    /**
     * ----------------------------------------------------------------------- isUserClass
     */
    public boolean isUserClass() {
        return false;
    }

    /**
     * ----------------------------------------------------------------------- isView
     */
    public boolean isView() {
        return false;
    }

    /**
     * ----------------------------------------------------------------------- isVoid
     */
    public boolean isVoid() {
        return false;
    }

    /**
     * ----------------------------------------------------------------------- toString
     */
    public String toString() {
        return getName();
    }

    public boolean isDate() {
        return false;
    }

    public boolean isImage() {
        return false;
    }

    public boolean isLiteralCollection() {
        return false;
    }

    public JoriaClass getAsParent() {
        return null;
    }
}
