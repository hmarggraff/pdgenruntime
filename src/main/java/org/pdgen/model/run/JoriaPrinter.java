// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import java.io.IOException;

/**
 * User: patrick
 * Date: Jan 14, 2003
 * Time: 1:20:57 PM
 */
public interface JoriaPrinter
{
	void startPage() throws IOException;

	void endPage() throws IOException;

	void printDecoration(GrelViewer gv) throws IOException;

	void printGETextLines(GraphElTextLines textLines);

	void printGEPicture(GraphElPicture picture);

	void printGEText(GraphElText text);

	void printGERect(GraphicElementRect rect);

	void printGELine(GraphElLine line);

	void printMetaFile(GraphElMetaFile data);

	void printGEHtmlText(GraphElHtmlText htmlText);

	void printGERtfText(GraphElRtfText graphElRtfText);

    void printGEStyledText(GraphElStyledText graphElStyledText);
}
