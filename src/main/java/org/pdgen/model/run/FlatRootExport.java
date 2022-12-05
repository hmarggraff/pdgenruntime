// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.*;
import org.pdgen.data.view.NameableAccess;
import org.pdgen.env.JoriaException;
import org.pdgen.env.JoriaUserException;
import org.pdgen.env.Res;
import org.pdgen.env.Settings;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

public class FlatRootExport {
    /**
     * TEST export of dictionaries via FlatRootExport
     */
    OutputStream out;
    byte fieldSeparator;
    byte quote;
    String lineSeparatorEncoding;
    String encoding;
    JoriaAccess root;
    Writer w;
    byte[] newLine;
    boolean withHeaders;
    HashSet<JoriaType> nonRecursive = new HashSet<JoriaType>();
    Stack<JoriaType> open = new Stack<JoriaType>();

    public FlatRootExport(OutputStream out, JoriaAccess root, byte fieldSeparator, byte quote, byte[] lineSeparatorEncoding, boolean withHeaders, String encoding) throws UnsupportedEncodingException, JoriaUserException {
        this.fieldSeparator = fieldSeparator;
        this.out = out;
        this.quote = quote;
        this.root = root;
        this.encoding = encoding;
        this.withHeaders = withHeaders;
        if (checkRecursion(root.getType())) {
            throw new JoriaUserException(Res.str("Model_is_recursive_it_can_not_be_exported_to_a_flat_file"));
        }
        nonRecursive = null;
        open = null;
        if (encoding != null)
            w = new java.io.OutputStreamWriter(out, encoding);
        else
            w = new java.io.OutputStreamWriter(out);
        newLine = lineSeparatorEncoding;
    }

    protected boolean checkRecursion(JoriaType t) {
        if (t == null || t.isLiteral() || nonRecursive.contains(t))
            return false;
        if (t.isClass()) {
            if (open.contains(t))
                return true;
            open.push(t);
            JoriaClass c = (JoriaClass) t;
            JoriaAccess[] mm = c.getFlatMembers();
            for (JoriaAccess ax : mm) {
                JoriaType mt = ax.getType();
                if (checkRecursion(mt))
                    return true;
            }
            nonRecursive.add(t);
            open.pop();
            return false;
        } else if (t.isDictionary()) {
            JoriaDictionary jd = (JoriaDictionary) t;
            if (checkRecursion(jd.getKeyMatchType()))
                return true;
            else
                return checkRecursion(jd.getElementType());
        } else if (t.isCollection()) {
            JoriaCollection jc = (JoriaCollection) t;
            return checkRecursion(jc.getElementType());
        } else
            throw new JoriaAssertionError("Unhandled category of type " + t.getName());
    }

    public void wskipclass(JoriaClass t, boolean first, String s) throws IOException, JoriaDataException {
        JoriaAccess[] mm = t.getFlatMembers();
        for (int i = 0; i < mm.length; i++) {
            if (!first && i > 0) {
                w.flush();
                out.write(fieldSeparator);
            }
            qw(s);
        }
    }

