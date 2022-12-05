// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

public class JoriaVoidCollection extends AbstractJoriaCollection {
    public static final JoriaVoidCollection voidCollection = new JoriaVoidCollection();
    private static final long serialVersionUID = 7L;

    public JoriaVoidCollection() {
        super(JoriaClassVoid.voidType);
    }

    protected Object readResolve() {
        return voidCollection;
    }
}
