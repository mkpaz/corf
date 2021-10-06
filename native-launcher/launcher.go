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
  executable, err := os.Executable()
  if err != nil { log.Fatal(err) }
  executablePath := filepath.Dir(executable)

  var (
    USER_OPTIONS_FILE = filepath.Join(executablePath, "user.options")
    JAVA_EXEC = filepath.Join(executablePath, "app", "bin", JAVA_EXEC)
  )

  fileBytes, err := ioutil.ReadFile(USER_OPTIONS_FILE)
  if err != nil { log.Fatal(err) }

  // gather options
  optionsText := strings.Trim(strings.Replace(string(fileBytes), "\r\n", "\n", -1), "\n");
  commentPredicate := func(s string) bool { return !strings.HasPrefix(strings.TrimSpace(s), "#") }
  userOptions := filter(strings.Split(optionsText, "\n"), commentPredicate)
  args := append(userOptions,
    "-Dtelekit.app.dir=" + executablePath,
    "-m",
    "telekit.desktop/telekit.desktop.Launcher")

  // run
  for {
    cmd := exec.Command(JAVA_EXEC, args...)

    if err := cmd.Run(); err != nil {
      if exitError, hasError := err.(*exec.ExitError); hasError {
        if exitCode := exitError.ExitCode(); exitCode == RESTART_EXIT_CODE {
          continue
        }
      }
    }

    break
  }
}

func filter(stringArray []string, predicate func(string) bool) (ret []string) {
  for _, s := range stringArray {
    if predicate(s) {
      ret = append(ret, s)
    }
  }
  return
}
