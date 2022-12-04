// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.Filter;
import org.pdgen.model.run.RunEnv;
import org.pdgen.oql.OQLNode;
import org.pdgen.env.Res;

import java.util.Date;
import java.util.List;

public class JoriaPlaceHolderAccess implements JoriaAccessTyped
{
    private static final long serialVersionUID = 7L;
    private final String oldName;
	private final String info;
	private transient String val;

	public JoriaPlaceHolderAccess(String oldName, String info)
	{
		this.oldName = oldName;
		this.info = info;
	}

	public String getLongName()
	{
		return Res.asis("Modified Field: ") + oldName;
	}

	public String getName()
	{
		return Res.asis("modified_") + oldName;
	}

	public JoriaClass getDefiningClass()
	{
		return null;
	}

	public JoriaType getType()
	{
		return DefaultStringLiteral.instance();
	}

	public JoriaCollection getCollectionTypeAsserted()
	{
		return null;
	}

	public JoriaClass getClassTypeAsserted()
	{
		return null;
	}

	public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException
	{
		if (val == null)
			val = Res.msg("Modified_field__0_was__1", oldName, info);
		return new DBStringImpl(this, val);
	}

	public boolean isRoot()
	{
		return false;
	}

	public boolean isTransformer()
	{
		return false;
	}

	public boolean isAccessTyped()
	{
		return true;
	}

	public String getXmlTag()
	{
		return Res.asis("PlaceHolderForModifiedAccess");
	}

	public boolean isExportingAsXmlAttribute()
	{
		return false;
	}

	public JoriaCollection getSourceCollection()
	{
		return null;
	}

	@Override
	public String getCascadedHostFilter()
	{
		return null;
	}

	public OQLNode getCascadedOQLFilter()
	{
		return null;
	}

	public String getCascadedOQLFilterString()
	{
		return null;
	}

	public void getCascadedOQLFilterList(List<Filter> collector)
	{
	}

	public String getInfo()
	{
		return info;
	}

	public long getIntValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		throw new JoriaAssertionError("Can only get a String from a JoriaPlaceHolderAccess");
	}

	public double getFloatValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		throw new JoriaAssertionError("Can only get a String from a JoriaPlaceHolderAccess");
	}

	public int getBooleanValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		throw new JoriaAssertionError("Can only get a String from a JoriaPlaceHolderAccess");
	}

	public String getStringValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		if (val == null)
			val = Res.msg("Modified_field__0_was__1", oldName, info);
		return val;
	}

	public Date getDateValue(DBObject from, RunEnv env)
	{
		throw new JoriaAssertionError("Can only get a String from a JoriaPlaceHolderAccess");
	}

	public Object getPictureValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		throw new JoriaAssertionError("Can only get a String from a JoriaPlaceHolderAccess");
	}

    public JoriaAccess getPlaceHolderIfNeeded()
	{
		return this;
	}
}
