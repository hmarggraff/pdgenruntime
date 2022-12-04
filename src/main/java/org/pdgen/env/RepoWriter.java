// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Utility functions need during repo saving
 */
public class RepoWriter {

    public static void save(String absolutePath) throws IOException {
        new RepoWriter().saveRepo(absolutePath);
    }

    public boolean saveRepo(String absolutePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(absolutePath)) {
            ObjectOutputStream ser = new ObjectOutputStream(fos);
            ser.writeObject(Env.schemaInstance);
            ser.writeObject(Env.instance().repo());
        }
        return false;
    }
}
