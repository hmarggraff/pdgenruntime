// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;


public interface DBReal extends DBData, java.io.Serializable
{

   /* ----------------------------------------------------------------------- getRealValue */

   double getRealValue();
    double NULL = Double.NaN;
}
