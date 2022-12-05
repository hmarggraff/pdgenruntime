// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.data.view.FilteredDBCollection;
import org.pdgen.model.run.RunEnv;
import org.pdgen.projection.ComputedDBCollectionValue;
import org.pdgen.projection.PseudoAccess;

import java.util.ArrayList;

public class PlusNode extends BinaryOperatorNode implements JoriaTypedNode {

    public PlusNode(int p0, NodeInterface p1, NodeInterface p2) {
        super(p0, p1, p2);
    }

    public DBCollection getCollection(RunEnv env, DBData d) throws JoriaDataException {
        DBCollection l = left.getCollection(env, d);
        DBCollection r = right.getCollection(env, d);
        if (l == null || l.isNull())
            return r;
        else if (r == null || r.isNull())
            return l;
        ArrayList<DBData> c = new ArrayList<DBData>(l.getLength() + r.getLength());
        if (l instanceof ComputedDBCollectionValue) {
            c.addAll(((ComputedDBCollectionValue) l).getList());
        } else {
            l.reset();
            while (l.next()) {
                DBData el = l.current();
                c.add(el);
            }
            l.reset();
        }
        if (r instanceof ComputedDBCollectionValue) {
            c.addAll(((ComputedDBCollectionValue) r).getList());
        } else {
            l.reset();
            while (r.next()) {
                DBData el = r.current();
                c.add(el);
            }
            l.reset();
        }
        return new ComputedDBCollectionValue(c, l.getAccess());
    }

    public double getFloatValue(RunEnv env, DBData p0) throws JoriaDataException {
        double floatValueLeft = left.getFloatValue(env, p0);
        double floatValueRight = right.getFloatValue(env, p0);
        if (Double.isNaN(floatValueLeft) || Double.isNaN(floatValueRight))
            return DBReal.NULL;
        return floatValueLeft + floatValueRight;
    }

    public long getIntValue(RunEnv env, DBData p0) throws JoriaDataException {
        long intValueLeft = left.getIntValue(env, p0);
        long intValueRight = right.getIntValue(env, p0);
        if (intValueLeft == DBInt.NULL || intValueRight == DBInt.NULL)
            return DBInt.NULL;
        return intValueLeft + intValueRight;
    }

    public String getStringValue(RunEnv env, DBData p0) throws JoriaDataException {
        String stringValueLeft = left.getStringValue(env, p0);
        String stringValueRight = right.getStringValue(env, p0);
        if (stringValueLeft == null)
            return stringValueRight;
        else if (stringValueRight == null)
            return stringValueLeft;
        return stringValueLeft + stringValueRight;
    }

    public String getTokenString() {
        return left.getTokenString() + '+' + right.getTokenString();
    }


    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        final int newLevel = 5;
        optBrace(bindingLevel, newLevel, collector, '(');
        left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        collector.append(" + ");
        right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        optBrace(bindingLevel, newLevel, collector, ')');
    }

    public JoriaType getElementType() {
        if (isCollection()) {
            JoriaTypedNode lt = (JoriaTypedNode) left; // must have been checked when parsing
            return lt.getType();
        } else
            throw new JoriaAssertionError("Unhandled type in plus node escaped parser");
    }

    public JoriaType getType() {
        if (isString())
            return DefaultStringLiteral.instance();
        else if (isInteger())
            return DefaultIntLiteral.instance();
        else if (isReal())
            return DefaultRealLiteral.instance();
        else if (isCollection()) {
            JoriaTypedNode lt = (JoriaTypedNode) left; // must have been checked when parsing
            return lt.getType();
        } else
            throw new JoriaAssertionError("Unhandled type in plus node escaped parser");
    }

    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        if (isString())
            return new DBStringImpl(new PseudoAccess(DefaultStringLiteral.instance()), getStringValue(env, p0));
        else if (isInteger())
            return new DBIntImpl(new PseudoAccess(DefaultIntLiteral.instance()), getIntValue(env, p0));
        else if (isReal())
            return new DBRealImpl(new PseudoAccess(DefaultRealLiteral.instance()), getFloatValue(env, p0));
        else if (isCollection()) {
            JoriaTypedNode lt = (JoriaTypedNode) left; // must have been checked when parsing
            JoriaTypedNode rt = (JoriaTypedNode) right; // must have been checked when parsing
            final DBCollection lc = lt.getCollection(env, p0);
            final DBCollection rc = rt.getCollection(env, p0);
            ArrayList<DBObject> rl = new ArrayList<DBObject>();
            if (lc != null && !lc.isNull()) {
                lc.reset();
                rl.ensureCapacity(lc.getLength());
                while (lc.next())
                    rl.add(lc.current());
            }
            if (rc != null && !rc.isNull()) {
                rc.reset();
                rl.ensureCapacity(rl.size() + rc.getLength());
                while (rc.next())
                    rl.add(rc.current());
            }
            FilteredDBCollection ret = new FilteredDBCollection(rl, new PseudoAccess(lt.getType()), lt.getType());
            return ret;
        } else
            throw new JoriaAssertionError("Unhandled type in plus node escaped parser");
    }
}
