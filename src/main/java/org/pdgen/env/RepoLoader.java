// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

import org.pdgen.data.*;
import org.pdgen.data.view.ClassProjection;
import org.pdgen.datasources.java.JavaSchema;
import org.pdgen.model.style.PredefinedStyles;
import org.pdgen.util.Log;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility functions needed during repo loading
 */
public class RepoLoader {
    private final ArrayList<WeakReference<JoriaModifiedAccess>> modifiedAccesses = new ArrayList<>();
    private final ArrayList<WeakReference<JoriaUnknownType>> unknownTypes = new ArrayList<>();
    private final Set<ClassProjection> extentOfAllProjections = new HashSet<>();

    public RepoLoader(String repofile, boolean forDesigner) {
        this(new File(repofile), forDesigner);
    }

    public RepoLoader(File repofile, boolean forDesigner) {
        Trace.log(Trace.init, "RepoLoader " + repofile);
        String pathname = repofile.getAbsolutePath();
        try {
            FileInputStream fis = new FileInputStream(repofile);
            doLoad(pathname, fis, forDesigner);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RepoLoader(String pathname, InputStream in, boolean forDesigner){
        Log.ini.info("RepoLoader " + pathname);

        doLoad(pathname,in, forDesigner);
    }

    private void doLoad(String pathname, InputStream ins, boolean forDesigner) {
        try {
            ObjectInputStream oin = new ObjectInputStream(ins);
            Object saved = oin.readObject();
            if (saved instanceof JavaSchema) { // Backwards compatibility: phase out after 2022
                Env.schemaInstance = (JavaSchema) saved;
            } else {
                SavedSchema fromFile = (SavedSchema) saved;

                Env.schemaInstance = fromFile.buildSchema(forDesigner);
            }
            Repository repository = (Repository) oin.readObject();
            new Env(repository, pathname);
            postLoad();
            repository.checkSchemaAndRepositoryForConsistency(modifiedAccesses, unknownTypes);
        } catch (Exception e) {
            Log.ini.error(e, "Loading Templates: ");
            throw new RuntimeException(e);
        }
    }

    public void postLoad() {

        PredefinedStyles.instance().loadCellStyles();
    }

/*
    public static void addProjection(ClassProjection classProjection) {
        instance.extentOfAllProjections.add(classProjection);
    }

    public static void addModifiedAccess(JoriaModifiedAccess axs) {
        instance.modifiedAccesses.add(new WeakReference<JoriaModifiedAccess>(axs));
    }

    public static void addUnknownType(JoriaUnknownType t) {
        instance.unknownTypes.add(new WeakReference<JoriaUnknownType>(t));
    }


    public static List<String> getSchemaChangeExplanations() {
        ArrayList<String> ret = new ArrayList<>();
        for (WeakReference<JoriaModifiedAccess> aTheJoriaModifiedAccessExtent : instance.modifiedAccesses) {
            JoriaModifiedAccess jma = aTheJoriaModifiedAccessExtent.get();
            if (jma != null) {
                ret.add(jma.explainModification());
            }
        }
        return ret;
    }

 */
}
