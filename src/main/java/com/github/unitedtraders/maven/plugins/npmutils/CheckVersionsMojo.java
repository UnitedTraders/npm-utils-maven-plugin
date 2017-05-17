package com.github.unitedtraders.maven.plugins.npmutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mojo to check version in package.json matches pom.xml version
 */
@Mojo(name = "check-versions", defaultPhase = LifecyclePhase.VALIDATE)
public final class CheckVersionsMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(property = "workingDirectory", defaultValue = "${basedir}")
    private String workingDirectory;

    @Parameter(property = "exact", defaultValue = "false")
    private boolean exact;

    /**
     * Update version in package.json to match pom.xml
     */
    @Parameter(property = "fix", defaultValue = "false")
    private boolean fix;

    public void execute() throws MojoExecutionException, MojoFailureException {
        File packageJson = new File(workingDirectory, "package.json");
        if (packageJson.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();

                PackageJson parsed = mapper.readValue(packageJson, PackageJson.class);

                if (parsed.version == null) {
                    throw new MojoExecutionException("No version in package.json");
                }

                if (exact) {
                    if (!parsed.version.equals(project.getVersion())) {
                        if (fix) {
                            fixVersion(packageJson);
                        } else {
                            throw new MojoExecutionException(String.format("Project version %s does not match package.json version %s",
                                    project.getVersion(), parsed.version));
                        }
                    }
                } else {
                    if (!parsed.version.startsWith(project.getVersion())) {
                        if (fix) {
                            fixVersion(packageJson);
                        } else {
                            throw new MojoExecutionException(String.format("package.json version %s does not start with %s from pom.xml",
                                    parsed.version, project.getVersion()));
                        }
                    }
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to parse package.json", e);
            }
        } else {
            getLog().warn("No package.json found, check versions ignored");
        }
    }

    private void fixVersion(File packageJson) throws IOException {
        Pattern pattern = Pattern.compile("(\\w*)\"version\": \"(.+)\"");

        // new file to override
        File updated = File.createTempFile("package", "json");

        // parse and substitute
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(updated))) {
            try (BufferedReader br = new BufferedReader(new FileReader(packageJson))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String versionFound = matcher.group(2);
                        line = matcher.replaceAll("$1\"version\": \"" + project.getVersion() + "\"");
                        getLog().warn(String.format("Replaced version %s with %s", versionFound, project.getVersion()));
                    }
                    writer.write(line);
                    writer.newLine();
                }
            }
        }

        // move new file in place of the old one
        FileUtils.rename(updated, packageJson);
    }
}
