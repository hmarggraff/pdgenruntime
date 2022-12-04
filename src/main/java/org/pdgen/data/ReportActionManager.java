// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.env.ReportAction;

import java.util.ArrayList;
import java.util.List;

/**
 * User: patrick
 * Date: Nov 7, 2006
 * Time: 10:04:35 AM
 */
public class ReportActionManager
{
	static ArrayList<ReportAction> reportActions;


	public static List<ReportAction> getReportActions()
	{
		return reportActions;
	}
}
