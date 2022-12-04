// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.*;
import org.pdgen.model.*;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.StyledTextCellDef;
import org.pdgen.model.style.*;
import org.pdgen.schemacheck.CheckFormulasForSchemaChange;
import org.pdgen.schemacheck.ViewMemberFinder;
import org.pdgen.styledtext.model.AttributeRun;
import org.pdgen.styledtext.model.StyledParagraph;
import org.pdgen.styledtext.model.StyledParagraphList;
import org.pdgen.util.StringPair;

import org.pdgen.env.Env;
import org.pdgen.env.Res;


import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.font.TextAttribute;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.*;

public class Repository implements Serializable, ListDataListener {
    private static final long serialVersionUID = 7L;
    public SortedNamedVector<ClassProjection> classProjections;
    public SortedNamedVector<CollectionProjection> collectionProjections;
    public SortedNamedVector<CastAccess> userRoots;
    public SortedNamedVector<RuntimeParameter> variables;
    public SortedNamedVector<Template> reports;
    public SortedNamedVector<PageStyle> pageStyles;
    public SortedNamedVector<CellStyle> cellStyles;
    public SortedNamedVector<TableStyle> tableStyles;
    public SortedNamedVector<FrameStyle> frameStyles;
    public SortedNamedVector<PaperSize> pageSizes;
    public SortedNamedVector<ChartStyle> chartStyles;
    public Internationalisation i18n;
    public boolean storeRelativePathes = true;
    public int lastReportNumber;
    public int repostitoryVersion = 100;

    public transient boolean loadingCentralRepository;
    public transient List<StringPair> fileReplacements;
    public transient List<Template> problematicTemplates;
    transient StringBuffer fixLog;

    public Repository(boolean init) {
        classProjections = new SortedNamedVector<>();
        collectionProjections = new SortedNamedVector<>();
        userRoots = new SortedNamedVector<>();
        variables = new SortedNamedVector<>();
        reports = new SortedNamedVector<>();
        pageStyles = new SortedNamedVector<>();
        cellStyles = new SortedNamedVector<>();
        tableStyles = new SortedNamedVector<>();
        frameStyles = new SortedNamedVector<>();
        pageSizes = new SortedNamedVector<>();
        if (init) {
            PaperSize.init(pageSizes);
            PageStyle.init(pageStyles);
            PredefinedStyles.init(cellStyles);
            FrameStyle.init(frameStyles);
            TableStyle.init(tableStyles);
        }
        userRoots.addListDataListener(this);
        variables.addListDataListener(this);
        reports.addListDataListener(this);
        pageStyles.addListDataListener(this);
        cellStyles.addListDataListener(this);
        tableStyles.addListDataListener(this);
        frameStyles.addListDataListener(this);
        pageSizes.addListDataListener(this);
        fileReplacements = new ArrayList<>();
    }

    public void intervalAdded(ListDataEvent e) {
        Env.repoChanged();
    }

    public void intervalRemoved(ListDataEvent e) {
        Env.repoChanged();
    }

    public void contentsChanged(ListDataEvent e) {
        Env.repoChanged();
    }

    public void checkSchemaAndRepositoryForConsistency(ArrayList<WeakReference<JoriaModifiedAccess>> modifiedAccesses, ArrayList<WeakReference<JoriaUnknownType>> unknownTypes) {
        fixLog = new StringBuffer();
        ViewMemberFinder.markAllFieldsInViews();

        renameDuplicateRoots();
        final boolean hasSchemaChanges = modifiedAccesses.size() >0 && unknownTypes.size() > 0;
        if (hasSchemaChanges) {
            removeOrRepairDeletedRoots();
        }
        for (int i = 0; i < reports.getSize(); i++) {
            final Template template = reports.elementAt(i);
            if (hasSchemaChanges && unbindReportFromDeletedRoot(template))
                i--;

            removeUsesOfDefaultFont(template); // Default font not compatible with portable PDF

            resurrectLostRoot(template);  // If a view-root was deleted, that is used in a template

            fixVisibilityConditionInReport(template);

            reconnectLocalFramesToPagemaster(template);  // Sometimes local frames are created, that are not connected to a page. Fix this.
        }        // checks that all formulas are valid and that report local formulas are marked.

        CheckFormulasForSchemaChange.check(fixLog);

        if (fixLog.length() > 0) {
            Trace.logWarn(fixLog.toString());
            Env.instance().tell(Res.msg("The_schema_of_the_datasource_has_changed_The_reports_were_updated__0_See_below_which_actions_were_taken", "\n"), fixLog.toString());
        }
        fixLog = null;
    }

