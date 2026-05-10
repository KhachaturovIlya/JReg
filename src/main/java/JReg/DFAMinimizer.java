package JReg;

import java.util.*;

public class DFAMinimizer {
    public DFA minimizeDFA(DFA dfa) {
        Set<Node> finalStates    = new HashSet<>();
        Set<Node> notFinalStates = new HashSet<>();
        Set<Character> alphabet  = dfa.getAlphabet();

        for (Node node : dfa.getGraph()) {
            if (dfa.getFinalNodes().contains(node))
                finalStates.add(node);
            else notFinalStates.add(node);
        }

        List<Set<Node>> partitions = new ArrayList<>();

        if (!notFinalStates.isEmpty()) {
            partitions.add(notFinalStates);
        }
        partitions.add(finalStates);

        boolean warm = true;
        while (warm) {
            warm = false; // If there is no more changes we need to stop.

            List<Set<Node>> nextPartitions = new ArrayList<>();
            Map<Node, Integer> nodesIndexes = new HashMap<>();

            int i = 0;
            for (Set<Node> partition : partitions) {
                for (Node node : partition) {
                    nodesIndexes.put(node, i);
                }
                i++;
            }

            for (Set<Node> partition : partitions) {
                Map<Map<Character, Integer>, Set<Node>> map = new HashMap<>();

                for (Node node : partition) {
                    Map<Character, Integer> signature = new HashMap<>();
                    for (char c : alphabet) {
                        Node target = null;
                        for (Edge edge : node.getEdges()) {
                            if (edge.transitionChars().contains(c)) {
                                target = edge.to();
                                break;
                            }
                        }

                        if (target != null) {
                            signature.put(c, nodesIndexes.get(target));
                        } else {
                            signature.put(c, -1);
                        }
                    }

                    map.computeIfAbsent(signature, k -> new HashSet<>()).add(node);
                }

                nextPartitions.addAll(map.values());
                if (map.size() >= 2) {
                    warm = true; // Still changing
                }
            }
            partitions = nextPartitions;
        }

        Map<Set<Node>, Node> groupToNewNode = new HashMap<>();
        Node newStart = null;
        Set<Node> newFinalNodes = new HashSet<>();
        List<Node> newGraph = new ArrayList<>();

        int newIdCounter = 0;

        for (Set<Node> partition : partitions) {
            boolean isFinalGroup = false;

            for (Node oldNode : partition) {
                if (dfa.getFinalNodes().contains(oldNode)) {
                    isFinalGroup = true;
                    break;
                }
            }

            Node newNode = new Node(newIdCounter++);
            groupToNewNode.put(partition, newNode);
            newGraph.add(newNode);

            if (isFinalGroup) {
                newFinalNodes.add(newNode);
            }

            if (partition.contains(dfa.getStartNode())) {
                newStart = newNode;
            }
        }

        for (Set<Node> partition : partitions) {
            Node sourceNewNode = groupToNewNode.get(partition);

            Node representative = partition.iterator().next();


            Map<Set<Node>, Set<Character>> transitionsToGroups = new HashMap<>();

            for (char c : alphabet) {

                Node targetOldNode = null;
                for (Edge edge : representative.getEdges()) {
                    if (edge.transitionChars().contains(c)) {
                        targetOldNode = edge.to();
                        break;
                    }
                }

                if (targetOldNode != null) {
                    Set<Node> targetPartition = findPartitionForNode(targetOldNode, partitions);

                    transitionsToGroups.computeIfAbsent(targetPartition, k -> new HashSet<>()).add(c);
                }
            }

            for (Map.Entry<Set<Node>, Set<Character>> entry : transitionsToGroups.entrySet()) {
                Set<Node> targetPartition = entry.getKey();
                Set<Character> symbols = entry.getValue();

                Node targetNewNode = groupToNewNode.get(targetPartition);

                sourceNewNode.getEdges().add(new Edge(new ArrayList<>(symbols), targetNewNode));
            }
        }

        return new DFA(newGraph, newStart, newFinalNodes, alphabet);
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
