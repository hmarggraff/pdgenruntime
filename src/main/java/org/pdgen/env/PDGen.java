// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

import org.pdgen.model.Template;
import org.pdgen.model.run.RunEnvImpl;
import org.pdgen.util.RuntimeComponentFactory;

import java.io.OutputStream;

public class PDGen {
    private String templateFileName;

    public PDGen(String templateFileName) {
        this.templateFileName = templateFileName;
        RuntimeComponentFactory.instance = new RuntimeComponentFactory();
        RepoLoader loader = new RepoLoader(templateFileName); // Builds the env
    }

    public void generatePDF(String reportName, Object data, OutputStream receiver) {
        Template template = Env.instance().repo().reports.find(reportName);
        if (template == null)
            throw new RuntimeException("Template with name " + reportName + " not found in " + templateFileName);
        RunEnvImpl r = new RunEnvImpl(template);
        try {
            r.exportPdf(receiver);
            r.endReport(r.getStartTemplate());
        } catch (JoriaException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
