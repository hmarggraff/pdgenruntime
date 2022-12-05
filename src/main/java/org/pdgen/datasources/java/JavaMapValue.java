// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;

import org.pdgen.data.*;
import org.pdgen.data.view.CollectionView;
import org.pdgen.env.Env;
import org.pdgen.model.run.RunEnv;

import java.util.ArrayList;
import java.util.Map;


public class JavaMapValue extends JavaValue implements DBDictionary {

    DBCollection myValues;
    DBCollection myKeys;
    ArrayList<Map.Entry> myElements; // java.util.map.Entry
    protected CollectionValueAccess elax;
    RunEnv env;
    int index;

    public JavaMapValue(Object o, JoriaAccess a, RunEnv env) {
        super(o, a);
        this.env = env;
        elax = new CollectionValueAccess(a);
    }

    public JoriaType getActualType() {
        return getAccess().getType();
    }

    JoriaDictionary getBaseDictionary() {
        JoriaType tt = getActualType();
        while (tt.isView() && !(tt instanceof JoriaDictionary)) {
            tt = ((CollectionView) tt).getBaseCollection();
        }
        return (JoriaDictionary) tt;
    }

    protected DBObject getItemInternal(int index) throws JoriaDataException {
        loadElements();
        if (myValue == null)
            return null;
        Object o;
        try {
            o = myElements.get(index);
            //String n = o.getClass().getName();
            //System.out.println(n);
            //Map.Entry<Object,Object> entry = (Map.Entry<Object,Object>) o;
            //o = entry.getValue();
            //n = o.getClass().getName();
            //System.out.println(n);
        } catch (IndexOutOfBoundsException ex) {
            throw new JoriaDataException("Index is out of bounds: " + getAccess().getName() + "[" + index + "]");
        }
        return JavaMember.makeObjectValue(o, elax, ((JavaSchema) Env.schemaInstance).getObjectType());
    }

    public DBCollection getKeys() throws JoriaDataException {
        if (myKeys == null)
            myKeys = JavaMember.makeCollectionValue(((Map<?, ?>) myValue).keySet(), getAccess(), ((JavaSchema) Env.schemaInstance).getObjectArrayType(), env);
        return myKeys;
    }

    public int getLength() throws JoriaDataException {
        loadElements();
        if (myValue == null)
            return -1;
        return myElements.size();
    }

    public DBData getValueForKey(DBData key) throws JoriaDataException {
        loadElements();
        if (myValue == null)
            return null;
        Map<?, ?> lv = (Map<?, ?>) myValue;
        Object o;
        JavaValue jkey = (JavaValue) key;
        try {
            o = lv.get(jkey.getJavaObject());
        } catch (Exception ex) {
            throw new JoriaDataException("Error mapping key: " + getAccess().getName() + "[" + key + "]");
        }

        return JavaMember.makeObjectValue(o, getAccess(), myKeys.getActualType());
    }

    public DBCollection getValues() throws JoriaDataException {
        loadElements();
        if (myValue == null)
            return null;
        if (myValues == null)
            myValues = JavaMember.makeCollectionValue(((Map<?, ?>) myValue).values(), getAccess(), ((JavaSchema) Env.schemaInstance).getObjectArrayType(), env);
        return myValues;
    }

    protected void loadElements() throws JoriaDataException {
        Trace.logDebug(Trace.fill, "loadElements:myElements " + myElements);
        if (myElements == null) {
            JavaSchema sch = (JavaSchema) Env.schemaInstance;
            sch.getReflectionDelegate().loadObject(myValue);
            if (myValue == null)
                return;
            JoriaDictionary ja = getBaseDictionary();
            Class<?> vMatchType = null;
            Class<?> kMatchType = null;
            if (ja.getElementMatchType() != null && ja.getElementMatchType().isClass()) {
                vMatchType = ((JavaClass) ja.getElementMatchType()).theClass();
            }
            if (ja.getKeyMatchType() != null && ja.getKeyMatchType().isClass()) {
                kMatchType = ((JavaClass) ja.getKeyMatchType()).theClass();
            }
            myElements = new ArrayList<Map.Entry>();
            Map<?, ?> lv = (Map<?, ?>) myValue;
            for (Map.Entry<?, ?> o1 : lv.entrySet()) {
                if (vMatchType == null || vMatchType.isInstance(o1.getValue())) {
                    if (kMatchType == null || kMatchType.isInstance(o1.getKey()))
                        myElements.add(o1);
                }
            }
        }
    }

    public DBData pick() throws JoriaDataException {
        return getItemInternal(0);
    }

    public boolean next() throws JoriaDataException // implizites freeItem
    {
        loadElements();
        if (myValue == null)
            return false;
        index++;
        return index < myElements.size();
    }

    public DBObject current() throws JoriaDataException {
        return getItemInternal(index);
    }

    public boolean reset() {
        index = -1;
        return true;
    }

    public String toString() {
        return getAccess().getType().getName();// + String.valueOf(getLength());
    }
}
