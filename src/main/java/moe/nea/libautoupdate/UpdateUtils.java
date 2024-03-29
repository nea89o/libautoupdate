package moe.nea.libautoupdate;

import com.google.gson.Gson;
import lombok.val;
import lombok.var;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public class UpdateUtils {
    private UpdateUtils() {
    }

    static final Gson gson = new Gson();

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

    private static Consumer<URLConnection> connectionPatcher = null;

    public static void patchConnection(Consumer<URLConnection> connectionPatcher) {
        UpdateUtils.connectionPatcher = connectionPatcher;
    }

    public static <T> CompletableFuture<T> httpGet(String url, Gson gson, Type clazz) {
        return CompletableFuture.supplyAsync(() -> {
            URLConnection conn = null;
            try {
                conn = new URL(url).openConnection();
                if (connectionPatcher != null)
                    connectionPatcher.accept(conn);
                try (val is = conn.getInputStream()) {
                    return gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), clazz);
                }
            } catch (IOException e) {
                if (conn instanceof HttpURLConnection) {
                    ((HttpURLConnection) conn).disconnect();
                }
                throw new CompletionException(e);
            }
        });
    }

}

