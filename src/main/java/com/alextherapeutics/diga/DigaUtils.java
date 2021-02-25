package com.alextherapeutics.diga;

public class DigaUtils {
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
