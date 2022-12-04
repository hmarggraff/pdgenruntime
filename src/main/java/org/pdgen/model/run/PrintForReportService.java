// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.Trace;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.JoriaDataExceptionWrapped;
import org.pdgen.env.Env;
import org.pdgen.env.JoriaException;
import org.pdgen.model.Template;
import org.pdgen.env.Res;

import javax.print.PrintService;
import java.awt.print.PrinterJob;

public class PrintForReportService
{

	PageEnv2 pager;
    Graphics2DPrinter printer = new Graphics2DPrinter();
	public static void print(PrintService destPrinter, RunEnvImpl env) throws JoriaDataException
    {
		if (destPrinter != null)
            try
            {
                String name = destPrinter.getName();
                Trace.log(Trace.run, "Printing " + env.getTemplate().getName() + " directly to " + name);
                new PrintForReportService(destPrinter, false, env);
            }
            catch (IllegalArgumentException e)
            {
                Env.instance().handle(e);
            }
            catch (Exception e)
            {
                Env.instance().handle(e);
            }
		else
			new PrintForReportService(null, true, env);
	}

	public static void printTo(String destPrinter, RunEnvImpl env) throws JoriaDataException
    {
        StringBuffer printers = new StringBuffer();
        try
        {
            PrintService[] ps = PrinterJob.lookupPrintServices();
            for (PrintService p : ps)
            {
                String printerName = p.getName();
                if (destPrinter.equals(printerName))
                {
                    new PrintForReportService(p, false, env);
                    return;
                }
                printers.append("\"").append(printerName).append("\" ");
            }
        }
        catch (IllegalArgumentException e)
        {
            Env.instance().handle(e);
        }
        catch (Exception e)
        {
            Env.instance().handle(e);
            return;
        }
        String message = Res.msg("printer_{0}_does_not_exist,_possible_printernames_are:_{1}", env.getLocale(),
                                              destPrinter, printers.toString());
        throw new JoriaDataException(message);
	}

	public static void print(boolean withDialog, RunEnvImpl env) throws JoriaDataException
    {
		new PrintForReportService(null, withDialog, env);
	}

	public PrintForReportService(PrintService ps, boolean withDialog, RunEnvImpl env) throws JoriaDataException
    {
		Template def = env.getTemplate();
		if (def == null)
		{
			return;
		}
		Trace.log(Trace.run, "Printing Template " + def.getName());
		def.getPage();
		/*
        if (AskVariablesDialog.askForVariables(env))
            return; // cancel was pressed while asking for variables
		*/
        pager = PageEnv2.makePageEnv(false, env);
        try
        {
            pager.doPrint(def, ps, withDialog);
        }
        catch (JoriaException e)
        {
            Trace.log(e);
            if(e instanceof JoriaDataException)
                throw (JoriaDataException)e;
            else
                throw new JoriaDataExceptionWrapped(e.getMessage(), e);
        }
	}

	/**
	 * this method exists to hide incompatibiliuty between jdk 1.3 and jdk 1.4
     * @return is the parameter a PrintService
     * @param o should be a PrintService
     */
	public static boolean isPrintService(Object o)
	{
        return o instanceof PrintService;
    }

}
