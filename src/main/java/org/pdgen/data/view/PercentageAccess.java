// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


import java.util.Set;

public class PercentageAccess extends AbstractMember implements IndirectAccess
{
    private static final long serialVersionUID = 7L;
    JoriaAccess baseAccess;

	public PercentageAccess(JoriaAccess baseAccess)
	{
		super(baseAccess.getDefiningClass(), "% " + baseAccess.getName());
		final JoriaType type = baseAccess.getType();
		Trace.check(type.isRealLiteral() || baseAccess.getType().isIntegerLiteral(), "percentage must be based on a number");
		this.baseAccess = baseAccess;
	}

	public JoriaType getType()
	{
		return DefaultRealLiteral.instance();
	}

	public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException
	{
		return baseAccess.getValue(from, this, env);
	}

	public JoriaAccess getBaseAccess()
	{
		return baseAccess;
	}

	public void unbind()
	{
		baseAccess = new UnboundAccessSentinel(baseAccess);
	}

	public boolean unbound()
	{
		return baseAccess instanceof UnboundAccessSentinel;
	}

	public boolean bindableTo(JoriaAccess newBinding, JoriaAccess newParentBinding)
	{
		return ((UnboundAccessSentinel) baseAccess).isBindable(newBinding);
	}

	public void rebind(JoriaAccess newBinding, JoriaAccess newParentBinding)
	{
		Trace.check(bindableTo(newBinding, newParentBinding));
		baseAccess = newBinding;
	}

    public JoriaAccess getRootAccess()
    {
        return getBaseAccess();
    }

    public boolean visitAllAccesses(AccessVisitor visitor, Set<JoriaAccess> seen)
    {
        if(seen.contains(this))
            return true;
        seen.add(this);
        if(!visitor.visit(baseAccess))
            return false;
        if(baseAccess instanceof VisitableAccess)
        {
			return ((VisitableAccess) baseAccess).visitAllAccesses(visitor, seen);
        }
        return true;
    }
    
}
