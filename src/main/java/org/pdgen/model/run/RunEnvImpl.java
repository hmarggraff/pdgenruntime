// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.jetbrains.annotations.NotNull;
import org.pdgen.data.*;

import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.data.view.SortOrder;
import org.pdgen.env.Env;
import org.pdgen.env.Res;
import org.pdgen.model.Template;
import org.pdgen.env.JoriaException;
import org.pdgen.env.JoriaUserDataException;
import org.pdgen.util.MutableLong;

import java.awt.*;
import java.io.OutputStream;
import java.io.Writer;
import java.util.*;
import java.util.List;

public class RunEnvImpl implements RunEnv
{
	private HashMap<JoriaAccess, DBData> variables = new HashMap<>();
	private HashMap<String, String[]> variablesAsStrings;
	Locale locale;
	AggregateCollector pager;
	protected Template template;
	Template startTemplate;
	protected DBData rootVal;
	private Stack<DBData> objectPath = new Stack<>();
	private final Stack<DBData> prevPath = new Stack<>();// stack of previous object in a collection
	private Object databaseConnection;
	private String serviceRootValue;
	private String currentPassword;
	private final Properties userStorage = new Properties();
	private Object collectionRoot;
	private final HashMap<JoriaCollection, SortOrder[]> runtimeOverrides = new HashMap<>();// for settings made in the run window
	private Graphics2D graphics2D;
	private HashMap<?, ?> connectorMap;
	private int transactionCounter;
	private HashMap<JoriaClass, List<JoriaAccess>> physicalAccessors;

	public RunEnvImpl(Template template)
	{
		this.template = template;
		startTemplate = template;
		locale = Env.instance().getCurrentLocale();
		if (locale == null)
			locale = Locale.getDefault();
        Env.instance().getThreadLocalStorage().getMap().put(Locale.class, locale);
		// pseudo entry as root
		if (template != null)
			physicalAccessors = template.getPhysicalAccessors();
	}

	public void putRuntimeParameter(JoriaAccess key, DBData val)
	{
		variables.put(key, val);
	}

	public Locale getLocale()
	{
		return locale;
	}

	public void setLocale(Locale newLocale)
	{
		locale = newLocale;
	}

	void setPager(AggregateCollector pager)
	{
		this.pager = pager;
	}

	public int getDisplayPageNo()
	{
		if (pager != null)
			return pager.getDisplayPageNo();
		return 0;
	}

	public int getTotalPagesNumber()
	{
		if (pager != null)
			return pager.getTotalPagesNumber();
		return 0;
	}

	public Template getTemplate()
	{
		return template;
	}

	public AggregateCollector getPager()
	{
		return pager;
	}

	public DBData popFromObjectPath()
	{
		prevPath.pop();
		return objectPath.pop();
	}

	public DBData topOfObjectPath()
	{
		return objectPath.peek();
	}

	public int lengthOfObjectPath()
	{
		return objectPath.size();
	}

	public DBData topOfObjectPath(int i)
	{
		return objectPath.get(objectPath.size() - 1 - i);
	}

	public void pushToObjectPath(DBData step)
	{
		prevPath.push(null);// init a new layer: PREV OF FIRST OBJECT IS NULL
		objectPath.push(step);
	}

	public DBData topOfPrevs()
	{
		return prevPath.peek();
	}

	void nextToPrevs(DBData step)
	{
		prevPath.set(prevPath.size() - 1, step);
	}


	static Template cri(Template t) throws JoriaException
	{
		if (t == null)
			throw new JoriaUserDataException(Res.str("The_report_template_was_null"));
		return t;
	}

	public DBData loadRootVal() throws JoriaDataException
	{
		rootVal = null;
		if (template.getStarter() != null)
		{
			rootVal = template.getStarter().getValue(null, template.getStarter(), this);
			return rootVal;
		}
		else
			return null;
	}

	public void putCounter(RuntimeParameter key, DBIntImplMutable counter)
	{
		variables.put(key, counter);
	}

	void resetCounters()
	{
		for (Iterator<Map.Entry<JoriaAccess, DBData>> iterator = variables.entrySet().iterator(); iterator.hasNext();)
		{
			Map.Entry<JoriaAccess, DBData> entry = iterator.next();
			DBData value = entry.getValue();
			if (value instanceof MutableLong)
				iterator.remove();
		}
	}

	public DBData getRootVal()
	{
		return rootVal;
	}

	void freeRootVal()
	{
		rootVal = null;// feed it to the hungry garbage eater
		Trace.logGC("After freeing rootVal");//trdone
	}

	protected void startReport(Template t) throws JoriaException
	{
		Env.instance().reportStart(t, this);
	}

	public void endReport(Template t)
	{
		Env.instance().reportEnd(t, this);
	}

