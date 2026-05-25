import JReg.AST.LabeledSyntaxTree;
import JReg.AST.PreparedRegularStatement;
import JReg.AST.SyntaxTree;
import JReg.DFA.DFA;
import JReg.DFA.DFACalc;
import JReg.DFA.DFAMinimizer;
import JReg.DFA.DFARenderer;
import JReg.NFA.NFA;
import JReg.NFA.NFARenderer;

import java.io.File;

public class Main {
    public void main() throws Exception {
        File outFile = new File("files/DFA.png");
        File minOutFile = new File("files/minDFA.png");

        File outNFAfile = new File("files/NFA.png");

        // DFAMinimizer dfaMinimizer = new DFAMinimizer();
        String reg1 = "(a|b)...";
        String reg2 = "(ab)...";

        DFA dfa1 = new DFA(new LabeledSyntaxTree(new SyntaxTree(new PreparedRegularStatement(reg1))));
        DFA dfa2 = new DFA(new LabeledSyntaxTree(new SyntaxTree(new PreparedRegularStatement(reg2))));

        DFA subDfa = DFACalc.subtraction(dfa1, dfa2);

        DFARenderer renderer = new DFARenderer(outFile);
        renderer.renderDFA(subDfa);
        System.out.println(DFACalc.isEqual(DFACalc.addition(DFACalc.addition(dfa1)), dfa2));

        /*
        dfa = dfaMinimizer.minimizeDFA(dfa);
        renderer = new DFARenderer(minOutFile);
        renderer.renderDFA(dfa); */

        // NFA nfa = new NFA(new SyntaxTree(new PreparedRegularStatement(reg)));

        // NFARenderer renderer = new NFARenderer(outNFAfile);
        // renderer.renderNFA(nfa);
    }
}
