import JReg.AST.PreparedRegularStatement;
import JReg.AST.SyntaxTree;
import JReg.DFA.DFAMinimizer;
import JReg.NFA.NFA;
import JReg.NFA.NFARenderer;

import java.io.File;

public class Main {
    public void main() throws Exception {
        File outFile = new File("files/DFA.png");
        File minOutFile = new File("files/minDFA.png");

        File outNFAfile = new File("files/NFA.png");

        // DFAMinimizer dfaMinimizer = new DFAMinimizer();
        String reg = "x(<first>a|b)y";

        // DFA dfa = new DFA(new LabeledSyntaxTree(new SyntaxTree(new PreparedRegularStatement(reg))));

        /* DFARenderer renderer = new DFARenderer(outFile);
        renderer.renderDFA(dfa);

        dfa = dfaMinimizer.minimizeDFA(dfa);
        renderer = new DFARenderer(minOutFile);
        renderer.renderDFA(dfa); */

        NFA nfa = new NFA(new SyntaxTree(new PreparedRegularStatement(reg)));

        NFARenderer renderer = new NFARenderer(outNFAfile);
        renderer.renderNFA(nfa);
    }
}
