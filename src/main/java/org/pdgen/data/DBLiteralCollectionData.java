// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.ProjectionComparator;
import org.pdgen.data.view.SortOrder;
import org.pdgen.data.view.TopNBuilder;
import org.pdgen.model.run.RunEnv;
import org.pdgen.env.Env;
import org.pdgen.oql.OQLNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DBLiteralCollectionData implements DBCollection
{
    Object data;
    int index = -1;
    LiteralCollectionClass myClass;
    JoriaAccess myAccess;
    int length;
    DBLiteralObject[] myDBObjects;
    public DBLiteralCollectionData(Object d, LiteralCollectionClass clazz, JoriaAccess asView, int l, OQLNode checker, SortOrder[] sorting, RunEnv env, int topN) throws JoriaDataException
    {
        data = d;
        myClass = clazz;
        myAccess = asView;
        length = l;
        if(sorting != null && sorting.length == 0)
            sorting = null;
        if(checker != null || sorting != null)
        {
            DBLiteralObject[] d2;
            List<DBObject> intermediate = new ArrayList<DBObject>(length);
            while(next())
            {
                DBData o = current();
                if(checker == null || checker.getBooleanValue(env, o))
                    intermediate.add((DBObject) o);
            }
            reset();
            d2 = new DBLiteralObject[intermediate.size()];
            intermediate.toArray(d2);
            myDBObjects = d2;
            length = myDBObjects.length;
            if(sorting != null)
            {
                ProjectionComparator comp = new ProjectionComparator(sorting, env);
                if (topN > 0)
                {
                    intermediate = TopNBuilder.extractTopN(this, topN, comp);
                    d2 = new DBLiteralObject[intermediate.size()];                }
                else
                {
                    Collections.sort(intermediate, comp);
                }
                intermediate.toArray(d2);
                myDBObjects = d2;
                length = d2.length;
            }
    		else if (topN > 0 && d2.length > topN)
	    	{
                myDBObjects = new DBLiteralObject[topN];
                System.arraycopy(d2, 0, myDBObjects, 0, topN);
                length = topN;
            }
        }
        else if(topN > 0 && length > topN)
        {
            if(myClass.literalType.isIntegerLiteral())
            {
                long[] t = new long[topN];
                System.arraycopy(data, 0, t, 0, topN);
                data = t;
            }
            else if(myClass.literalType.isBooleanLiteral())
            {
                boolean [] t = new boolean[topN];
                System.arraycopy(data, 0, t, 0, topN);
                data = t;
            }
            else if(myClass.literalType.isStringLiteral())
            {
                String[] t = new String[topN];
                System.arraycopy(data, 0, t, 0, topN);
                data = t;
            }
            else if(myClass.literalType.isRealLiteral())
            {
                double [] t = new double[topN];
                System.arraycopy(data, 0, t, 0, topN);
                data = t;
            }
            else if(myClass.literalType.isImage())
            {
                Object[] t = new Object[topN];
                System.arraycopy(data, 0, t, 0, topN);
                data = t;
            }
            else
            {
                throw new JoriaAssertionError("Literal collection for unsupported type "+myClass.literalType.getName());
            }
            length = topN;
        }
    }

    public int getLength() throws JoriaDataException
    {
        return length;
    }

    public DBData pick() throws JoriaDataException
    {
        if (length > 0)
        {
            if(myDBObjects != null)
                return myDBObjects[0];
            else
                return new DBLiteralObject(this, 0, myAccess);
        }
        else
            return null;
    }

    public boolean next() throws JoriaDataException // implizites freeItem
    {
        return ++index < length;
    }

    public DBObject current() throws JoriaDataException
    {
        if(myDBObjects != null)
            return myDBObjects[index];
        else
            return new DBLiteralObject(this, index, myAccess);
    }

    public boolean reset()
    {
        index = -1;
        return true;
    }

    public JoriaAccess getAccess()
    {
        return myAccess;
    }

    public boolean isNull()
    {
        return false;
    }

    public JoriaType getActualType()
    {
        return myClass;
    }

    public boolean same(DBData theOther)
    {
        return false;
    }

    public DBData getValue(int ix, JoriaAccess asView)
    {
        if(myClass.literalType.isIntegerLiteral())
        {
            long v = ((long[])data)[ix];
            return new DBIntImpl(asView, v);
        }
        else if(myClass.literalType.isBooleanLiteral())
        {
            boolean v = ((boolean[])data)[ix];
            return new DBBooleanImpl(asView, v);
        }
        else if(myClass.literalType.isStringLiteral())
        {
			String v = null;
			try
			{
				v = ((String[])data)[ix];
			}
			catch (Exception e)
			{
				Env.instance().handle(e);
			}
			return new DBStringImpl(asView, v);
        }
        else if(myClass.literalType.isRealLiteral())
        {
            double v = ((double [])data)[ix];
            return new DBRealImpl(asView, v);
        }
        else if(myClass.literalType.isImage())
        {
            Object v = ((Object[])data)[ix];
            return new DBImage(v, asView);
        }
        else
        {
            throw new JoriaAssertionError("Literal collection for unsupported type "+myClass.literalType.getName());
        }
    }
}
