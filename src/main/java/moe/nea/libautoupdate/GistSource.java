package moe.nea.libautoupdate;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.concurrent.CompletableFuture;

@Value
@EqualsAndHashCode(callSuper = false)
public class GistSource extends JsonUpdateSource {
    String owner;
    String gistId;
    private static final String GIST_RAW_URL = "https://gist.githubusercontent.com/%s/%s/raw/%s.json";

    @Override
    public CompletableFuture<UpdateData> checkUpdate(String updateStream) {
        return getJsonFromURL(String.format(GIST_RAW_URL, owner, gistId, updateStream), UpdateData.class);
    }
}
