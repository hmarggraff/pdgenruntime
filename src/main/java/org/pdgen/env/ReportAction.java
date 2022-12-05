// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

import org.pdgen.data.DBData;

import javax.swing.*;
import java.util.Locale;
import java.util.Stack;

/**
 * Interface to implement a context menu action in the report window
 * User: patrick
 * Date: 16.01.2007
 * Time: 07:44:24
 */
public interface ReportAction {
    /**
     * Determine the menu entry text in a localized way
     *
     * @param loc Locale for the name
     * @return the menu entry text
     */
    String getName(Locale loc);

    /**
     * Determine the menu entry icon in a localized way
     *
     * @param loc Locale for the icon
     * @return the icon
     */
    Icon getIcon(Locale loc);

    /**
     * Determine if the action should be visible for the current object
     *
     * @param data  the object
     * @param stack the stack of objects
     * @return if the action should be included in the menu
     */
    boolean isVisible(DBData data, Stack<DBData> stack);

    /**
     * Determine if the action should be enabled for the current object
     *
     * @param data  the object
     * @param stack the stack of objects
     * @return if the action should be enabled in the menu
     */
    boolean isEnabled(DBData data, Stack<DBData> stack);

    /**
     * execute the action
     *
     * @param data  the object
     * @param stack the stack of objects
     */
    void execute(DBData data, Stack<DBData> stack);
}
