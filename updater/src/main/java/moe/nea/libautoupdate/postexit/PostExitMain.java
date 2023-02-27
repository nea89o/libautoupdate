package moe.nea.libautoupdate.postexit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class PostExitMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        File outputFile = new File(".autoupdates", "postexit.log");
        outputFile.getParentFile().mkdirs();
        PrintStream printStream = new PrintStream(new FileOutputStream(outputFile, true));
        System.setErr(printStream);
        System.setOut(printStream);
        System.out.println("Starting update (with identifier " + args[0] + " and uuid " + args[1] + ")");
        for (int i = 2; i < args.length; i++) {
            switch (args[i].intern()) {
                case "delete":
                    File file = unlockedFile(args[++i]);
                    System.out.println("Deleting " + file);
                    if (!file.delete()) {
                        System.out.println("Failed to delete " + file);
                    }
                    break;
                case "move":
                    File from = unlockedFile(args[++i]);
                    File to = unlockedFile(args[++i]);
                    System.out.println("Moving " + from + " to " + to);
                    // Use Files.move instead of File.renameTo, since renameTo is not well-defined.
                    Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    break;
                default:
                    System.out.println("Unknown instruction " + args[i]);
                    System.exit(1);
            }
        }
    }

    public static File unlockedFile(String name) throws InterruptedException {
        File file = new File(name);
        while (file.exists() && !file.renameTo(file)) {
            System.out.println("Waiting on a process to relinquish access to " + file);
            Thread.sleep(1000L);
        }
        file.getParentFile().mkdirs();
        return file;
    }

}
