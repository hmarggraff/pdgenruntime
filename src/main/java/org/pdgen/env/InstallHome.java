// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.Trace;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * User: patrick
 * Date: Apr 12, 2005
 * Time: 9:16:29 AM
 */
public abstract class InstallHome
{
	private static File installHome;

	public static File findInstallHome()
	{
		if (installHome != null)
			return installHome;
		URL u = InstallHome.class.getResource("/org/pdgen/env/Res.class");
		if (u == null)
		{
			throw new Error("Panic: Installation home not found, because class A was not found");
		}
		String urlStr = u.toExternalForm();
		final String encoding = System.getProperty("file.encoding");
		try
		{
			urlStr = URLDecoder.decode(urlStr, encoding);
		}
		catch (UnsupportedEncodingException e)
		{
			Trace.logError("installation path cannot be determined, unknown encoding " + encoding + " on " + urlStr);
		}
		if (urlStr.startsWith("jar:file:"))
		{
			int ix1 = urlStr.indexOf("file:") + "file:".length();
			int ix2 = urlStr.indexOf("!");
			String jarstr = urlStr.substring(ix1, ix2);
			File jarf = new File(jarstr);
            installHome = jarf.getParentFile().getParentFile();
			return installHome;
		}
		else if (urlStr.startsWith("file:"))
		{
			int ix1 = urlStr.indexOf("file:") + "file:".length();
			String dirstr = urlStr.substring(ix1);
			File classf = new File(dirstr).getParentFile().getParentFile().getParentFile().getParentFile();
            installHome = classf;
			return classf;
		}
		else
			System.err.println("Could not determine installationDirectory from " + urlStr);
		return null;
	}
}
