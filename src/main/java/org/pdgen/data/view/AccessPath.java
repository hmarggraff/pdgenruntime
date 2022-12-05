// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

import org.pdgen.env.Env;
import org.pdgen.oql.OQLNode;
import org.pdgen.env.Res;
import org.pdgen.oql.AndNode;
import org.pdgen.oql.Node;
import org.pdgen.oql.NodeInterface;
import org.pdgen.oql.OQLParseException;

import java.util.*;

public class AccessPath extends AbstractJoriaAccess implements VariableProvider, CollectUsedViewsAccess, RootGetableAccess, VisitableAccess, JoriaAccess {
    private static final long serialVersionUID = 7L;
    JoriaAccess[] myPath;
	boolean myIsColl;// cache info whether this path returns a collection
	boolean haveTransformer;// cache info whether this path returns a collection

	public String getPathExpression()
	{
		StringBuffer sb = new StringBuffer();
		for (JoriaAccess ja : myPath)
		{
			sb.append(ja.getName()).append('.');
		}
		sb.setLength(sb.length() - 1);
		String src = sb.toString();
		return src;
	}

	/**
	* If the last path component is the only collection in the path, then
	* getvalue does not have to build the collection, but rather returns the value
	* from the last component. Internal.
	 * @param thePath the steps to combine
	 * @return a new access taht combines the steps
	*/

	public static JoriaAccess makePath(JoriaAccess[] thePath)
	{
		if (thePath == null || thePath.length == 0)
			return null;
		else if (thePath.length == 1)
			return thePath[0];
		for (int n = 0; n < thePath.length; n++)
		{
			JoriaAccess step = thePath[n];
			JoriaType stt = step.getType();			/* if there is a collection along the path then the accesspath consists
			   of a path to the collection and a path from the collection onwards which becomes a
			   member of the element type of the collection
			*/
			if (stt.isCollection())
			{
				if (n == thePath.length - 1)
					return new AccessPath(thePath, true);
				else
				{
					MutableAccess leftP;
					if (n > 1)
					{
						JoriaAccess[] myPath = new JoriaAccess[n + 1];
						System.arraycopy(thePath, 0, myPath, 0, n + 1);
						JoriaAccess leftBase = new AccessPath(myPath, true);
						leftP = new MutableAccess(leftBase);// exp pm 091305 dieser MutableAccess ist zwar nicht sauber						// stoert aber nicht, da der AccessPath nicht mehr aufgedroeselt wird.
					}
					else
					{
						leftP = new MutableAccess(step);// exp pm 091305 siehe oben
					}
					CollectionProjection cp = new CollectionProjection((JoriaCollection) stt);
					leftP.setType(cp);
					JoriaAccess[] rightPath = new JoriaAccess[thePath.length - n - 1];
					System.arraycopy(thePath, n + 1, rightPath, 0, thePath.length - n - 1);
					JoriaAccess right = makePath(rightPath);					//TODO collection in collection
					if (right.getType().isCollection())
						throw new NotYetImplementedError("Path would result in a multi dimensional collection, which is not yet implemented");
					ClassProjection rType = new ClassProjection(((JoriaCollection) stt).getElementType());
					rType.addChild(right);
					cp.setElementType(rType);
					return leftP;
				}
			}
		}
		return new AccessPath(thePath, false);
	}

	public static JoriaAccess makePathToFirstColl(JoriaAccess[] thePath, final int from)
	{
		for (int n = from; n < thePath.length; n++)
		{
			JoriaAccess step = thePath[n];
			JoriaType stt = step.getType();
			if (stt.isCollection())
			{
				if (n == thePath.length - 1)
					return new AccessPath(thePath, true);
				else
				{
					if (n > 1)
					{
						JoriaAccess[] myPath = new JoriaAccess[n + 1];
						System.arraycopy(thePath, 0, myPath, 0, n + 1);
						return new AccessPath(myPath, true);
					}
					else
					{
						return step;
					}
				}
			}
		}
		return null;
	}

