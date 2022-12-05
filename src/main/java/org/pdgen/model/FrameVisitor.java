// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

/**
 * User: patrick
 * Date: Feb 8, 2006
 * Time: 12:46:44 PM
 */
public interface FrameVisitor {
    boolean visitFrame(TemplateBox frame);
}
