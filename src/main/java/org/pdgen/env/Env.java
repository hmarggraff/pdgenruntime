// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

import org.pdgen.data.*;
import org.pdgen.data.view.ClassView;
import org.pdgen.model.Template;
import org.pdgen.model.run.RunEnv;
import org.pdgen.model.run.pdf.TrueTypeFont;
import org.pdgen.model.style.StyleBase;
import org.pdgen.oql.JoriaQuery;
import org.pdgen.oql.OQLNode;
import org.pdgen.oql.OQLParseException;
import org.pdgen.oql.OQLParser;
import org.pdgen.util.ErrorHint;


import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.prefs.Preferences;

public class Env {
    protected static String relativeRoot;

    public static FontRenderContext fontRenderContext = new FontRenderContext(null, false, false);
    protected static JoriaFileService fileService;
    public static String currentFile;
    protected Map<JoriaClass, ProjectionHolder> viewMap = new HashMap<>();
    ArrayList<DataChangeListener> repoChangeListeners = new ArrayList<>();


    protected Locale currentLocale;
    private final Graphics2D defaultGraphics2D = (Graphics2D) new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB).getGraphics();
    public HashMap<OQLParser.QueryKey, JoriaQuery> theQueryCache = new HashMap<>();
    JoriaThreadLocalStorage threadLocalData;
    public static JoriaSchema schemaInstance; // can exist before an Env is instantiated, during reading of save file
    Repository repositoryInstance;


    protected static Env theInstance;

    public Env(Repository repository) {
        theInstance = this;
        repositoryInstance = repository;
        TrueTypeFont.readAllFontFileHeaders();
    }

    public static Env instance() {
        return theInstance;
    }


    public static void repoChanged() {
        if (theInstance == null)
            return;
        for (DataChangeListener l : instance().repoChangeListeners) {
            l.dataChanged();
        }
    }

    public void setCurrentFile(String currFile) {
        currentFile = currFile;
    }

    //public abstract OQLParser getOQLParser(String filter, JoriaType scope, boolean inCentralRepositoryContext);
    public OQLNode parseUnparented(String filter, JoriaType scope, boolean commentOnlyAllowed) throws OQLParseException {
        return OQLParser.parse(filter, scope, commentOnlyAllowed);
    }

    public void collectI18nKeys(String expression, JoriaType scope, boolean emptyIsTrue, String message, HashMap<String, List<I18nKeyHolder>> bag) {
        if (expression == null || expression.length() == 0)
            return;
        try {
            JoriaQuery root = OQLParser.parse(expression, scope, emptyIsTrue);
            root.i18nKeys(bag);
        } catch (OQLParseException e) {
            tell(Res.str("Error_when_finding_localisation_keys_Please_use_the_editor_to_change_it"));
        }
    }

    public Class<?> loadMangledClass(String name) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Deprecated.");
    }

    public void reportStart(Template t, RunEnv env) throws JoriaException {
    }

    public void reportEnd(Template t, RunEnv env) {
    }

    public void finishSession() {
    }

    public void exit(int retVal) {        //noinspection finally
        try {
            Trace.action("systemExit()");
            Preferences pref = Preferences.userNodeForPackage(Env.class);
            if (getCurrentLocale() != null)
                pref.put("locale", getCurrentLocale().toString());//trdone
            finishSession();
            Trace.closeLogStream();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            System.exit(retVal);
        }
    }

    public Locale getCurrentLocale() {
        if (currentLocale == null)
            currentLocale = Locale.getDefault();
        return currentLocale;
    }

    public void setCurrentLocale(Locale newLocale) {
        currentLocale = newLocale;
    }

    public FontRenderContext getFontRenderContext() {
        return defaultGraphics2D.getFontRenderContext();
    }

    public String getCurrentFile() {
        return currentFile;
    }

    public boolean isHeadless() {
        return true;
    }

    public JoriaFileService getFileService() {
        if (fileService == null)
            fileService = new JoriaFileServiceFileSystem(relativeRoot);
        return fileService;
    }

    public void setDefaultFileService() {
        fileService = new JoriaFileServiceFileSystem(relativeRoot);
    }

    public void registerTopLevelWindow(JComponent w) {        //nothing to do
    }

    public void deregisterTopLevelWindow(JComponent w) {        //nothing to do
    }

    public static Env instenv() {
        return theInstance;
    }

    public void reflectStyleChange(StyleBase style) {
    }

    public void updateFontRenderContext() {
        Env.fontRenderContext = Env.instance().getDefaultGraphics2D().getFontRenderContext();
    }

    public boolean isCancelPressed() {
        return false;
    }

    public boolean runInBackground(Runnable job, Locale loc) throws JoriaBackgroundException, JoriaUserDataException {
        job.run();
        return true;
    }

    public void runOnGuiThread(Runnable r) {
        throw new HeadlessException("no gui available");//trdone
    }

    public Graphics2D getDefaultGraphics2D() {
        return defaultGraphics2D;
    }

    public Object getServiceHelper() {
        return null;
    }


    String getSystemProperty(String key) {
        String sysprop = System.getProperty(key);
        Trace.log(Trace.init, "SystemProperty." + key + "=" + sysprop);
        return sysprop;
    }

    public Repository repo() {
        return repositoryInstance;
    }

    public void handle(Throwable t, ErrorHint[] hints, final String defaultMessage) {
        handle(t);
    }

    public void handle(Throwable t) {
        Trace.log(t); // make sure it is logged
        if (t instanceof Error)
            throw (Error) t;
        else
            throw new JoriaInternalError("Wrapped Exception", t);
    }

    public void handle(Throwable t, String msg) {
        System.err.println("ReportService passes throwable: " + msg);
        Trace.log(t);
        //t.printStackTrace();
        if (t instanceof Error)
            throw (Error) t;
        else
            throw new JoriaInternalError(msg, t);
    }

    public void handle(Throwable t, Window frame) {
        handle(t);
    }

    public void handle(Throwable t, String msg, Window frame) {
        handle(t, msg);
    }

    public void tell(String t) {
        System.out.println(t);
    }

    public void fatal(String t) {
        tell(t);
    }

    public void tell(String t, String i) {
        System.out.println(t);
        System.out.println(i);
    }

    public void addUserRoot(JoriaAccess root) {
        throw new JoriaAssertionError("Not possible in run mode");
    }

    public void newExplorePanel() {
        throw new JoriaAssertionError("Not possible in run mode");
    }

    public JoriaThreadLocalStorage getThreadLocalStorage() {
        if (threadLocalData == null) {
            threadLocalData = new JoriaThreadLocalStorage();
        }
        return threadLocalData;
    }

    public Map<JoriaClass, ProjectionHolder> getViewMap() {
        return viewMap;
    }

    public ProjectionHolder viewsFor(JoriaClass joriaClass) {
        if (joriaClass == null)
            return null;
        if (joriaClass instanceof ClassView)
            joriaClass = ((ClassView) joriaClass).getPhysicalClass();
        ProjectionHolder ret = viewMap.get(joriaClass);
        if (ret == null) {
            ret = new ProjectionHolder(joriaClass);
            viewMap.put(joriaClass, ret);
        }
        return ret;
    }

    public String getCurrentUserName() {
        return Settings.getUsername();
    }

    public JoriaSchema getSchema() {
        return schemaInstance;
    }

    public void addRepoChangeListener(DataChangeListener listener) {
        repoChangeListeners.add(listener);
    }
}
