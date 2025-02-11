package moe.nea.libautoupdate;

import com.google.gson.Gson;
import lombok.val;
import lombok.var;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
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

	public static InputStream openUrlConnection(URL url) throws IOException {
		val conn = url.openConnection();
		if (connectionPatcher != null)
			connectionPatcher.accept(conn);
		return conn.getInputStream();
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

	/**
	 * Insert a connection patcher, which can modify connections before they are read from.
	 */
	public static void patchConnection(Consumer<URLConnection> connectionPatcher) {
		UpdateUtils.connectionPatcher = connectionPatcher;
	}

	public static <T> CompletableFuture<T> httpGet(String url, Gson gson, Type clazz) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				try (val is = openUrlConnection(new URL(url))) {
					return gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), clazz);
				}
			} catch (IOException e) {
				throw new CompletionException(e);
			}
		});
	}

	public static Iterable<String> stringSplitIterator(String subject, String splitOn) {
		return () -> new Iterator<String>() {
			int endIndex = -splitOn.length();
			int startIndex = -1;

			@Override
			public boolean hasNext() {
				if (startIndex >= 0) return true;
				if (endIndex >= subject.length()) return false;
				var searchIndex = endIndex + splitOn.length();
				if (searchIndex >= subject.length()) return false;
				startIndex = searchIndex;
				int nextNeedle = subject.indexOf(splitOn, searchIndex);
				if (nextNeedle < 0) {
					endIndex = subject.length();
					return true;
				}
				endIndex = nextNeedle;
				return true;
			}

			@Override
			public String next() {
				if (!hasNext()) throw new NoSuchElementException();
				var match = subject.substring(startIndex, endIndex);
				startIndex = -1;
				return match;
			}
		};
	}

	public static String lastStringSegment(String subject, String needle) {
		int i = subject.lastIndexOf(needle);
		if (i < 0) return null;
		return subject.substring(i + needle.length());
	}

	public static String urlEncode(String part) {
		try {
			return URLEncoder.encode(part, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("StandardCharsets.UTF_8 should always be available", e);
		}
	}

	/**
	 * Round-trip a string, url decoding and re-encoding it, so that url encoded characters
	 *
	 * @param urlEncoded an url encoded string with potentially some un-encoded sequences contained
	 * @return an url encoded string with all characters url encoded
	 */
	public static String urlRoundtrip(String urlEncoded) {
		return urlEncode(urlDecode(urlEncoded));
	}

	/**
	 * Create a safe file name for a string. If needed, some characters will be replaced with their url encoded counterparts, as to preserve all information.
	 *
	 * @param fileName a file name
	 * @return a safe name for a file
	 */
	public static String sanitizeFileName(String fileName) {
		if (fileName == null) return "null";
		if (fileName.isEmpty()) return "empty";
		if (fileName.equals("..")) {
			return "%2E%2E";
		}
		if (fileName.equals(".")) {
			return "%2E";
		}
		fileName = fileName.replace("/", "%2F");
		fileName = fileName.replace("\\", "%5C");
		// If even a fully url encoded file name throws here, propagate that error. This can't be saved anymore.
		// I specifically chose not to mangle the name anymore than this.
		var path = Paths.get(fileName);
		if (path.getNameCount() != 1)
			throw new RuntimeException("Could not sanitize file name " + fileName + " into a single name");
		return path.getFileName().toString();
	}

	/**
	 * Create a {@link File} instance in a directory, and check for directory traversal. This method does not sanitize the file name.
	 *
	 * @param directory the directory containing the file
	 * @param filename  the name of the file
	 * @return a file reference for the file in the directory.
	 */
	public static File getContainedFile(File directory, String filename) throws IOException {
		var file = new File(directory, filename).getCanonicalFile();
		if (!directory.getCanonicalFile().equals(file.getParentFile())) {
			throw new IllegalArgumentException("Tried to create a file named '" + filename + "' in directory '" + directory + "', but leaked out to '" + file + "'.");
		}
		return file;
	}

	public static String urlDecode(String part) {
		try {
			return URLDecoder.decode(part, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("StandardCharsets.UTF_8 should always be available", e);
		}
	}
}

