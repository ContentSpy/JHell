```bash
# jhell

A Unix-style shell written in Java.

A learning project — built to understand how shells work under the hood: process spawning, file descriptor wiring, pipes, and the REPL loop.

## Features

- External command execution via `ProcessBuilder`
- Built-ins: `cd`, `pwd`, `exit`
- I/O redirection: `>`, `>>`, `<`
- Multi-stage pipelines: `cmd1 | cmd2 | cmd3`
- Combined redirection and pipes: `cmd < in.txt | filter | sort > out.txt`
- Working directory tracking (so `cd` actually persists across commands)

## Requirements

- Java 21 or later
- Maven (for building)
- Linux/macOS (Windows works via WSL; native Windows runs but most Unix commands won't be found)

## Build

```bash
mvn clean package
```

This produces a runnable JAR in `target/`.

## Run

```bash
java -jar target/jhell-1.0-SNAPSHOT.jar
```

You'll get a `>` prompt. Type commands like you would in bash.

## Examples

```
> ls
Main.java  Shell.java  pom.xml  target
> cd /tmp
> pwd
/tmp
> echo hello > greeting.txt
> cat greeting.txt
hello
> ls | grep .java
Main.java
Shell.java
> cat < greeting.txt | tr a-z A-Z > shout.txt
> cat shout.txt
HELLO
> exit
```

## Architecture

The shell is structured around four methods:

- **`start`** — the REPL loop. Reads input, dispatches to built-ins or external execution.
- **`parse`** — splits input on `|` into pipeline stages, then delegates each stage to `parseSegment` for tokenization and redirection extraction.
- **`handleBuiltIn`** — handles commands that must modify the shell's own state (`cd`, `exit`) or bypass `ProcessBuilder` for efficiency (`pwd`).
- **`execute`** — builds a `ProcessBuilder` per pipeline stage, applies input/output redirection to the endpoints, and uses `ProcessBuilder.startPipeline` to wire the stages together.

A `ParsedCommand` record carries each stage's command tokens and redirection info from `parse` to `execute`.

## Limitations

This is a learning project, not a daily driver. Things it doesn't do:

- No quoting — `echo "hello world"` tokenizes naively as three tokens
- No environment variables (`$FOO`, `export`)
- No background jobs (`cmd &`)
- No globbing (`*.txt` is passed literally to commands)
- No signal handling — Ctrl+C may not behave as expected
- TUI apps (vim, less) launch but have terminal mode quirks
- Built-ins don't compose with pipes (`pwd | grep /` runs only `pwd`)
- Error messages don't identify which stage failed in a multi-stage pipeline

## Why I built this

Part of a self-study track in low-level systems programming. Writing a shell forced me to learn how `fork`/`exec` work conceptually, why built-ins exist (try implementing `cd` as an external process — it can't change your shell's directory), and how Unix's "everything is a file descriptor" design enables pipes to be a few lines of code rather than a complex feature.

Next iteration: rewrite in C using raw `fork`, `execve`, and `pipe` syscalls.
```

Replace `1.0-SNAPSHOT` with whatever your `target/` folder actually contains (check with `ls target/`). Test the example session before committing — the `tr a-z A-Z` line should work but verify on your machine.