	public static List<JoriaAccess> makePathViaColls(JoriaAccess[] thePath)
	{
		if (thePath == null || thePath.length == 0)
			return null;
		ArrayList<JoriaAccess> ret = new ArrayList<JoriaAccess>();
		int start = 0;
		for (int n = 0; n < thePath.length; n++)
		{
			JoriaAccess step = thePath[n];
			JoriaType stt = step.getType();
			if (stt.isCollection())
			{
				if (n-start > 0)
				{
					JoriaAccess[] myPath = new JoriaAccess[n + 1 - start];
					System.arraycopy(thePath, start, myPath, 0, n + 1 - start);
					ret.add(new AccessPath(myPath, true));
					start = n+1;
				}
				else
				{
					ret.add(step);
					start ++;
				}
			}
		}
		final int remainder = thePath.length - start;
		if (remainder > 1)
		{
			JoriaAccess[] myPath = new JoriaAccess[remainder];
			System.arraycopy(thePath, start, myPath, 0, remainder);
			ret.add(new AccessPath(myPath, true));
		}
		else if (remainder == 1)
		{
			ret.add(thePath[thePath.length-1]);
		}
		//else path ends with a collection: nothing more to do

		return ret;
	}

	private AccessPath(JoriaAccess[] thePath, boolean isColl)// may only be called from makepath
	{
		if (thePath.length == 0)
			throw new EmptyStackException();// empty path not allowed
		myPath = thePath;
		myIsColl = isColl;
		for (JoriaAccess aMyPath : myPath)
		{
			haveTransformer |= aMyPath.isTransformer();
		}
		makeName();
	}

	void makeName()
	{
		StringBuffer b = new StringBuffer();
		int l = myPath.length - 1;
		for (int i = 0; i < l; i++)
		{
			JoriaAccess step = myPath[i];
			if (step instanceof BaseViewAccess)
				continue;
			b.append(step.getName()).append('_');
		}
		b.append(myPath[l].getName());
		name = b.toString();
	}

	public void collectVariables(Set<RuntimeParameter> ret, Set<Object> seen)
	{
		for (JoriaAccess aMyPath : myPath)
		{
			if (aMyPath instanceof VariableProvider)
				((VariableProvider) aMyPath).collectVariables(ret, seen);
		}
	}

