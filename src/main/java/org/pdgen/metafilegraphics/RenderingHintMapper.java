// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.metafilegraphics;

import java.awt.RenderingHints;
import java.util.HashMap;

/**
 * User: hmf at Oct 10, 2004 10:57:47 AM
 * maps rendering hints to ints that can be serialized
 * and caches the current value to do peephole optimisation.
 */
public class RenderingHintMapper
{
	final byte mapto;
	public Object currVal;

	public RenderingHintMapper(byte mapto)
	{
		this.mapto = mapto;
	}

	static HashMap<RenderingHints.Key, RenderingHintMapper> createKeyMapper()
	{
		HashMap<RenderingHints.Key, RenderingHintMapper> r = new HashMap<RenderingHints.Key, RenderingHintMapper>();

		for (byte i = 0; i< DrawCommands.renderingHintKeyTable.length; i++)
		{
			RenderingHints.Key k = DrawCommands.renderingHintKeyTable[i];
			r.put(k, new RenderingHintMapper(i));
		}

		return r;
	}
	static HashMap<Object, Byte> createValueMapper()
	{
		HashMap<Object, Byte> r = new HashMap<Object, Byte>();

		for (byte i = 0; i< DrawCommands.renderingHintValueTable.length; i++)
		{
			Object v = DrawCommands.renderingHintValueTable[i];
			r.put(v, Byte.valueOf(i));
		}

		return r;
	}
}
