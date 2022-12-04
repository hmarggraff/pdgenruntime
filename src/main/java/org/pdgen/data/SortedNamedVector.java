// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.io.Serializable;
import java.util.*;

@SuppressWarnings("unused")
public class SortedNamedVector<E extends Named> implements ListModel<E>, Serializable, NameableListener<Nameable>, Iterable<E> {
    private static final long serialVersionUID = 7L;
    protected ArrayList<E> data;
    transient Vector<ListDataListener> listeners;
    private boolean caseSensitive;

    /**
     * ----------------------------------------------------------------------- SortedNamedVector
     */
    public SortedNamedVector() {
        data = new ArrayList<>();
    }

    public SortedNamedVector(boolean caseSensitive) {
        data = new ArrayList<>();
        this.caseSensitive = caseSensitive;
    }

    /**
     * copy constructor. Copies elements but not listeners
     * Creation date: (29.3.00 14:27:51)
     *
     * @param from: the source
     */
    public SortedNamedVector(SortedNamedVector<? extends E> from) {
        if (from == null)
            data = new ArrayList<>();
        else {
            data = new ArrayList<>(from.data);
            caseSensitive = from.caseSensitive;
        }
    }

    public SortedNamedVector(Collection<? extends E> source) {
        data = new ArrayList<>(source);
        data.sort(new NamedComparator(caseSensitive));
    }

    public SortedNamedVector(E[] source) {
        data = new ArrayList<>(Arrays.asList(source));
        data.sort(new NamedComparator(caseSensitive));
    }

    @SuppressWarnings("UnusedReturnValue")
    public int addOrReplace(E e) {
        return baseAdd(e, true, false);
    }

    public int addOrIgnore(E e) {
        return baseAdd(e, false, true);
    }

    public int add(E e) {
        return baseAdd(e, false, false);
    }

    private int baseAdd(E e, boolean canReplace, boolean canIgnore) {
        Trace.check(e);
        checkOrder();
        final String name = e.getName();
        Trace.check(name);
        for (int i = 0; i < data.size(); i++) {
            int r;
            if (caseSensitive)
                r = name.compareTo(data.get(i).getName());
            else
                r = name.compareToIgnoreCase(data.get(i).getName());
            if (0 > r) {
                for (int j = i + 1; j < data.size(); j++) // check remainder of data for a duplicate!
                {
                    int ir;
                    if (caseSensitive)
                        ir = name.compareTo(data.get(j).getName());
                    else
                        ir = name.compareToIgnoreCase(data.get(j).getName());
                    Integer j1 = null;
                    if (ir == 0) {
                        return insertAtFound(e, canReplace, canIgnore, name, j);
                    }
                }
                data.add(i, e);
                fireAdd(i);
                return i;
            } else if (r == 0) {
                return insertAtFound(e,canReplace, canIgnore, name, i);
            }
        }
        data.add(e);
        fireAdd(data.size() - 1);
        return data.size() - 1;
    }

    private int insertAtFound(E e, boolean canReplace, boolean canIgnore, String name, int j) {
        if (canReplace) {
            fireDel(j);
            data.set(j, e);
            fireAdd(j);
            return j;
        } else if (canIgnore) {
            return -1;
        } else
            throw new JoriaAssertionError("Cannot add two objects with the same name to a SortedNamedVector " + name + " id = " + this);
    }

    public int checkOrder() {
        int ret = -1;
        if (data.size() < 2)
            return -1;
        String name = data.get(0).getName();
        if (name == null) {
            Trace.logError("SNV with null name");
            return 0;
        }
        for (int i = 1; i < data.size(); i++) {
            int r;
            final String newname = data.get(i).getName();
            if (newname == null) {
                Trace.logError("SNV with null name at " + i);
                continue;
            }
            if (caseSensitive)
                r = name.compareTo(newname);
            else
                r = name.compareToIgnoreCase(newname);
            if (r >= 0) {
                ret = i;
                break;
            } else
                name = newname;
        }
        if (ret >= 0)
            Trace.logError("SNV out of order");
        return ret;
    }

