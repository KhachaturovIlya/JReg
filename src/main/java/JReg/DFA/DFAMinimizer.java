package JReg.DFA;

import java.util.*;

public class DFAMinimizer {
    protected record Signature(
            int groupId,
            Map<Character, Integer>  transitions
    ) {}

    public DFA minimize(DFA dfa) {
        // Need to collect dead nodes
        Set<Node> visited = new HashSet<>();
        Queue<Node> nodeQueue = new ArrayDeque<>();
        visited.add(dfa.getStartNode());
        nodeQueue.add(dfa.getStartNode());

        while (!nodeQueue.isEmpty()) {
            Node node = nodeQueue.poll();
            for (Edge edge : node.getEdges()) {
                if (!visited.contains(edge.to())) {
                    visited.add(edge.to());
                    nodeQueue.add(edge.to());
                }
            }
        }

        // Populating first 2 groups
        Map<Node, Integer> groupId = new HashMap<>();
        boolean hasNonFinal = false;
        boolean hasFinal = false;

        for (Node node : visited) {
            if (dfa.getFinalNodes().contains(node)) {
                groupId.put(node, 1);
                hasFinal = true;
            } else {
                groupId.put(node, 0);
                hasNonFinal = true;
            }
        }

        int currentGroupCount;
        if (!hasNonFinal || !hasFinal) {
            for (Node node : visited) groupId.put(node, 0);
            currentGroupCount = 1;
        } else {
            currentGroupCount = 2;
        }

        Set<Character> alphabet = dfa.getAlphabet();
        boolean warm = true;

        // Working while group separation still working
        while (warm) {
            Map<Signature, List<Node>> groups = new HashMap<>();

            for (Node node : visited) {
                Map<Character, Integer> transitions = new HashMap<>();
                for (char c : alphabet) {
                    int targetGroup = -1;
                    for (Edge edge : node.getEdges()) {
                        if (edge.transitionChars().contains(c) && visited.contains(edge.to())) {
                            targetGroup = groupId.get(edge.to());
                            break;
                        }
                    }
                    transitions.put(c, targetGroup);
                }

                Signature sig = new Signature(groupId.get(node), transitions);
                groups.computeIfAbsent(sig, k -> new ArrayList<>()).add(node);
            }

            if (groups.size() == currentGroupCount) {
                warm = false;
            } else {
                currentGroupCount = groups.size();
                Map<Node, Integer> nextGroupId = new HashMap<>();
                int newId = 0;

                for (List<Node> groupNodes : groups.values()) {
                    for (Node node : groupNodes) {
                        nextGroupId.put(node, newId);
                    }
                    newId++;
                }
                groupId = nextGroupId;
            }
        }

        // Finish group separating, now can build new DFA
        List<Node> newGraph = new ArrayList<>();
        Map<Integer, Node> groupToNewNode = new HashMap<>();

        for (int i = 0; i < currentGroupCount; i++) {
            Node newNode = new Node(i);
            groupToNewNode.put(i, newNode);
            newGraph.add(newNode);
        }

        Node newStart = groupToNewNode.get(groupId.get(dfa.getStartNode()));
        Set<Node> newFinalNodes = new HashSet<>();

        Map<Integer, Node> representatives = new HashMap<>();
        for (Map.Entry<Node, Integer> entry : groupId.entrySet()) {
            representatives.putIfAbsent(entry.getValue(), entry.getKey());
        }

        for (int i = 0; i < currentGroupCount; i++) {
            Node oldRep = representatives.get(i);
            Node newSource = groupToNewNode.get(i);

            if (dfa.getFinalNodes().contains(oldRep)) {
                newFinalNodes.add(newSource);
            }

            Map<Integer, List<Character>> newEdgesMap = new HashMap<>();

            for (char c : alphabet) {
                for (Edge edge : oldRep.getEdges()) {
                    if (edge.transitionChars().contains(c) && visited.contains(edge.to())) {
                        int targetGroup = groupId.get(edge.to());
                        newEdgesMap.computeIfAbsent(targetGroup, k -> new ArrayList<>()).add(c);
                        break;
                    }
                }
            }

            for (Map.Entry<Integer, List<Character>> entry : newEdgesMap.entrySet()) {
                newSource.getEdges().add(new Edge(entry.getValue(), groupToNewNode.get(entry.getKey())));
            }
        }

        return new DFA(newGraph, newStart, newFinalNodes, new HashSet<>(alphabet), currentGroupCount);
    }

    private Set<Node> findPartitionForNode(Node target, List<Set<Node>> partitions) {
        for (Set<Node> partition : partitions) {
            if (partition.contains(target)) {
                return partition;
            }
        }
        throw new IllegalStateException("Critical error...");
    }
}
