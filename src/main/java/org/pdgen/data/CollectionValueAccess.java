// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.model.run.RunEnv;

public class CollectionValueAccess extends AbstractTypedJoriaMember {
    private static final long serialVersionUID = 7L;
    protected JoriaAccess collectionAccess;


    public CollectionValueAccess(JoriaAccess coll) {
        super(coll.getDefiningClass(), coll.getName());

        collectionAccess = coll;
        JoriaCollection jcoll = coll.getSourceCollection();
        Trace.check(jcoll, "source collection is null because type is probably not a collection");
        type = jcoll.getElementType();
        makeLongName();
    }

    public JoriaAccess getCollectionAccess() {
        return collectionAccess;
    }


    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        return from;
    }
}
