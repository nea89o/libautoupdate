package moe.nea.libautoupdate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SanitizeFileNameTest {
	@Test
	void testSpace() {
		Assertions.assertEquals("filename 1.2.0.jar", UpdateUtils.sanitizeFileName("filename 1.2.0.jar"));
	}

	@Test
	void testTraversal() {
		assertDoesNotContain(".", UpdateUtils.sanitizeFileName("."));
		Assertions.assertEquals(".", UpdateUtils.urlDecode(UpdateUtils.sanitizeFileName(".")));

		assertDoesNotContain("..", UpdateUtils.sanitizeFileName(".."));
		Assertions.assertEquals("..", UpdateUtils.urlDecode(UpdateUtils.sanitizeFileName("..")));

		Assertions.assertEquals("...", UpdateUtils.sanitizeFileName("..."));
	}

	static void assertDoesNotContain(String search, String subject) {
		Assertions.assertFalse(subject.contains(search), () -> "'" + subject + "' should not contain '" + search + "' but does.");
	}

	@Test
	void testSlashes() {
		assertDoesNotContain("a/b", UpdateUtils.sanitizeFileName("a/b"));
		Assertions.assertEquals("a/b", UpdateUtils.urlDecode(UpdateUtils.sanitizeFileName("a/b")));

		assertDoesNotContain("a\\b", UpdateUtils.sanitizeFileName("a\\b"));
		Assertions.assertEquals("a\\b", UpdateUtils.urlDecode(UpdateUtils.sanitizeFileName("a\\b")));

		assertDoesNotContain("/", UpdateUtils.sanitizeFileName("/"));
		Assertions.assertEquals("/", UpdateUtils.urlDecode(UpdateUtils.sanitizeFileName("/")));

		assertDoesNotContain("\\", UpdateUtils.sanitizeFileName("\\"));
		Assertions.assertEquals("\\", UpdateUtils.urlDecode(UpdateUtils.sanitizeFileName("\\")));
	}

	@Test
	void testPlus() {
		Assertions.assertEquals("filename+1.2.0.jar", UpdateUtils.sanitizeFileName("filename+1.2.0.jar"));
	}

	@Test
	void testDot() {
		Assertions.assertEquals("filename.jar", UpdateUtils.sanitizeFileName("filename.jar"));
	}
}


