// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

/**
 * one specific exception to break out of a running template if an exception occured deep inside
 */
public class RunBreakException extends Error
{
    private static final long serialVersionUID = 7L;

    RunBreakException(Throwable ex)
   {
      super(ex);
   }

}
