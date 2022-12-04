// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;


public class DBBooleanImpl extends AbstractDBData implements DBBoolean
{
   boolean myBoolean;

   public DBBooleanImpl(JoriaAccess axs, boolean value)
   {
      super(axs);
      myBoolean = value;
   }

   /** ----------------------------------------------------------------------- equals */

   public boolean equals(Object obj)
   {
       return obj instanceof DBBoolean && ((DBBoolean) obj).getBooleanValue() == myBoolean;
   }

   /** ----------------------------------------------------------------------- getBooleanValue */

   public boolean getBooleanValue()
   {
      return myBoolean;
   }

   /** ----------------------------------------------------------------------- isNull */

   public boolean isNull()
   {
      return false;
   }

   /** ----------------------------------------------------------------------- toString */

    public String toString()
    {
        return String.valueOf(myBoolean);
    }

    public int hashCode()
    {
        return myBoolean?0:1;
    }

    public boolean same(DBData theOther)
    {
        return theOther instanceof DBBoolean && ((DBBoolean) theOther).getBooleanValue() == myBoolean;
    }
}
