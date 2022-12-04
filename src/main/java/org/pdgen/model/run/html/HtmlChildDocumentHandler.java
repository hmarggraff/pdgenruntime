// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run.html;

import org.pdgen.model.run.ImageDetection;

import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Nov 22, 2004
 * Time: 6:36:35 AM
 * To change this template use File | Settings | File Templates.
 */
public interface HtmlChildDocumentHandler
{
    String mapFileNameToUrl(String fileName);
    StreamData getDocumentStream(ImageDetection.ImageClass imageClass) throws IOException;
    PrintWriter getContentWriter();
    PrintWriter getStyleWriter();
    String getDrilldownUrlPrefix();

    void setTitle(String title);

    class StreamData
    {
        public StreamData(String url, OutputStream stream)
        {
            this.url = url;
            this.stream = stream;
        }
        public String getUrl()
        {
            return url;
        }

        public OutputStream getStream()
        {
            return stream;
        }

        protected String url;
        protected OutputStream stream;

    }
}
