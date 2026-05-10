# jhell
A Unix-style command-line shell written in Java.
This is a learning project built to understand how shells work under the hood, specifically focusing on process spawning, file descriptor wiring, pipes, and the REPL (Read-Eval-Print Loop) architecture.
## Features
 * **External Command Execution:** Utilizes ProcessBuilder to spawn processes.
 * **Built-in Commands:** Native support for cd, pwd, and exit.
 * **I/O Redirection:** Supports standard output/input redirection (>, >>, <).
 * **Multi-Stage Pipelines:** Chain multiple commands seamlessly (e.g., cmd1 | cmd2 | cmd3).
 * **Combined Operations:** Mix redirection and pipes (e.g., cmd < in.txt | filter | sort > out.txt).
 * **Stateful Directory Tracking:** Working directory changes persist across commands and external processes.
## Requirements
 * Java 21 or later
 * Maven
 * Linux/macOS (Windows is supported via WSL; native Windows will run, but most standard Unix utilities will not be found)
## Build
Compile and package the project using Maven:
```bash
mvn clean package

```
This will generate a runnable JAR file in the target/ directory.
## Run
Execute the compiled JAR file:
```bash
java -jar target/jhell-*.jar

```
You will be greeted with a > prompt. You can now type commands just like you would in bash.
## Examples
```bash
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
The shell's execution cycle is structured around four primary components:
 * **start**: The main REPL loop. It continuously reads user input and routes it to either built-in handlers or the external executor.
 * **parse**: Splits the raw input string by the | delimiter to create pipeline stages, delegating each segment to a tokenizer that extracts commands, arguments, and redirection targets.
 * **handleBuiltIn**: Manages commands that modify the shell's internal JVM state (like cd and exit) or bypass ProcessBuilder entirely for efficiency (like pwd).
 * **execute**: Constructs a ProcessBuilder instance for each pipeline stage, wires up the necessary file descriptors for I/O redirection, and links the stages together using ProcessBuilder.startPipeline.
A ParsedCommand record acts as the data carrier between the parsing and execution phases, holding the command tokens and redirection metadata for each specific stage.
## Limitations
As a conceptual learning project, jhell lacks some features expected in a daily-driver shell:
 * **Quoting:** No support for string literals (e.g., echo "hello world" is tokenized naively as three separate tokens).
 * **Environment Variables:** No $VAR expansion or export functionality.
 * **Job Control:** No background job execution (&).
 * **Globbing:** Wildcards (*.txt) are passed directly to commands as literal strings.
 * **Signal Handling:** Interrupts like Ctrl+C may terminate the shell rather than just the running child process.
 * **TUI Compatibility:** Terminal User Interface applications (like vim or less) will launch but may exhibit terminal mode rendering quirks.
 * **Built-in Piping:** Built-in commands do not compose within pipes (e.g., pwd | grep / will only execute pwd).
 * **Error Granularity:** Pipeline error messages do not isolate which specific stage failed.
## Motivation
This project is part of a broader self-study track bridging the gap between high-level application development and low-level systems programming.
Writing a shell from scratch in Java forced a practical understanding of how fork/exec operate conceptually, why built-in commands are architecturally necessary (an external process cannot modify the parent shell's working directory), and how the Unix philosophy of "everything is a file descriptor" allows pipeline wiring to be remarkably elegant.
*Next iteration goal: Re-implement this architecture in C using raw fork(), execve(), and pipe() system calls.*
