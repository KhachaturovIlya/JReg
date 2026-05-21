package JReg.DFA;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DFACalc {
    static DFA addition(DFA dfa) {
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
}
