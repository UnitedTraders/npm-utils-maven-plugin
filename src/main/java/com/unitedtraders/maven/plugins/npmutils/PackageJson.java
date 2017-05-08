package com.unitedtraders.maven.plugins.npmutils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Parsed package.json model
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageJson {
    @JsonProperty("version")
    public String version;

    @JsonProperty("dependencies")
    public Map<String, String> dependencies;

    @JsonProperty("devDependencies")
    public Map<String, String> devDependencies;
}
