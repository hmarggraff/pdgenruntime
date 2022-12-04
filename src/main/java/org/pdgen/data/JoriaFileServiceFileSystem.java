// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

//MARKER The strings in this file shall not be translated

import org.pdgen.env.JoriaUserException;
import org.pdgen.env.Env;

import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * User: patrick
 * Date: Mar 23, 2005
 * Time: 10:24:13 AM
 */
public class JoriaFileServiceFileSystem implements JoriaFileService
{
	public JoriaFileServiceFileSystem(String relativeRoot)
	{
		this(relativeRoot, null);
	}

	public JoriaFileServiceFileSystem(String relativeRoot, Set<String> usedFiles)
	{
		//noinspection EmptyCatchBlock
		try
		{
			if (relativeRoot != null)
				this.relativeRoot = new File(relativeRoot).getCanonicalPath();
		}
		catch (IOException e)
		{
		}
		if (usedFiles != null)
			this.usedFiles = usedFiles;
	}

	public void setRelativeRootFile(String root)
	{
		//noinspection EmptyCatchBlock
		try
		{
			if (root != null)
            {
                relativeRoot = makeAbsoluteFile(root).getCanonicalFile().getParent();
            }
			else
				relativeRoot = null;
		}
		catch (IOException e)
		{
		}
	}

	public File makeAbsoluteFile(String filename)
	{
		//System.out.println(Res.asis("starting file name: ")+filename);
		if (filename == null)
			return null;
		File f = new File(filename);
		if (mapFiles && Env.instance().repo() != null)
		{
			f = new File(Env.instance().repo().mapDir(f.getPath()));
			filename = f.getPath();
			//System.out.println(Res.asis("filename after mapping:")+filename);
		}
		if (relativeRoot != null & !f.isAbsolute())
		{
			f = new File(relativeRoot, filename);
			//System.out.println(Res.asis("filename after making absolute:")+f.getPath());
		}
		return f;
	}

	public InputStream getFileData(String filename) throws IOException
	{
		usedFiles.add(makeFilenameAbsolute(filename));
		return new BufferedInputStream(new FileInputStream(makeAbsoluteFile(filename)));
	}
	public byte[] getFileBytes(String filename) throws IOException
	{
		usedFiles.add(makeFilenameAbsolute(filename));
		final File file = makeAbsoluteFile(filename);
		InputStream is = new FileInputStream(file);

	    // Get the size of the file
	    long length = file.length();

	    // Create the byte array to hold the data
	    byte[] bytes = new byte[(int)length];

	    // Read in the bytes
	    int offset = 0;
	    int numRead = 0;
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

	public URL getFileAsURL(String filename) throws IOException
	{
		return makeAbsoluteFile(filename).toURI().toURL();
	}

	public OutputStream setFileData(String filename) throws IOException
	{
		usedFiles.add(filename);
		File file = makeAbsoluteFile(filename);
		OpenFileData ofd = new OpenFileData();
		if (file.exists())
		{
			ofd.target = file;
			String name = file.getAbsolutePath() + "~~";
			int index = 1;
			for (file = new File(name + index); file.exists(); index++, file = new File(name + index))
			{
				
			}
			ofd.source = file;
		}
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
		ofd.stream = bufferedOutputStream;
		openedFiles.add(ofd);
		return bufferedOutputStream;
	}

	public boolean existsAsFile(String filename) throws IOException
	{
		File f = makeAbsoluteFile(filename);
		return f.exists() && f.isFile();
	}

	public String[] getParentDirectoryList(String filename) throws IOException
	{
		File f = makeAbsoluteFile(filename);
		if (!f.exists())
			return null;
		return f.getParentFile().list();
	}

	public String getDirectory(String filename) throws IOException
	{
		File f = makeAbsoluteFile(filename);
		return f.getParent();
	}

	public String getFileName(String filename) throws IOException
	{
		File f = makeAbsoluteFile(filename);
		return f.getName();
	}

	public String getCanonicalPath(String filename) throws IOException
	{
		File f = makeAbsoluteFile(filename);
		return f.getCanonicalPath();
	}

	public long getFileLastModified(String filename) throws IOException
	{
		File f = makeAbsoluteFile(filename);
		return f.lastModified();
	}

	public boolean lockRepository()
	{
		return true;
	}

	public boolean unlockRepository()
	{
		return true;
	}

	public boolean commitRepository() throws IOException, JoriaUserException
	{
		for (OpenFileData ofd : openedFiles)
		{
			ofd.stream.close();
			if (ofd.target != null)
			{
				ofd.target.delete();
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					// ignore
				}
				int count = 0;
				while (!ofd.source.renameTo(ofd.target))
				{
					count++;
					if (count == 11)
					{
						String msg = MessageFormat.format("cannot rename file from {0} to {1}", ofd.source.getAbsolutePath(), ofd.target.getAbsolutePath());
						throw new JoriaUserException(msg);
					}
					try
					{
						Thread.sleep(100 * count);
					}
					catch (InterruptedException e)
					{
						// ignore
					}
				}
			}
		}
		openedFiles.clear();
		return true;
	}

