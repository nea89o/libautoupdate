package moe.nea.libautoupdate.postexit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class PostExitMain {
    public static void main(String[] args) throws IOException {
        File outputFile = new File(".autoupdates", "postexit.log");
        outputFile.getParentFile().mkdirs();
        PrintStream printStream = new PrintStream(new FileOutputStream(outputFile, true));
        System.setErr(printStream);
        System.setOut(printStream);

        for (int i = 0; i < args.length; i++) {
            switch (args[i].intern()) {
                case "delete":
                    File file = new File(args[++i]);
                    System.out.println("Deleting " + file);
                    file.delete();
                    break;
                case "move":
                    File from = new File(args[++i]);
                    File to = new File(args[++i]);
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

    public void unlockedFile(File file) throws InterruptedException {
        while (!file.exists() || !file.renameTo(file)) {
            Thread.sleep(1000L);
        }
        file.getParentFile().mkdirs();
    }

}
