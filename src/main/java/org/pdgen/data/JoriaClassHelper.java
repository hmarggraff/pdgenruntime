// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

public class JoriaClassHelper {
    public static boolean isAssignableFrom(JoriaType target, JoriaType source) {
        if (target == source)
            return true;
        if (source instanceof JoriaClass) {
            JoriaClass[] bases = ((JoriaClass) source).getBaseClasses();
            for (JoriaClass bas : bases) {
                if (isAssignableFrom(target, bas))
                    return true;
            }
        }
        return false;
    }

    public static boolean isAssignableFrom(DBData sourceData, JoriaType target) {
        if (sourceData instanceof DBObject)
            return ((DBObject) sourceData).isAssignableTo(target);
        JoriaType source = sourceData.getActualType();
        return isAssignableFrom(target, source);
    }

}
