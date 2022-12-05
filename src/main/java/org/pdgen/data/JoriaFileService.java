// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.env.JoriaUserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Set;

/**
 * User: patrick
 * Date: Mar 23, 2005
 * Time: 10:09:17 AM
 */
public interface JoriaFileService {
    InputStream getFileData(String filename) throws IOException;

    byte[] getFileBytes(String filename) throws IOException;

    URL getFileAsURL(String filename) throws IOException;

    OutputStream setFileData(String filename) throws IOException;

    boolean existsAsFile(String filename) throws IOException;

    String[] getParentDirectoryList(String filename) throws IOException;

    String getDirectory(String filename) throws IOException;

    String getFileName(String filename) throws IOException;

    String getCanonicalPath(String filename) throws IOException;

    long getFileLastModified(String filename) throws IOException;

    boolean lockRepository() throws JoriaDataExceptionWrapped;

    boolean unlockRepository() throws JoriaDataExceptionWrapped;

    boolean commitRepository() throws JoriaDataExceptionWrapped, IOException, JoriaUserException;

    boolean canCreateNewOrReconnect();

    void setRelativeRootFile(String root);

    String makeFilenameRelative(String filename);

    String makeFilenameAbsolute(String filename);

    Set<String> getUsedFiles();

    void removeFromUsed(String absolutePath);

    JoriaFileService createCopy();

    String getRelativeRoot();

    void setRelativeRoot(String relativeRoot);
}
