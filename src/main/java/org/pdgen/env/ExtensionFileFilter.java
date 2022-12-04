// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A convenience implementation of FileFilter that filters out
 * all files except for those type extensions that it knows about.
 * <p/>
 * Extensions are of the type ".foo", which is typically found on
 * Windows and Unix boxes, but not on Macinthosh. Case is ignored.
 * <p/>
 * Example - create a new filter that filerts out all files
 * but gif and jpg image files:
 * <p/>
 * JFileChooser chooser = new JFileChooser();
 * ExtensionFileFilter filter = new ExtensionFileFilter(
 * new String{"gif", "jpg"}, "JPEG & GIF Images")
 * chooser.addChoosableFileFilter(filter);
 * chooser.showOpenDialog(this);
 *
 */
public class ExtensionFileFilter extends FileFilter
{
	private Hashtable<String,ExtensionFileFilter> filters;
	private String description;
	private String fullDescription;
	private boolean useExtensionsInDescription = true;

	/**
	 * Creates a file filter. If no filters are added, then all
	 * files are accepted.
	 *
	 * @see #addExtension
	 */
	public ExtensionFileFilter()
	{
		filters = new Hashtable<String,ExtensionFileFilter>();
	}

	/**
	 * Creates a file filter that accepts files with the given extension.
	 * Example: new ExtensionFileFilter("jpg");
	 *
	 * @see #addExtension
	 */
	public ExtensionFileFilter(String extension)
	{
		this(extension, null);
	}

	/**
	 * Creates a file filter that accepts the given file type.
	 * Example: new ExtensionFileFilter("jpg", "JPEG Image Images");
	 * <p/>
	 * Note that the "." before the extension is not needed. If
	 * provided, it will be ignored.
	 *
	 * @see #addExtension
	 */
	public ExtensionFileFilter(String extension, String description)
	{
		this();
		if (extension != null)
			addExtension(extension);
		if (description != null)
			setDescription(description);
	}

	/**
	 * Creates a file filter from the given string array.
	 * Example: new ExtensionFileFilter(String {"gif", "jpg"});
	 * <p/>
	 * Note that the "." before the extension is not needed adn
	 * will be ignored.
	 *
	 * @see #addExtension
	 */
	public ExtensionFileFilter(String[] filters)
	{
		this(filters, null);
	}

	/**
	 * Creates a file filter from the given string array and description.
	 * Example: new ExtensionFileFilter(String {"gif", "jpg"}, "Gif and JPG Images");
	 * <p/>
	 * Note that the "." before the extension is not needed and will be ignored.
	 *
	 * @see #addExtension
	 */
	public ExtensionFileFilter(String[] filters, String description)
	{
		this();
		for (String filter : filters) {
			// add filters one by one
			addExtension(filter);
		}
		if (description != null)
			setDescription(description);
	}

	/**
	 * Return true if this file should be shown in the directory pane,
	 * false if it shouldn't.
	 * <p/>
	 * Files that begin with "." are ignored.
	 *
	 * @see #getExtension
	 * @see FileFilter#accept
	 */
	public boolean accept(File f)
	{
		if (f == null) {
			return false;
		}
		if (f.isDirectory())
		{
			return true;
		}
		return getExtension(f) != null && filters.get(getExtension(f)) != null;
	}

	/**
	 * Return the extension portion of the file's name .
	 *
	 * @see #getExtension
	 * @see FileFilter#accept
	 */
	public String getExtension(File f)
	{
		if (f != null)
		{
			String filename = f.getName();
			int i = filename.lastIndexOf('.');
			if (i > 0 && i < filename.length() - 1)
			{
				return filename.substring(i + 1).toLowerCase();
			}
		}
		return null;
	}

	/**
	 * Adds a filetype "caret" extension to filter against.
	 * <p/>
	 * For example: the following code will create a filter that filters
	 * out all files except those that end in ".jpg" and ".tif":
	 * <p/>
	 * ExtensionFileFilter filter = new ExtensionFileFilter();
	 * filter.addExtension("jpg");
	 * filter.addExtension("tif");
	 * <p/>
	 * Note that the "." before the extension is not needed and will be ignored.
	 */
	public void addExtension(String extension)
	{
		if (filters == null)
		{
			filters = new Hashtable<String,ExtensionFileFilter>(5);
		}
		filters.put(extension.toLowerCase(), this);
		fullDescription = null;
	}

	/**
	 * Returns the human readable description of this filter. For
	 * example: "JPEG and GIF Image Files (*.jpg, *.gif)"
	 *
	 * @see FileFilter#getDescription
	 */
	public String getDescription()
	{
		if (fullDescription == null)
		{
			if (description == null || isExtensionListInDescription())
			{
				fullDescription = description == null ? "(" : description + " (";
				// build the description from the extension list
				Enumeration<?> extensions = filters.keys();
				if (extensions != null)
				{
					fullDescription += "*." + extensions.nextElement();
					while (extensions.hasMoreElements())
					{
						fullDescription += ", *." + extensions.nextElement();
					}
				}
				fullDescription += ")";
			}
			else
			{
				fullDescription = description;
			}
		}
		return fullDescription;
	}

	/**
	 * Sets the human readable description of this filter. For
	 * example: filter.setDescription("Gif and JPG Images");
	 *
	 */
	public void setDescription(String description)
	{
		this.description = description;
		fullDescription = null;
	}

	/**
	 * Determines whether the extension list (.jpg, .gif, etc) should
	 * show up in the human readable description.
	 * <p/>
	 * Only relevent if a description was provided in the constructor
	 * or using setDescription();
	 *
	 */
	public void setExtensionListInDescription(boolean b)
	{
		useExtensionsInDescription = b;
		fullDescription = null;
	}

	/**
	 * Returns whether the extension list (.jpg, .gif, etc) should
	 * show up in the human readable description.
	 * <p/>
	 * Only relevent if a description was provided in the constructor
	 * or using setDescription();
	 *
	 */
	public boolean isExtensionListInDescription()
	{
		return useExtensionsInDescription;
	}

	public String toString()
	{
		return getDescription();
	}
}
