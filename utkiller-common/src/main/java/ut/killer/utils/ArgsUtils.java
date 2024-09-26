package ut.killer.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 提供处理命令行参数的实用工具方法。
 * <p>
 * 此工具类包含了一些静态方法，用于解析和验证命令行参数，以便应用程序可以更方便地使用这些参数。
 * </p>
 */
public class ArgsUtils {
    /**
     * 将代理参数字符串解析为键值对映射。
     *
     * <p>
     * 如果输入字符串为空或者空白，则直接返回一个空的映射。
     * 输入字符串应包含分号分隔的键值对，每个键值对由等号连接。
     * 无效的键值对（如键或值为空）将被忽略。
     * </p>
     *
     * @param agentArgs 代理参数字符串，格式为 "key1=value1;key2=value2"。
     * @return 包含解析后的键值对的映射。如果输入字符串为空或者所有键值对都无效，则返回一个空映射。
     */
    public static Map<String, String> toMap(final String agentArgs) {
        Map<String, String> agentMap = new LinkedHashMap<>();

        if (isBlankString(agentArgs)) {
            return agentMap;
        }

        String[] keyValue = agentArgs.split(";");

        for (String keyValueStr : keyValue) {
            if (isBlankString(keyValueStr)) {
                continue;
            }
            String[] keyValueArray = keyValueStr.split("=");
            if (keyValueArray.length != 2
                    || isBlankString(keyValueArray[0])
                    || isBlankString(keyValueArray[1])) {
                continue;
            }
            agentMap.put(keyValueArray[0], keyValueArray[1]);
        }

        return agentMap;
    }

    private static boolean isBlankString(String string) {
        return !isNotBlankString(string);
    }

    private static boolean isNotBlankString(String string) {
        return Objects.nonNull(string)
                && !string.isEmpty()
                && !string.matches("^\\s*$");
    }
}