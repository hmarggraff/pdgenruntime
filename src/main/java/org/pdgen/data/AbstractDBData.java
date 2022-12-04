// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;


public abstract class AbstractDBData implements DBData
{
   protected JoriaAccess myAccess;

   protected AbstractDBData(JoriaAccess axs)
   {
      myAccess = axs;
   }

   protected AbstractDBData() // for deserialisation
   {
   }


   public JoriaAccess getAccess()
   {
      return myAccess;
   }


   public JoriaType getActualType()
   {
      return myAccess.getType();
   }

}
