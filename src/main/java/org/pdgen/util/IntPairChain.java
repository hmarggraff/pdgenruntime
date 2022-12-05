// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.util;

/**
 * User: hmf
 * Date: 20.04.2008
 */
public class IntPairChain {
    public final int a;
    public final int b;
    public IntPairChain next;

    public IntPairChain(final int a, final int b) {
        this.a = a;
        this.b = b;
    }
}
