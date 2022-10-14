package moe.nea.libautoupdate;

public class Main {
    public static void main(String[] args) {

        UpdateContext updater = new UpdateContext(
                UpdateSource.gistSource("romangraef", "9b62fe32bc41c09d2d7e2d3153f14ee8"),
                UpdateTarget.deleteAndSaveInTheSameFolder(Main.class),
                CurrentVersion.of(10000),
                "test"
        );
        updater.cleanup();
        System.out.println("Update cleaned");
        System.out.println("Created update context: " + updater);
        updater.checkUpdate("stable").thenCompose(it -> {
            System.out.println("Checked for update on stable: " + it);
            System.out.println("Can update: " + it.isUpdateAvailable());
            System.out.println("Executing update.");
            return it.launchUpdate();
        }).join();
    }
}
