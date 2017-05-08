package com.unitedtraders.maven.plugins.npmutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Mojo to validate does package.json refer SNAPSHOT versions when project version is release
 */
@Mojo(name = "check-snapshots", defaultPhase = LifecyclePhase.VALIDATE)
public final class CheckSnapshotsMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(property = "workingDirectory", defaultValue = "${basedir}")
    private String workingDirectory;

    /**
     * Do plugin check devDependencies
     */
    @Parameter(property = "checkDev", defaultValue = "false")
    private boolean checkDev;

    @Parameter(property = "stopVersions")
    private List<String> stopVersions;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // check stopVersions were overridden
        if (stopVersions == null || stopVersions.isEmpty()) {
            stopVersions = Arrays.asList("snapshot", "alpha", "beta", "rc");
        }

        // do work
        File packageJson = new File(workingDirectory, "package.json");
        if (packageJson.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();

                PackageJson parsed = mapper.readValue(packageJson, PackageJson.class);

                // check version
                if (project.getVersion().contains("SNAPSHOT")) {
                    getLog().info("Current version is SNAPSHOT, check snapshot ignored");
                    return;
                }

                // check dependencies
                checkDependencies(parsed.dependencies);

                // check devDependencies
                if (checkDev) {
                    checkDependencies(parsed.devDependencies);
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to parse package.json", e);
            }
        } else {
            getLog().warn("No package.json found, check snapshot ignored");
        }
    }

    private void checkDependencies(Map<String, String> deps) throws MojoExecutionException {
        for (Map.Entry<String, String> dep: deps.entrySet()) {
            getLog().info(String.format("Checking %s version %s", dep.getKey(), dep.getValue()));
            for (String stopVersion: stopVersions) {
                if (dep.getValue().toLowerCase().contains(stopVersion.toLowerCase())) {
                    throw new MojoExecutionException(String.format("Dependency %s version %s contains %s",
                            dep.getKey(), dep.getValue(), stopVersion));
                }
            }
        }
    }
}
