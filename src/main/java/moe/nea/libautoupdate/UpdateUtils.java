package moe.nea.libautoupdate;

import lombok.val;
import lombok.var;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class UpdateUtils {
    private UpdateUtils() {
    }

    public static File getJarFileContainingClass(Class<?> clazz) {
        val location = clazz.getProtectionDomain().getCodeSource().getLocation();
        if (location == null)
            return null;
        var path = location.toString();
        path = path.split("!", 2)[0];
        if (path.startsWith("jar:")) {
            path = path.substring(4);
        }
        try {
            return new File(new URI(path));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void connect(InputStream from, OutputStream to) throws IOException {
        val buf = new byte[4096];
        int r;
        while ((r = from.read(buf)) != -1) {
            to.write(buf, 0, r);
        }
    }

    public static String sha256sum(InputStream stream) throws IOException {
        try {
            val digest = MessageDigest.getInstance("SHA-256");
            int r;
            val buf = new byte[4096];
            while ((r = stream.read(buf)) != -1) {
                digest.update(buf, 0, r);
            }
            return String.format("%64s",
                            new BigInteger(1, digest.digest())
                                    .toString(16))
                    .replace(' ', '0')
                    .toLowerCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}

