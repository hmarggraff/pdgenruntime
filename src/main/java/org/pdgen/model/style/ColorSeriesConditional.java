// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.env.JoriaInternalError;
import org.pdgen.model.run.RunEnv;
import org.pdgen.oql.JoriaQuery;
import org.pdgen.oql.OQLParser;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class ColorSeriesConditional implements ColorSeries, VariableProvider {
    private static final long serialVersionUID = 7L;
    final String[] conditions;
    final Color[] colors; // one more colors than condititions (default color)
    final JoriaType scope;

    public ColorSeriesConditional(String[] conditions, Color[] colors, JoriaType scope) {
        this.conditions = conditions;
        this.colors = colors;
        this.scope = scope;
    }

    public Color getColorAt(int index, DBData val, RunEnv env)  {
        for (int ix = 0; ix < conditions.length - 1; ix++) {
            JoriaQuery q = getParsedCondition(ix);
            final boolean bool;
            try {
                bool = q.getBooleanValue(env, val);
            } catch (JoriaDataException e) {
                throw new RuntimeException(e);
            }
            if (bool)
                return colors[ix];
        }
        return colors[colors.length - 1];
    }

    public void paintDesignerComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        final Rectangle bounds = g2.getClipBounds();
        final float width = bounds.width;
        final int slices = colors.length;
        float step = width / slices;
        float at = 0;
        final int height = bounds.height;
        for (Color color : colors) {
            g2.setColor(color);
            g2.fill(new Rectangle2D.Float(at, 0, step, height));
            at += step;
        }
    }

    public JoriaQuery getParsedCondition(int i) {
        JoriaQuery q = OQLParser.lookInCache(conditions[i], scope);
        if (q != null)
            return q;
        try {
            q = OQLParser.parse(conditions[i], scope, false);
        } catch (Exception ex) {
            throw new JoriaInternalError("Unexpected Parse Exception in run", ex);
        }
        return q;
    }

    public String[] getConditions() {
        return conditions;
    }

    public Color[] getColors() {
        return colors;
    }

    public void collectVariables(Set<RuntimeParameter> v, Set<Object> seen) {
        //Todo implement in Colorseriesconditional collectVariables
    }

    public void collectI18nKeys2(HashMap<String, List<I18nKeyHolder>> s, Set<Object> seen) {
        //Todo implement in Colorseriesconditional collectI18nKeys2
    }

    public void collectVisiblePickersInScope(List<JoriaAccess[]> collection, Set<RuntimeParameter> visible, Stack<JoriaAccess> pathStack, Set<Object> seen) {
        //Todo implement in Colorseriesconditional collectVisiblePickersInScope
    }
}
