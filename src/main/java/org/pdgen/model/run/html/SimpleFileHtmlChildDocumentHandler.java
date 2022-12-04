// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run.html;

import org.pdgen.data.JoriaAssertionError;
import org.pdgen.env.Env;
import org.pdgen.env.Settings;
import org.pdgen.env.JoriaUserException;
import org.pdgen.model.run.ImageDetection;
import org.pdgen.env.Res;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Nov 22, 2004
 * Time: 7:08:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleFileHtmlChildDocumentHandler implements HtmlChildDocumentHandler
{
    protected File outputDirectory;
    protected static boolean windows;
    protected PrintWriter outPW;
    protected CharArrayWriter styleCAW  = new CharArrayWriter();
    protected CharArrayWriter contentCAW = new CharArrayWriter();
    protected String title;
    static
    {
        String osName = System.getProperty("os.name"); //trdone
        windows = osName.toLowerCase().startsWith("window"); //trdone

    }
    public SimpleFileHtmlChildDocumentHandler(File outputFile) throws FileNotFoundException
    {
        String property = Settings.get("htmlOutputDirectory"); //trdone
        if(property != null)
        {
            outputDirectory = new File(property);
            if(!outputDirectory.exists())
                outputDirectory = null;
        }
        if(outputDirectory == null)
            outputDirectory = outputFile.getParentFile();

        outPW = new PrintWriter(new FileOutputStream(outputFile));
    }

    public SimpleFileHtmlChildDocumentHandler(String directory, OutputStream outStream) throws JoriaUserException
    {
        outputDirectory = new File(directory);
        if(!outputDirectory.exists())
            outputDirectory = null;
        if(outputDirectory == null)
        {
            String property = Settings.get("htmlOutputDirectory"); //trdone
            if(property != null)
            {
                outputDirectory = new File(property);
                if(!outputDirectory.exists())
                    outputDirectory = null;
            }
        }
        if(outputDirectory == null)
            throw new JoriaUserException("no valid directory for html output files");
        outPW = new PrintWriter(outStream);
    }

    public SimpleFileHtmlChildDocumentHandler(OutputStream outStream) throws JoriaUserException
    {
        String property = Settings.get("htmlOutputDirectory"); //trdone
        if(property != null)
        {
            outputDirectory = new File(property);
            if(!outputDirectory.exists())
                outputDirectory = null;
        }
        if(outputDirectory == null)
            throw new JoriaUserException(Res.str("no_valid_directory_for_html_output_files"));
        outPW = new PrintWriter(outStream);
    }

    public String mapFileNameToUrl(String fileName)
    {
        fileName = Env.instance().getFileService().makeFilenameAbsolute(fileName);
        StringBuffer retVal = new StringBuffer(fileName.length() * 2);
        retVal.append("file:///"); //trdone
        for(int i = 0; i < fileName.length(); i++)
        {
            char ch = fileName.charAt(i);
            if(ch == File.separatorChar)
                retVal.append("/");
            else if((ch >= '0' && ch <= '9') ||
                    (ch >= 'a' && ch <= 'z') ||
                    (ch >= 'A' && ch <= 'Z') ||
                    (ch == ':' && windows))
                retVal.append(ch);
            else if(ch < 256)
            {
                retVal.append("%");
                String hex = Integer.toHexString(ch);
                if(hex.length() == 1)
                    retVal.append('0');
                retVal.append(hex);
            }
            else
                throw new JoriaAssertionError("in url's only characters smaller than 256 are supported");
        }
        return retVal.toString();
    }

    public StreamData getDocumentStream(ImageDetection.ImageClass imageClass) throws IOException
    {
        File outFile = File.createTempFile("file", imageClass.getDefaultExtention(), outputDirectory); //trdone
        OutputStream outStream = new BufferedOutputStream(new FileOutputStream(outFile));
        return new StreamData(mapFileNameToUrl(outFile.getAbsolutePath()), outStream);
    }

    public PrintWriter getContentWriter()
    {
        return new PrintWriter(contentCAW);
    }

    public PrintWriter getStyleWriter()
    {
        return new PrintWriter(styleCAW);
    }

    public String getDrilldownUrlPrefix()
    {
        return null; // no drilldown here
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    protected void printReportHeaderTop()
    {
        outPW.print("<html>\n<head>\n<title>"); //trdone
        outPW.print(title);
        outPW.println("</title>\n<style type=\"text/css\">"); //trdone
        outPW.println("<!-- ");
    }

    protected void printReportHeaderBottom()
    {
        outPW.println(" -->");
        outPW.println("</style></head>"); //trdone
    }

    protected void printReportBodyTop()
    {
        outPW.println("<body>\n<table border=\"0\" width=\"100%\">"); //trdone
    }

    protected void printReportBodyBottom()
    {
        outPW.println("</table>\n</body>\n</html>"); //trdone
    }

    public void completeDocument()
    {
        printReportHeaderTop();
        outPW.write(styleCAW.toCharArray());
        printReportHeaderBottom();
        printReportBodyTop();
        outPW.write(contentCAW.toCharArray());
        printReportBodyBottom();
        outPW.flush();
    }
}