    private void fixVisibilityConditionInReport(final Template template) {
        int logAt = fixLog.length();
        template.fixVisibiltyCondition();
        if (logAt < fixLog.length()) {
            fixLog.insert(logAt, Res.strp("Template_fixed", template.getName()) + "\n");
        }
    }

    private void reconnectLocalFramesToPagemaster(final Template template) {
        final PageMaster.AllFramesIterator framesIterator = template.getPage().iterateAllFrames();
        while (framesIterator.hasNext()) {
            PageLevelBox box = framesIterator.next();
            if (box.getPage() == null)
                box.setPage(template.getPage());
        }
    }


    private boolean unbindReportFromDeletedRoot(final Template template) {
        int logAt = fixLog.length();
        if (!template.fixAccess()) {
            logFix(Res.str("Template_"), template, Res.str("unbound_base_root_was_removed_from_schema"));
            template.makeUnbound();
            return true;
        } else if (logAt < fixLog.length()) {
            fixLog.insert(logAt, Res.strp("Template_fixed", template.getName()) + "\n");
        }
        return false;
    }

    private void removeOrRepairDeletedRoots() {
        SortedNamedVector<CastAccess> newUserRoots = new SortedNamedVector<>();
        for (int i = 0; i < userRoots.getSize(); i++) {
            final CastAccess root = userRoots.elementAt(i);
            JoriaAccess fixed = root.getPlaceHolderIfNeeded();
            if (fixed instanceof JoriaPlaceHolderAccess) {
                logFix(Res.str("Root_view"), fixed, Res.str("deleted_base_root_was_removed_from_schema"));// forget this root
                userRoots.remove(root);
            } else if (fixed instanceof CastAccess) {
                logFix(Res.str("Root_view"), fixed, Res.str("changed_base_root_was_upgraded"));
                newUserRoots.add((CastAccess) fixed);
            } else {
                newUserRoots.add(root);
            }
        }
        userRoots = newUserRoots;
    }

    private void fixMultipleUsesOfUnnamedFrameStyle(final Template template) {
        final Map<FrameStyle, TemplateBox> frameStyleUsage = new HashMap<>();
        template.visitFrames(new FrameVisitor() {
            public boolean visitFrame(TemplateBox frame) {
                FrameStyle style = frame.getFrameStyle();
                if (style != null && !style.hasName()) {
                    TemplateBox frameOwner = frameStyleUsage.get(style);
                    if (frameOwner != null && frameOwner != frame) // duplicate usage
                    {
                        frameOwner = null;
                        style = FrameStyle.duplicateLocal(style);
                        frame.setFrameStyle(style);
                    }
                    if (frameOwner == null)
                        frameStyleUsage.put(style, frame);
                }
                return true;
            }
        });
    }

    private void resurrectLostRoot(final Template r) {
        JoriaAccess ja = r.getData();
        if (ja == null || ja instanceof AccessPath || userRoots.containsName(ja) || Env.schemaInstance.getRoots().containsName(ja))
            return;
        if (ja instanceof CastAccess) {
            String nName = userRoots.uniqueName(ja.getName() + Res.asis("_repair"));
            userRoots.remove((CastAccess) ja);
            if (!nName.equals(ja.getName()))
                ((NameableAccess) ja).setName(nName);
            userRoots.add((CastAccess) ja);
            logFix(Res.str("Roots"), r, Res.str("Missing_root_repaired"));
        }
    }

    private void removeUsesOfDefaultFont(final Template r) {
        final Set<CellStyle> seenStyles = new HashSet<>(100);
        r.visitCells(new CellVisitor() {
            public boolean visit(CellDef cd, int r, int c) {
                if (cd == null)
                    return true;
                if (cd instanceof StyledTextCellDef) {
                    StyledTextCellDef styled = (StyledTextCellDef) cd;
                    StyledParagraphList paragraphs = styled.getParagraphs();
                    for (int i = 0; i < paragraphs.length(); i++) {
                        StyledParagraph paragraph = paragraphs.get(i);
                        String[] usedFonts = paragraph.getUsedFontFamilies();
                        boolean badFont = false;
                        for (String usedFont : usedFonts) {
                            if (usedFont.charAt(0) == '*')
                                badFont = true;
                        }
                        if (badFont) {
                            String baseFont = styled.getCascadedStyle().getFont();
                            ArrayList<AttributeRun> runs = paragraph.getStyleRuns();
                            for (AttributeRun attributeRun : runs) {
                                if (((String) attributeRun.getValueFor(TextAttribute.FAMILY)).charAt(0) == '*') {
                                    attributeRun.setAttribute(TextAttribute.FAMILY, baseFont);
                                }
                            }
                        }
                    }
                }
                final CellStyle style = cd.getStyle();
                if (style != null && style.getName() == null) {
                    if (seenStyles.contains(style))
                        cd.setStyle(new CellStyle(style));
                    else
                        seenStyles.add(style);
                }
                return true;
            }
        });
    }

