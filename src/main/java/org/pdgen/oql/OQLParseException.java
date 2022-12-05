// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

public class OQLParseException extends Exception {

    private static final long serialVersionUID = 7L;
    // fields
    public String query;
    public int pos;

    public OQLParseException(final String message) {
        super(message);
    }

    public OQLParseException(String message, String queryText, int errorPos) {
        super(message);
        query = queryText;
        pos = errorPos;
    }

    public OQLParseException(String message, OQLParser oqlParser) {
        super(message);
        query = oqlParser.getQueryString();
        pos = oqlParser.getPos();
    }
}
