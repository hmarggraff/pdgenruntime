package org.pdgen.runtimeapi.beans;

import org.pdgen.runtimeapi.JoriaException;

import java.util.Properties;

/**
 * DataAccessTrigger must be implemented by users of the beans interface
 * Its methods are called during a reporting session to get access to the data
 * in the report. Pass the name of the implementing class to ReportsAnywhere via the xml based curtain definition file.
 * ReportsAnywhere expects a public no arg constructor. This constructor is called when ReportsAnywhere
 * starts accessing data. If a database has to be opened, this is done best in the constructor.
 */
public interface DataAccessTrigger
{
	/**
	 * called immediately after the constructor. Allows passing properties from the schema file to the
	 * database code.
	 *
	 * @param userProps the user properties defined in the user schema file
	 * @throws JoriaException if initialisation failed
	 */
	void init(GenericTag userProps) throws JoriaException;

	/**
	 * When a report is started, ReportsAnywhere calls getRootObjectForTemplate.
	 * When the data is not available throw a JoriaException. ReportsAnywhere will display/log the exception
	 * and abort the report run.
	 *
	 * @param rootName  ReportsAnywhere passes the name of the root to the application.
	 * @param userProps ReportsAnywhere passes the properties back that were handed in when the report was started.
	 * @return The reference to the root object (data entry point) for the report.
	 * @throws JoriaException when data cannot be accessed
	 */
	Object getRootObjectForTemplate(String rootName, Properties userProps) throws JoriaException;

	/**
	 * ReportsAnywhere calls this function whenever data for a page is loaded. If you
	 * are connected to an external transactional data store (database) then you may want to start
	 * a transaction on the database. Throw an exception if starting the transaction fails.
	 *
	 * @throws JoriaException if starting the transaction fails.
	 */
	void startTransaction() throws JoriaException;

	/**
	 * ReportsAnywhere calls this function whenever loading of data is complete. If you
	 * started a transaction on an external database, then you should close it now.
	 */
	void endTransaction();

	/**
	 * ReportsAnywhere calls this function whenever processing of the report is complete.
	 * This happens after the ReportDisplay window is closed, or after the report has been printed or exported.
	 */
	void reportEnded();

	/**
	 * ReportsAnywhere calls this function before it is shutting down. This gives you
	 * the opportunity to close the extenal database and to do other cleanup work.
    */
	void sessionFinished();
}
