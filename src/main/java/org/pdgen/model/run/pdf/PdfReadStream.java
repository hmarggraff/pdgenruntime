// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run.pdf;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;

public class PdfReadStream {
    public static void main(String[] args) throws IOException, DataFormatException {
        FileInputStream fis = new FileInputStream(args[0]);
        fis.skip(63438);
        byte[] buffer = new byte[2048];
        fis.read(buffer);
        fis.close();

        byte[] compress = new byte[367];
        System.arraycopy(buffer, 58, compress, 0, 367);
        FileOutputStream fos = new FileOutputStream("destOutputProfile.bin");
        fos.write(compress);
        fos.close();
    }
}
