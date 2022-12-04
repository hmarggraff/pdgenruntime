// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

/**
 * Extends the logic for target sizes to specify how the target size is to be interpreted.
 */
public enum SizeLimit
{
	FlexSize, // as specified by the accompanying flex size
	Fix, // take the specified units as the fixed size
	AtLeast, // Interpret it as the minimum size
	AtMost // Interpret it as the maximum
}
