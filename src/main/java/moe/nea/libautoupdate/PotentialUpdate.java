package moe.nea.libautoupdate;

import lombok.Value;
import lombok.val;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Value
public class PotentialUpdate {
    UpdateData update;
    UpdateContext context;
    UUID updateUUID = UUID.randomUUID();

    public File getUpdateDirectory() {
        return new File(".autoupdates", context.getIdentifier() + "/" + updateUUID);
    }


    public boolean isUpdateAvailable() {
        if (update == null) return false;
        return update.getVersionNumber() > context.getCurrentVersion().getCurrentVersionNumber();
    }

    private File getFile(String name) {
        getUpdateDirectory().mkdirs();
        return new File(getUpdateDirectory(), name);
    }

    public File getUpdateJarStorage() {
        return getFile("next.jar");
    }

    public String getFileName() throws MalformedURLException {
        val split = update.getDownloadAsURL().getPath().split("/");
        return split[split.length - 1];
    }


    public void extractUpdater() throws IOException {
        val file = getFile("updater.jar");
        try (val from = getClass().getResourceAsStream("/updater.jar");
             val to = new FileOutputStream(file)) {
            UpdateUtils.connect(from, to);
        }
    }

    public void downloadUpdate() throws IOException {
        try (val from = update.getDownloadAsURL().openStream();
             val to = new FileOutputStream(getUpdateJarStorage())) {
            UpdateUtils.connect(from, to);
        }
        try (val check = new FileInputStream(getUpdateJarStorage())) {
            val updateSha = UpdateUtils.sha256sum(check);
            if (!update.getSha256().equalsIgnoreCase(updateSha)) {
                throw new UpdateException(
                        "Hash of downloaded file " + getUpdateJarStorage() +
                                " (" + updateSha + ") does not match expected hash of " +
                                update.getSha256());
            }
        }
    }

    public void prepareUpdate() throws IOException {
        extractUpdater();
        downloadUpdate();
    }


    public void executeUpdate() throws IOException {
        prepareUpdate();
        ExitHookInvoker.setExitHook(getFile("updater.jar"),
                context.getTarget().generateUpdateActions(this));
    }


    public CompletableFuture<Void> launchUpdate() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                executeUpdate();
            } catch (IOException e) {
                throw new CompletionException(e);
            }
            return null;
        });
    }

}
