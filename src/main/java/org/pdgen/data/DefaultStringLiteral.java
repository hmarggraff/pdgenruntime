// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;
//MARKER The strings in this file shall not be translated
import java.io.ObjectStreamException;


public class DefaultStringLiteral extends AbstractJoriaLiteral
{
   static final long         serialVersionUID = -1L;
   static DefaultStringLiteral instance = new DefaultStringLiteral();

   /* ----------------------------------------------------------------------- instance */

   public static DefaultStringLiteral instance()
   {
      return instance;
   }


   /* ----------------------------------------------------------------------- getName */

   public String getName()
   {
      return "String";
   }

   /* ----------------------------------------------------------------------- getParamString */

   public String getParamString()
   {
      return "DefaultStringLiteral";
   }

   /* ----------------------------------------------------------------------- isRealLiteral */

   public boolean isStringLiteral()
   {
      return true;
   }

   /* ----------------------------------------------------------------------- isLiteral */

   public boolean isLiteral()
   {
      return true;
   }

   /* ----------------------------------------------------------------------- readResolve */

   protected Object readResolve() throws ObjectStreamException
   {
      return instance();
   }
}