	public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> s, Set<Object> seen)
	{
		for (JoriaAccess aMyPath : myPath)
		{
			if (aMyPath instanceof VariableProvider)
				((VariableProvider) aMyPath).collectI18nKeys2(s, seen);
		}
	}

	public void collectVisiblePickersInScope(List<JoriaAccess[]> collection, Set<RuntimeParameter> visible, Stack<JoriaAccess> pathStack, Set<Object> seen)
	{
		if (seen.contains(this))
			return;
		else
			seen.add(this);
		for (JoriaAccess aMyPath : myPath)
		{
			if (aMyPath instanceof VariableProvider)
				((VariableProvider) aMyPath).collectVisiblePickersInScope(collection, visible, pathStack, seen);
		}
	}

	public JoriaClass getDefiningClass()
	{
		if (myPath[0] instanceof JoriaAccess)
			return myPath[0].getDefiningClass();
		else
			return null;
	}

	public JoriaAccess[] getPath()
	{
		return myPath;
	}

	public JoriaAccess getPathComponent(int i)
	{
		return myPath[i];
	}

	/**
	 * ----------------------------------------------------------------------- getType
	 */
	public JoriaType getType()
	{
		JoriaAccess f = myPath[myPath.length - 1];
		JoriaType p = f.getType();
		return p;
	}

	/**
	 * ----------------------------------------------------------------------- getValue
	 */
	public DBData getValue(DBData anObject, JoriaAccess asView, RunEnv env) throws JoriaDataException
	{
		boolean isRoot = myPath[0].isRoot();
		if (anObject == null && !isRoot)
			return null;
		JoriaAccess axs = myPath[0];
		DBData v = axs.getValue(anObject, axs, env);
		for (int i = 1; i < myPath.length - 1 && v != null && !v.isNull(); i++)
		{
			axs = myPath[i];
			v = axs.getValue(v, axs, env);
		}
		if (v != null && !v.isNull())
		{
			axs = myPath[myPath.length - 1];
			v = axs.getValue(v, asView, env);
		}
		if (v == null || v.isNull())
			return null;
		return v;
	}

	public boolean isCollection()
	{
		return myIsColl;
	}

	public boolean isRoot()
	{
		return myPath[0].isRoot();
	}

	public static int countCollections(JoriaAccess[] a)
	{
		int ret = 0;
		for (JoriaAccess anA : a)
		{
			if (anA.getType().isCollection())
				ret++;
		}
		return ret;
	}

	public static JoriaAccess findFirstColl(JoriaAccess[] thePath)
	{
		for (JoriaAccess step : thePath)
		{
			JoriaType stt = step.getType();
			if (stt.isCollection())
			{
				return step;
			}
		}
		return null;
	}

	public int getPathLength()
	{
		return myPath.length;
	}

	public boolean isTransformer()
	{
		return haveTransformer;
	}

	public void setName(String name)
	{
		this.name = name;
		makeLongName();
	}

	public JoriaAccess getPlaceHolderIfNeeded()
	{
		for (int i = 0; i < myPath.length; i++)
		{
			JoriaAccess access = myPath[i];
			JoriaAccess fixed = access.getPlaceHolderIfNeeded();
			if (fixed instanceof JoriaPlaceHolderAccess)
			{
				Env.repoChanged();
				return fixed;
			}
			else if (fixed != null)
			{
				Env.repoChanged();
				myPath[i] = fixed;
			}
		}
		return null;
	}

	public void collectViewUsage(Map<MutableView, Set<Object>> viewUsage, Set<MutableView> visitedViews)
	{
		for (JoriaAccess joriaAccess : myPath)
		{
			if (joriaAccess instanceof CollectUsedViewsAccess)
				((CollectUsedViewsAccess) joriaAccess).collectViewUsage(viewUsage, visitedViews);
		}
	}

	public void collectUsedViews(Set<MutableView> s)
	{
		for (JoriaAccess joriaAccess : myPath)
		{
			if (joriaAccess instanceof CollectUsedViewsAccess)
				((CollectUsedViewsAccess) joriaAccess).collectUsedViews(s);
		}
	}

	private AccessPath(String name)
	{
		super(name);
	}

	public JoriaCollection getSourceCollection()
	{
		return myPath[myPath.length - 1].getSourceCollection();
	}

	public JoriaAccess getRootAccess()
	{
		JoriaAccess base = myPath[0];
		return base;
	}

	public boolean visitAllAccesses(AccessVisitor visitor, Set<JoriaAccess> seen)
	{
		if (seen.contains(this))
			return true;
		seen.add(this);
		for (JoriaAccess joriaAccess : myPath)
		{
			if (!visitor.visit(joriaAccess))
				return false;
			if (joriaAccess instanceof VisitableAccess)
			{
				if (!((VisitableAccess) joriaAccess).visitAllAccesses(visitor, seen))
					return false;
			}
		}
		return true;
	}

	@Override
	public String getCascadedHostFilter()
	{
		String bFilter = myPath[myPath.length - 1].getCascadedHostFilter();
		JoriaCollection p = getSourceCollection();
		if (p.getFilter() != null && p.getFilter().getHostFilterString() != null)
		{
			String f = p.getFilter().getHostFilterString();
			if (bFilter != null)
			{
				return "(" + bFilter + Res.asis(")and(") + f + ")";
			}
			else
				return f;
		}
		else
			return bFilter;
	}

	public OQLNode getCascadedOQLFilter() throws OQLParseException
	{
		OQLNode bFilter = myPath[myPath.length - 1].getCascadedOQLFilter();
		JoriaCollection p = getSourceCollection();
		if (p.getFilter() != null && p.getFilter().getOqlString() != null)
		{
			OQLNode f = super.getCascadedOQLFilter();
			if (f != null && bFilter != null)
				return new AndNode(NodeInterface.booleanType, (Node) bFilter, (Node) f);
			else if (f != null)
				return f;
			else
				return bFilter;
		}
		else
			return bFilter;
	}
}
