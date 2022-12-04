// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.projection;

import org.pdgen.data.JoriaDataException;
import org.pdgen.data.view.CollectionView;
import org.pdgen.data.view.DefaultAccess;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaClass;
import org.pdgen.data.JoriaCollection;
import org.pdgen.data.I18nKeyHolder;

import java.util.Set;
import java.util.HashMap;
import java.util.List;

/**Implements picking access for the elements of the master collection of a
 * master deatil report.
 */
public class MaDetElementAccess extends DefaultAccess
{

    private static final long serialVersionUID = 7L;
    CollectionView sourceCollection;

    public MaDetElementAccess(JoriaClass parent, JoriaAccess access, CollectionView sourceCollection)
    {
        super(parent, sourceCollection.getElementType(), access);
        this.sourceCollection = sourceCollection;
    }

    public void collectVariables(Set<RuntimeParameter> s, Set<Object> seen)
    {
        super.collectVariables(s, seen);
        sourceCollection.collectVariables(s, seen);
    }

    public JoriaCollection getSourceCollection()
    {
        return sourceCollection;
    }

    public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException
    {
        return myBaseAccess.getValue(from, this, env);
    }

    public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> s, Set<Object> seen)
    {
        super.collectI18nKeys2(s, seen);
        sourceCollection.collectI18nKeys2(s, seen);
    }
}
