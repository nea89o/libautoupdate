package moe.nea.libautoupdate;

import java.util.concurrent.CompletableFuture;

public interface UpdateSource {
    static UpdateSource gistSource(String owner, String gistId) {
        return new GistSource(owner, gistId);
    }

    CompletableFuture<UpdateData> checkUpdate(String updateStream);
}
