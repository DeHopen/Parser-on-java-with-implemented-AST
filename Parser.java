import java.util.*;

class ASTNode {
    String type;
    List<ASTNode> children = new ArrayList<>();

    ASTNode(String type) {
        this.type = type;
    }

    void addChild(ASTNode child) {
        children.add(child);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0);
        return sb.toString();
    }

    private void toString(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) sb.append("  ");
        sb.append(type).append("\n");
        for (ASTNode child : children) {
            child.toString(sb, level + 1);
        }
    }
}

public class Parser {
    private List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode parseProgram() {
        ASTNode program = new ASTNode("Program");
        while (pos < tokens.size()) {
            Token token = peek();
            if (token.type.equals("CLASS")) {
                program.addChild(parseClassDeclaration());
            } else {
                program.addChild(parseStatement());
            }
        }
        return program;
    }


    private ASTNode parseClassDeclaration() {
        expect("CLASS");
        Token className = expect("ID");
        ASTNode classDecl = new ASTNode("ClassDeclaration: " + className.value);

        if (peek().type.equals("LBRACKET")) {
            expect("LBRACKET");
            Token genericType = expect("ID");
            expect("RBRACKET");
            classDecl.addChild(new ASTNode("GenericType: " + genericType.value));
        }

        if (peek().type.equals("EXTENDS")) {
            expect("EXTENDS");
            Token baseClass = expect("ID");
            classDecl.addChild(new ASTNode("Extends: " + baseClass.value));
        }

        expect("IS");
        while (!peek().type.equals("END")) {
            classDecl.addChild(parseMemberDeclaration());
        }
        expect("END");
        return classDecl;
    }

    private ASTNode parseMemberDeclaration() {
        Token token = peek();
        if (token.type.equals("VAR")) {
            return parseVariableDeclaration();
        } else if (token.type.equals("METHOD")) {
            return parseMethodDeclaration();
        } else if (token.type.equals("THIS")) {
            return parseConstructorDeclaration();
        } else if (token.type.equals("CLASS")) {
            return parseClassDeclaration();
        }
        throw new RuntimeException("Unexpected token: " + token);
    }

    private ASTNode parseVariableDeclaration() {
        expect("VAR");
        Token id = expect("ID");
    
        ASTNode varDecl = new ASTNode("VariableDeclaration");
        varDecl.addChild(new ASTNode("ID: " + id.value));
    
        if (peek().type.equals("COLON")) {
            expect("COLON");
            Token type = expect("ID");
            varDecl.addChild(new ASTNode("Type: " + type.value));
    
            if (peek().type.equals("LBRACKET")) {
                expect("LBRACKET");
                Token genericType = expect("ID");
                expect("RBRACKET");
                varDecl.addChild(new ASTNode("GenericType: " + genericType.value));
            }
    
            if (peek().type.equals("LPAREN")) {
                varDecl.addChild(parseMethodCall(new ASTNode("ConstructorCall: " + type.value), type));
            }

            
    
        } else if (peek().type.equals("IS")) {
            expect("IS");
            if (peek().type.equals("ID") || peek().type.equals("BOOLEAN_LITERAL") || peek().type.equals("INT_LITERAL")) {
                varDecl.addChild(parseExpression());
            } else {
                varDecl.addChild(new ASTNode("Expression: null"));
            }
        } else {
            throw new RuntimeException("Unexpected token in variable declaration: " + peek());
        }
        
        return varDecl;
    }

    private ASTNode parseMethodDeclaration() {
        expect("METHOD");
        Token methodName = expect("ID");
        ASTNode methodDecl = new ASTNode("MethodDeclaration: " + methodName.value);

        if (peek().type.equals("LPAREN")) {
            methodDecl.addChild(parseParameters());
        }

        if (peek().type.equals("COLON")) {
            expect("COLON");
            Token returnType = expect("ID");
            methodDecl.addChild(new ASTNode("ReturnType: " + returnType.value));
        }

        expect("IS");
        methodDecl.addChild(parseBlock());
        expect("END");
        return methodDecl;
    }

    private ASTNode parseParameters() {
        expect("LPAREN");
        ASTNode params = new ASTNode("Parameters");
        if (!peek().type.equals("RPAREN")) {
            params.addChild(parseParameterDeclaration());
            while (peek().type.equals("COMMA")) {
                expect("COMMA");
                params.addChild(parseParameterDeclaration());
            }
        }
        expect("RPAREN");
        return params;
    }

    private ASTNode parseParameterDeclaration() {
        Token id = expect("ID");
        expect("COLON");
        Token type = expect("ID");
        return new ASTNode("Parameter: " + id.value + " : " + type.value);
    }

    private ASTNode parseConstructorDeclaration() {
        expect("THIS");
        ASTNode constructor = new ASTNode("ConstructorDeclaration");
    
        if (peek().type.equals("LPAREN")) {
            constructor.addChild(parseParameters());
        }
    
        expect("IS");
        constructor.addChild(parseBlock());
        expect("END");
        return constructor;
    }

