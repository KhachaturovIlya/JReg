package JReg;

import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LabeledSyntaxTree {
    @Getter
    private final SyntaxTree syntaxTree;

    private final Map<SyntaxTree.Node, Set<Integer>> firstPos  = new HashMap<>();
    private final Map<SyntaxTree.Node, Set<Integer>> lastPos   = new HashMap<>();
    private final Map<SyntaxTree.Node, Boolean>      nullable  = new HashMap<>();
    private final Map<Integer, Set<Integer>>         followPos = new HashMap<>();

    public Map<Integer, Set<Integer>> getFollowPosTable() {
        return followPos;
    }

    private void labelNullable(SyntaxTree.Node node) {
        switch (node) {
            case SyntaxTree.Node.And(SyntaxTree.Node left, SyntaxTree.Node right) -> {
                labelNullable(left);
                labelNullable(right);
                nullable.put(node, nullable.get(left) && nullable.get(right));
            }
            case SyntaxTree.Node.Or(SyntaxTree.Node left, SyntaxTree.Node right) -> {
                labelNullable(left);
                labelNullable(right);
                nullable.put(node, nullable.get(left) || nullable.get(right));
            }
            case SyntaxTree.Node.Leaf(char c, int id) -> nullable.put(node, false);
            case SyntaxTree.Node.Optional(SyntaxTree.Node child) -> {
                labelNullable(child);
                nullable.put(node, true);
            }
            case SyntaxTree.Node.Snap(SyntaxTree.Node child) -> {
                labelNullable(child);
                nullable.put(node, true);
            }
            case SyntaxTree.Node.EndNode(int id) -> nullable.put(node, false);
        }
    }
    
    private void labelFirstPos(SyntaxTree.Node node) {
        switch (node) {
            case SyntaxTree.Node.And(SyntaxTree.Node left, SyntaxTree.Node right) -> {
                labelFirstPos(left);
                labelFirstPos(right);

                Set<Integer> newFirstPos = new HashSet<>();

                if (nullable.get(left)) {
                    newFirstPos.addAll(firstPos.get(left));
                    newFirstPos.addAll(firstPos.get(right));
                } else {
                    newFirstPos = new HashSet<>(firstPos.get(left));
                }

                firstPos.put(node, newFirstPos);
            }
            case SyntaxTree.Node.Or(SyntaxTree.Node left, SyntaxTree.Node right) -> {
                labelFirstPos(left);
                labelFirstPos(right);

                Set<Integer> newFirstPos = new HashSet<>(firstPos.get(left));
                newFirstPos.addAll(firstPos.get(right));
                firstPos.put(node, newFirstPos);
            }
            case SyntaxTree.Node.Leaf(char c, int id) -> {
                Set<Integer> newFirstPos = new HashSet<>();
                newFirstPos.add(id);

                firstPos.put(node, newFirstPos);
            }
            case SyntaxTree.Node.Optional(SyntaxTree.Node child) -> {
                labelFirstPos(child);
                firstPos.put(node, new HashSet<>(firstPos.get(child)));
            }
            case SyntaxTree.Node.Snap(SyntaxTree.Node child) -> {
                labelFirstPos(child);
                firstPos.put(node, new HashSet<>(firstPos.get(child)));
            }
            case SyntaxTree.Node.EndNode(int id) -> {
                Set<Integer> newFirstPos = new HashSet<>();
                newFirstPos.add(id);

                firstPos.put(node, newFirstPos);
            }
        }
    }
    
    private void labelLastPos(SyntaxTree.Node node) {
        switch (node) {
            case SyntaxTree.Node.And(SyntaxTree.Node left, SyntaxTree.Node right) -> {
                labelLastPos(left);
                labelLastPos(right);

                Set<Integer> newLastPos = new HashSet<>();

                if (nullable.get(right)) {
                    newLastPos.addAll(lastPos.get(left));
                    newLastPos.addAll(lastPos.get(right));
                } else {
                    newLastPos = new HashSet<>(lastPos.get(right));
                }

                lastPos.put(node, newLastPos);
            }
            case SyntaxTree.Node.Or(SyntaxTree.Node left, SyntaxTree.Node right) -> {
                labelLastPos(left);
                labelLastPos(right);

                Set<Integer> newLastPos = new HashSet<>(lastPos.get(left));
                newLastPos.addAll(lastPos.get(right));
                lastPos.put(node, newLastPos);
            }
            case SyntaxTree.Node.Leaf(char c, int id) -> {
                Set<Integer> newLastPos = new HashSet<>();
                newLastPos.add(id);

                lastPos.put(node, newLastPos);
            }
            case SyntaxTree.Node.Optional(SyntaxTree.Node child) -> {
                labelLastPos(child);
                lastPos.put(node, new HashSet<>(lastPos.get(child)));
            }
            case SyntaxTree.Node.Snap(SyntaxTree.Node child) -> {
                labelLastPos(child);
                lastPos.put(node, new HashSet<>(lastPos.get(child)));
            }
            case SyntaxTree.Node.EndNode(int id) -> {
                Set<Integer> newLastPos = new HashSet<>();
                newLastPos.add(id);

                lastPos.put(node, newLastPos);
            }
        }
    }
    
    private void labelFollowPos(SyntaxTree.Node node) {
        switch (node) {
            case SyntaxTree.Node.And(SyntaxTree.Node left, SyntaxTree.Node right) -> {
                labelFollowPos(left);
                labelFollowPos(right);

                for (int i : lastPos.get(left)) {
                    followPos.computeIfAbsent(i, k -> new HashSet<>()).addAll(firstPos.get(right));
                }
            }
            case SyntaxTree.Node.Or(SyntaxTree.Node left, SyntaxTree.Node right) -> {
                labelFollowPos(left);
                labelFollowPos(right);
            }
            case SyntaxTree.Node.Leaf(char c, int id) -> {}
            case SyntaxTree.Node.Optional(SyntaxTree.Node child) -> {
                labelFollowPos(child);
            }
            case SyntaxTree.Node.Snap(SyntaxTree.Node child) -> {
                labelFollowPos(child);

                for (int i : lastPos.get(child)) {
                    followPos.computeIfAbsent(i, k -> new HashSet<>()).addAll(firstPos.get(child));
                }
            }
            case SyntaxTree.Node.EndNode(int id) -> {}
        }
    }

    public Set<Integer> getFollowPos(int id) {
        return followPos.get(id);
    }
    public Set<Integer> getFirstPos(SyntaxTree.Node node) {
        return firstPos.get(node);
    }

    LabeledSyntaxTree(SyntaxTree syntaxTree) {
        this.syntaxTree = syntaxTree;
        
        labelNullable(syntaxTree.getRoot());
        labelFirstPos(syntaxTree.getRoot());
        labelLastPos(syntaxTree.getRoot());
        labelFollowPos(syntaxTree.getRoot());
    }
}