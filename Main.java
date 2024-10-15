import java.io.*;
import java.nio.file.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String inputFolder = "tests";
        String outputFolder = "results";

        new File(outputFolder).mkdirs();

        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(inputFolder), "*.txt");
            for (Path entry : stream) {
                String input = readFile(entry.toString());

                Lexer lexer = new Lexer(input);
                List<Token> tokens = lexer.tokenize();

                lexer.saveTokensToFile("tokens.txt");

                Parser parser = new Parser(tokens);
                ASTNode ast = parser.parseProgram();

                System.out.println("Abstract Syntax Tree for " + entry.getFileName() + ":");
                System.out.println(ast);

                String outputFileName = outputFolder + "/ast" + entry.getFileName().toString().replace(".txt", "") + ".txt";
                saveASTToFile(ast, outputFileName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readFile(String fileName) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static void saveASTToFile(ASTNode ast, String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(ast.toString());
        }
    }
}