    private void renameDuplicateRoots() {
        Hashtable<String, JoriaAccess> uniqueNameCheck = new Hashtable<>();
        HashSet<JoriaAccess> uniqueCheck = new HashSet<>();
        ArrayList<CastAccess> renamed = new ArrayList<>();
        ArrayList<CastAccess> al = userRoots.getData();

        for (Iterator<CastAccess> it = al.iterator(); it.hasNext(); ) {
            CastAccess ja = it.next();
            if (uniqueCheck.contains(ja)) {
                it.remove();
                Trace.logError("Removed duplicate user root " + ja.getName());
                continue;
            }
            uniqueCheck.add(ja);
            if (uniqueNameCheck.containsKey(ja.getName())) {
                it.remove();
                renamed.add(ja);
            }
            uniqueNameCheck.put(ja.getName(), ja);
        }
        if (renamed.size() > 0) {
            for (CastAccess ja : renamed) {
                String nName = userRoots.uniqueName(ja.getName());
                if (!nName.equals(ja.getName())) {
                    Trace.logError("Duplicate name for user root: " + ja.getClass().getName() + " " + ja.getName() + Res.asis(" changed to ") + nName);
                    ja.setName(nName);
                }
                userRoots.add(ja);
            }
        }
    }

    public void logFix(String cat, Named who, String whatwhy) {
        fixLog.append(cat).append(' ').append(who.getName()).append(" -- ").append(whatwhy).append('\n');
    }

    public void logFix(String cat, String who, String whatwhy) {
        fixLog.append(cat).append(' ').append(who).append(" -- ").append(whatwhy).append('\n');
    }

    public Set<MutableView> collectUsedViews() {
        HashSet<MutableView> s = new HashSet<>();
        for (int i = 0; i < userRoots.getData().size(); i++) {
            JoriaAccess access = userRoots.getData().get(i);
            if (access != null)
                ((MutableAccess) access).collectUsedViews(s);            //((MutableAccess) access).collectUsedViews(s);
        }
        for (int i = 0; i < reports.getData().size(); i++) {
            Template template = reports.getData().get(i);
            JoriaAccess access = template.getStarter();
            if (access instanceof MutableAccess)
                ((MutableAccess) access).collectUsedViews(s);
        }
        return s;
    }


    @SuppressWarnings("UnusedDeclaration")

    public String mapDir(String from) {
        if (fileReplacements == null)
            return from;
        for (StringPair m : fileReplacements) {
            if (m.s != null && from.startsWith(m.s)) {
                String ret = m.v + from.substring(m.s.length());
                Trace.log(Trace.mapdir, "File location: " + from + " mapped to: " + ret);
                return ret;
            }
        }
        Trace.log(Trace.mapdir, "File location: " + from + " not mapped.");
        return from;
    }


    public static void addViewUsage(Map<MutableView, Set<Object>> viewUsageMap, MutableView view, Object obj) {
        Set<Object> testSet = viewUsageMap.get(view);
        if (testSet == null) {
            testSet = new HashSet<>();
            viewUsageMap.put(view, testSet);
        }
        if (obj != null)
            testSet.add(obj);
    }


    @SuppressWarnings("UnusedDeclaration")
    public Set<String> getAllReferencedFiles() {
        Set<String> used = Env.instance().getFileService().getUsedFiles();
        final HashSet<String> ret = new HashSet<>(used);
        ret.add(Env.instance().getCurrentFile());

        if (i18n != null)
            ret.add(i18n.getDirectory() + "/" + i18n.getPropertiesName() + Res.asis(".properties"));
        for (int i = 0; i < reports.getSize(); i++) {
            Template r = reports.getElementAt(i);
            r.getAllReferencedFiles(ret);
        }
        return ret;
    }

    public String getStoredFileName(String filename) {
        if (storeRelativePathes)
            return Env.instance().getFileService().makeFilenameRelative(filename);
        else
            return Env.instance().getFileService().makeFilenameAbsolute(filename);
    }

    public int nextReportNumber() {
        return ++lastReportNumber;
    }

    public int getRepositoryVersion() {

        return repostitoryVersion;
    }
}
