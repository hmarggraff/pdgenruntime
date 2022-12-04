// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.Filter;
import org.pdgen.data.view.SortOrder;
import org.pdgen.model.run.RunEnv;

/**
 * use this class if you want to wrap a dictionary class of a database into the form
 * expected by Joria.
 * I.e only two members of the element type are exposed: key and value
 */

public abstract class AbstractJoriaDictionary implements JoriaDictionary
{
    private static final long serialVersionUID = 7L;
    protected transient JoriaClass realElementType;
   protected transient JoriaClass elementType;
   protected transient JoriaType  keyType;
   protected transient JoriaType  valueType;
   protected transient JoriaType  keyCollectionType;
   protected transient JoriaType  valueCollectionType;

   /** ----------------------------------------------------------------------- AbstractJoriaDictionary */

   protected AbstractJoriaDictionary(JoriaClass et, JoriaType kt, JoriaType vt, JoriaType kct, JoriaType vct)
   {
      realElementType     = et;
      keyType             = kt;
      valueType           = vt;
      keyCollectionType   = kct;
      valueCollectionType = vct;
   }

	/** ----------------------------------------------------------------------- getElementType */

   public JoriaClass getElementType()
   {
      return elementType;
   }

   /** ----------------------------------------------------------------------- getKeyType */

   public JoriaType getKeyType()
   {
      return keyType;
   }

   /** ----------------------------------------------------------------------- getValueType */

   public JoriaType getValueType()
   {
      return valueType;
   }

   /** ----------------------------------------------------------------------- getKeyCollectionType */

   public JoriaType getKeyCollectionType()
   {
      return keyCollectionType;
   }

   /** ----------------------------------------------------------------------- getValueCollectionType */

   public JoriaType getValueCollectionType()
   {
      return valueCollectionType;
   }

   /** ----------------------------------------------------------------------- isBlob */

   public boolean isBlob()
   {
      return false;
   }

   /** ----------------------------------------------------------------------- isClass */

   public boolean isClass()
   {
      return false;
   }

   /** ----------------------------------------------------------------------- isCollection */

   public boolean isCollection()
   {
      return true;
   }

	/** ----------------------------------------------------------------------- isDictionary */

   public boolean isDictionary()
   {
      return true;
   }

   /** ----------------------------------------------------------------------- isInternal */

   public boolean isInternal()
   {
      return false;
   }

   /** ----------------------------------------------------------------------- isLiteral */

   public boolean isLiteral()
   {
      return false;
   }

   /** ----------------------------------------------------------------------- isUnknown */

   public boolean isUnknown()
   {
      return false;
   }

   /** ----------------------------------------------------------------------- isUserClass */

   public boolean isUserClass()
   {
      return true;
   }

   /** ----------------------------------------------------------------------- isView */

   public boolean isView()
   {
      return false;
   }

	public boolean isLarge()
	{
		return false;
	}

   /** ----------------------------------------------------------------------- isVoid */

   public boolean isVoid()
   {
      return false;
   }

	public boolean isLiteralCollection()
	{
		return false;
	}

   public String toString()
   {
      return getName();
   }
   public boolean isBooleanLiteral() { return false; }
   public boolean isCharacterLiteral() { return false; }
   public boolean isIntegerLiteral() { return false; }
   public boolean isRealLiteral() { return false; }
   public boolean isStringLiteral() { return false; }
   public boolean isDate(){ return false; }

    public boolean isImage()
    {
        return false;
    }

    public SortOrder[] getSorting() { return null; }
	public Filter getFilter() { return null; }

	public int getMinTopN(RunEnv env)
	{
		return 0;
	}

    public boolean hasFilterOrSortingOrTopN()
    {
        return false;  
    }


}
