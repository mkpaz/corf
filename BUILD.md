# BUILD

## Prerequisites

You'll need:

* JDK13 to build `telekit-ui` and all its modules except `telekit-controls`
* JDK11 to build `telekit-controls`. The reason is that this module contains custom FXML components for Scene Builder but Gluon at the moment only provides Scene Builder built with JDK11, so you can't load component compiled with higher JDK version into it.
* Maven

You can choose any OpenJDK vendor, but only which bundles OpenJFX (e.g. Liberica JDK).

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
If you want to recompile native launchers you'll also need:

* Golang
* Resource Hacker application to embed an icon into EXE file (Windows only)


## BUILD

Clone the repo:

```sh
git clone https://github.com/mkpaz/telekit
cd telekit
```

Build modules:

```sh
mvn install -f parent/pom.xml
# make sure that JAVA_HOME points to JDK11
mvn install -f controls/pom.xml
mvn install -f base/pom.xml
```

Build main application:

```sh
mvn clean verufy -P generate-docs,build-dependencies,build-runtime-image,<platform> -f telekit-ui/pom.xml
```

Regarding `<platform>`, you have to specify correct Maven profile ID (`win32`, `win64` or `linux64`).

Successfully built application will be placed into `telekit-ui/target/dist` directory.

### Native Launcher

Normally, you don't need to rebuild native launchers, but if you want to just use `compile_*` scripts that reside in corresponding directory. It should be pretty clear how they work.


## Running and Testing

**Note:** Normally you shouldn't do it with command line but with an IDE of your choice. 

It's not convenient to rebuild the whole app every time you want to inspect some GUI changes or runtime behavior. If you want to launch app for testing instead of building `telekit-ui` use:

```sh
cd telekit-ui
mvn clean verify -P dev
# JavaFX won't detect your project JDK, you have specify it explicitly
mvn javafx:run -Dtelekit.app.dir=/path/to/telekit-ui/target -Djava.home=/path/to/jdk
```

You can use exactly the same commands to run and test plugins (e.g. `telekit-plugin-example`)