    private ASTNode parseAssignmentOrMethodCall() {
        Token id = expect("ID");
        ASTNode left = parseDotOrMethodCall(id);
    
        if (peek().type.equals("ASSIGN")) {
            expect("ASSIGN");
            ASTNode right = parseExpression();
            ASTNode assignment = new ASTNode("Assignment");
            assignment.addChild(left);
            assignment.addChild(right);
            return assignment;
        }
    
        return left;
    }

    private ASTNode parseDotOrMethodCall(Token id) {
        ASTNode node = new ASTNode("ID: " + id.value);
    
        while (pos < tokens.size() && (peek().type.equals("DOT") || peek().type.equals("SUPER"))) {
            if (peek().type.equals("SUPER")) {
                expect("SUPER");
                Token method = expect("ID");
                node = new ASTNode("SuperMethodCall: " + method.value);
                node.addChild(new ASTNode("ID: " + id.value));
            } else {
                expect("DOT");
                Token method = expect("ID");
                if (peek().type.equals("LPAREN")) {
                    node = parseMethodCall(node, method);
                } else {
                    node = new ASTNode("MethodCall: " + method.value);
                    node.addChild(new ASTNode("ID: " + id.value));
                }
            }
        }
        return node;
    }

    private ASTNode parseMethodCall(ASTNode object, Token method) {
        expect("LPAREN");
        ASTNode methodCall = new ASTNode("MethodCall: " + method.value);
        methodCall.addChild(object);

        if (peek().type.equals("RPAREN")) {
            expect("RPAREN");
            return methodCall;
        }

        methodCall.addChild(parseExpression());
        while (peek().type.equals("COMMA")) {
            expect("COMMA");
            methodCall.addChild(parseExpression());
        }

        expect("RPAREN");
        return methodCall;
    }

    private ASTNode parseExpression() {
        Token token = peek();
        if (token.type.equals("ID")) {
            expect("ID");
            if (peek().type.equals("DOT")) {
                return parseDotOrMethodCall(token);
            } else if (peek().type.equals("LPAREN")) {
                return parseMethodCall(new ASTNode("ID: " + token.value), token);
            } else if (peek().type.equals("RPAREN")) {
                return new ASTNode("PropertyAccess: " + token.value);
            } else {
                return new ASTNode("Expression: " + token.value);
            }
        } else if (token.type.equals("INT_LITERAL")) {
            return new ASTNode("Expression: " + expect("INT_LITERAL").value);
        } else if (token.type.equals("BOOLEAN_LITERAL")) {
            return new ASTNode("Expression: " + expect("BOOLEAN_LITERAL").value);
        }
        throw new RuntimeException("Unexpected token in expression: " + token);
    }

    private ASTNode parseBlock() {
        ASTNode block = new ASTNode("Block");
        while (!peek().type.equals("END") && !peek().type.equals("ELSE")) {
            if (peek().type.equals("RETURN")) {
                block.addChild(parseReturnStatement());
            } else {
                block.addChild(parseStatement());
            }
        }
        return block;
    }

    private ASTNode parseReturnStatement() {
        expect("RETURN");
        ASTNode returnNode = new ASTNode("ReturnStatement");
        returnNode.addChild(parseExpression());
        return returnNode;
    }

    private ASTNode parseStatement() {
        Token token = peek();
        if (token.type.equals("VAR")) {
            return parseVariableDeclaration();
        } else if (token.type.equals("WHILE")) {
            return parseWhileStatement();
        } else if (token.type.equals("IF")) {
            return parseIfStatement();
        } else if (token.type.equals("ID") || token.type.equals("LOOP_VAR")) {
            return parseAssignmentOrMethodCall();
        } else {
            throw new RuntimeException("Unexpected token: " + token);
        }
    }

    private ASTNode parseIfStatement() {
        expect("IF");
        ASTNode ifNode = new ASTNode("IfStatement");
        ifNode.addChild(parseExpression());
        expect("THEN");
        ifNode.addChild(parseBlock());
        if (peek().type.equals("ELSE")) {
            expect("ELSE");
            ifNode.addChild(parseBlock());
        }
        expect("END");
        return ifNode;
    }

    private ASTNode parseWhileStatement() {
        expect("WHILE");
        ASTNode whileNode = new ASTNode("WhileStatement");
        whileNode.addChild(parseExpression());
        expect("LOOP");
        whileNode.addChild(parseBlock());
        expect("END");
        return whileNode;
    }

    private Token peek() {
        if (pos < tokens.size()) {
            return tokens.get(pos);
        }
        throw new RuntimeException("Unexpected end of input");
    }

    private Token expect(String... expectedTypes) {
        Token token = peek();
        for (String type : expectedTypes) {
            if (token.type.equals(type)) {
                pos++;
                return token;
            }
        }
        throw new RuntimeException("Expected " + Arrays.toString(expectedTypes) + " but found " + token.type);
    }
}
