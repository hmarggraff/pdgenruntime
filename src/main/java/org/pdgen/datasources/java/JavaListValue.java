// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;

import org.pdgen.data.*;
import org.pdgen.env.Env;

import java.util.*;

@SuppressWarnings("unchecked")
public class JavaListValue extends JavaValue implements DBCollection {
    protected CollectionValueAccess elax;
    protected int index = -1;

    public JavaListValue(Object o, JoriaAccess a) throws JoriaDataException {
        super(o, a);
        elax = new CollectionValueAccess(a);
        if (o instanceof Iterator) {
            myValue = new ArrayList<>();
            JavaSchema sch = (JavaSchema) Env.schemaInstance;
            sch.getReflectionDelegate().loadObject(o);
            Iterator<Object> it = (Iterator<Object>) o;
            while (it.hasNext()) {
                Object io = it.next();
                if (io != null) {
                    ((ArrayList<Object>) myValue).add(io);
                }
            }
        } else if (o instanceof Enumeration) {
            myValue = new ArrayList<>();
            Enumeration<Object> it = (Enumeration<Object>) o;
            while (it.hasMoreElements()) {
                Object io = it.nextElement();
                if (io != null) {
                    ((ArrayList<Object>) myValue).add(io);
                }
            }
        } else if (o instanceof Collection) {
            if (!(o instanceof List)) {
                myValue = new ArrayList<>();
                JavaSchema sch = (JavaSchema) Env.schemaInstance;
                sch.getReflectionDelegate().loadObject(o);
                for (Object io : ((Collection<Object>) o)) {
                    if (io != null) {
                        ((ArrayList<Object>) myValue).add(io);
                    }
                }
            }
        } else
            throw new JoriaAssertionError("JavaListValue requires a List, Iterator or Enumeration. Found: " + o.getClass().getName());
    }

    protected DBObject getItemInternal(int index) throws JoriaDataException {
        List<Object> lv = (List<Object>) myValue;
        Object o;
        try {
            o = lv.get(index);
        } catch (IndexOutOfBoundsException ex) {
            throw new JoriaDataException("Index is out of bounds: " + getAccess().getName() + "[" + index + "]");
        }
        JoriaCollection ja = getAccess().getSourceCollection();
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        sch.getReflectionDelegate().loadObject(o);
        return JavaMember.makeObjectValue(o, elax, ja.getElementType());
    }

    public int getLength() throws JoriaDataException {
        return ((List<Object>) myValue).size();
    }

    public DBData pick() throws JoriaDataException {
        return getItemInternal(0);
    }

    public boolean next() // implizites freeItem
    {
        if (index >= 0 && index < ((List<Object>) myValue).size())
            freeItem(index);
        index++;
        return index < ((List<Object>) myValue).size();
    }

    protected void freeItem(int index) {
        // only used in derived class
    }

    public DBObject current()
            throws JoriaDataException {
        return getItemInternal(index);
    }

    public boolean reset() {
        index = -1;
        return true;
    }

    public String toString() {
        return getAccess().getType().getName();
    }

}