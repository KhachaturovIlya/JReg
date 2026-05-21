package JReg.AST;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SyntaxTree {
    @Getter
    final private Map<Integer, Character> charMap = new HashMap<>();
    @Getter
    private boolean named = false; // WARNING!!! NEED SET TO FALSE!!!

    public sealed interface Node {
        public Node copy(AtomicInteger id, Map<Integer, Character> charMap);
        
        record Leaf(char c, int id) implements Node {
            @Override
            public Node copy(AtomicInteger id, Map<Integer, Character> charMap) {
                charMap.put(id.incrementAndGet(), c);
                return new Leaf(c, id.get());
            }
        }
        record And (Node left, Node right) implements Node {
            @Override
            public Node copy(AtomicInteger id, Map<Integer, Character> charMap) {
                return new And(left.copy(id, charMap), right.copy(id, charMap));
            }
        }
        record Or (Node left, Node right) implements Node {
            @Override
            public Node copy(AtomicInteger id, Map<Integer, Character> charMap) {
                return new Or(left.copy(id, charMap), right.copy(id, charMap));
            }
        } 
        record Snap (Node child) implements Node {
          @Override
          public Node copy(AtomicInteger id, Map<Integer, Character> charMap) {
              return new Snap(child.copy(id, charMap));
          }  
        } 
        record Optional (Node child) implements Node {
            @Override
            public Node copy(AtomicInteger id, Map<Integer, Character> charMap) {
                return new Optional(child.copy(id, charMap));
            }
        } 
        record EndNode (int id) implements Node {
            @Override
            public Node copy(AtomicInteger id, Map<Integer, Character> charMap) {
                return new EndNode(id.incrementAndGet());
            }
        }
        record NamedGroup (Node child, String name) implements Node {
            @Override
            public Node copy(AtomicInteger id, Map<Integer, Character> charMap) {
                return new NamedGroup(child.copy(id, charMap), name);
            }
        }
    }

    @Getter
    private Node root = null;

    private final Map<Node, Boolean> nullable = new HashMap<>();
    private final Map<Node, Set<Integer>> firstPos = new HashMap<>();
    private final Map<Node, Set<Integer>> lastPos = new HashMap<>();
    private final Map<Integer, Set<Integer>> followPos = new HashMap<>();

    public SyntaxTree(Node root) {
        this.root = root;
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

    public SyntaxTree(PreparedRegularStatement preparedRegularStatement) throws IllegalArgumentException {
        String expr = preparedRegularStatement.getRegularStatement();

        Deque<Node> nodeStack = new ArrayDeque<>();
        Deque<Character> charactersStack = new ArrayDeque<>();
        Stack<String> namedGroups = new Stack<>();

        AtomicInteger id = new AtomicInteger(0);
        for (int i = 0; i < expr.length(); ++i) {
            char c = expr.charAt(i);
            switch (c) {
                case '\\' -> {
                    nodeStack.push(new Node.Leaf(expr.charAt(++i), id.incrementAndGet()));
                    charMap.put(id.get(), expr.charAt(i));
                }
                case '*'  -> nodeStack.push(new Node.Snap(nodeStack.pop()));
                case '?'  -> nodeStack.push(new Node.Optional(nodeStack.pop()));
                case '|', '.' -> collapseStack(charactersStack, nodeStack, c);
                case '(' -> {
                    if (expr.charAt(i+1) == '<') {
                        i += 2;
                        StringBuilder name = new StringBuilder();
                        named = true;
                        char nameC = expr.charAt(i);
                        while (nameC != '>') {
                            name.append(c);
                            nameC = expr.charAt(++i);
                        }

                        namedGroups.push(name.toString());
                        charactersStack.push((char) 0);
                    }
                    charactersStack.push('(');
                }
                case ')' -> {
                    char op = charactersStack.pop();
                    while (op != '(') {
                        applyOp(nodeStack, op);
                        op = charactersStack.pop();
                    }
                    if (!charactersStack.isEmpty()) {
                        op = charactersStack.pop();
                        if (op != (char) 0) charactersStack.push(op);
                        else {
                            String name = namedGroups.pop();
                            nodeStack.push(new Node.NamedGroup(nodeStack.pop(), name));
                        }
                    }
                }

                case '{' -> {
                    StringBuilder cnt = new StringBuilder();
                    i++;

                    while (expr.charAt(i) != '}') {
                        cnt.append(expr.charAt(i++));
                    }

                    int n = Integer.parseInt(cnt.toString());
                    if (n == 1) continue;

                    Node original = nodeStack.pop();
                    Node current  = original;

                    while (--n > 0) {
                        current = new Node.And(current, original.copy(id, charMap));
                    }

                    nodeStack.push(current);
                }

                default -> {
                    nodeStack.push(new Node.Leaf(c, id.incrementAndGet()));
                    charMap.put(id.get(), c);
                }
            }
        }

        while (!charactersStack.isEmpty()) {
            applyOp(nodeStack, charactersStack.pop());
        }

        if (!named) {
            this.root = new Node.And(nodeStack.pop(), new Node.EndNode(id.incrementAndGet()));
            charMap.put(id.get(), (char) 0);
        } else {
            this.root = new Node.NamedGroup(nodeStack.pop(), "0");
        }

        if (!nodeStack.isEmpty()) throw new IllegalArgumentException("Bad regular expression");
    }

    private static String traverseRecursive(Node node) {
        return switch (node) {
            case Node.Or(Node left, Node right) -> "(" + traverseRecursive(left) + " or " + traverseRecursive(right) + ")";
            case Node.And(Node left, Node right) -> "(" + traverseRecursive(left) + " and " + traverseRecursive(right) + ")";
            case Node.Leaf(char c, int id) -> c + "(" + id + ")";
            case Node.Snap(Node child) -> "snap(" + traverseRecursive(child) + ")";
            case Node.Optional(Node child) -> "opt(" + traverseRecursive(child) + ")";
            case Node.NamedGroup(Node child, String name) -> "group<" + name + ">(" + traverseRecursive(child) + ")";
            case Node.EndNode(int id) -> "(end)";
        };
    }
    
    public String traverseTree() {
        if (root == null) return "";
        return traverseRecursive(root);
    }
}
