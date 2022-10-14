package moe.nea.libautoupdate;

import java.io.File;
import java.util.List;

public interface UpdateTarget {
    List<UpdateAction> generateUpdateActions(PotentialUpdate update);

    static UpdateTarget replaceJar(Class<?> containedClass) {
        File file = UpdateUtils.getJarFileContainingClass(containedClass);
        return new ReplaceJarUpdateTarget(file);
    }

    static UpdateTarget deleteAndSaveInTheSameFolder(Class<?> containedClass) {
        File file = UpdateUtils.getJarFileContainingClass(containedClass);
        return new DeleteAndSaveInSameFolderUpdateTarget(file);
    }

}
