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

import de.chaosfisch.google.youtube.upload.metadata.TagParser;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TagParserTest {

	@Test
	public void testAreTagsValid() throws Exception {
		final String invalidInput = ",,this are so amazing tags we he,<dwdwd>,,this are so amazing tags we hes,,,this are so amazing tags we have to test,,," +
				"it,, it,i, or not?, this is awesome,,,,,,";
		final List<String> invalidParsed = TagParser.parse(invalidInput);
		assertFalse(TagParser.areTagsValid(invalidParsed));

		final List<String> validParsed = TagParser.parse(invalidInput, true);
		assertTrue(TagParser.areTagsValid(validParsed));

		final String inputTooLong = "hello,we,have,here,very,nice,tags,that,we,want,to,test,they,are,far,too,long,but this is the only,way to test this shit," +
				"or do you know,any other way,at least it will work," +
				"as long as,the input,is correct,but no one,can,tell.,let's have a,look at it,500 tags should be easy,to reach,in short time," +
				"we will know more,do we?,this should be,possible,easy,really,I don't lie,never," +
				"but as you wish,I'll do that for you,C'mon,500 is hard,nearly got it,just some more";

		assertTrue(TagParser.areTagsValid(TagParser.parse(inputTooLong, false)));
		assertFalse(TagParser.areTagsValid(TagParser.parse(inputTooLong + "s", false)));
	}

	@Test
	public void testParse() throws Exception {
		final String tags = ",,,<<,,,,,this are so amazing tags we have to test,,,it,, it,i, or not?, this is awesome,,,,,,";

		final List<String> result = TagParser.parse(tags);
		final List<String> expected = Arrays.asList("<<", "this are so amazing tags we have to test", "it", "i", "or not?", "this is awesome");

		assertEquals(6, result.size());
		assertEquals(expected, result);
	}

	@Test
	public void testParseRemoveInvalid() throws Exception {
		final String tags = ",,this are so amazing tags we he,<dwdwd>,,this are so amazing tags we hes,,,this are so amazing tags we have to test,,,it,, it,i," +
				" or not?, this is awesome,ä,,,,,";

		final List<String> result = TagParser.parse(tags, true);
		final List<String> expected = Arrays.asList("this are so amazing tags we he", "it", "or not?", "this is awesome", "ä");

		assertEquals(5, result.size());
		assertEquals(expected, result);
	}

	@Test
	public void testGetLength() throws Exception {
		final List<String> testArrayOne = Arrays.asList("this are so amazing tags we he", "it", "or not?", "this is awesomeä", "ä");
		assertEquals(68, TagParser.getLength(testArrayOne));
	}
}
