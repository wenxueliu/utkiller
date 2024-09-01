package com.ut.killer.utils;

import com.ut.killer.config.UTKillerConfiguration;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;

public class YamlUtils {
    public static UTKillerConfiguration parse(String filePath) {
        try (InputStream inputStream = YamlUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("YAML file not found.");
            }
            Yaml yaml = new Yaml();
            UTKillerConfiguration utKillerConfiguration = yaml.loadAs(inputStream, UTKillerConfiguration.class);
            System.out.println(utKillerConfiguration);
            return utKillerConfiguration;
        } catch (Exception e) {
            throw new IllegalStateException("YAML file parse error");
        }
    }
}