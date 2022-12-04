// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.Trace;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public final class Res {
    public static final String version = "2.1";
    private static Locale guiLocale = initGuiLocale();


    public static Locale initGuiLocale() {
        String guiLang = Settings.get("language", "en");
        if ("de".equalsIgnoreCase(guiLang))
            return Locale.GERMAN;
        else
            return Locale.ENGLISH;
    }

    public static Locale getGuiLocale() {
        return guiLocale;
    }

    public static void setGuiLocale(final Locale newGuiLocale) {
        guiLocale = newGuiLocale;
    }

    static Icon missingIcon = javax.swing.plaf.metal.MetalIconFactory.getMenuArrowIcon();
    static HashMap<Locale, ResourceBundle> resourceBundles = new HashMap<Locale, ResourceBundle>();
    public static final Icon aboutImage = gi("qswhellblau3.png");
    public static final Icon addColAfterIcon = gi("column_add_after.png");
    public static final Icon addColBeforeIcon = gi("column_add_before.png");
    public static final Icon addDateIcon = gi("adddate16.png");
    public static final Icon addRowAfterIcon = gi("row_add_after.png");
    public static final Icon addRowBeforeIcon = gi("row_add_before.png");
    public static final Icon barcodeIcon = gi("barcode.png");
    public static final Icon beansIcon = gi("path17.png");
    public static final Icon boldIcon = gi("bold16.gif");
    public static final Icon booleanIcon = gi("boolean12.png");
    public static final Icon borderRightIcon = gi("borderright.png");
    public static final Icon borderBottomIcon = gi("borderbottom.png");
    public static final Icon borderHInsideIcon = gi("borderhinner.png");
    public static final Icon borderLeftIcon = gi("borderleft.png");
    public static final Icon bordersIcon = gi("borders.png");
    public static final Icon borderTopIcon = gi("bordertop.png");
    public static final Icon borderVInsideIcon = gi("bordervinner.png");
    public static final Icon bwarIcon = gi("bwar.png");
    public static final Icon cancelIcon = gi("cancel16.png");
    public static final Icon castIcon = gi("cast12.png");
    public static final Icon changeconnIcon = gi("changeconn.png");
    public static final Icon chartIcon = gi("chart16.png");
    public static final Icon chart3DIcon = gi("chart3d.png");
    public static final Icon chartAreaIcon = gi("chartarea48.png");
    public static final Icon chartBubbleIcon = gi("chartbubble48.png");
    public static final Icon chartCandleIcon = gi("chartcandle48.png");
    public static final Icon chartColumnIcon = gi("chartcolumn48.png");
    public static final Icon chartLineIcon = gi("chartline48.png");
    public static final Icon chartMultiIcon = gi("chartcombi48.png");
    public static final Icon chartPieIcon = gi("chartpie48.png");
    public static final Icon classIcon = gi("class12b.png");
    public static final Icon classEmptyIcon = gi("classempty.png");
    public static final Icon classFullIcon = gi("classfull.png");
    public static final Icon cellNewIcon = gi("cellnew.png");
    public static final Icon colorsIcon = gi("colors.png");
    public static final Icon collIcon = gi("docs12.png");
    public static final Icon connectIcon = gi("connect16.png");
    public static final Icon contextCellIcon = gi("paperclip.png");
    public static final Icon copyIcon = gi("copy16.gif");
    public static final Icon ctrIcon = gi("aligncenter16.gif");
    public static final Icon dashLineIcon = gi("dashline.gif");
    public static final Icon dataConnectionIcon = gi("data_connection.png");
    public static final Icon dataBlocksIcon = gi("datablocks.png");
    public static final Icon dbIcon = gi("db_db_object.png");

    public static final Icon dateIcon = gi("time.png");
    public static final Icon deleteIcon = gi("delcell16.png");
    public static final Icon delColIcon = gi("delcol16.png");
    public static final Icon delReportIcon = gi("delreport16.png");
    public static final Icon delRowIcon = gi("delrow16.png");
    public static final Icon delViewIcon = gi("delview12.png");
    public static final Icon dialogDecorIcon = gi("dialogdecor.png");
    public static final Icon dictIcon = gi("dictionary12.png");
    public static final Icon dotLineIcon = gi("dotline.gif");
    public static final Icon doubleLineIcon = gi("doubleline.gif");
    public static final Icon drillIcon = gi("power-drill.png");
    public static final Icon dupReportIcon = gi("addpage16.png");
    public static final Icon editContentIcon = gi("edit.png");
    public static final Icon editViewIcon = gi("view12.png");
    public static final Icon editReportIcon = gi("editreport16.png");
    public static final Icon edschemaIcon = gi("edschema.png");
    public static final Icon eraseIcon = gi("erase16.png");
    public static final Icon exitIcon = gi("exit.png");
    public static final Icon exportIcon = gi("export24.gif");
    public static final Icon exportPdfIcon = gi("exportpdf.gif");
    public static final Icon extendIcon = gi("selecthigher.gif");
    public static final Icon fieldsTreeIcon = gi("branch.png");
    public static final Icon findIcon = gi("find.png");
    public static final Icon findNextIcon = gi("find_next.png");
    public static final Icon firstPageIcon = gi("first.png");
    public static final Icon filterIcon = gi("funnel_edit.png");
    public static final Icon flattenIcon = gi("flatten16.png");
    public static final Icon folderoutIcon = gi("folder_out.png");
    public static final Icon frameAboveIcon = gi("frameabove.png");
    public static final Icon frameBelowIcon = gi("framebelow.png");
    public static final Icon formulaIcon = gi("function.png");
    public static final Icon formulaShadowedIcon = gi("functionshadow.png");
    public static final Icon functionIcon = gi("function12.png");
    public static final Icon groupIcon = gi("group.png");
    public static final Icon groupCollIcon = gi("groupcoll.png");
    public static final Icon historyIcon = gi("history.png");
    public static final Icon history32Icon = gi("history2.png");
    public static final Icon inheritStyleIcon = gi("inheritstyle.png");
    public static final Icon insideBordersIcon = gi("insideborder.gif");
    public static final Icon inlineBarIcon = gi("inlinebar.png");
    public static final Icon instTableOutlineIcon = gi("tableoutlineborder.png");
    public static final Icon instTableInsideIcon = gi("tableinnerborder.png");
    public static final Icon intIcon = gi("int12.png");
    public static final Icon italicIcon = gi("italic16.gif");
    public static final Icon joinIcon = gi("join_new.png");
    public static final Icon justIcon = gi("alignjustify16.gif");
    public static final Icon lastPageIcon = gi("last.png");
    public static final Icon leftIcon = gi("alignleft16.gif");
    public static final Icon lenIcon = gi("len.png");
    public static final Icon lightbulbIcon = gi("lightbulb_on.png");
    public static final Icon localStyleIcon = gi("localstyle.png");
    public static final Icon mailIcon = gi("mail.png");
    public static final Icon memberIcon = gi("addfield16.png");
    public static final Icon multidocumentIcon = gi("folders.png");
    public static final Icon newBoxIcon = gi("boxnew.png");
    public static final Icon newDirIcon = gi("newdir16.png");
    public static final Icon newReportIcon = gi("reportnew16.png");
    public static final Icon newReportWizIcon = gi("reportwiz16.png");
    public static final Icon newReposIcon = gi("newrepos.png");
    public static final Icon newTableIcon = gi("tablenew16.png");
    public static final Icon newViewIcon = gi("newprojection16.png");
    public static final Icon nextIcon = gi("arrow_right_green.png");
    public static final Icon nextPageIcon = gi("next.png");
    public static final Icon noborderIcon = gi("noborder.gif");
    public static final Icon okIcon = gi("ok16.png");
    public static final Icon openIcon = gi("openfile.gif");
    public static final Icon openRecentIcon = gi("history.png");
    public static final Icon outlineBorderIcon = gi("outlineborder.gif");
    public static final Icon pagenoIcon = gi("pageno.png");
    public static final Icon pagenoxofyIcon = gi("pagexofy.png");
    public static final Icon pictureIcon = gi("picture16.png");
    public static final Icon percentIcon = gi("percent.png");
    public static final Icon pictureDataIcon = gi("image12.png");
    public static final Icon prevIcon = gi("arrow_left_green.png");
    public static final Icon prevPageIcon = gi("prev.png");
    public static final Icon printIcon = gi("print24.gif");
    public static final Icon printDialogIcon = gi("printdialog24.gif");
    public static final Icon projectDirIcon = gi("docs16.png");
    public static final Icon reconnectIcon = gi("reconnect16.png");
    public static final Icon refreshIcon = gi("status16.png");
    public static final Icon refreshNewParameterIcon = gi("change_parameters.png");
    public static final Icon rowNumberIcon = gi("rowcnt.png");
    public static final Icon rtfIcon = gi("rtfexport24.png");
    public static final Icon queryVarIcon = gi("queryvars16.png");
    public static final Icon rightIcon = gi("alignright16.gif");
    public static final Icon runIcon = gi("reportrun16.png");
    public static final Icon saveAsIcon = gi("saveas.png");
    public static final Icon saveIcon = gi("save16.png");
    public static final Icon saveParametersIcon = gi("parametersave.png");
    public static final Icon solidLineIcon = gi("solidline.gif");
    public static final Icon sortDownIcon = gi("sortdown.png");
    public static final Icon sortUpIcon = gi("sortup.png");
    public static final Icon sortingIcon = gi("sort_descending.png");
    public static final Icon stringIcon = gi("string12.png");
    public static final Icon styleBaseIcon = gi("stylebase.png");
    public static final Icon styleDeleteIcon = gi("styledelete.gif");
    public static final Icon styleDeriveIcon = gi("stylederive.png");
    public static final Icon styleEditIcon = gi("styleedit.png");
    public static final Icon styleItalicIcon = gi("styleitalic.png");
    public static final Icon styleTreeIcon = gi("styleleaficon.png");
    public static final Icon styleLockedTreeIcon = gi("styleleaficonlocked.png");
    public static final Icon styleNewIcon = gi("stylenew.gif");
    public static final Icon styledTextIcon = gi("text_rich.png");
    public static final Icon systemDirIcon = gi("objectdatabase16.png");
    public static final Icon subFormIcon = gi("tablenew16.png");
    public static final Icon sumfieldIcon = gi("sumfield.png");
    public static final Icon tableeditIcon = gi("tableedit16.png");
    public static final Icon textIcon = gi("addtext16.png");
    public static final Icon templatesIcon = gi("runreport.png");
    public static final Icon longtextIcon = gi("text.png");
    public static final Icon totalsIcon = gi("totals.png");
    public static final Icon totalsmoreIcon = gi("totalsmore.png");
    public static final Icon unboundCollIcon = gi("docs12grey.png");
    public static final Icon underlineIcon = gi("undrln.gif");
    public static final Icon upIcon = gi("shiftup.gif");
    public static final Icon userIcon = gi("user3.png");
    public static final Icon viewIcon = gi("view12a.png");
    public static final Icon visibiltyIcon = gi("eye.png");
    public static final Icon voidIcon = gi("void12.png");
    public static final Icon xmlIcon = gi("xmlexport16.png");
    public static final ImageIcon frameIcon = gin("rw16.gif");
    public static final ImageIcon missingImageIcon = gin("pdfmiss.png");
    public static final ImageIcon internalFrameIconR = gin("ifricon.png");
    public static final ImageIcon internalFrameIconL = gin("iflicon.png");
    public static final Color lightBlue = new Color(221, 236, 255);
    public static final Color alloy = new Color(0xD6, 0xD3, 0xC6);

    static Icon gi(String name) {
        ImageIcon i = gin(name);
        if (i == null)
            return missingIcon;
        else
            return i;
    }

    static ImageIcon gin(String name) {
        try {
            URL u = Res.class.getResource("/org/pdgen/icons/" + name);
            if (u == null) {
                System.out.println("Icon " + name + " not found.");
                return null;
            }
            return new ImageIcon(u);
        } catch (Exception ex) {
            System.out.println("Icon " + name + " could not be read.");
            return null;
        }
    }

    public static String str(String key, Locale loc) {
        if (loc == null) {
            if (getGuiLocale() == null)
                loc = Locale.getDefault();
            else
                loc = getGuiLocale();
        }
        if (loc == null || key == null) {
            return key;
        }
        ResourceBundle bundle = resourceBundles.get(loc);
        if (bundle == null) {
            try {
                if (loc == Locale.ENGLISH)   // english is in the defaults file force its use
                    bundle = ResourceBundle.getBundle("org.pdgen.icons.textresources", new Locale("", "", ""));
                else
                    bundle = ResourceBundle.getBundle("org.pdgen.icons.textresources", loc);
            } catch (Exception e) {
                Trace.log(e);
                return key;
            }
            if (bundle == null) {
                return key;
            }
            resourceBundles.put(loc, bundle);
        }
        String ret;
        try {
            ret = bundle.getString(key);
        } catch (Exception e) {
            Trace.logError("Missing text resource for: " + key);
            final StackTraceElement[] stackTraceElements = e.getStackTrace();
            for (int i = stackTraceElements.length - 1; i >= 0; i--) {
                StackTraceElement ste = stackTraceElements[i];
                if ("org.pdgen.env.Res".equals(ste.getClassName())) {
                    StackTraceElement call = stackTraceElements[i + 1];
                    Trace.logError("    at " + call.getClassName() + "." + call.getMethodName() + "(" + call.getFileName() + ":" + call.getLineNumber() + ")");
                    break;
                }
            }

            //Trace.log(e);
            return key;
        }
        if (ret != null)
            return ret;
        else
            return key;
    }

    public static String str(String key) {
        return str(key, null);
    }

    /**
     * returns localized key padded with a blank at the end
     *
     * @param key the translation key
     * @return the localized key
     */
    public static String strb(String key) {
        return str(key, null) + " ";
    }

    public static String strb(String key, Locale loc) {
        return str(key, loc) + " ";
    }

    /**
     * returns localized key padded with a blank at front and rear
     *
     * @param key the translation key
     * @return the localized key
     */
    public static String strib(String key) {
        return " " + str(key, null) + " ";
    }

    public static String strib(String key, Locale loc) {
        return " " + str(key, loc) + " ";
    }

    /**
     * returns localized key padded with a blank at front
     *
     * @param key the translation key
     * @return the localized key
     */
    public static String stri(String key) {
        return " " + str(key, null);
    }

    /**
     * returns localized key padded with a blank at front
     *
     * @param key the translation key
     * @return the localized key
     */
    public static String strProduct(String key) {
        return productName() + " " + str(key, null);
    }

    public static String stri(String key, Locale loc) {
        return " " + str(key, loc);
    }

    /**
     * returns localized key padded with a colon at the end
     *
     * @param key the translation key
     * @return the localized key
     */
    public static String strc(String key) {
        return str(key, null) + ":";
    }

    public static String strc(String key, Locale loc) {
        return str(key, loc) + ":";
    }

    /**
     * returns localized key padded with a colon and ablank at the end
     *
     * @param key the translation key
     * @return the localized key
     */
    public static String strcb(String key) {
        return str(key, null) + ": ";
    }

    public static String strcb(String key, Locale loc) {
        return str(key, loc) + ": ";
    }

    /**
     * return the key without translation, Serves as a marker that the string shall be ignored in the localisation editor
     *
     * @param s string
     * @return the string as is is
     */
    public static String asis(String s) {
        return s;
    }

    /**
     * localizes a MessageFormat
     *
     * @param pattern   the pattern
     * @param arguments the arguments
     * @return the localized message
     */
    public static String msg(String pattern, Object... arguments) {
        String tpat = str(pattern);
        String ret = MessageFormat.format(tpat, arguments);
        return ret;
    }

    public static String msg(String pattern, Locale loc, Object... arguments) {
        String tpat = str(pattern, loc);
        String ret = MessageFormat.format(tpat, arguments);
        return ret;
    }

    public static String stricb(String s) {
        return " " + str(s, null) + ": ";
    }

    public static String stricb(String s, Locale loc) {
        return " " + str(s, loc) + ": ";
    }

    public static String strp(final String key, final Object p) {
        String ret = str(key, null) + " " + p;
        return ret;
    }

    public static String strp(final String key, final long p) {
        String ret = str(key, null) + " " + p;
        return ret;
    }

    public static String strp(final String key, Locale loc, final Object p) {
        String ret = str(key, loc) + " " + p;
        return ret;
    }

    public static String productName() {
        return "Pdgen";
    }

    public static String msgWithProduct(final String s) {
        return msg(s, productName());
    }

}

