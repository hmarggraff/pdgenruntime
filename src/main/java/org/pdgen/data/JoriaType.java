// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import java.io.Serializable;

public interface JoriaType extends Named, Serializable {
    String getParamString();

    boolean isBlob();

    boolean isClass();

    boolean isCollection();

    boolean isLiteralCollection();

    boolean isDictionary();

    boolean isInternal();

    boolean isLiteral();

    boolean isUnknown();

    boolean isUserClass();

    boolean isView();

    boolean isVoid();

    boolean isBooleanLiteral();

    boolean isCharacterLiteral();

    boolean isIntegerLiteral();

    boolean isRealLiteral();

    boolean isStringLiteral();

    boolean isDate();

    boolean isImage();

    JoriaClass getAsParent();
}
