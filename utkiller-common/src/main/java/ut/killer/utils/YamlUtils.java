package ut.killer.utils;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import ut.killer.config.UTKillerConfiguration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 提供用于处理 YAML 文件的工具方法。
 * <p>
 * 这个工具类包含用于从 YAML 文件中加载配置的方法。
 * </p>
 */
public class YamlUtils {
    /**
     * 从指定的文件路径中解析 YAML 文件，并将其转换为 UTKillerConfiguration 对象。
     *
     * @param filePath 文件路径，该路径指向一个存储了配置信息的 YAML 文件。
     * @return 返回一个根据 YAML 文件内容构建的 UTKillerConfiguration 对象。
     * @throws IllegalStateException 如果 YAML 文件无法解析，则抛出此异常。
     */
    public static UTKillerConfiguration parse(String filePath) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            Yaml yaml = new Yaml(new Constructor(UTKillerConfiguration.class));
            return yaml.load(inputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException("YAML file parse error");
        }
    }
}