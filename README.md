# npm-utils-maven-plugin
Maven plugin for package.json validations

## Usage

Add plugin to dependencies
```xml
    <groupId>com.github.unitedtraders</groupId>
    <artifactId>npm-utils-maven-plugin</artifactId>
    <version>${npm-utils-maven-plugin.version}</version>

```

Configure executions
```xml
    <executions>
        <execution>
            <id>check snapshots</id>
            <phase>validate</phase>
            <goals>
                <goal>check-snapshots</goal>
            </goals>
            <configuration>
                <checkDev>true</checkDev>
            </configuration>
        </execution>
        <execution>
            <id>check version</id>
            <phase>validate</phase>
            <goals>
                <goal>check-versions</goal>
            </goals>
            <configuration>
                <exact>true</exact>
                <fix>true</fix>
            </configuration>
        </execution>
    </executions>

```

### Check snapshots

Goal `check-snapshots` validates your package.json does not contain snapshot or beta versions of dependencies. Flag `checkDev`
enables devDependecies check, by default it's off.

### Check versions

Goal `check-versions` check version in package.json matches version in current project's `pom.xml`.

Flag `exact` enables exact match. When it's off only prefix check for ex, versions `1.2.3` of `pom.xml` 
and `1.2.3.1` of `package.json` match. It is on by default.

Flag `fix` shows that plugin will try to fix version in `package.json`. By default it is off.
