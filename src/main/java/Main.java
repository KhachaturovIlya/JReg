import JReg.AST.LabeledSyntaxTree;
import JReg.AST.PreparedRegularStatement;
import JReg.AST.SyntaxTree;
import JReg.DFA.DFA;
import JReg.DFA.DFACalc;
import JReg.DFA.DFAMinimizer;
import JReg.DFA.DFARenderer;
import JReg.NFA.NFA;
import JReg.NFA.NFARenderer;
import JReg.Restore.RestoreReg;

import java.io.File;

public class Main {
    public void main() throws Exception {
        // File outFile = new File("files/DFA.png");
        // File outFileRestored = new File("files/restoredDFA.png");
        // File minOutFile = new File("files/minDFA.png");
        File outNFAfile = new File("files/NFA.png");

        /* DFAMinimizer dfaMinimizer = new DFAMinimizer();
        String reg1 = "a(bc...|d)...e";
        String reg2 = "abcdef";

        DFA dfa1 = new DFA(new LabeledSyntaxTree(new SyntaxTree(new PreparedRegularStatement(reg1))));

        DFA subDfa = DFACalc.subtraction(dfa1, dfa2);

        System.out.println(DFACalc.isEqual(DFACalc.addition(DFACalc.addition(dfa1)), dfa2));

        DFARenderer renderer = new DFARenderer(outFile);
        renderer.renderDFA(dfa1);

        DFA dfa2 = dfaMinimizer.minimize(new DFA(
                new LabeledSyntaxTree(
                        new SyntaxTree(
                                new PreparedRegularStatement(RestoreReg.restore(dfa1))))));

        DFARenderer rendererRestored = new DFARenderer(outFileRestored);
        rendererRestored.renderDFA(dfa2);


        System.out.println(RestoreReg.restore(dfa1));

        dfa = dfaMinimizer.minimizeDFA(dfa);
        renderer = new DFARenderer(minOutFile);
        renderer.renderDFA(dfa); */

        String reg = "(<data>(1|2|3|4|5|6|7|8|9|0){2}\\.(1|2|3|4|5|6|7|8|9|0){2})";

        NFA nfa = new NFA(new SyntaxTree(new PreparedRegularStatement(reg)));

        System.out.println(nfa.match("12.05"));

        NFARenderer renderer = new NFARenderer(outNFAfile);
        renderer.renderNFA(nfa);
    }
}
