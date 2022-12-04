// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.CellDef;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.Trace;
import org.pdgen.env.Env;
import org.pdgen.model.RDBase;
import org.pdgen.model.TemplateModel;
import org.pdgen.env.JoriaException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * User: patrick
 * Date: Jul 12, 2005
 * Time: 8:35:37 AM
 */
public class CsvOutput2 extends NonPagedOutput
{
    public static boolean writeCsv(OutputStream output, byte separator, byte qoutechar, byte[] lineSep, boolean includeHeaderFooter, String encoding, RunEnvImpl env, boolean hasVariables) throws JoriaException
    {
        if (!hasVariables && env.askForVariables())
            return false; // cancel was pressed while asking for variables
        CsvOutput2 out = new CsvOutput2(output, separator, qoutechar, lineSep, includeHeaderFooter, encoding, env);
        out.doOutput();
        return true;
    }
    private static final byte[] lineSeparatorDefault = OutputMode.determinePlatformLineSeparator(); // may not be final
    OutputStream w;
    private byte sep = 0x2c;
    private String sepString = "\"";
    private byte qot = 0x22;
    private boolean separatorRequired;
    private final byte[] lineSep;
    private final String encoding;
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
    private final boolean includeHeaderFooter;

    private CsvOutput2(OutputStream output, byte separator, byte quotechar, byte[] lineSep, boolean includeHeaderFooter, String encoding, RunEnvImpl env)
    {
        super(env);
        if(!(output instanceof BufferedOutputStream) && !(output instanceof ByteArrayOutputStream))
            w = new BufferedOutputStream(output);
        else
            w = output;
        if(separator != -1)
        {
            sep = separator;
            sepString = new String(new byte[] {sep});
        }
        if(quotechar != -1)
            qot = quotechar;
        if(lineSep == null)
            this.lineSep = lineSeparatorDefault;
        else
            this.lineSep = lineSep;
        this.includeHeaderFooter = includeHeaderFooter;
        this.encoding = encoding;
    }

    protected boolean addNullCellInt(RunRangeBase rr, CellDef cd, int row, int col, boolean firstRepeat) throws JoriaDataException
    {
        if (separatorRequired)
            wwrite(sep);
        else
            separatorRequired = true;
        out.colInCurrRow++;
        return false;
    }

    protected void addSpanInt(int count) throws JoriaDataException
    {
        separatorRequired = true;
        for (int i = 0; i < count; i++)
        {
            wwrite(sep);
        }
        out.colInCurrRow += count;
    }

    protected void generateOutputInt(RunRangeBase rr, CellDef cd, int row, int c) throws JoriaDataException
    {
        if (separatorRequired)
            wwrite(sep);
        else
            separatorRequired = true;
        if(rr.values == null)
        {
            out.colInCurrRow++;
            Trace.logWarn("generateOutput no values");
            return;
        }
        RVAny value = rr.values.subs[row][c];
        if(value instanceof RValue)
        {
            RValue val = (RValue)value;
            if(val instanceof RVStringCol)
            {
                ((RVStringCol)val).buildFormattedStrings(cd, aggs.getRunEnv().getLocale());
            }
            String fs = val.get(rr.getIteration());
            if (fs != null)
            {
                boolean needQoutes = false;
                try
                {
                    if (fs.indexOf(sep) >= 0 || fs.indexOf(' ') >= 0 || fs.indexOf('\t') >= 0 || fs.indexOf('\n') >= 0 || fs.indexOf('\r') >= 0)
                    {
                        if(qot != 0)
                        {
                            bos.write(qot);
                            needQoutes = true;
                        }
                        else if(fs.indexOf(sep) >= 0){

                            fs = fs.replace(sepString, "");
                        }
                    }
                    int at;
                    int lastAt = 0;
                    while (0 <= (at = fs.indexOf(qot, lastAt)))
                    {
                        String t = fs.substring(lastAt, at);
                        lastAt = at + 1;
                        if (encoding != null)
                            bos.write(t.getBytes(encoding));
                        else
                            bos.write(t.getBytes());
                        bos.write((byte) '\\');
                        bos.write(qot);
                    }
                    if (lastAt == 0)
                    {
                        if (encoding != null)
                            bos.write(fs.getBytes(encoding));
                        else
                            bos.write(fs.getBytes());
                    }
                    else if (lastAt < fs.length() - 1)
                    {
                        String remainder = fs.substring(lastAt);
                        if (encoding != null)
                            bos.write(remainder.getBytes(encoding));
                        else
                            bos.write(remainder.getBytes());
                    }
                    if (needQoutes)
                        bos.write(qot);
                    bos.writeTo(w);
                    bos.reset();
                }
                catch (IOException e)
                {
                    bos.reset();
                    throw new JoriaDataException("I/O error during CSV export. " + e.getMessage());
                }
            }
        }
        out.colInCurrRow++;
    }

    protected void processOneFrame(RDBase[][] fields, RVTemplate rvt, TemplateModel template)
    {
        // not needed here
    }

    protected void preprocess()
    {
        // not needed here
    }

    protected void postprocess()
    {
        try
        {
            w.flush();
        }
        catch (IOException e)
        {
            Env.instance().handle(e);
        }
    }

    protected boolean includeHeaderFooterFrames()
    {
        return includeHeaderFooter;
    }

    protected void startOneFrame(int frameType)
    {
        // not needed here
    }

    protected void endOneFrame()
    {
        // not needed here
    }

    protected void startOneRow()
    {
        // not needed here
    }

    protected void endOneRow() throws JoriaDataException
    {
        separatorRequired = false;
        try
        {
            w.write(lineSep);
        }
        catch (IOException e)
        {
            throw new JoriaDataException("An I/O error occured during CSV export. " + e.getMessage());
        }
    }

    protected boolean includeAllHeaderAndFooterFrames()
    {
        return false;
    }

    private void wwrite(byte b) throws JoriaDataException
    {
        try
        {   if(b != 0)
                w.write(b);
        }
        catch (IOException e)
        {
            throw new JoriaDataException("An I/O error occured during CSV export. " + e.getMessage());
        }
    }
}
