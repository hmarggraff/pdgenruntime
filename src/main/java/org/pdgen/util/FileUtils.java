// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * User: patrick
 * Date: Feb 13, 2006
 * Time: 9:29:35 AM
 */
public class FileUtils
{
	public static boolean copyFile(File f, File dest)
	{
		try
		{
			FileInputStream fis = new FileInputStream(f);
			File destFile;
			if (dest.isDirectory())
			{
				String inf = f.getName();
				destFile = new File(dest, inf);
			}
			else
				destFile = dest;
			File copyDest = File.createTempFile(f.getName(), ".tmp", destFile.getParentFile());
			FileOutputStream df = new FileOutputStream(copyDest);
			int nRead;
			byte[] buf = new byte[-Short.MIN_VALUE];
			do
			{
				nRead = fis.read(buf);
				if (nRead > 0)
					df.write(buf, 0, nRead);
			}
			while (nRead > 0);
			df.close();
			fis.close();

            //noinspection ResultOfMethodCallIgnored
            destFile.delete();
            return copyDest.renameTo(destFile);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	public static void close(Closeable r)
	{
		if (r != null)
		{
			try
			{
				r.close();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * rewrites text file and adds line at end
	 *
	 * @param f the file to rewrite
	 * @param s the line to add at the end
	 * @throws IOException if underlying streams fail
	 */
	@SuppressWarnings("UnusedDeclaration")
    public static void addLine(File f, String s) throws IOException
	{
		if (f.exists())
		{
			BufferedReader r = new BufferedReader(new FileReader(f));
			final File tempFile = File.createTempFile("addline", ".tmp", f.getParentFile());
			PrintWriter w = new PrintWriter(tempFile);
			String l;
			while (null != (l = r.readLine()))
			{
				w.println(l);
			}
			w.println(s);
			w.close();
			r.close();
			if (!f.delete())
				throw new IOException("Could not delete file for rewiting: " + f.getName());
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				// ignore
			}
			int count = 0;
			while (!tempFile.renameTo(f))
			{
				count++;
				if (count == 20)
				{
					throw new IOException("Could not rewrite file: " + f.getName());
				}
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					// ignore
				}
			}
		}
		else
		{
			PrintWriter w = new PrintWriter(f);
			w.println(s);
			w.close();
		}
	}

	@SuppressWarnings("UnusedDeclaration")
    public static void addLine(File f, String s, String except) throws IOException
	{
		if (f.exists())
		{
			BufferedReader r = new BufferedReader(new FileReader(f));
			final File tempFile = File.createTempFile("addline", ".tmp", f.getParentFile());
			PrintWriter w = new PrintWriter(tempFile);
			String l;
			while (null != (l = r.readLine()))
			{
				if (!l.trim().startsWith(except))
					w.println(l);
			}
			w.println(s);
			w.close();
			r.close();
			if (!f.delete())
				throw new IOException("Could not delete file for rewriting: " + f.getName());
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				// ignore
			}
			int count = 0;
			while (!tempFile.renameTo(f))
			{
				count++;
				if (count == 20)
				{
					throw new IOException("Could not rewrite file: " + f.getName());
				}
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					// ignore
				}
			}
		}
		else
		{
			PrintWriter w = new PrintWriter(f);
			w.println(s);
			w.close();
		}
	}

	@SuppressWarnings("UnusedDeclaration")
    public static File findOneMatchingFile(File dir, final String prefix, final String end)
	{
		String[] files = dir.list(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.length() >= prefix.length() + end.length() && name.startsWith(prefix) && name.endsWith(end);
			}
		});
		if (files.length == 1)
			return new File(dir, files[0]);
		else
			return null;
	}
	public static File[] findMatchingFilesWithExt(File dir, final String end)
	{
        return dir.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.length() > end.length() && name.endsWith(end);
            }
        });
	}
	public static File[] findSubDirs(File dir)
	{
        return dir.listFiles(new FileFilter()
        {
            public boolean accept(File file)
            {
                return file.isDirectory();
            }
        });
	}

	// Returns the contents of the file in a byte array.
	@SuppressWarnings("UnusedDeclaration")
    public static byte[] getBytesFromFile(File file) throws IOException {
	    InputStream is = new FileInputStream(file);

	    // Get the size of the file
	    long length = file.length();

	    // You cannot create an array using a long type.
	    // It needs to be an int type.
	    // Before converting to an int type, check
	    // to ensure that file is not larger than Integer.MAX_VALUE.
	    if (length > Integer.MAX_VALUE) {
	        // File is too large
			throw new IllegalArgumentException("file is too large");
	    }

	    // Create the byte array to hold the data
	    byte[] bytes = new byte[(int)length];

	    // Read in the bytes
	    int offset = 0;
	    int numRead;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }

	    // Ensure all the bytes have been read in
	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file "+file.getName());
	    }

	    // Close the input stream and return bytes
	    is.close();
	    return bytes;
	}


}
