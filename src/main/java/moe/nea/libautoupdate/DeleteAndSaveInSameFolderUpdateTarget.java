package moe.nea.libautoupdate;

import lombok.SneakyThrows;
import lombok.Value;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Value
public class DeleteAndSaveInSameFolderUpdateTarget implements UpdateTarget {
    File file;

    @SneakyThrows
    @Override
    public List<UpdateAction> generateUpdateActions(PotentialUpdate update) {
        return Arrays.asList(
                new UpdateAction.DeleteFile(file),
                new UpdateAction.MoveDownloadedFile(update.getUpdateJarStorage(), new File(file.getParentFile(), update.getFileName()))
        );
    }
}
