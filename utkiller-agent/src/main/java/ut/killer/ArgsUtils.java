package ut.killer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ArgsUtils {
    public static Map<String, String> toMap(final String agentArgs) {
        final Map<String, String> featureMap = new LinkedHashMap<>();

        if (isBlankString(agentArgs)) {
            return featureMap;
        }

        final String[] keyValue = agentArgs.split(";");

        for (String kvPairSegmentString : keyValue) {
            if (isBlankString(kvPairSegmentString)) {
                continue;
            }
            final String[] kvSegmentArray = kvPairSegmentString.split("=");
            if (kvSegmentArray.length != 2
                    || isBlankString(kvSegmentArray[0])
                    || isBlankString(kvSegmentArray[1])) {
                continue;
            }
            featureMap.put(kvSegmentArray[0], kvSegmentArray[1]);
        }

        return featureMap;
    }

    private static boolean isBlankString(final String string) {
        return !isNotBlankString(string);
    }

    private static boolean isNotBlankString(final String string) {
        return Objects.nonNull(string)
                && !string.isEmpty()
                && !string.matches("^\\s*$");
    }
}
