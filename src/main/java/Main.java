import JReg.*;

import java.io.File;
import java.io.IOException;

public class Main {
    public void main() throws Exception {
        File outFile = new File("files/DFA.png");
        File minOutFile = new File("files/minDFA.png");

        DFAMinimizer dfaMinimizer = new DFAMinimizer();
        String reg = "(a...|aaa|ba...)q...q?";

        DFA dfa = new DFA(new LabeledSyntaxTree(new SyntaxTree(new PreparedRegularStatement(reg))));

        DFARenderer renderer = new DFARenderer(outFile);
        renderer.renderDFA(dfa);

        dfa = dfaMinimizer.minimizeDFA(dfa);
        renderer = new DFARenderer(minOutFile);
        renderer.renderDFA(dfa);
    }
}
