import JReg.SyntaxTree;

import java.io.IOException;

public class Main {
    public void main() throws Exception {
        SyntaxTree tree = SyntaxTree.compile("((a?|b...){2}|(c|d?)*)...e?\\*");
    }
}
