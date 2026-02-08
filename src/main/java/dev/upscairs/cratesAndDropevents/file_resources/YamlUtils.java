package dev.upscairs.cratesAndDropevents.file_resources;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public final class YamlUtils {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> loadYamlAsMap(File file) throws IOException {
        if (file == null || !file.exists()) return Collections.emptyMap();
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
        try (FileReader fr = new FileReader(file)) {
            Object data = yaml.load(fr);
            if (data instanceof Map) {
                return (Map<String, Object>) data;
            } else {
                return Collections.emptyMap();
            }
        }
    }
}