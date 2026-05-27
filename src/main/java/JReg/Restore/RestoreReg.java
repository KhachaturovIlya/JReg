package JReg.Restore;

import JReg.DFA.DFA;
import JReg.DFA.Edge;
import JReg.DFA.Node;

import java.util.*;

public class RestoreReg {
    protected record StringTransition(
            String str,
            Node node
    ) {}


    public static String restore(DFA dfa) {
        Map<Node, List<StringTransition>> in = new HashMap<>();
        Map<Node, List<StringTransition>> out = new HashMap<>();
        Map<Node, String> loops = new HashMap<>();

        for (Node node : dfa.getGraph()) {
            List<StringTransition> outTransitions = new ArrayList<>();

            for (Edge edge : node.getEdges()) {
                StringBuilder simpleStrBuilder = new StringBuilder();
                boolean first = true;
                for (char  c : edge.transitionChars()) {
                    if (!first) {
                        simpleStrBuilder.append("|");
                    }
                    simpleStrBuilder.append(c);
                    first = false;
                }
                String simpleStr = simpleStrBuilder.toString();

                if (edge.to() == node) {
                    loops.put(node, simpleStr);
                    continue;
                }

                List<StringTransition> transition = in.computeIfAbsent(edge.to(), k -> new ArrayList<>());

                transition.add(new StringTransition(simpleStr, node));

                outTransitions.add(new StringTransition(simpleStr, edge.to()));
            }

            out.put(node, outTransitions);
        }

        // Finish map populating now can start algorithm work...
        // Need to add end, start nodes...

        Node start = new Node(dfa.getNodeID() + 1);
        List<StringTransition> startTransitions =  new ArrayList<>();
        startTransitions.add(new StringTransition("", dfa.getStartNode()));
        out.put(start, startTransitions);
        List<StringTransition> firstNodeTransition = in.computeIfAbsent(dfa.getStartNode(), k -> new ArrayList<>());
        firstNodeTransition.add(new StringTransition("", start));

        Node end   = new Node(dfa.getNodeID() + 2);
        List<StringTransition> endTransitions = new ArrayList<>();
        for (Node node : dfa.getFinalNodes()) {
            endTransitions.add(new StringTransition("", node));
            List<StringTransition> finalNodeTransitions = out.computeIfAbsent(node, k -> new ArrayList<>());
            finalNodeTransitions.add(new StringTransition("", end));
        }
        in.put(end, endTransitions);

        // Now can start our algo

        List<Node> nodes = new ArrayList<>(dfa.getGraph());
        nodes.remove(start);
        nodes.remove(end);

        for (Node algoNode : nodes) {
            if (in.get(algoNode) == null || out.get(algoNode) == null) continue;

            List<StringTransition> inTransitions  = new ArrayList<>(in.get(algoNode));
            List<StringTransition> outTransitions = new ArrayList<>(out.get(algoNode));

            for (StringTransition inTransition : inTransitions) {
                for (StringTransition outTransition : outTransitions) {
                    Node startNode = inTransition.node;
                    Node endNode   = outTransition.node;

                    String R1 = inTransition.str;
                    String R2 = loops.get(algoNode);
                    String R3 = outTransition.str;
                    String R4 = null;

                    // Trying to find shortcut

                    for (StringTransition inEndTransition : in.get(endNode)) {
                        if (inEndTransition.node == startNode) {
                            R4 = inEndTransition.str;
                            break;
                        }
                    }

                    StringBuilder res = new StringBuilder();
                    res.append('(');
                    if (R4 != null) {
                        res.append(R4);
                        res.append("|(");
                    }
                    res.append(R1);
                    if (R2 != null) {
                        res.append('(');
                        res.append(R2);
                        res.append(')');
                        res.append("...");
                    }
                    res.append(R3);
                    if (R4 != null) {
                        res.append(")");
                    }
                    res.append(')');

                    List<StringTransition> fromStart = out.get(startNode);
                    fromStart.removeIf(transition -> transition.node == endNode);

                    List<StringTransition> toEnd = in.get(endNode);
                    toEnd.removeIf(transition -> transition.node == startNode);

                    if (startNode == endNode) {
                        String loop = loops.get(startNode);
                        if (loop != null) {
                            loops.put(startNode, loop + "|" + res.toString());
                        }
                    } else {
                        fromStart.add(new StringTransition(res.toString(), endNode));
                        toEnd.add(new StringTransition(res.toString(), startNode));
                    }
                }
            }

            for (List<StringTransition> transitions : in.values()) {
                transitions.removeIf(s -> {
                    return s.node == algoNode;
                });
            }

            for (List<StringTransition> transitions : out.values()) {
                transitions.removeIf(s -> {
                    return s.node == algoNode;
                });
            }

            in.remove(algoNode);
            out.remove(algoNode);
        }

        for (StringTransition transition : in.get(end)) {
            if (transition.node == start) {
                return transition.str;
            }
        }
        return null;
    }
}
