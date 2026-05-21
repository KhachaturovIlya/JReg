package JReg.NFA;

import JReg.AST.SyntaxTree;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@AllArgsConstructor @NoArgsConstructor @Getter
public class NFA {
    @NoArgsConstructor
    protected static class NfaNode {
        protected final List<NfaNode> epsTransitions = new ArrayList<>();
        protected final Map<Character, NfaNode> transitions = new HashMap<>();

        protected Set<NfaNode> buildEpsilonClosure() {
            Set<NfaNode> closure = new HashSet<>();
            Deque<NfaNode> stack = new ArrayDeque<>();
            stack.push(this);
            closure.add(this);

            while (!stack.isEmpty()) {
                NfaNode node = stack.pop();

                for (NfaNode next : node.epsTransitions) {
                    if (closure.add(next)) {
                        stack.push(next);
                    }
                }
            }

            return closure;
        }
    }

    @AllArgsConstructor
    protected static class NfaNamedGroup extends NfaNode {
        protected boolean start;
        String name;
    }


    private NfaNode start = null;
    private NfaNode end = null;

    @Getter
    private final Map<String, String> namedGroupsMap = new HashMap<>();

    static private NFA buildLeafNFA(char c) {
        NfaNode first = new NfaNode();
        NfaNode second = new NfaNode();

        first.transitions.put(c, second);
        return new NFA(first, second);
    }

    static private NFA buildAndNFA(NFA first, NFA second) {
        NFA newNfa = new NFA(first.start, first.end);

        newNfa.end.epsTransitions.add(second.start);
        newNfa.end = second.end;
        return newNfa;
    }

    static private NFA buildSnapNFA(NFA nfa) {
        NfaNode first = new NfaNode();
        NfaNode second = new NfaNode();

        first.epsTransitions.add(nfa.start);
        first.epsTransitions.add(second);

        nfa.end.epsTransitions.add(second);
        nfa.end.epsTransitions.add(nfa.start);

        return new NFA(first, second);
    }

    static private NFA buildOrNFA(NFA nfa1, NFA nfa2) {
        NfaNode first = new NfaNode();
        NfaNode second = new NfaNode();

        first.epsTransitions.add(nfa1.start);
        nfa1.end.epsTransitions.add(second);

        first.epsTransitions.add(nfa2.start);
        nfa2.end.epsTransitions.add(second);

        return new NFA(first, second);
    }

    static private NFA buildOptionalNFA(NFA nfa) {
        NfaNode first = new NfaNode();
        NfaNode second = new NfaNode();

        first.epsTransitions.add(nfa.start);
        first.epsTransitions.add(second);
        nfa.end.epsTransitions.add(second);

        return new NFA(first, second);
    }

    static private NFA buildNamedGroup(NFA nfa, String name) {
        NfaNode first = new NfaNamedGroup(true, name);
        NfaNode second = new NfaNamedGroup(false, name);

        first.epsTransitions.add(nfa.start);
        nfa.end.epsTransitions.add(second);
        return new NFA(first, second);
    }

    static private NFA buildNfaFromNode(SyntaxTree.Node node) {
        if (node == null) return new NFA();

        switch (node) {
            case SyntaxTree.Node.EndNode _ -> throw new IllegalArgumentException("End node not allowed in NFA");
            case SyntaxTree.Node.And(SyntaxTree.Node left, SyntaxTree.Node right) -> {
                return buildAndNFA(buildNfaFromNode(left), buildNfaFromNode(right));
            }
            case SyntaxTree.Node.Or(SyntaxTree.Node left, SyntaxTree.Node right) -> {
                return buildOrNFA(buildNfaFromNode(left), buildNfaFromNode(right));
            }
            case SyntaxTree.Node.Leaf(char c, int id) -> {
                return buildLeafNFA(c);
            }
            case SyntaxTree.Node.Snap(SyntaxTree.Node child) -> {
                return buildSnapNFA(buildNfaFromNode(child));
            }
            case SyntaxTree.Node.Optional(SyntaxTree.Node child) -> {
                return buildOptionalNFA(buildNfaFromNode(child));
            }
            case SyntaxTree.Node.NamedGroup(SyntaxTree.Node child, String name) -> {
                return buildNamedGroup(buildNfaFromNode(child), name);
            }
        }
    }

    public NFA(SyntaxTree tree) {
        if (tree == null) return;

        NFA nfa = buildNfaFromNode(tree.getRoot());
        this.start = nfa.start;
        this.end = nfa.end;
    }
}