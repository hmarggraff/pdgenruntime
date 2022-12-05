// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


public class LikeNode extends UnaryOperatorNode {
    // fields
    String matcher;

    public LikeNode(NodeInterface p0, String p1) {
        super(p0);
        matcher = p1;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException {
        String source = sub.getStringValue(env, p0);
        if (source == null)
            return false;
        return match(source, 0, 0);
    }

    public String getTokenString() {
        StringBuffer b = new StringBuffer(sub.getTokenString());
        buildTokenString(b);
        return b.toString();
    }

    private void buildTokenString(final StringBuffer b) {
        b.append(" like ");
        b.append('"');
        for (int i = 0; i < matcher.length(); i++) {
            char c = matcher.charAt(i);
            if (c == '"')
                b.append('\\');
            b.append(c);
        }
        b.append('"');
    }

    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        final int newLevel = 3;
        optBrace(bindingLevel, newLevel, collector, '(');
        buildTokenString(collector);
        optBrace(bindingLevel, newLevel, collector, ')');
    }


    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        return new DBBooleanImpl(null, getBooleanValue(env, p0));
    }

    public JoriaType getType() {
        return DefaultBooleanLiteral.instance();
    }

    public boolean isBoolean() {
        return true;
    }

    protected boolean match(final String source, int sx, int px) {
        while (sx < source.length() && px < matcher.length()) {
            char pc = matcher.charAt(px);
            if (pc == '*')
                return matchStar(source, sx, ++px);
            else if (pc == '\\') {
                px++;
                pc = matcher.charAt(px);
                if (pc == source.charAt(sx)) {
                    px++;
                    sx++;
                } else
                    return false;
            } else if (pc == '?' || pc == source.charAt(sx)) {
                px++;
                sx++;
            } else
                return false;
        }
        while (px < matcher.length() && matcher.charAt(px) == '*') {
            px++;
        }
        return sx >= source.length() && px >= matcher.length();
    }

    protected boolean matchStar(final String source, int st, int px) {
        int sx = source.length();
        while (sx >= st && !match(source, sx, px)) {
            sx--;
        }
        return sx >= st;
    }

    public boolean hasText(final String text, final boolean searchLabels, final boolean searchData) {
        return searchLabels && matcher != null && matcher.toLowerCase().contains(text);
    }
}
