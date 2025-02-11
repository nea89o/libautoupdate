package moe.nea.libautoupdate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class StringSplitIteratorTest {
	@Test
	void testOverlappingNeedles() {
		Assertions.assertIterableEquals(
			Arrays.asList("abc", "1def"),
			UpdateUtils.stringSplitIterator("abc111def", "11")
		);
	}
	@Test
	void testNoNeedle() {
		Assertions.assertIterableEquals(
			Arrays.asList("abc1def"),
			UpdateUtils.stringSplitIterator("abc1def", "11")
		);
	}
}
