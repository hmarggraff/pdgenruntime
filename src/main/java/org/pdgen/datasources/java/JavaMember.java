// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;
//MARKER The strings in this file shall not be translated

import org.joda.time.DateTime;
import org.pdgen.data.*;
import org.pdgen.data.view.*;
import org.pdgen.env.Env;
import org.pdgen.model.run.RunEnv;
import org.pdgen.oql.OQLNode;
import org.pdgen.oql.OQLParseException;

import java.io.ObjectStreamException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public abstract class JavaMember extends AbstractTypedJoriaMember implements JoriaAccessTyped {
    private static final long serialVersionUID = 7L;

    public JavaMember(JavaClass definingClass, String name, JoriaType type) {
        super(definingClass, name, type);
        makeLongName();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static DBCollection makeCollectionValue(Object o, JoriaAccess axs, JoriaCollection t, RunEnv env) throws JoriaDataException {
        long t0 = System.currentTimeMillis();        // find match type
        JoriaCollection t1 = axs.getSourceCollection();
        Trace.check(t1, "Access of a java collection must have a source collection: " + axs.getLongName());
        SortOrder[] sortRules = AbstractJoriaAccess.getSorting(t1, env);
        int topN = t1.getMinTopN(env);
        boolean large = false;
        while (t1.isView() && t1.getElementMatchType() == null) {
            CollectionView v = (CollectionView) t1;
            large |= v.isLarge();
            t1 = v.getBaseCollection();
        }
        large |= t1.isLarge();
        large &= topN == 0;
        Class<?> matchClass = null;
        final JoriaClass potentialMatchClass = t1.getElementMatchType();
        if (potentialMatchClass != null) {
            if (potentialMatchClass instanceof JavaClass) {
                JavaClass mt = (JavaClass) potentialMatchClass;
                matchClass = mt.theClass();
            } else {
                System.out.println("JavaMember.makeCollectionValue getElementMatchType not instance of JavaClass but of " + potentialMatchClass.getClass());
                t1.getElementMatchType();
            }
        }
        OQLNode checker;
        try {
            checker = axs.getCascadedOQLFilter();
        } catch (OQLParseException ex) {
            throw new JoriaAssertionError("Uncaught OQL Error detected at runtime");
        }
        JoriaType et = t.getElementType();
        if (et instanceof LiteralCollectionClass) {
            return makeLiteralCollectionValue(o, et, checker, sortRules, axs, topN, env);
        }
        DBCollection res;
        if (matchClass == null && checker == null && topN == 0 && sortRules == null && !(t1 instanceof JavaOtherCollection)) {
            if (o == null)
                return null;
            else if (o instanceof List) {
                if (large) {
                    List<Object> l = (List<Object>) o;
                    ArrayList<Object> nl = new ArrayList<Object>(l.size());
                    nl.addAll(l);
                    res = new JavaLargeListValue(nl, axs);
                } else
                    res = new JavaListValue(o, axs);
            } else if (o instanceof Iterator || o instanceof Enumeration) {
                if (large)
                    res = new JavaLargeListValue(o, axs);// no copying needed: happens automatically
                else
                    res = new JavaListValue(o, axs);
            } else if (o.getClass().isArray()) {
                if (large)
                    res = new JavaLargeListValue(o, axs);
                else
                    res = new JavaArrayValue((Object[]) o, axs);
            } else if (o instanceof Map)
                res = new JavaMapValue(o, axs, env);
            else if (o instanceof Collection) {
                if (large)
                    res = new JavaLargeListValue(o, axs);
                else
                    res = new JavaArrayValue(((Collection<Object>) o).toArray(), axs);
            } else
                throw new NotYetImplementedError("cannot make value for " + t);
        } else {
            ArrayList<DBObject> sand = new ArrayList<DBObject>();
            ArrayList<Object> sandLarge = new ArrayList<Object>();
            Iterator it;
            if (o instanceof Collection)
                it = ((Collection<Object>) o).iterator();
            else if (o.getClass().isArray())
                it = Arrays.asList((Object[]) o).iterator();
            else if (o instanceof Map)
                it = ((Map<Object, Object>) o).entrySet().iterator();
            else if (t1 instanceof JavaOtherCollection)
                it = ((JavaOtherCollection) t).iterator(o);
            else if (o instanceof Iterator)
                it = (Iterator<Object>) o;
            else if (o instanceof Enumeration)
                it = new EnumerationIterator((Enumeration<Object>) o);
            else
                throw new JoriaAssertionError("Cannot make iterator for: " + axs.getLongName());
            CollectionValueAccess elax = new CollectionValueAccess(axs);            //JoriaType et = t.getElementType();
            final ReflectionDelegate reflectionDelegate = ((JavaSchema) Env.schemaInstance).getReflectionDelegate();
            if (sortRules != null || topN > 0)// we can only sort if the collection is built conventionally
                large = false;
            TopNBuilder tb = null;
            if (topN > 0) {
                if (sortRules != null)
                    tb = new TopNBuilder(topN, sortRules, env);
            } else
                topN = Integer.MAX_VALUE;
            int cnt = 0;
            while (it.hasNext()) {
                cnt++;
                Object el = it.next();                //DBData elData = makeObjectValue(el, elax, et);
                DBData elData = makeValue(null, el, elax, et, env);
                if ((matchClass == null || matchClass.isInstance(el)) && (checker == null || checker.getBooleanValue(env, elData))) {
                    if (large) {
                        reflectionDelegate.evictObject(el);
                        sandLarge.add(el);// only keep the object itself, so that we can use a largelist value, which rebuilds the dbdata when required
                    } else if (tb != null)
                        tb.addTopN(sand, (DBObject) elData);
                    else {
                        sand.add((DBObject) elData);
                        if ((checker == null && cnt >= topN) || sand.size() >= topN)
                            break;
                    }
                } else
                    reflectionDelegate.evictObject(el);
            }            //System.out.println("Filtering time: " + String.valueOf(System.currentTimeMillis() - tf0));
            if (sortRules != null && tb == null)// only sort if not topN, because topN did already sort
            {
                ProjectionComparator comp = new ProjectionComparator(sortRules, env);
                sand.sort(comp);
            }
            if (large)
                res = new JavaLargeListValue(sandLarge, axs);
            else
                res = new FilteredDBCollection(sand, axs, t);
            Trace.log(Trace.run, "Collection build time for " + sand.size() + " elements: " + (System.currentTimeMillis() - t0));
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    private static DBCollection makeLiteralCollectionValue(Object o, JoriaType et, OQLNode checker, SortOrder[] sortRules, JoriaAccess axs, int topN, RunEnv runEnv) throws JoriaDataException {
        List<Object> list;
        if (o instanceof List)
            list = (List<Object>) o;
        else if (o.getClass().isArray()) {
            Class<?> cls = o.getClass().getComponentType();
            if (cls == Byte.TYPE) {
                byte[] ba = (byte[]) o;
                list = new ArrayList<>(ba.length);
                for (byte aBa : ba) {
                    list.add((long) aBa);
                }
            } else if (cls == Short.TYPE) {
                short[] sa = (short[]) o;
                list = new ArrayList<>(sa.length);
                for (short aSa : sa) {
                    list.add((long) aSa);
                }
            } else if (cls == Character.TYPE) {
                char[] ca = (char[]) o;
                list = new ArrayList<>(ca.length);
                for (char aCa : ca) {
                    list.add((long) aCa);
                }
            } else if (cls == Integer.TYPE) {
                int[] ia = (int[]) o;
                list = new ArrayList<>(ia.length);
                for (int anIa : ia) {
                    list.add((long) anIa);
                }
            } else if (cls == Long.TYPE) {
                long[] la = (long[]) o;
                list = new ArrayList<>(la.length);
                for (long aLa : la) {
                    list.add(aLa);
                }
            } else if (cls == Float.TYPE) {
                float[] fa = (float[]) o;
                list = new ArrayList<>(fa.length);
                for (float aFa : fa) {
                    list.add((double) aFa);
                }
            } else if (cls == Double.TYPE) {
                double[] da = (double[]) o;
                list = new ArrayList<>(da.length);
                for (double aDa : da) {
                    list.add(aDa);
                }
            } else if (cls == Boolean.TYPE) {
                boolean[] ba = (boolean[]) o;
                list = new ArrayList<>(ba.length);
                for (boolean aBa : ba) {
                    list.add(aBa);
                }
            } else
                list = Arrays.asList((Object[]) o);
        } else {
            Iterator<Object> it;
            if (o instanceof Collection)
                it = ((Collection<Object>) o).iterator();
            else if (o instanceof Map)
                it = ((Map<Object, Object>) o).values().iterator();
            else if (o instanceof Iterator)
                it = (Iterator<Object>) o;
            else if (o instanceof Enumeration)
                it = new EnumerationIterator<>((Enumeration<Object>) o);
            else
                throw new JoriaAssertionError("unknown Literalcollection " + o.getClass());
            list = new ArrayList<Object>();
            while (it.hasNext()) {
                list.add(it.next());
            }
        }
        Object out = null;
        LiteralCollectionClass lcc = (LiteralCollectionClass) et;
        JoriaType elt = lcc.getLiteralType();
        if (elt.isIntegerLiteral()) {
            long[] la = new long[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Number number = (Number) list.get(i);
                la[i] = number.longValue();
            }
            out = la;
        } else if (elt.isRealLiteral()) {
            double[] da = new double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Number number = (Number) list.get(i);
                da[i] = number.doubleValue();
            }
            out = da;
        } else if (elt.isBooleanLiteral()) {
            boolean[] ba = new boolean[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Boolean aBoolean = (Boolean) list.get(i);
                ba[i] = aBoolean;
            }
            out = ba;
        } else if (elt.isStringLiteral()) {
            String[] sa = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                String s = (String) list.get(i);
                sa[i] = s;
            }
            out = sa;
        } else if (elt.isImage()) {
            Object[] oa = new Object[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Object o1 = list.get(i);
                oa[i] = o1;
            }
            out = oa;
        }
        if (out == null)
            throw new JoriaAssertionError("unknown Literalcollection " + et.getName());
        return new DBLiteralCollectionData(out, lcc, axs, list.size(), checker, sortRules, runEnv, topN);
    }

    public static DBData makeLiteralValue(Object o, JoriaAccess axs) {
        if (o instanceof Integer)
            return new DBIntImpl(axs, (Integer) o);
        else if (o instanceof String)
            return new DBStringImpl(axs, ((String) o));
        else if (o instanceof Boolean)
            return new DBBooleanImpl(axs, (Boolean) o);
        else if (o instanceof Float)
            return new DBRealImpl(axs, (Float) o);
        else if (o instanceof Double)
            return new DBRealImpl(axs, (Double) o);
        else if (o instanceof Long)
            return new DBIntImpl(axs, (Long) o);
        else if (o instanceof Class)
            return new DBStringImpl(axs, ((Class<?>) o).getName());
        else if (o instanceof Byte)
            return new DBIntImpl(axs, (Byte) o);
        else if (o instanceof Short)
            return new DBIntImpl(axs, (Short) o);
        else if (o instanceof Character)
            return new DBIntImpl(axs, (Character) o);
        else if (o instanceof BigInteger)
            return new DBIntImpl(axs, ((BigInteger) o).longValue());
        else if (o instanceof BigDecimal)
            return new DBRealImpl(axs, ((BigDecimal) o).doubleValue());
        else if (axs.getType().isImage())
            return new DBImage(o, axs);
        else
            throw new JoriaAssertionError("unknown Literal " + o.getClass());
    }

    public static DBObject makeObjectValue(Object o, JoriaAccess axs, JoriaType ignoredT) {
        if (o instanceof Date) {
            return new DBDateTime(axs, (Date) o);
        } else if (o instanceof Calendar) {
            return new DBDateTime(axs, (Calendar) o);
        } else if (o instanceof DateTime) {
            return new DBDateTime(axs, ((DateTime) o).toDate());
        } else if (o instanceof LocalDateTime) {
            return new DBDateTime(axs, ((LocalDateTime) o).toEpochSecond(ZoneOffset.UTC));
        }
		/*
        JoriaType matchType = t;   // determine matchtype from AsView
        if (matchType != t) {
            JavaClass mc = (JavaClass) matchType;
            Class cc = mc.theClass();
            if (cc.isInstance(o))
                return new JavaValue(o, axs);
            else
                return null;
        }
		*/
        else
            return new JavaValue(o, axs);
    }

    public static DBData makeValue(DBData definingClass, Object o, JoriaAccess axs, JoriaType type, RunEnv env) throws JoriaDataException {
        if (o == null)
            return null;
        JoriaType t = axs.getType();
        if (type.isLiteral())
            return makeLiteralValue(o, axs);
        else if (type.isCollection()) {
            if (definingClass instanceof DBCollectionCache) {
                DBCollectionCache cache = (DBCollectionCache) definingClass;
                DBCollection cv = cache.getCachedCollectionValue(axs);
                if (cv != null) {
                    Trace.logDebug(Trace.fill, "Found in cache " + axs.getLongName());
                } else {
                    cv = makeCollectionValue(o, axs, (JoriaCollection) type, env);
                    cache.addCollectionToCache(cv, axs);
                }
                return cv;// found in cache: done
            } else {
                Trace.logError("Internal warning: type " + type.getName() + " is a collection but defining class does not implement collection cache");
                return makeCollectionValue(o, axs, (JoriaCollection) type, env);
            }
        } else if (o instanceof Date) {
            return new DBDateTime(axs, (Date) o);
        } else if (o instanceof Calendar) {
            return new DBDateTime(axs, (Calendar) o);
        } else if (t.isClass())
            return makeObjectValue(o, axs, t);
        else {
            throw new NotYetImplementedError("cannot make value for " + axs.getType());
        }
    }

    protected Object readResolve() throws ObjectStreamException {
        if (definingClass instanceof JoriaUnknownType) {
            return JoriaModifiedAccess.createJoriaModifiedAccess(name, type, definingClass, JoriaModifiedAccess.classNotFound, this, null);
        }
        JoriaAccess mem = definingClass.findMember(name);
        if (mem == null) {
            return JoriaModifiedAccess.createJoriaModifiedAccess(name, type, definingClass, JoriaModifiedAccess.memberNotFound, this, null);
        }
        if (type instanceof JoriaUnknownType || type != mem.getType()) {
            return JoriaModifiedAccess.createJoriaModifiedAccess(name, type, definingClass, JoriaModifiedAccess.typeChanged, this, mem);
        }
        return mem;
    }

    public boolean isAccessTyped() {
        return true;
    }

}
