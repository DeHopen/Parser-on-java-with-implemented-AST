import java.io.*;
import java.util.*;

class Token {
    String type;
    String value;

    Token(String type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return type + "('" + value + "')";
    }
}

public class Lexer {
    private String input;
    private int pos = 0;
    private List<Token> tokens = new ArrayList<>();

    // Define keywords and operators
    private final String[] keywords = {"var", "class", "is", "end", "method", "this", "if", "then", "else", "while", "loop", "return"};
    private final String[] singleCharSymbols = {":", ".", "+", "-", "*", "/", "(", ")", "[", "]", ","};

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        while (pos < input.length()) {
            char currentChar = input.charAt(pos);

            if (Character.isWhitespace(currentChar)) {
                pos++; // Skip whitespace
            } else if (Character.isDigit(currentChar)) {
                tokens.add(new Token("INT_LITERAL", readNumber()));
            } else if (Character.isLetter(currentChar)) {
                String word = readWord();
                if (isKeyword(word)) {
                    tokens.add(new Token(word.toUpperCase(), word));
                } else if (word.startsWith("l_")) { // Check for loop variable prefix
                    tokens.add(new Token("LOOP_VAR", word)); // Mark it as a loop variable
                } else {
                    tokens.add(new Token("ID", word));
                }
            } else if (currentChar == '/' && peek() == '/') {
                skipComment();
            } else if (input.startsWith(":=", pos)) {
                tokens.add(new Token("ASSIGN", ":=")); // Handle := as ASSIGN
                pos += 2;
            } else {
                String symbol = readSymbol();
                if (symbol != null) {
                    tokens.add(new Token(getSymbolType(symbol), symbol));
                } else {
                    throw new RuntimeException("Unexpected character: " + currentChar);
                }
            }
        }
        return tokens;
    }

    private String readNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }
        return sb.toString();
    }

    private String readWord() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }
        return sb.toString();
    }

    private boolean isKeyword(String word) {
        return Arrays.asList(keywords).contains(word);
    }

    private String readSymbol() {
        for (String symbol : singleCharSymbols) {
            if (input.startsWith(symbol, pos)) {
                pos += symbol.length();
                return symbol;
            }
        }
        return null;
    }

    private String getSymbolType(String symbol) {
        switch (symbol) {
            case ":":
                return "COLON";
            case ".":
                return "DOT";
            case "+":
                return "PLUS";
            case "-":
                return "MINUS";
            case "*":
                return "MULT";
            case "/":
                return "DIV";
            case "(":
                return "LPAREN";
            case ")":
                return "RPAREN";
            case "[":
                return "LBRACKET";
            case "]":
                return "RBRACKET";
            case ",":
                return "COMMA";
            default:
                return "UNKNOWN";
        }
    }

    private void skipComment() {
        while (pos < input.length() && input.charAt(pos) != '\n') {
            pos++;
        }
    }

    private char peek() {
        if (pos + 1 < input.length()) {
            return input.charAt(pos + 1);
        }
        return '\0';
    }

    public void saveTokensToFile(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Token token : tokens) {
                writer.write(token.toString());
                writer.newLine();
            }
        }
    }
}
