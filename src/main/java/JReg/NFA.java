package JReg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor @Getter
public class NFA {
    @NoArgsConstructor @Getter
    protected static class Node {
        final List<Node> epsTransitions = new ArrayList<>();
        final Map<Character, Node> transitions = new HashMap<>();
        final List<NamedGroup> groups = new ArrayList<>();
    }

    protected static record NamedGroup(
        int id,
        String name,
        StringBuilder resStr
    ) {}

    Node start;
    Node end;

    // List<NamedGroup> namedGroups = new ArrayList<>();

    private NFA buildLeafNFA(char c) {
        Node first  = new Node();
        Node second = new Node();

        first.transitions.put(c, second);
        return new NFA(first, second);
    }

    private NFA buildConcatNFA(NFA first, NFA second) {
        NFA newNfa = new NFA(first.getStart(), first.getEnd());

        newNfa.getEnd().getEpsTransitions().add(second.getStart());
        return newNfa;
    }

    /* private NFA buildKleeneNFA(NFA nfa) {
        Node first  = new Node();
        Node second = new Node();

        first.getEpsTransitions().add(nfa.getStart());
        first.getEpsTransitions().add(second);


    } */

    public NFA(SyntaxTree tree) {



    }
}
