package com.github.klboke.kkrepo.server.migration;

import com.github.klboke.kkrepo.protocol.cargo.CargoCrateName;
import com.github.klboke.kkrepo.protocol.cargo.CargoVersions;
import com.github.klboke.kkrepo.server.cargo.CargoCrateInspector;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

record MigratedCargoMetadata(
    String name,
    String normalizedName,
    String version,
    String versionKey,
    String description,
    Map<String, Object> publishJson) {

  static MigratedCargoMetadata fromCrate(Path crateFile) {
    CargoCrateInspector.Manifest manifest = CargoCrateInspector.inspect(crateFile);
    return fromPublishJson(manifest.publishJson());
  }

  static MigratedCargoMetadata fromPublishJson(Map<String, Object> json) {
    String name = text(json.get("name"));
    String version = CargoVersions.requireVersion(text(json.get("vers")));
    CargoCrateName crateName = CargoCrateName.parse(name);
    return new MigratedCargoMetadata(
        crateName.value(),
        crateName.lowerDashUnderscoreKey(),
        version,
        CargoVersions.uniquenessKey(version),
        text(json.get("description")),
        new LinkedHashMap<>(json));
  }

  Map<String, Object> componentAttributes(String checksum, String cratePath, boolean yanked) {
    LinkedHashMap<String, Object> attributes = new LinkedHashMap<>();
    attributes.put("crateName", name);
    attributes.put("normalizedName", normalizedName);
    attributes.put("version", version);
    attributes.put("versionKey", versionKey);
    attributes.put("cratePath", cratePath);
    putText(attributes, "description", description);
    putText(attributes, "homepage", publishJson.get("homepage"));
    putText(attributes, "documentation", publishJson.get("documentation"));
    putText(attributes, "repository", publishJson.get("repository"));
    attributes.put("indexEntry", indexEntry(checksum, yanked));
    return attributes;
  }

  Map<String, Object> assetAttributes() {
    LinkedHashMap<String, Object> attributes = new LinkedHashMap<>();
    attributes.put("crateName", name);
    attributes.put("normalizedName", normalizedName);
    attributes.put("version", version);
    attributes.put("versionKey", versionKey);
    putText(attributes, "description", description);
    putText(attributes, "homepage", publishJson.get("homepage"));
    putText(attributes, "documentation", publishJson.get("documentation"));
    putText(attributes, "repository", publishJson.get("repository"));
    return attributes;
  }

  private Map<String, Object> indexEntry(String checksum, boolean yanked) {
    LinkedHashMap<String, Object> entry = new LinkedHashMap<>();
    entry.put("name", name);
    entry.put("vers", version);
    entry.put("deps", indexDependencies(publishJson.get("deps")));
    entry.put("cksum", checksum);
    entry.put("features", mapOrEmpty(publishJson.get("features")));
    entry.put("yanked", yanked);
    entry.put("links", publishJson.containsKey("links") ? publishJson.get("links") : null);
    Object features2 = publishJson.get("features2");
    if (features2 instanceof Map<?, ?>) {
      entry.put("features2", features2);
      entry.put("v", 2);
    } else {
      entry.put("v", 0);
    }
    Object rustVersion = publishJson.get("rust_version");
    if (rustVersion != null) {
      entry.put("rust_version", rustVersion);
    }
    return entry;
  }

  private static List<Object> indexDependencies(Object raw) {
    if (!(raw instanceof List<?> deps)) {
      return List.of();
    }
    List<Object> result = new ArrayList<>(deps.size());
    for (Object item : deps) {
      if (!(item instanceof Map<?, ?> dep)) {
        continue;
      }
      LinkedHashMap<String, Object> mapped = new LinkedHashMap<>();
      String originalName = text(dep.get("name"));
      String explicit = text(dep.get("explicit_name_in_toml"));
      mapped.put("name", explicit == null ? originalName : explicit);
      mapped.put("req", text(dep.get("version_req")));
      mapped.put("features", defaultValue(dep, "features", List.of()));
      mapped.put("optional", defaultValue(dep, "optional", false));
      mapped.put("default_features", defaultValue(dep, "default_features", true));
      mapped.put("target", dep.get("target"));
      mapped.put("kind", defaultValue(dep, "kind", "normal"));
      mapped.put("registry", dep.get("registry"));
      mapped.put("package", explicit == null ? null : originalName);
      result.add(mapped);
    }
    return List.copyOf(result);
  }

  private static Object defaultValue(Map<?, ?> map, String key, Object fallback) {
    Object value = map.get(key);
    return value == null ? fallback : value;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> mapOrEmpty(Object value) {
    return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
  }

  private static void putText(Map<String, Object> target, String key, Object value) {
    String text = text(value);
    if (text != null) {
      target.put(key, text);
    }
  }

  private static String text(Object value) {
    if (value == null) {
      return null;
    }
    String text = String.valueOf(value).trim();
    return text.isBlank() ? null : text;
  }
}
