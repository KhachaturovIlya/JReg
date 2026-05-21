package JReg.DFA;

import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;
import static guru.nidi.graphviz.model.Link.to;

@AllArgsConstructor
public class DFARenderer {
    File targetFile;

    public void renderDFA(DFA graph) throws IOException {
        MutableGraph g = mutGraph("JReg/NFA").setDirected(true);
        g.graphAttrs().add("rankdir", "LR");

        for (Node node : graph.getGraph()) {
            MutableNode source = mutNode(String.valueOf(node.getId()));

            if (graph.getFinalNodes().contains(node)) {
                source.add(Shape.DOUBLE_CIRCLE);
            } else {
                source.add(Shape.CIRCLE);
            }

            for (Edge edge : node.getEdges()) {
                MutableNode target = mutNode(String.valueOf(edge.to().getId()));

                if (graph.getFinalNodes().contains(edge.to())) {
                    target.add(Shape.DOUBLE_CIRCLE);
                } else {
                    target.add(Shape.CIRCLE);
                }

                StringBuilder transChars = new StringBuilder();
                boolean first = true;
                for (char c : edge.transitionChars()) {
                    if (!first) transChars.append(", ");
                    transChars.append(c);
                    first = false;
                }

                source.addLink(to(target).with(Label.of(transChars.toString())));
            }

            g.add(source);
        }

        Graphviz.fromGraph(g).render(Format.PNG).toFile(targetFile);
    }
}
