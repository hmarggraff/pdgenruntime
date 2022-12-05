// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.DBData;
import org.pdgen.data.DBObject;
import org.pdgen.model.Template;

import java.util.Stack;

public class DrillDownLink {
    public DBObject obj;
    public Stack<DBData> fromStack;
    public Template target;

    public DrillDownLink(DBObject obj, Template target, Stack<DBData> fromStack) {
        this.obj = obj;
        this.target = target;
        this.fromStack = new Stack<DBData>();
        for (DBData dbData : fromStack) {
            this.fromStack.push(dbData);
        }
    }
}
