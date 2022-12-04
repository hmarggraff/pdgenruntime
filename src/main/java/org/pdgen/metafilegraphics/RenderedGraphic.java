// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.metafilegraphics;

import java.util.ArrayList;

/**
 * User: hmf at Oct 13, 2004 8:22:54 AM
 * This represents an in-core metafile.
 * the data containsName the sequence of drawing operations
 * The heavyweight objects referenced by the drawing operatrions are in
 * the list of ref objects. (These may be images, Attributed CharacterIterators etc)
 */
public class RenderedGraphic
{
	byte[] data;
	ArrayList<?> refObjects;

	public RenderedGraphic(byte[] data, ArrayList<?> refObjects)
	{
		this.data = data;
		this.refObjects = refObjects;
	}
}
