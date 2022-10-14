package moe.nea.libautoupdate;


import lombok.NonNull;
import lombok.Value;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Value
public class UpdateContext {
    @NonNull UpdateSource source;
    @NonNull UpdateTarget target;
    @NonNull CurrentVersion currentVersion;
    @NonNull String identifier;


    public void cleanup() {
        File file = new File(".autoupdates", identifier).getAbsoluteFile();
        try {
            UpdateUtils.deleteDirectory(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<PotentialUpdate> checkUpdate(String updateStream) {
        return source.checkUpdate(updateStream)
                .thenApply(it -> new PotentialUpdate(it, this));
    }
}
