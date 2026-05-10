package JReg;

import lombok.Getter;

import java.util.*;

public class DFA {
    Node startNode;
    Set<Node> finalNodes = new HashSet<>();

    @Getter
    List<Node> graph = new ArrayList<>();

    public DFA(LabeledSyntaxTree tree) {
        int nodeID = 0;

        HashMap<Set<Integer>, Node> nodes = new HashMap<>();
        Queue<Set<Integer>> statesQueue = new ArrayDeque<>();

        Map<Integer, Set<Integer>> followPos = tree.getFollowPosTable();
        Map<Integer, Character> symbolMap = tree.getSyntaxTree().getCharMap();
        Set<Character> alphabet = new HashSet<>(symbolMap.values());
        alphabet.remove((char) 0);

        Set<Integer> state = tree.getFirstPos(tree.getSyntaxTree().getRoot());

        statesQueue.add(state);
        startNode = new Node(nodeID++);
        nodes.put(state, startNode);
        graph.add(startNode);

        while (!statesQueue.isEmpty()) {
            state = statesQueue.poll();
            Node nowNode = nodes.get(state);
            List<Edge> nowEdges = nowNode.getEdges();

            for (Integer id : state) {
                if (symbolMap.get(id) == (char) 0) {
                    finalNodes.add(nodes.get(state));
                    break;
                }
            }

            for (Character c : alphabet) {
                Set<Integer> newState = new HashSet<>();

                for (Integer id : state) {
                    if (symbolMap.get(id) == c) {
                        Set<Integer> follow = followPos.get(id);
                        if (follow != null) {
                            newState.addAll(follow);
                        }
                    }
                }

                if (!newState.isEmpty()) {
                    if (!nodes.containsKey(newState)) {
                        Node newNode = new Node(nodeID++);
                        nowEdges.add(new Edge(c, newNode));
                        nodes.put(newState, newNode);
                        graph.add(newNode);
                        statesQueue.add(newState);
                    } else {
                        Node newNode = nodes.get(newState);
                        nowEdges.add(new Edge(c, newNode));
                    }
                }
            }
        }
    }

    public boolean match(String word) {
        Node nowNode = startNode;

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);

            List<Edge> nowEdges = nowNode.getEdges();
            Node nextNode = null;
            for (Edge edge : nowEdges) {
                if (edge.transitionChar() == c) {
                    nextNode = edge.to();
                    break;
                }
            }

            if (nextNode == null) return false;

            nowNode = nextNode;
        }

        return finalNodes.contains(nowNode);
    }
}