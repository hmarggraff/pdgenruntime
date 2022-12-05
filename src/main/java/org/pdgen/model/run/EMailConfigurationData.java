// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.projection.ComputedField;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * User: patrick
 * Date: Jul 21, 2005
 * Time: 2:16:55 PM
 */
public class EMailConfigurationData implements VariableProvider, Serializable {
    public static final String KEY = "EMAIL";
    private static final long serialVersionUID = 7L;
    ComputedField addressField;
    ComputedField subjectField;
    ComputedField letterField;
    String sender;
    String reportFileName;

    public String getAddress(DBData from, RunEnv env) throws JoriaDataException {
        return ((DBString) addressField.getValue(from, null, env)).getStringValue();
    }

    public String getSubject(DBData from, RunEnv env) throws JoriaDataException {
        return ((DBString) subjectField.getValue(from, null, env)).getStringValue();
    }

    public String getLetter(DBData from, RunEnv env) throws JoriaDataException {
        return ((DBString) letterField.getValue(from, null, env)).getStringValue();
    }

    public void collectVariables(Set<RuntimeParameter> s, Set<Object> seen) {
        addressField.collectVariables(s, seen);
        subjectField.collectVariables(s, seen);
        letterField.collectVariables(s, seen);
    }

    public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> s, Set<Object> seen) {
        addressField.collectI18nKeys2(s, seen);
        subjectField.collectI18nKeys2(s, seen);
        letterField.collectI18nKeys2(s, seen);
    }

    public void collectVisiblePickersInScope(List<JoriaAccess[]> collection, Set<RuntimeParameter> visible, Stack<JoriaAccess> pathStack, Set<Object> seen) {
        addressField.collectVisiblePickersInScope(collection, visible, pathStack, seen);
        subjectField.collectVisiblePickersInScope(collection, visible, pathStack, seen);
        letterField.collectVisiblePickersInScope(collection, visible, pathStack, seen);
    }

    public ComputedField getAddressField() {
        return addressField;
    }

    public ComputedField getLetterField() {
        return letterField;
    }

    public ComputedField getSubjectField() {
        return subjectField;
    }

    public void setAddressField(ComputedField addressField) {
        this.addressField = addressField;
    }

    public void setLetterField(ComputedField letterField) {
        this.letterField = letterField;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setSubjectField(ComputedField subjectField) {
        this.subjectField = subjectField;
    }

    public String getSender() {
        return sender;
    }

    public String getReportFileName() {
        return reportFileName;
    }

    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName;
    }
}
