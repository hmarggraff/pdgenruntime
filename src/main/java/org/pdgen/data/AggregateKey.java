// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

/**
 * User: patrick
 * Date: Jul 7, 2007
 * Time: 6:44:47 AM
 */
public class AggregateKey {
    protected JoriaAccess collection;
    protected JoriaAccess aggregate;

    public AggregateKey(JoriaAccess collection, JoriaAccess aggregate) {
        this.aggregate = aggregate;
        this.collection = collection;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof AggregateKey))
            return false;
        AggregateKey o = (AggregateKey) obj;
        return collection.equals(o.collection) && aggregate.equals(o.aggregate);
    }

    public int hashCode() {
        return collection.hashCode() ^ aggregate.hashCode();
    }

    public String toString() {
        return collection.toString() + "|" + aggregate.toString();
    }

}
