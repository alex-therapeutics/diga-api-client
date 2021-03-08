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

/**
 * Utility methods
 */
public final class DigaUtils {
    /**
     * Remove IK from an IK number (IK#######) if it exists.
     * @param IK
     * @return
     */
    public static String ikNumberWithoutPrefix(String IK) {
        return IK.contains("IK") ? IK.replace("IK", "") : IK;
    }

    /**
     * Add IK to an IK number if it doesnt exist
     * @param IK
     * @return
     */
    public static String ikNumberWithPrefix(String IK) {
        if (IK.contains(("IK"))) {
            return IK;
        } else {
            var sb = new StringBuilder("IK");
            sb.append(IK);
            return sb.toString();
        }
    }

    /**
     * Given an endpoint as contanied in the health insurance company mapping file (f.e diga.bitmarck-daten.de),
     * return the full https endpoint to POST codes to (f.e https://diga.bitmarck-daten.de/diga)
     * @param endpoint
     * @return
     */
    public static String buildPostDigaEndpoint(String endpoint) {
        // TODO check if https already exists etc
        var sb = new StringBuilder("https://");
        sb.append(endpoint);
        sb.append("/diga");
        return sb.toString();
    }

    /**
     * Return whether a given DiGA code is a test code or not.
     * @param code
     * @return
     */
    public static boolean isDigaTestCode(String code) {
        return code.startsWith("77");
    }
}
