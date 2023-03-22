// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.runtime;

import org.pdgen.env.Env;
import org.pdgen.env.JoriaException;
import org.pdgen.env.RepoLoader;
import org.pdgen.model.Template;
import org.pdgen.model.run.RunEnvImpl;

import java.io.*;

public class PDGen {
    /**
     * Holds the name of the template file.
     */
    private final String templateFileName;

    /**
     * The created PDGgen object can then be called to create reports from templates.
     * @param templateFile the file that contains the templates. To produce such a file, use the pdgen-designer from qint software
     */
    public PDGen(File templateFile) {
        this.templateFileName = templateFile.getAbsolutePath();
        new RepoLoader(templateFile, false);
    }

    /**
     * The created PDGgen object can then be called to create reports from templates.
     * @param name Name the repo for reference.
     * @param repo An inputstream with template data. Use this, when the template data is not stored in a file, but somewhere else, e.g. a database.
     */
    public PDGen(String name, InputStream repo) {
        this.templateFileName = name;
        new RepoLoader(name, repo, false); // Builds the env
    }


    /**
     * Start the actual generation process. You can call this method as often, as you want, but you must be aware,that it is not built to be run in parallel tasks.
     * @param templateName The name of the template. This must be defined in the template set, that is currently open.
     * @param data The data for the report. The object must match the data type, that has been used in the designer to define the template.
     * @param receiver An output stream, where the pdf code is written to. PDGen will close this stream at the end.
     * @exception RuntimeException throws a plain runtime exception, when something goes wrong.
     */
    public void generatePDF(String templateName, Object data, OutputStream receiver) {
        Template template = Env.instance().repo().reports.find(templateName);
        if (template == null)
            throw new RuntimeException("Template with name " + templateName + " not found in " + templateFileName);
        RunEnvImpl r = new RunEnvImpl(template);
        try {
            r.exportPdf(receiver);
            r.endReport(r.getStartTemplate());
        } catch (JoriaException e) {
            throw new RuntimeException(e.getCause());
        }
    }

}
