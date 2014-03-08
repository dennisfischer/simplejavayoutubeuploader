/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload.metadata;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TagParserTest {

	@Test
	public void testAreTagsValid() throws Exception {
		final String invalidInput = ",,this are so amazing tags we he,<dwdwd>,,this are so amazing tags we hes,,,this are so amazing tags we have to test,,,it,, it,i, or not?, this is awesome,,,,,,";
		final List<String> invalidParsed = TagParser.parse(invalidInput);
		assertFalse(TagParser.areTagsValid(invalidParsed));

		final List<String> validParsed = TagParser.parse(invalidInput, true);
		assertTrue(TagParser.areTagsValid(validParsed));
	}

	@Test
	public void testParse() throws Exception {
		final String tags = ",,,<<,,,,,this are so amazing tags we have to test,,,it,, it,i, or not?, this is awesome,,,,,,";

		final List<String> result = TagParser.parse(tags);
		final List<String> expected = Arrays.asList("<<",
													"this are so amazing tags we have to test",
													"it",
													"i",
													"or not?",
													"this is awesome");

		assertEquals(6, result.size());
		assertEquals(expected, result);
	}

	@Test
	public void testParseRemoveInvalid() throws Exception {
		final String tags = ",,this are so amazing tags we he,<dwdwd>,,this are so amazing tags we hes,,,this are so amazing tags we have to test,,,it,, it,i, or not?, this is awesome,,,,,,";

		final List<String> result = TagParser.parse(tags, true);
		final List<String> expected = Arrays.asList("this are so amazing tags we he",
													"it",
													"or not?",
													"this is awesome");

		assertEquals(4, result.size());
		assertEquals(expected, result);
	}

	@Test
	public void testGetLength() throws Exception {
		final List<String> testArrayOne = Arrays.asList("this are so amazing tags we he",
														"it",
														"or not?",
														"this is awesome");
		assertEquals(63, TagParser.getLength(testArrayOne));
	}
}
