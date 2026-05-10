import JReg.*;

import java.io.File;
import java.io.IOException;

public class Main {
    public void main() throws Exception {
        File outFile = new File("files/DFA.png");

        String reg = "a(b|c)?d...";

        DFA dfa = new DFA(new LabeledSyntaxTree(new SyntaxTree(new PreparedRegularStatement(reg))));

        DFARenderer renderer = new DFARenderer(outFile);
        renderer.renderDFA(dfa);
    }
}
