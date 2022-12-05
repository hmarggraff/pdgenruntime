// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

import org.pdgen.data.*;
import org.pdgen.data.view.ClassProjection;
import org.pdgen.model.style.PredefinedStyles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Untility functions need during repo loading
 */
public class RepoLoader {
    private final ArrayList<WeakReference<JoriaModifiedAccess>> modifiedAccesses = new ArrayList<>();
    private final ArrayList<WeakReference<JoriaUnknownType>> unknownTypes = new ArrayList<>();
    private final Set<ClassProjection> extentOfAllProjections = new HashSet<>();
    private static RepoLoader instance;


    public RepoLoader(String repofile) {
        this(new File(repofile));
    }

    public RepoLoader(File repofile) {
        Trace.log(Trace.init, "RepoLoader " + repofile);
        instance = this;
        //BIn in = new BIn(repofile);
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(repofile));
            Env.schemaInstance = (JoriaSchema) in.readObject();
            Repository repository = (Repository) in.readObject();
            new Env(repository, repofile.getAbsolutePath());
            postLoad();
            repository.checkSchemaAndRepositoryForConsistency(modifiedAccesses, unknownTypes);
            instance = null;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addProjection(ClassProjection classProjection) {
        instance.extentOfAllProjections.add(classProjection);
    }

    public void postLoad() {

        PredefinedStyles.instance().loadCellStyles();
        for (JoriaClass c : Env.schemaInstance.getClasses()) {
            Env.instance().viewsFor(c);
        }
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
}
