package moe.nea.libautoupdate;

public class Main {
    public static void main(String[] args) {

        UpdateContext updater = new UpdateContext(
                UpdateSource.githubUpdateSource("NotEnoughUpdates", "NotEnoughUpdates"),
                UpdateTarget.deleteAndSaveInTheSameFolder(Main.class),
                CurrentVersion.ofTag("v2.1.1-pre1"),
                "test"
        );
        updater.cleanup();
        System.out.println("Update cleaned");
        System.out.println("Created update context: " + updater);
        String stream = "pre";
        updater.checkUpdate(stream).thenCompose(it -> {
            System.out.println("Checked for update on " + stream + ": " + it);
            System.out.println("Can update: " + it.isUpdateAvailable());
            System.out.println("Executing update.");
            return it.launchUpdate();
        }).join();
    }
}
