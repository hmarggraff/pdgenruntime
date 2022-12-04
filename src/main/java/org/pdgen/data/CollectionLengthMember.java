// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;
//MARKER The strings in this file shall not be translated

import org.pdgen.model.run.RunEnv;

import java.io.ObjectStreamException;

public class CollectionLengthMember extends WrappedMember

{

    private static final long serialVersionUID = 7L;

    public CollectionLengthMember(JoriaClass definingClass, JoriaAccess coll)
	{
		super(definingClass, DefaultIntLiteral.instance(), coll, coll.getName() + "_length");
	}

	public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException
	{
		DBCollectionCache f = (DBCollectionCache) from;
		DBCollection dc = f.getCachedCollectionValue(asView);
		try
		{
			if (dc == null)
			{
				dc = (DBCollection) base.getValue(from, base, env);
				//d = JavaMember.makeCollectionValue(f.getRootDef().getValue(), asView, asView.getSourceCollection());
				f.addCollectionToCache(dc, base);
			}
			if (dc != null)
				return new DBIntImpl(this, dc.getLength());
			else
				return new DBIntImpl(this, -1);
		}
		catch (JoriaDataException ex)
		{
			Trace.log(ex);
			//ex.printStackTrace();
			throw new JoriaDataException(ex.getMessage());
		}
	}

   protected Object readResolve() throws ObjectStreamException
   {
      if (definingClass instanceof JoriaUnknownType)
      {
         return JoriaModifiedAccess.createJoriaModifiedAccess(name, type, definingClass, JoriaModifiedAccess.classNotFound, this, null);
      }
      JoriaAccess mem = definingClass.findMember(name);

      if (mem == null)
      {
         return JoriaModifiedAccess.createJoriaModifiedAccess(name, type, definingClass, JoriaModifiedAccess.memberNotFound, this, null);
      }
      if (type instanceof JoriaUnknownType || type != mem.getType())
      {
         return JoriaModifiedAccess.createJoriaModifiedAccess(name, type, definingClass, JoriaModifiedAccess.typeChanged, this, mem);
      }
      return mem;
   }
}
