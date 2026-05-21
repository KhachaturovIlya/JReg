package JReg.DFA;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Node {
    final private int id;
    final private List<Edge> edges = new ArrayList<>();

    public Node(int id) {
        this.id = id;
    }
}
