package JReg;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

public class SyntaxTree {
    public sealed interface Node {
        record Leaf(char c, int id) implements Node {}
        record And (Node left, Node right) implements Node {} // .
        record Or (Node left, Node right) implements Node {} // |
        record Snap (Node child) implements Node {} // ... == *
        record Optional (Node child) implements Node {} // ?
    }

    public SyntaxTree(Node root) {
        this.root = root;
    }

    Node root = null;

    private static String prepareExpr(String expr) throws IllegalArgumentException {
        StringBuilder correctExpr = new StringBuilder();

        int openedParenthesis = 0;
        boolean curlyOpened = false;
        boolean angleOpened = false;
        boolean lastOp = true;

        for (int i = 0; i < expr.length(); ++i) {
            char c = expr.charAt(i);
            boolean isUnaryOrClose = (c == ')' || c == '>' || c == '}' || c == '?' || c == '|' ||
                    (c == '.' && i < expr.length() - 2 && expr.charAt(i+1) == '.' && expr.charAt(i+2) == '.'));

            if (!lastOp && !isUnaryOrClose) correctExpr.append('.');

            switch (c) {
                case '\\' -> {
                    if (i == expr.length() - 1) throw new IllegalArgumentException("Bad regular expression");
                    correctExpr.append('\\');
                    correctExpr.append(expr.charAt(i + 1));
                    ++i;
                    lastOp = false;
                }
                case '.' -> {
                    if (i == expr.length() - 1) throw new IllegalArgumentException("Bad regular expression");

                    if (expr.charAt(i + 1) != '.') {
                        correctExpr.append('\\');
                        correctExpr.append('.');
                        lastOp = false;
                        continue;
                    }

                    if (i == expr.length() - 2 || expr.charAt(i + 2) != '.')
                        throw new IllegalArgumentException("Bad regular expression");

                    correctExpr.append('*');

                    i += 2; // Need to seek i, because we captured 3 chars at once

                    lastOp = false;
                }
                case '|' -> {
                    correctExpr.append('|');
                    lastOp = true;
                }
                case '?' -> {
                    correctExpr.append('?');
                    lastOp = false;
                }
                case '(' -> {
                    ++openedParenthesis;
                    correctExpr.append('(');
                    lastOp = true;
                }
                case ')' -> {
                    if (openedParenthesis == 0) throw new IllegalArgumentException("Bad regular expression");
                    --openedParenthesis;
                    correctExpr.append(')');
                    lastOp = false;
                }
                case '<' -> {
                    if (angleOpened) throw new IllegalArgumentException("Bad angle expression");
                    angleOpened = true;
                    correctExpr.append('<');
                    lastOp = true;
                }
                case  '>' -> {
                    if (!angleOpened) throw new IllegalArgumentException("Bad angle expression");
                    angleOpened = false;
                    correctExpr.append('>');
                    lastOp = true;
                }
                case '{' -> {
                    if (curlyOpened) throw new IllegalArgumentException("Bad curly expression");
                    curlyOpened = true;
                    correctExpr.append('{');
                    lastOp = true;
                }
                case '}' -> {
                    if (!curlyOpened) throw new IllegalArgumentException("Bad curly expression");
                    curlyOpened = false;
                    correctExpr.append('}');
                    lastOp = false;
                }
                case '*' -> {
                    correctExpr.append('\\');
                    correctExpr.append('*');
                    lastOp = false;
                }
                default -> {
                    if (curlyOpened && !Character.isDigit(expr.charAt(i))) throw new IllegalArgumentException("Bad regular expression");
                    lastOp = curlyOpened || angleOpened;

                    correctExpr.append(expr.charAt(i));
                }
            }
        }
        if (openedParenthesis != 0) throw new IllegalArgumentException("Bad regular expression");
        if (!lastOp) correctExpr.append('.');
        correctExpr.append('#');

        return correctExpr.toString();
    }

    private static int getPriority(char c) {
        return switch (c) {
            case '|' -> 1;
            case '.' -> 2;
            default  -> 0;
        };
    }

    private static void applyOp(Deque<Node> nodeStack, char op) {
        Node right = nodeStack.pop();
        Node left  = nodeStack.pop();
        if (op == '|') nodeStack.push(new Node.Or(left, right));
        if (op == '.') nodeStack.push(new Node.And(left, right));
    }

    private static void collapseStack(Deque<Character> characterStack, Deque<Node> nodeStack, char op) {
        while (!characterStack.isEmpty() && getPriority(characterStack.peek()) >= getPriority(op)) {
            applyOp(nodeStack, characterStack.pop());
        }
        characterStack.push(op);
    }

    private static SyntaxTree buildTree(String expr) throws IllegalArgumentException {
        Deque<Node> nodeStack = new ArrayDeque<>();
        Deque<Character> charactersStack = new ArrayDeque<>();

        int id = 0;

        for (int i = 0; i < expr.length(); ++i) {
            char c = expr.charAt(i);
            switch (c) {
                case '\\' -> nodeStack.push(new Node.Leaf(expr.charAt(++i), ++id));
                case '*'  -> nodeStack.push(new Node.Snap(nodeStack.pop()));
                case '?'  -> nodeStack.push(new Node.Optional(nodeStack.pop()));
                case '|', '.' -> collapseStack(charactersStack, nodeStack, c);
                case '(' -> charactersStack.push('(');
                case ')' -> {
                    char op = charactersStack.pop();
                    while (op != '(') {
                        applyOp(nodeStack, op);
                        op = charactersStack.pop();
                    }
                }
                default -> nodeStack.push(new Node.Leaf(c, ++id));
            }
        }

        while (!charactersStack.isEmpty()) {
            applyOp(nodeStack, charactersStack.pop());
        }

        Node root = nodeStack.pop();

        if (!nodeStack.isEmpty()) throw new IllegalArgumentException("Bad regular expression");

        return new SyntaxTree(root);
    }

    public static SyntaxTree compile(String regExpr) throws Exception {
        regExpr = prepareExpr(regExpr);

        System.out.println("regExpr: " + regExpr);

        SyntaxTree tree = buildTree(regExpr);

        return tree;
    }
}
