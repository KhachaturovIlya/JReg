package JReg.DFA;

import java.util.*;

// TODO: testing and correct work
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

        while (!queue.isEmpty()) {
            NodePair pair = queue.poll();
            Node node = visited.get(pair);

            for (Edge firstEdge : pair.first.getEdges()) {
                for (Edge secondEdge : pair.second.getEdges()) {
                    Set<Character> intersection = new HashSet<>(firstEdge.transitionChars());
                    intersection.retainAll(secondEdge.transitionChars());

                    NodePair newPair = new NodePair(firstEdge.to(), secondEdge.to());
                    if (!intersection.isEmpty() && !visited.containsKey(newPair)) {
                        Node newNode = newDfa.addNode();
                        node.getEdges().add(new Edge(new ArrayList<>(intersection), newNode));
                        visited.put(newPair, newNode);
                        queue.add(newPair);
                    }
                }
            }
        }
        
        return newDfa;
    }

    static public DFA subtraction(DFA first, DFA second) {
        return intersection(first, addition(second));
    }
}