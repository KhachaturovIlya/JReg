package JReg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SyntaxTreeTests {
    @Test
    public void testSingleAnd() {
        PreparedRegularStatement r = new PreparedRegularStatement("ab");
        SyntaxTree t = new SyntaxTree(r);

        assertEquals(
                "((a(1) and b(2)) and (end))",
                t.traverseTree()
        );
    }

    @Test
    public void testSingleOr() {
        PreparedRegularStatement r = new PreparedRegularStatement("a|b");
        SyntaxTree t = new SyntaxTree(r);

        assertEquals(
                "((a(1) or b(2)) and (end))",
                t.traverseTree()
        );
    }

    @Test
    public void testAndBeforeOr() {
        PreparedRegularStatement r = new PreparedRegularStatement("ab|c");
        SyntaxTree t = new SyntaxTree(r);

        assertEquals(
                "(((a(1) and b(2)) or c(3)) and (end))",
                t.traverseTree()
        );
    }

    @Test
    public void testOrInsideAnd() {
        PreparedRegularStatement r = new PreparedRegularStatement("a(b|c)");
        SyntaxTree t = new SyntaxTree(r);

        assertEquals(
                "((a(1) and (b(2) or c(3))) and (end))",
                t.traverseTree()
        );
    }

    @Test
    public void testLeftAssociativeAnd() {
        PreparedRegularStatement r = new PreparedRegularStatement("abc");
        SyntaxTree t = new SyntaxTree(r);

        assertEquals(
                "(((a(1) and b(2)) and c(3)) and (end))",
                t.traverseTree()
        );
    }

    @Test
    public void testOrChain() {
        PreparedRegularStatement r = new PreparedRegularStatement("a|b|c");
        SyntaxTree t = new SyntaxTree(r);

        assertEquals(
                "(((a(1) or b(2)) or c(3)) and (end))",
                t.traverseTree()
        );
    }

    @Test
    public void testGroupedExpression() {
        PreparedRegularStatement r = new PreparedRegularStatement("(ab|c)d");
        SyntaxTree t = new SyntaxTree(r);

        assertEquals(
                "((((a(1) and b(2)) or c(3)) and d(4)) and (end))",
                t.traverseTree()
        );
    }

    @Test
    public void testMegaExpression() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("(((ab|c)|d(dg)))");

        SyntaxTree t = new SyntaxTree(r);

        assertEquals(
                "((((a(1) and b(2)) or c(3)) or (d(4) and (d(5) and g(6)))) and (end))",
                t.traverseTree()
        );
    }

    @Test
    void testLeafSingle() {
        SyntaxTree t = new SyntaxTree(new PreparedRegularStatement("a"));
        assertEquals("(a(1) and (end))", t.traverseTree());
    }

    @Test
    void testOptionalSingle() {
        SyntaxTree t = new SyntaxTree(new PreparedRegularStatement("a?"));
        assertEquals("(opt(a(1)) and (end))", t.traverseTree());
    }

    @Test
    void testSnapSingle() {
        SyntaxTree t = new SyntaxTree(new PreparedRegularStatement("a..."));
        assertEquals("(snap(a(1)) and (end))", t.traverseTree());
    }

    @Test
    void testRepeatSingle() {
        SyntaxTree t = new SyntaxTree(new PreparedRegularStatement("a{3}"));
        assertEquals("(((a(1) and a(2)) and a(3)) and (end))", t.traverseTree());
    }

    @Test
    void testRepeatInsideOr() {
        SyntaxTree t = new SyntaxTree(new PreparedRegularStatement("a{2}|b"));
        assertEquals("(((a(1) and a(2)) or b(3)) and (end))", t.traverseTree());
    }

    @Test
    void testSnapChain() {
        PreparedRegularStatement reg = new PreparedRegularStatement("ab...");

        SyntaxTree t = new SyntaxTree(reg);
        assertEquals("((a(1) and snap(b(2))) and (end))", t.traverseTree());
    }

    @Test
    void testHardExpression() {
        SyntaxTree t = new SyntaxTree(
                new PreparedRegularStatement("(a|b)c?d{2}")
        );

        assertEquals(
                "((((a(1) or b(2)) and opt(c(3))) and (d(4) and d(5))) and (end))",
                t.traverseTree()
        );
    }

    @Test
    void testRepeatWithGroup() {
        SyntaxTree t = new SyntaxTree(new PreparedRegularStatement("(ab){2}"));
        assertEquals(
                "(((a(1) and b(2)) and (a(3) and b(4))) and (end))",
                t.traverseTree()
        );
    }

    @Test
    void testRepeatWithAlternation() {
        SyntaxTree t = new SyntaxTree(new PreparedRegularStatement("(a|b){2}"));
        assertEquals(
                "(((a(1) or b(2)) and (a(3) or b(4))) and (end))",
                t.traverseTree()
        );
    }

    @Test
    void testNestedRepeats() {
        SyntaxTree t = new SyntaxTree(new PreparedRegularStatement("(a{2}){2}"));
        assertEquals(
                "(((a(1) and a(2)) and (a(3) and a(4))) and (end))",
                t.traverseTree()
        );
    }

    @Test
    void testRepeatWithOptional() {
        SyntaxTree t = new SyntaxTree(new PreparedRegularStatement("(a?){2}"));
        assertEquals(
                "((opt(a(1)) and opt(a(2))) and (end))",
                t.traverseTree()
        );
    }
}
