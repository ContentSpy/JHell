import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Shell {
    private final Scanner scanner;
    private boolean isRunning;
    private Path currentDir;

    public Shell(Scanner scanner) {
        this.scanner = scanner;
        isRunning = true;
        currentDir = Paths.get(System.getProperty("user.dir"));
    }

    public void start(){
        String input = "";
        while(isRunning){
            System.out.print(">");
            System.out.flush();
            if(scanner.hasNextLine()){
                input = scanner.nextLine();
            }

            input = input.trim();
            if(input.isEmpty()){
                continue;
            }
            List<ParsedCommand> stages = parse(input);
            if(stages.get(0).command().isEmpty()){
                continue;
            }
            if (handleBuiltIn(stages.get(0))) continue;
            execute(stages);
        }
    }

    private ParsedCommand parseSegment(String rawUserInput){
        String[] raw = rawUserInput.split("\\s+");
        List<String> command = new ArrayList<>();
        Path inputFile = null;
        Path outputFile = null;
        boolean append = false;
        for (int i = 0; i < raw.length; i++) {
            switch (raw[i]) {
                case ">":
                    outputFile = currentDir.resolve(raw[++i]);
                    break;
                case ">>":
                    outputFile = currentDir.resolve(raw[++i]);
                    append = true;
                    break;
                case "<":
                    inputFile = currentDir.resolve(raw[++i]);
                    break;
                default:
                    command.add(raw[i]);
            }
        }
        return new ParsedCommand(command, inputFile, outputFile, append);
    }

    private List<ParsedCommand> parse(String rawUserInput){
        String[] segments = rawUserInput.split("\\|");
        List<ParsedCommand> stages = new ArrayList<>();
        for(String segment : segments){
            segment = segment.trim();
            stages.add(parseSegment(segment));
        }
        return stages;
    }

    private void execute(List<ParsedCommand> tokens){
        ParsedCommand first = tokens.get(0);
        ProcessBuilder pb = new ProcessBuilder(tokens.get(0).command());
        pb.directory(currentDir.toFile());
        pb.inheritIO();
        if (first.inputFile() != null) {
            pb.redirectInput(first.inputFile().toFile());
        }
        if (first.outputFile() != null) {
            if (first.append()) {
                pb.redirectOutput(ProcessBuilder.Redirect.appendTo(first.outputFile().toFile()));
            } else {
                pb.redirectOutput(first.outputFile().toFile());
            }
        }
        try {
            Process p = pb.start();
            p.waitFor();
        } catch (IOException e) {
            System.out.println("command not found: " + first.command().get(0));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    private boolean handleBuiltIn(ParsedCommand tokens){
        if(tokens.command().get(0).equals("exit")){
            isRunning = false;
            return true;
        }
        if(tokens.command().get(0).equals("cd")){
            String arg = (tokens.command().size() > 1) ? tokens.command().get(1) : System.getProperty("user.home");
            Path newDir = currentDir.resolve(arg).normalize();
            if (Files.isDirectory(newDir)) {
                currentDir = newDir;
            } else {
                System.out.println("cd: not a directory: " + arg);
            }
            return true;
        }
        if(tokens.command().get(0).equals("pwd")){
            System.out.println(currentDir);
            return true;
        }
        return false;

    }
    public record ParsedCommand(
            List<String> command,
            Path inputFile,
            Path outputFile,
            boolean append
    ) {}
}

