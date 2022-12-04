// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import java.util.*;

public class NameableTracer<E extends Nameable>
{
    static Map<Nameable, List<NameableListener<Nameable>>> workMap;

    public static void reset()
    {
        workMap = null;
    }

    public static void registerForNameable(Nameable what, NameableListener<Nameable> who)
    {
        if(workMap == null)
            workMap = new HashMap<Nameable, List<NameableListener<Nameable>>>();
        List<NameableListener<Nameable>> listeners = workMap.get(what);
        if(listeners == null)
        {
            listeners = new ArrayList<NameableListener<Nameable>>();
            workMap.put(what, listeners);
        }
        listeners.add(who);
    }

    public static void unregisterForNameable(Nameable what, NameableListener<Nameable> who)
    {
        if(workMap == null)
            return;
        List<NameableListener<Nameable>> listeners = workMap.get(what);
        if(listeners == null)
        {
            return;
        }
        listeners.remove(who);
    }

    public static void notifyListenersPre(Nameable what)
    {
        if(workMap == null)
            return;
        List<NameableListener<Nameable>> listeners = workMap.get(what);
        if(listeners == null)
        {
            return;
        }
        for (NameableListener<Nameable> listener : Collections.unmodifiableCollection(listeners)) listener.nameableWillBeRenamed(what);
    }

    public static void notifyListenersPost(Nameable what)
    {
        if(workMap == null)
            return;
        List<NameableListener<Nameable>> listeners = workMap.get(what);
        if(listeners == null)
        {
            return;
        }
        for (NameableListener<Nameable> listener : Collections.unmodifiableCollection(listeners)) listener.nameableHasBeenRenamed(what);
    }
}
