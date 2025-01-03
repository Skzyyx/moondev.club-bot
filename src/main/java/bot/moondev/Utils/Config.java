package bot.moondev.Utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Config {
    private static Config instance;
    private static Map<String, Object> config;

    private Config() {
        // Constructor privado para el singleton
    }

    public static void initialize(InputStream inputStream) {
        if (instance == null) {
            instance = new Config();
            try {
                Yaml yaml = new Yaml();
                instance.config = yaml.load(inputStream);
                if (instance.config == null) {
                    throw new RuntimeException("El archivo de configuración está vacío o es inválido.");
                }
            } catch (Exception e) {
                throw new RuntimeException("Error al cargar el archivo de configuración: " + e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("Config instance ya fue inicializada.");
        }
    }

    private static Object getValue(String path) {
        String[] keys = path.split("\\.");
        Object current = config;

        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(key);
            } else {
                System.err.println("El valor actual no es un mapa: " + current);
                return null;
            }
        }
        return current;
    }

    public static String getString(String path) {
        Object value = getValue(path);
        return value instanceof String ? (String) value : null;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getStringList(String path) {
        Object value = getValue(path);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return new ArrayList<>();
    }
}
