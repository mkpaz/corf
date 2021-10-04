package main

import (
  "log"
  "io/ioutil"
  "os"
  "os/exec"
  "path/filepath"
  "strings"
)

const RESTART_EXIT_CODE = 3

func main() {
  cwd, err := os.Getwd()
  if err != nil {
    log.Fatal(err)
  }

  var (
    OPTIONS_FILE = filepath.Join(cwd, "user.options")
    JAVA_EXEC = filepath.Join(cwd, "app", "bin", JAVA_EXEC)
  )

  fileBytes, err := ioutil.ReadFile(OPTIONS_FILE)
  if err != nil {
    log.Fatal(err)
  }

  // gather options
  optsText := strings.Trim(strings.Replace(string(fileBytes), "\r\n", "\n", -1), "\n");
  jvmOptions := strings.Split(optsText, "\n")
  args := append(jvmOptions,
    "-Dtelekit.app.dir=" + cwd,
    "-m",
    "telekit.desktop/telekit.desktop.Launcher")

  // run
  for {
    cmd := exec.Command(JAVA_EXEC, args...)

    if err := cmd.Run(); err != nil {
      if exitError, hasError := err.(*exec.ExitError); hasError {
        if exitCode := exitError.ExitCode(); exitCode == RESTART_EXIT_CODE {
          continue;
        }
      }
    }

    break;
  }
}
