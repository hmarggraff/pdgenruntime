// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

public interface TotalsAccumulator
{
	void rowComplete();
	void reset(int scope);
	void add(double nVal);
	void add(String nVal);
	double getDoubleVal(int scope);
	String getStringVal(int scope);
	long getLongVal(int scope);
	void redoRow();
}