    public void wclass(DBData d, JoriaClass t, boolean first, RunEnv env) throws IOException, JoriaDataException {
        JoriaAccess[] mm = t.getFlatMembers();
        ArrayList<JoriaAccess> colls = null;
        boolean furtherLiteral = false;
        for (int i = 0; i < mm.length; i++) {
            JoriaAccess ax = mm[i];
            JoriaType mt = ax.getType();
            if (mt.isCollection()) {
                if (colls == null)
                    colls = new ArrayList<JoriaAccess>();
                colls.add(ax);
                continue;
            }
            DBData md = null;
            boolean accessError = false;
            try {
                md = ax.getValue(d, ax, env);
            } catch (JoriaDataRetrievalExceptionInUserMethod e) {
                accessError = true;
            }
            if (mt.isLiteral()) {
                String s;
                if (accessError)
                    s = Res.str("AccessError");
                else if (md == null || md.isNull())
                    s = "";
                else if (ax instanceof NameableAccess && (md instanceof DBInt || md instanceof DBReal)) {
                    String fs = ((NameableAccess) ax).getFormatString();
                    if (fs != null) {
                        DecimalFormat df = new DecimalFormat(fs);
                        df.setRoundingMode(Settings.getRoundingMode());
                        if (md instanceof DBInt)
                            s = df.format(((DBInt) md).getIntValue());
                        else
                            s = df.format(((DBReal) md).getRealValue());
                    } else if (md instanceof DBInt)
                        s = Long.toString(((DBInt) md).getIntValue());
                    else
                        s = Double.toString(((DBReal) md).getRealValue());
                } else
                    s = md.toString();
                if (s == null)
                    s = "";
                if (!first || furtherLiteral) {
                    w.flush();
                    out.write(fieldSeparator);
                }
                furtherLiteral = true;
                qw(s);
            } else if (mt.isClass()) {
                if (accessError)
                    wskipclass((JoriaClass) mt, first && i == 0, Res.str("AccessError"));
                else if (md == null || md.isNull())
                    wskipclass((JoriaClass) mt, first && i == 0, "");
                else
                    wclass(md, (JoriaClass) mt, first && i == 0, env);
            } else
                throw new JoriaAssertionError("Unhandled type category in CSV export");
        }
        if (colls == null)
            return;
        for (int j = 0; j < colls.size(); j++) {
            JoriaAccess ax = colls.get(j);
            DBCollection md = (DBCollection) ax.getValue(d, ax, env);
            if (md == null || md.isNull())
                continue;
            JoriaCollection jc = ax.getCollectionTypeAsserted();
            if (!first || j > 0) {
                w.flush();
                out.write(newLine);
            }
            if (withHeaders) {
                writeHeader(jc.getElementType(), true);
                w.flush();
                out.write(newLine);
            }
            wcoll(md, jc, env, first);
        }
    }

    protected void writeHeader(JoriaClass t, boolean first) throws IOException {
        JoriaAccess[] mm = t.getFlatMembers();
        for (int i = 0; i < mm.length; i++) {
            JoriaAccess ax = mm[i];
            JoriaType mt = ax.getType();
            if (mt.isCollection()) {
                continue;
            } else if (mt.isClass()) {
                writeHeader((JoriaClass) mt, first && i == 0);
            }
            if (!first || i > 0) {
                w.flush();
                out.write(fieldSeparator);
            }
            qw(ax.getName());
        }
    }

    protected void wcoll(DBCollection collVal, JoriaCollection t, RunEnv env, boolean first) throws IOException, JoriaDataException {
        JoriaClass jc = t.getElementType();
        while (collVal.next()) {
            w.flush();
            out.write(newLine);
            DBData ed = collVal.current();
            if (ed == null)
                continue;
            wclass(ed, jc, first, env);
        }
    }

    void qw(String s) throws IOException {
        boolean needQuote = false;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                needQuote = true;
                break;
            }
        }
        if (needQuote)
            out.write(quote);
        w.write(s);
        w.flush();
        if (needQuote)
            out.write(quote);
    }

    public static void export(OutputStream out, JoriaAccess root, byte fieldSeparator, byte quote, byte[] lineSeparatorEncoding, boolean withHeaders, String encoding, RunEnvImpl env) throws JoriaException {
        try {
            FlatRootExport run = new FlatRootExport(out, root, fieldSeparator, quote, lineSeparatorEncoding, withHeaders, encoding);
            DBData rd;
            rd = root.getValue(null, root, env);
            run.wclass(rd, root.getClassTypeAsserted(), true, env);
            run.w.flush();
            out.write(run.newLine);
        } catch (IOException e) {
            throw new JoriaUserException(Res.str("Could_not_export_data"), e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                //noinspection ThrowFromFinallyBlock
                throw new JoriaUserException(Res.str("Could_not_close_export_file"), e);
            }
        }
    }
}
