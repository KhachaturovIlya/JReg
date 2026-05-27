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


    /* public DFA minimizeDFA(DFA dfa) {
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
    } */

    private Set<Node> findPartitionForNode(Node target, List<Set<Node>> partitions) {
        for (Set<Node> partition : partitions) {
            if (partition.contains(target)) {
                return partition;
            }
        }
        throw new IllegalStateException("Critical error...");
    }
}
