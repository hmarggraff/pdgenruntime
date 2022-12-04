// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

import org.pdgen.env.Res;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PickAccess extends MutableAccess implements JoriaReportPrivateAccess
{
    private static final long serialVersionUID = 7L;
    protected boolean picking;
    protected MutableCollection sourceCollection; // source collection in case of pick; if not picked, then the same as type

    public PickAccess(JoriaAccess collAxs)
    {
        super(collAxs);
        sourceCollection = (MutableCollection) collAxs.getType();
    }

    protected PickAccess(JoriaClass parent, JoriaAccess collAxs) // used by dup: will probably fail for others unless source collection must be set
    {
        super(parent, collAxs);
    }

    public PickAccess(JoriaAccess collAxs, MutableCollection type)
    {
        super(collAxs);
        sourceCollection = type;
        super.setType(type);
    }

    public PickAccess(JoriaClass parent, JoriaAccess collAxs, MutableCollection type)
    {
        super(parent, collAxs);
        sourceCollection = type;
        setType(type);
    }

    protected PickAccess(JoriaClass parent, String name)
    {
        super(parent, name);
    }

    public void makeName()
    {
        String newName = null;
        if (picking)
            newName = Res.asis("pick");
        makeName(newName);
    }

    public DBData getValue(DBData from, JoriaAccess as, RunEnv env) throws JoriaDataException
    {
        if (from == null || from.isNull())
            return null;
        final DBCollection v;
        if (!(getBaseAccess() instanceof PickAccess))
        {
            env.pushToObjectPath(from);
            v = (DBCollection) getBaseAccess().getValue(from, as, env);
            env.popFromObjectPath();
        }
        else
            v = (DBCollection) getBaseAccess().getValue(from, as, env);
        if (picking)
        {
            if (v != null && !v.isNull())
                return v.pick();
            else
                return null;
        }
        else
            return v;
    }

    public boolean isPlain()
    {
        return !picking;
    }

    public void setPicking(boolean newPicking)
    {
        picking = newPicking;
        if (picking)
            super.setType(sourceCollection.getElementType());
        else
            super.setType(sourceCollection);
    }

    public void setType(JoriaType newType)
    {
        if (newType instanceof MutableCollection)
        {
            sourceCollection = (MutableCollection) newType;
            if (picking)
                super.setType(sourceCollection.getElementType());
            else
                super.setType(sourceCollection);
        }
        else
            throw new JoriaAssertionError("PickAccess may only have MutableCollections as type");
    }

    public boolean isPicking()
    {
        return picking;
    }

    public JoriaCollection getSourceCollection()
    {
        return sourceCollection;
    }

    public JoriaType getSourceTypeForChildren()
    {
        //exp hmf 030226 : should not return source collection but rather base type
        //return sourceCollection;
        //return ((IndirectAccess)getBaseAccess()).getBaseAccess().getType();
        //hmf 220223 try to use source collection after all
        //return ((ClassView) sourceCollection.getElementType()).getBase();
        return sourceCollection;
    }

    public void collectVariables(Set<RuntimeParameter> s, Set<Object> seen)
    {
        super.collectVariables(s, seen);
        sourceCollection.collectVariables(s, seen);
    }

    public boolean isTransformer()
    {
        return true;
    }

    protected PickAccess(ClassView newParent, String name)
    {
        super(newParent, name);
    }

    public NameableAccess dup(JoriaClass newParent, Map<Object, Object> alreadyCopied)
    {
        final Object duplicate = alreadyCopied.get(this);
        if (duplicate != null)
            return (NameableAccess) duplicate;
        PickAccess ret = new PickAccess(newParent, myBaseAccess);
        dupType(ret, alreadyCopied);
        alreadyCopied.put(this, ret);
        fillDup(ret, alreadyCopied);
        return ret;
    }

    public void fillDup(MutableAccess newAxs, Map<Object, Object> alreadyCopiedViews)
    {
        super.fillDup(newAxs, alreadyCopiedViews);
        ((PickAccess) newAxs).picking = picking;
    }

    protected void dupType(MutableAccess newAxs, Map<Object, Object> alreadyCopiedViews)
    {
        PickAccess pa = (PickAccess) newAxs;
        MutableView newType = (MutableView) alreadyCopiedViews.get(sourceCollection);
        if (newType == null)
        {
            newType = sourceCollection.dup(alreadyCopiedViews);
        }
        pa.sourceCollection = (MutableCollection) newType;
        if (picking)
            pa.type = pa.sourceCollection.getElementType();
        else
            pa.type = pa.sourceCollection;
        pa.makeLongName();
    }

    public void collectViewUsage(Map<MutableView, Set<Object>> viewUsage, Set<MutableView> visitedViews)
    {
        super.collectViewUsage(viewUsage, visitedViews);
        if (sourceCollection != null)
        {
            Repository.addViewUsage(viewUsage, sourceCollection, this);
            sourceCollection.collectViewUsage(viewUsage, visitedViews);
        }
    }

    public void collectUsedViews(Set<MutableView> s)
    {
        super.collectUsedViews(s);
        if (sourceCollection != null)
            sourceCollection.collectUsedViews(s);
    }

    public JoriaAccess copyReportPrivateAccess(Map<Object, Object> copiedData)
    {
	    MutableCollection newSource = sourceCollection.copyReportPrivate(copiedData);
	    copiedData.put(sourceCollection, newSource);
	    PickAccess ret = new PickAccess(myBaseAccess, newSource);
	    ret.picking = picking;
	    ret.name = name; // required for identical undo, when the base access has been renamed
	    ret.xmlTag = xmlTag;
	    copiedData.put(this, ret);
	    return ret;
    }

    public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> s, Set<Object> seen)
    {
        super.collectI18nKeys2(s, seen);
        if (picking)
            sourceCollection.collectI18nKeys2(s, seen);
    }

    public void unbind()
    {
        super.unbind();
        sourceCollection.unbind();
    }

    public void rebind(JoriaAccess newBinding, JoriaAccess newParentBinding)
    {
        Trace.check(bindableTo(newBinding, newParentBinding));
        //boolean sameName = myBaseAccess.getName().equals(getName());
        myBaseAccess = newBinding;
        if (isPicking())
        {
            if (type instanceof ClassProjection)
            {
                ClassProjection cp = (ClassProjection) type;
                cp.rebindType(newBinding.getCollectionTypeAsserted());
            }
            else if (type instanceof Rebindable)
            {
                Rebindable rebindable = (Rebindable) type;
                rebindable.rebind(newBinding, newParentBinding);
            }
            else
                type = newBinding.getCollectionTypeAsserted().getElementType();
            makeName();
        }
        else
            rebindInner(newBinding, newParentBinding);
        sourceCollection.rebind(newBinding, newParentBinding);
    }
}
