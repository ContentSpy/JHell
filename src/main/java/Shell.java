import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Shell {
    private final Scanner scanner;
    private boolean isRunning;

    public Shell(Scanner scanner) {
        this.scanner = scanner;
        isRunning = true;
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
            List<String> resultOfParse = parse(input);
            if(resultOfParse.isEmpty()){
                continue;
            }
            if(resultOfParse.get(0).equalsIgnoreCase("exit")){
                isRunning = false;
                continue;
            }
            execute(resultOfParse);
        }
    }

    private List<String> parse(String s){
        String[] p = s.split("\\s+");
        return Arrays.asList(p);
    }

    private void execute(List<String> tokens){
        ProcessBuilder pb = new ProcessBuilder(tokens);
        pb.inheritIO();
        try {
            Process p = pb.start();
            p.waitFor();
        } catch (IOException e) {
            System.out.println("command not found: " + tokens.get(0));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

