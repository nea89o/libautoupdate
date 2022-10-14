package moe.nea.libautoupdate;

import com.google.gson.Gson;
import lombok.val;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public abstract class JsonUpdateSource implements UpdateSource {
    private static final Gson gson = new Gson();

    protected Gson getGson() {
        return gson;
    }

    protected <T> CompletableFuture<T> getJsonFromURL(String url, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                try (val is = new URL(url).openStream()) {
                    return getGson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), clazz);
                }
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }
}