    @SuppressWarnings("unused")
    public void addOrReplaceAll(SortedNamedVector<E> els) {
        if (els == null)
            return;
        for (int i = 0; i < els.getSize(); i++) {
            baseAdd(els.getElementAt(i), true, false);
        }
    }

    @SuppressWarnings("unused")
    public void addOrIgnoreAll(SortedNamedVector<E> els) {
        if (els == null)
            return;
        for (int i = 0; i < els.getSize(); i++) {
            baseAdd(els.getElementAt(i), false, true);
        }
    }


    public void addAll(E[] els) {
        if (els == null)
            return;
        for (E el : els) {
            add(el);
        }
    }

    public void addOrReplaceAll(E[] els) {
        if (els == null)
            return;
        for (E el : els) {
            baseAdd(el, true, false);
        }
    }

    public void addOrIgnoreAll(E[] els) {
        if (els == null)
            return;
        for (E el : els) {
            baseAdd(el, false, true);
        }
    }

    public void addAll(SortedNamedVector<? extends E> els) {
        if (els == null)
            return;
        for (int i = 0; i < els.getSize(); i++) {
            add(els.getElementAt(i));
        }
    }

    public void addAll(Collection<? extends E> els) {
        if (els == null)
            return;
        for (E el : els) {
            add(el);
        }
    }

    public void addOrReplaceAll(Collection<? extends E> els) {
        if (els == null)
            return;
        for (E el : els) {
            baseAdd(el, true, false);
        }
    }

    public void addOrIgnoreAll(Collection<? extends E> els) {
        if (els == null)
            return;
        for (E el : els) {
            baseAdd(el, false, true);
        }
    }

    public void addListDataListener(ListDataListener l) {
        if (listeners == null)
            listeners = new Vector<>();
        listeners.add(l);
    }

    public int binarySearch(String key) {
        if (key == null)
            return -1;
        int low = 0;
        int high = data.size() - 1;
        if (caseSensitive) {
            while (low <= high) {
                int mid = (low + high) / 2;
                String midVal = data.get(mid).getName();
                if (0 < key.compareTo(midVal))
                    low = mid + 1;
                else if (0 > key.compareTo(midVal))
                    high = mid - 1;
                else
                    return mid;   // key found
            }
        } else {
            while (low <= high) {
                int mid = (low + high) / 2;
                String midVal = data.get(mid).getName();
                if (0 < key.compareToIgnoreCase(midVal))
                    low = mid + 1;
                else if (0 > key.compareToIgnoreCase(midVal))
                    high = mid - 1;
                else
                    return mid;   // key found
            }
        }
        return -(low + 1);   // key not found.
    }

    public boolean containsName(Named n) {
        if (n == null)
            return false;
        Named nn = find(n.getName());
        return (nn != null && nn == n);
    }

    public E elementAt(int index) {
        return data.get(index);
    }

    public Iterator<E> elements() {
        return data.iterator();
    }

    public E find(String name) {
        int ix = get(name);
        if (ix >= 0)
            return data.get(ix);
        else
            return null;
    }

