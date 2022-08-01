package grakkit;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

public class Config {
   /** A list of possible config types. */
   public enum Format { JSON, YAML }

   /** The "main" property within the config. */
   public String main;

   /** Creates a new configuration from the given options. If `member` is true, the sub-property "grakkit" will be used as the config root. */
   public Config (Format format, String root, String path, Boolean member) {
      Path info = Paths.get(root, path);
      if (info.toFile().exists()) {
         try {
            StringBuilder builder = new StringBuilder();
            Files.lines(info).forEach(line -> builder.append(line).append("\n"));
            String content = builder.toString();
            if (format == Format.JSON) {
               JsonObject object = Json.parse(content).asObject();
               if (member) {
                  object = object.get("grakkit").asObject();
               }
               this.main = object.getString("main", null);
            } else if (format == Format.YAML) {
               ConfigurationSection object = YamlFile.loadConfiguration(info.toFile()).getRoot();
               if (member) {
                  object = object.getConfigurationSection("grakkit");
               }
               this.main = object.getString("main");
            }
         } catch (Throwable error) {
            // do nothing
         }
      }
   }
}
