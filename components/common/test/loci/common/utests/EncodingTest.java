//
// EncodingTest.java
//

/*
LOCI Common package: utilities for I/O, reflection and miscellaneous tasks.
Copyright (C) 2005-@year@ Melissa Linkert and Curtis Rueden.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package loci.common.utests;

import static org.testng.AssertJUnit.assertTrue;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import loci.common.Constants;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for string encoding.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/common/test/loci/common/utests/EncodingTest.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/common/test/loci/common/utests/EncodingTest.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 */
public class EncodingTest {

  // -- Encodings --
  private static final String MAC = getMacEncoding();
  private static final String WINDOWS = "Windows-1252";

  // -- Test strings --
  private static final String ENGLISH_ALPHABET =
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String UMLAUTS = "äëüöïÄËÖÜÏ";
  private static final String ESZETT = "ß";
  private static final String ASSORTED_LETTERS = "åÅñÑçÇøØ";
  private static final String ACUTE_ACCENTS = "áéúíóÁÉÍÓÚ";
  private static final String SUPERSCRIPTS = "¹²³";
  private static final String MATH = "+=-×÷";
  private static final String FRACTIONS = "¼½¾";
  private static final String CURRENCY = "€$¥£¢";
  private static final String PUNCTUATION = ",.?':;\"¶¿“”\\!˝¸-˘‘’`ˇ§";
  private static final String SQUARE_BRACKETS = "[]";
  private static final String CURLY_BRACKETS = "{}";
  private static final String ANGLE_BRACKETS = "<>";
  private static final String ROUND_BRACKETS = "()";
  private static final String DOUBLE_ANGLE_BRACKETS = "«»";

  static String getMacEncoding() {
    if (Charset.isSupported("MacRoman")) {
      return "MacRoman";
    }
    if (Charset.isSupported("x-MacRoman")) {
      return "x-MacRoman";
    }
    return "macintosh";
  }

  // -- Tests --

  @Test
  public void testAlphabetMac() {
    try {
      assertTrue(ENGLISH_ALPHABET.equals(
        new String(ENGLISH_ALPHABET.getBytes(MAC), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testAlphabetWindows() {
    try {
      assertTrue(ENGLISH_ALPHABET.equals(
        new String(ENGLISH_ALPHABET.getBytes(WINDOWS), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testUmlautsMac() {
    try {
      assertTrue(UMLAUTS.equals(
        new String(UMLAUTS.getBytes(MAC), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testUmlautsWindows() {
    try {
      assertTrue(UMLAUTS.equals(
        new String(UMLAUTS.getBytes(WINDOWS), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testEszettLettersMac() {
    try {
      assertTrue(ESZETT.equals(
        new String(ESZETT.getBytes(MAC), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testEszettLettersWindows() {
    try {
      assertTrue(ESZETT.equals(
        new String(ESZETT.getBytes(WINDOWS), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testAssortedLettersMac() {
    try {
      assertTrue(ASSORTED_LETTERS.equals(
        new String(ASSORTED_LETTERS.getBytes(MAC), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testAssortedLettersWindows() {
    try {
      assertTrue(ASSORTED_LETTERS.equals(
        new String(ASSORTED_LETTERS.getBytes(WINDOWS), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testAcuteAccentsMac() {
    try {
      assertTrue(ACUTE_ACCENTS.equals(
        new String(ACUTE_ACCENTS.getBytes(MAC), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testAcuteAccentsWindows() {
    try {
      assertTrue(ACUTE_ACCENTS.equals(
        new String(ACUTE_ACCENTS.getBytes(WINDOWS), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testSuperscriptsMac() {
    try {
      assertTrue(SUPERSCRIPTS.equals(
        new String(SUPERSCRIPTS.getBytes(MAC), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testSuperscriptsWindows() {
    try {
      assertTrue(SUPERSCRIPTS.equals(
        new String(SUPERSCRIPTS.getBytes(WINDOWS), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testMathMac() {
    try {
      assertTrue(MATH.equals(
        new String(MATH.getBytes(MAC), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testMathWindows() {
    try {
      assertTrue(MATH.equals(
        new String(MATH.getBytes(WINDOWS), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testFractionsMac() {
    try {
      assertTrue(FRACTIONS.equals(
        new String(FRACTIONS.getBytes(MAC), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testFractionsWindows() {
    try {
      assertTrue(FRACTIONS.equals(
        new String(FRACTIONS.getBytes(WINDOWS), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testCurrencyMac() {
    try {
      assertTrue(CURRENCY.equals(
        new String(CURRENCY.getBytes(MAC), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testCurrencyWindows() {
    try {
      assertTrue(CURRENCY.equals(
        new String(CURRENCY.getBytes(WINDOWS), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testPunctuationMac() {
    try {
      assertTrue(PUNCTUATION.equals(
        new String(PUNCTUATION.getBytes(MAC), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testPunctuationWindows() {
    try {
      assertTrue(PUNCTUATION.equals(
        new String(PUNCTUATION.getBytes(WINDOWS), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testSquareBracketsMac() {
    try {
      assertTrue(SQUARE_BRACKETS.equals(
        new String(SQUARE_BRACKETS.getBytes(MAC), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testSquareBracketsWindows() {
    try {
      assertTrue(SQUARE_BRACKETS.equals(
        new String(SQUARE_BRACKETS.getBytes(WINDOWS), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testCurlyBracketsMac() {
    try {
      assertTrue(CURLY_BRACKETS.equals(
        new String(CURLY_BRACKETS.getBytes(MAC), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testCurlyBracketsWindows() {
    try {
      assertTrue(CURLY_BRACKETS.equals(
        new String(CURLY_BRACKETS.getBytes(WINDOWS), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testAngleBracketsMac() {
    try {
      assertTrue(ANGLE_BRACKETS.equals(
        new String(ANGLE_BRACKETS.getBytes(MAC), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testAngleBracketsWindows() {
    try {
      assertTrue(ANGLE_BRACKETS.equals(
        new String(ANGLE_BRACKETS.getBytes(WINDOWS), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testDoubleAngleBracketsMac() {
    try {
      assertTrue(DOUBLE_ANGLE_BRACKETS.equals(
        new String(DOUBLE_ANGLE_BRACKETS.getBytes(MAC), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testDoubleAngleBracketsWindows() {
    try {
      assertTrue(DOUBLE_ANGLE_BRACKETS.equals(new String(
        DOUBLE_ANGLE_BRACKETS.getBytes(WINDOWS), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testRoundBracketsMac() {
    try {
      assertTrue(ROUND_BRACKETS.equals(
        new String(ROUND_BRACKETS.getBytes(MAC), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

  @Test
  public void testRoundBracketsWindows() {
    try {
      assertTrue(ROUND_BRACKETS.equals(
        new String(ROUND_BRACKETS.getBytes(WINDOWS), Constants.ENCODING)));
    }
    catch (UnsupportedEncodingException e) { }
  }

}
