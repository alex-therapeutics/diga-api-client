/*
 * Copyright 2021-2021 Alex Therapeutics AB and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package com.alextherapeutics.diga;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DigaUtilsTest {

  @Test
  void testIkNumberWithoutPrefixContainingIk() {
    var testIK = "IK123456789";
    var expected = "123456789";
    assertEquals(expected, DigaUtils.ikNumberWithoutPrefix(testIK));
  }

  @Test
  void testIkNumberWithoutPrefixNotContainingIk() {
    var testIK = "123456789";
    var expected = "123456789";
    assertEquals(expected, DigaUtils.ikNumberWithoutPrefix(testIK));
  }

  @Test
  void testIkNumberWithPrefixContainingIk() {
    var testIK = "IK123456789";
    var expected = "IK123456789";
    assertEquals(expected, DigaUtils.ikNumberWithPrefix(testIK));
  }

  @Test
  void testIkNumberWithPrefixNotContainingIk() {
    var testIK = "123456789";
    var expected = "IK123456789";
    assertEquals(expected, DigaUtils.ikNumberWithPrefix(testIK));
  }

  @Test
  void testIsDigaTestCode() {
    assertTrue(DigaUtils.isDigaTestCode("77AAAAAAAAAAADEV"));
    assertFalse(DigaUtils.isDigaTestCode("BHAAAAAAAAAAADEV"));
  }

  @Test
  void testBuildPostDigaEndpoint() {
    var url = "diga.bitmarck-daten.de";
    var expected = "https://diga.bitmarck-daten.de/diga";
    assertEquals(expected, DigaUtils.buildPostDigaEndpoint(url));
  }
}