	public boolean canCreateNewOrReconnect()
	{
		return true;
	}

	public Set<String> getUsedFiles()
	{
		return usedFiles;
	}

	public void removeFromUsed(String absolutePath)
	{
		usedFiles.remove(absolutePath);
	}

	public JoriaFileService createCopy()
	{
		return new JoriaFileServiceFileSystem(relativeRoot, usedFiles);
	}

	public String makeFilenameRelative(String filename)
	{
		if (relativeRoot == null || "".equals(relativeRoot) || filename == null || !new File(filename).isAbsolute() || "".equals(filename))
			return filename;
		try
		{
			filename = new File(filename).getCanonicalPath();
		}
		catch (IOException e)
		{
			return filename;
		}
		if (filename.equals(relativeRoot))
		{
			filename = ".";
		}
		else if (filename.startsWith(relativeRoot))
		{
			char separator = filename.charAt(relativeRoot.length());
			if (separator == File.separatorChar || separator == '/')
			{
				return filename.substring(relativeRoot.length() + 1).replace(File.separatorChar, '/');
			}
		}
		else
		{
			filename = relPath(relativeRoot + "/", filename);
		}
		return filename;
	}

	public String makeFilenameAbsolute(String filename)
	{
		if (filename == null)
			return null;
        return makeAbsoluteFile(filename).getAbsolutePath();
	}

	public boolean isMapFiles()
	{
		return mapFiles;
	}

	public void setMapFiles(boolean mapFiles)
	{
		this.mapFiles = mapFiles;
	}

	private boolean mapFiles = true;
	private String relativeRoot;
	Set<String> usedFiles = new HashSet<String>();
	ArrayList<OpenFileData> openedFiles = new ArrayList<OpenFileData>();

	private static class OpenFileData
	{
		private BufferedOutputStream stream;
		private File target;
		private File source;
	}

	public String getRelativeRoot()
	{
		return relativeRoot;
	}

    public void setRelativeRoot(String relativeRoot)
    {
        this.relativeRoot = relativeRoot;
    }

    public static String relPath(String s, String t)
	{
		//System.out.print("relPath " + s + " --> " + t);
		s = s.replace(File.separatorChar, '/');
		t = t.replace(File.separatorChar, '/');
		int at = 0;
		for (; ;)
		{
			int to = s.indexOf('/', at + 1);
			if (to < 0)
				break;
			if (!t.regionMatches(at, s, at, to - at))
				break;
			at = to;
		}
		if (s.charAt(at) == '/')
			s = s.substring(at + 1);
		else
			s = s.substring(at);
		if (t.charAt(at) == '/')
			t = t.substring(at + 1);
		else
			t = t.substring(at);
		at = -1;
		StringBuffer sb = new StringBuffer(t.length() + 30);
		while ((at = s.indexOf('/', at + 1)) > 0)
		{
			sb.append("../");
		}
		sb.append(t);
		// System.out.println(" = " + sb.toString());
		return sb.toString();
	}
}
