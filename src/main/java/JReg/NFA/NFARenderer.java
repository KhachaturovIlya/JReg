package JReg.NFA;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;
import static guru.nidi.graphviz.model.Link.to;

public class NFARenderer {
    File targetFile;

    public NFARenderer(File targetFile) {
        this.targetFile = targetFile;
    }

    private MutableGraph g;
    private int id;
    private Map<NFA.NfaNode, MutableNode> visited;
    private NFA.NfaNode end;

    private void traverse(NFA.NfaNode node) {
        if (node == null) return;

        if (visited.containsKey(node)) return;

        int new_id = ++id;
        MutableNode source = mutNode(String.valueOf(new_id));
        if (node == end) source.add(Shape.DOUBLE_CIRCLE);
        else source.add(Shape.CIRCLE);

        visited.put(node, source);
        g.add(source);

        for (NFA.NfaNode nextNode : node.epsTransitions) {
            traverse(nextNode);
            MutableNode target = visited.get(nextNode);
            source.addLink(to(target).with(Label.of("ε")));
        }

        for (char c : node.transitions.keySet()) {
            NFA.NfaNode next = node.transitions.get(c);
            traverse(next);
            MutableNode target = visited.get(next);
            source.addLink(to(target).with(Label.of(String.valueOf(c))));
        }
    }

    public void renderNFA(NFA nfa) throws IOException {
        g = mutGraph("JReg/NFA").setDirected(true);
        g.graphAttrs().add("rankdir", "LR");

        id = 0;
        visited = new HashMap<>();
        end = nfa.getEnd();

        traverse(nfa.getStart());

        Graphviz.fromGraph(g).render(Format.PNG).toFile(targetFile);
    }
}