    protected void fireAdd(int ix) {
        if (data.get(ix) instanceof Nameable) {
            Nameable what = (Nameable) data.get(ix);
            NameableTracer.registerForNameable(what, this);
        }
        if (listeners == null || listeners.size() == 0)
            return;
        ListDataEvent lde = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, ix, ix);
        for (int i = listeners.size() - 1; i >= 0; i--) {
            listeners.get(i).intervalAdded(lde);
        }
    }

    protected void fireDel(int ix) {
        if (data.get(ix) instanceof Nameable) {
            Nameable what = (Nameable) data.get(ix);
            NameableTracer.unregisterForNameable(what, this);
        }
        if (listeners == null || listeners.size() == 0)
            return;
        ListDataEvent lde = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, ix, ix);
        for (int i = listeners.size() - 1; i >= 0; i--) {
            listeners.get(i).intervalRemoved(lde);
        }
    }

    public int get(String name) {
        return binarySearch(name);
    }

    public E getElementAt(int index) {
        return data.get(index);
    }

    public Vector<ListDataListener> getListeners() {
        return listeners;
    }

    public int getSize() {
        return data.size();
    }

    public Iterator<E> iterator() {
        return data.iterator();
    }

    public void remove(int ix) {
        if (data.get(ix) instanceof NameableProtectedRemove && !((NameableProtectedRemove) data.get(ix)).isRemoveable())
            throw new JoriaAssertionError("not allowed to remove " + data.get(ix));
        fireDel(ix);
        data.remove(ix);
    }

    public void clear() {
        if (data.size() == 0)
            return;
        int seq = data.size() - 1;
        for (E aData : data) {
            if (aData instanceof Nameable)
                NameableTracer.unregisterForNameable((Nameable) aData, this);
        }
        data.clear();
        if (listeners != null && listeners.size() != 0) {
            ListDataEvent lde = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, seq);
            for (int j = listeners.size() - 1; j >= 0; j--) {
                listeners.get(j).intervalRemoved(lde);
            }
        }
    }

    public void remove(int[] ix) {
        int seq0 = ix[0];
        int seq = ix[0] - 1;
        for (int i = ix.length - 1; i >= 0; i--) {
            if (data.get(ix[i]) instanceof NameableProtectedRemove && !((NameableProtectedRemove) data.get(ix[i])).isRemoveable())
                throw new JoriaAssertionError("not allowed to remove " + data.get(ix[i]));
            data.remove(ix[i]);
            if (ix[i] != seq + 1 && listeners != null && listeners.size() != 0) {
                ListDataEvent lde = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, seq0, seq);
                seq0 = ix[i];
                seq = ix[i] - 1;
                for (int j = listeners.size() - 1; j >= 0; j--) {
                    listeners.get(j).intervalRemoved(lde);
                }
            }
        }
    }

    public int remove(E e) {
        if (e == null || e.getName() == null)
            return -1;
        int ix = binarySearch(e.getName());
        if (ix >= 0) {
            E r = data.get(ix);
            if (r == e)
                remove(ix);
        }
        return ix;
    }

    public void remove(String s) {
        if (s == null)
            return;
        int ix = binarySearch(s);
        if (ix >= 0)
            remove(ix);
    }

    /**
     * ----------------------------------------------------------------------- removeListDataListener
     */
    public void removeListDataListener(ListDataListener l) {
        if (listeners != null)
            listeners.remove(l);
    }

    public void setCaseSensitive(boolean newCaseSensitive) {
        caseSensitive = newCaseSensitive;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void elementAtChanged(int ix) {
        if (listeners == null || listeners.size() == 0)
            return;
        ListDataEvent lde = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, ix, ix);
        for (int i = listeners.size() - 1; i >= 0; i--) {
            listeners.get(i).contentsChanged(lde);
        }
    }

    public E[] toArray(E[] a) {
        data.toArray(a);
        return a;
    }

    public Object[] toArray() {
        return data.toArray();
    }

    public void reSort() {
        if (caseSensitive)
            data.sort(NamedComparator.caseSensitiveComparator);
        else
            data.sort(NamedComparator.caseInsensitiveComparator);
    }

    public ArrayList<E> getData() {
        return data;
    }

    public String uniqueName(String base) {
        int n = 1;
        int newLength = base.length();
        char ch = base.charAt(newLength - 1);
        while (Character.isDigit(ch) || ch == '_') {
            newLength--;
            ch = base.charAt(newLength - 1);
        }
        base = base.substring(0, newLength);
        String nName = base;
        do {
            Named fn = find(nName);
            if (fn == null)
                return nName;
            nName = base + '_' + n++;
        }
        while (true);
    }

    public void removeInconsistentAll(E e) {
        int ix;

        while ((ix = data.indexOf(e)) > 0) {
            data.remove(ix);
        }
    }

    protected void check() throws java.io.ObjectStreamException {
        if (checkOrder() > 0) {
            reSort();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void nameableHasBeenRenamed(Nameable what) {
        //noinspection unchecked
        add((E) what);
        NameableTracer.unregisterForNameable(what, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void nameableWillBeRenamed(Nameable what) {
        //noinspection unchecked
        remove((E) what);
        NameableTracer.registerForNameable(what, this);
    }

    protected Object readResolve() {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) instanceof Nameable)
                NameableTracer.registerForNameable((Nameable) data.get(i), this);
        }
        return this;
    }
}