	public Object getDatabaseConnection()
	{
		return databaseConnection;
	}

	public void setDatabaseConnection(Object databaseConnection)
	{
		this.databaseConnection = databaseConnection;
	}

	public String getServiceRootValue()
	{
		return serviceRootValue;
	}

	public void setServiceRootValue(String serviceRootValue)
	{
		this.serviceRootValue = serviceRootValue;
	}

	public Properties getUserStorage()
	{
		return userStorage;
	}

	@SuppressWarnings("UnusedDeclaration")
	public Object getCollectionRoot()
	{
		return collectionRoot;
	}

	public void setCollectionRoot(Object collectionRoot)
	{
		this.collectionRoot = collectionRoot;
	}

	public void dismiss()
	{
	}

	public void nextSection() throws JoriaException
	{
		JoriaAccess data = template.getData();
		template = template.getNextSection();
		if (template.getData() != data)
		{
			loadRootVal();
		}
	}

	public HashMap<JoriaCollection, SortOrder[]> getRuntimeOverrides()
	{
		return runtimeOverrides;
	}

	public void copyVariables(RunEnvImpl env) throws JoriaUserDataException
	{
		// TODO hier sollten wir eigentlich die Variablen richtig kopieren
		variables = env.variables;
		variablesAsStrings = env.variablesAsStrings;
		reset();
	}

	public Graphics2D getGraphics2D()
	{
		return graphics2D;
	}

	public Template getStartTemplate()
	{
		return startTemplate;
	}

	public String getCurrentPassword()
	{
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword)
	{
		this.currentPassword = currentPassword;
	}

	public void setGraphics2D(Graphics2D graphics2D)
	{
		this.graphics2D = graphics2D;
	}

	public void reset() throws JoriaUserDataException
	{
		for (Object o : variables.values())
		{
			if (o instanceof DBObject && !((DBObject) o).isValid())
				throw new JoriaUserDataException(Res.str("cannot_rerun_because_of_stale_data", locale));
			if (o instanceof DBCollectionCache)
				((DBCollectionCache) o).clearCache();
			else if (o instanceof DBCollection)
			{
				DBCollection coll = (DBCollection) o;
				coll.reset();
				try
				{
					while (coll.next())
					{
						DBData d = coll.current();
						//noinspection ConstantConditions
						if (d instanceof DBObject && !((DBObject) d).isValid())
							throw new JoriaUserDataException(Res.str("cannot_rerun_because_of_stale_data", locale));
						if (d instanceof DBCollectionCache)
							((DBCollectionCache) d).clearCache();
					}
				}
				catch (JoriaDataException e)
				{
					Trace.log(e);
				}
			}
		}
		resetCounters();
	}

	public boolean exportPdf(OutputStream out) throws JoriaException
	{
		if (askForVariables())
			return false;// cancel was pressed while asking for variables
		PageEnv2 pageRunner = PageEnv2.makePageEnv(false, this);
		pageRunner.exportToPdf(out);
		return true;
	}

	public boolean exportRtf(OutputStream out) throws JoriaException
	{
		if (askForVariables())
			return false;// cancel was pressed while asking for variables
		PageEnv2 pageRunner = PageEnv2.makePageEnv(false, this);
		pageRunner.exportToRtf(out);
		return true;
	}


	public boolean exportXml(Writer out) throws JoriaException
	{
		if (askForVariables())
			return false;// cancel was pressed while asking for variables
		PageEnv2 pageRunner = PageEnv2.makePageEnv(false, this);
		pageRunner.exportToXml(out);
		return true;
	}



	Stack<DBData> getObjectPath()
	{
		return objectPath;
	}

	public void setObjectPath(Stack<DBData> objectPath)
	{
		this.objectPath = objectPath;
	}

	public HashMap<?, ?> getConnectorMap()
	{
		if (connectorMap == null)
		{
			connectorMap = new HashMap<>();
		}
		return connectorMap;
	}

    public boolean isConnectorMapUsed()
    {
        return connectorMap != null;
    }

    public void removeVariable(JoriaAccess key)
	{
		variables.remove(key);
	}


	public boolean isReaskVariables()
	{
		return false;
	}

	public int getTransactionCounter()
	{
		return transactionCounter;
	}

	public void setTransactionCounter(int transactionCounter)
	{
		this.transactionCounter = transactionCounter;
	}

	public HashMap<JoriaClass, List<JoriaAccess>> getPhysicalAccessors()
	{
		return physicalAccessors;
	}

	public boolean askForVariables() throws JoriaException {
		return false;
	}

	@Override
	public DBData getRuntimeParameterValue(@NotNull JoriaAccess param) {
		return variables.get(param);
	}
}
