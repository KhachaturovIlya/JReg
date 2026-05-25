package JReg.DFA;

import java.util.*;

// TODO: testing
public class DFACalc {
    static public DFA addition(DFA dfa) {
        DFA newDfa = new DFA(dfa);

        Set<Node> oldFinal = new HashSet<>(newDfa.getFinalNodes());
        newDfa.getFinalNodes().clear();
        for (Node node : newDfa.getGraph()) {
            if (!oldFinal.contains(node)) newDfa.getFinalNodes().add(node);
        }

        // Now need to add trash node

        Node newNode = newDfa.addNode();
        newDfa.getFinalNodes().add(newNode);

        for (Node node : newDfa.getGraph()) {
            Set<Character> unusedAlpha = new HashSet<>(newDfa.getAlphabet());

            for (Edge edge : node.getEdges()) {
                for (char c : edge.transitionChars()) {
                    unusedAlpha.remove(c);
                }
            }

            if (!unusedAlpha.isEmpty()) node.getEdges().add(new Edge(new ArrayList<>(unusedAlpha), newNode));
        }

        return newDfa;
    }

    protected record NodePair(
        Node first,
        Node second
    ) {}

    static public DFA intersection(DFA first, DFA second) {
        Map<NodePair, Node> visited = new HashMap<>();
        DFA newDfa = new DFA();

        NodePair nowPair = new NodePair(first.getStartNode(), second.getStartNode());
        Queue<NodePair> queue = new ArrayDeque<>();
        queue.add(nowPair);

        Node startNode = newDfa.addNode();
        visited.put(nowPair, startNode);

        if (first.getFinalNodes().contains(nowPair.first()) &&
                second.getFinalNodes().contains(nowPair.second())) {
            newDfa.getFinalNodes().add(startNode);
        }

        while (!queue.isEmpty()) {
            NodePair pair = queue.poll();
            Node node = visited.get(pair);

            for (Edge firstEdge : pair.first().getEdges()) {
                for (Edge secondEdge : pair.second().getEdges()) {
                    Set<Character> intersection = new HashSet<>(firstEdge.transitionChars());
                    intersection.retainAll(secondEdge.transitionChars());

                    if (!intersection.isEmpty()) {
                        NodePair newPair = new NodePair(firstEdge.to(), secondEdge.to());
                        Node targetNode;

                        if (!visited.containsKey(newPair)) {
                            targetNode = newDfa.addNode();
                            visited.put(newPair, targetNode);
                            queue.add(newPair);

                            if (first.getFinalNodes().contains(newPair.first()) &&
                                    second.getFinalNodes().contains(newPair.second())) {
                                newDfa.getFinalNodes().add(targetNode);
                            }
                        } else {
                            targetNode = visited.get(newPair);
                        }

                        node.getEdges().add(new Edge(new ArrayList<>(intersection), targetNode));
                    }
                }
            }
        }

        return newDfa;
    }

    static public DFA subtraction(DFA first, DFA second) {
        return intersection(first, addition(second));
    }
    
    public static boolean isEqual(DFA a, DFA b) {
        Set<NodePair> visited = new HashSet<>();
        Queue<NodePair> queue = new ArrayDeque<>();

        NodePair start = new NodePair(a.getStartNode(), b.getStartNode());
        queue.add(start);
        visited.add(start);
        
        Set<Character> alphabet = new HashSet<>(a.getAlphabet());
        alphabet.addAll(b.getAlphabet());

        while (!queue.isEmpty()) {
            NodePair pair = queue.poll();
            
            boolean isFirstFinal = a.getFinalNodes().contains(pair.first());
            boolean isSecondFinal = b.getFinalNodes().contains(pair.second());
            if (isFirstFinal != isSecondFinal) {
                return false;
            }
            
            for (char c : alphabet) {
                Node nextA = getTransition(pair.first(), c);
                Node nextB = getTransition(pair.second(), c);

                if (nextB == null && nextA == null) {
                    continue;
                } else if (nextB == null || nextA == null) {
                    return false;
                }

                NodePair nextPair = new NodePair(nextA, nextB);
                if (!visited.contains(nextPair)) {
                    visited.add(nextPair);
                    queue.add(nextPair);
                }
            }
        }
        return true;
    }
    
    private static Node getTransition(Node node, char c) {
        if (node == null) return null;
        for (Edge edge : node.getEdges()) {
            if (edge.transitionChars().contains(c)) {
                return edge.to();
            }
        }
        return null;
    }
}