# BUILD

## Prerequisites

You'll need OpenJDK 15+ with **bundled OpenJFX** (e.g. Liberica JDK) and Maven.

Configure Maven Toolchains (`~/.m2/toolchains.xml`), only include installed JDK:

```xml
<?xml version="1.0" encoding="UTF8"?>
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>13</version>
      <vendor>liberica</vendor>
      <platform>linux64</platform>
    </provides>
    <configuration>
      <jdkHome>/path/to/jdk</jdkHome>
    </configuration>
  </toolchain>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>13</version>
      <vendor>liberica</vendor>
      <platform>win64</platform>
    </provides>
    <configuration>
      <jdkHome>/path/to/jdk</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

If you want to recompile native launchers, you'll also need:

* Golang
* *Resource Hacker* tool to embed app icon into EXE file (Windows only)


## BUILD

Clone the repo:

```sh
git clone https://github.com/mkpaz/telekit
cd telekit
```

Build modules:

```sh
mvn install -f parent/pom.xml
mvn install -f base/pom.xml
mvn install -f controls/pom.xml
```

Build main application:

```sh
mvn clean verufy -P generate-docs,build-dependencies,build-runtime-image,<platform> -f telekit-ui/pom.xml
```

`<platform>` is whatever of the Maven profile ids (`win32`, `win64` or `linux64`).

Successfully built application will be placed into `telekit-ui/target/dist` directory.

## Running and Testing

**Note:** Normally you shouldn't run app from command line but with an IDE of your choice.

It's not convenient to rebuild the whole app every time you want to inspect some UI changes or runtime behavior. If you want to launch app for testing use:

```sh
cd telekit-ui
mvn clean verify -P dev
# JavaFX won't detect your project JDK, you have specify it explicitly
mvn javafx:run -Dtelekit.app.dir=/path/to/telekit-ui/target -Djava.home=/path/to/jdk
```
