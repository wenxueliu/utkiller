package ut.killer;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class YamlUtils {
    public static UTKillerConfiguration parse(String filePath) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            Yaml yaml = new Yaml(new Constructor(UTKillerConfiguration.class));
            return yaml.load(inputStream);
        } catch (Exception ex) {
            throw new IllegalStateException("YAML file parse error");
        }
    }
}