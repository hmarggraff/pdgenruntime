// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;
//MARKER The strings in this file shall not be translated

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExternalFileUsage {
    public static final String icon = "Icon";
    public static final String doc = "Doc";
    public static final String localisation = "Localisation";
    public static final String classpath = "Classpath";

    HashMap<String, AppliedMapping> mappings = new HashMap<String, AppliedMapping>();

    public ExternalFileUsage() {
    }

    public void add(String key, String type, String where) {
        AppliedMapping mapping = mappings.get(key);
        if (mapping == null) {

            mapping = new AppliedMapping(key, type);
            mappings.put(key, mapping);

        }
        mapping.usages.add(where);
    }

    public HashMap<String, AppliedMapping> getMappings() {
        return mappings;
    }

    public static class AppliedMapping {

        public String type;
        public String src;
        public String mapResult;
        public String mapRule;
        public List<String> usages = new ArrayList<String>();

        public AppliedMapping(String src, String type) {
            this.src = src;
            this.type = type;
        }
    }
}
