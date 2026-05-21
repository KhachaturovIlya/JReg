package JReg;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import JReg.AST.PreparedRegularStatement;

public class PreparedRegularTests {
    @Test
    public void testEmptyRegular() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("");
        });
    }

    @Test
    public void testSpaceRegular() {
        PreparedRegularStatement r = new PreparedRegularStatement(" ");
        assertEquals(" ", r.getRegularStatement());
    }

    @Test
    public void testSingleChar() {
        PreparedRegularStatement r = new PreparedRegularStatement("a");
        assertEquals("a", r.getRegularStatement());
    }

    @Test
    public void testTwoCharsConcat() {
        PreparedRegularStatement r = new PreparedRegularStatement("ab");
        assertEquals("a.b", r.getRegularStatement());
    }

    @Test
    public void testOnlyEscape() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("\\");
        });
    }

    @Test
    public void testEscapedChar() {
        PreparedRegularStatement r = new PreparedRegularStatement("\\a");
        assertEquals("\\a", r.getRegularStatement());
    }

    @Test
    public void testTripleDot() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("...");
        });
    }

    @Test
    public void testEscapedDot() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement(".");
        });
    }

    @Test
    public void testEscapedRightDot() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("a.");
        });
    }

    @Test
    public void testCurlyRepeat() {
        PreparedRegularStatement r = new PreparedRegularStatement("a{3}");
        assertEquals("a{3}", r.getRegularStatement());
    }

    @Test
    public void testCurlyInvalidChar() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("a{a}");
        });
    }

    @Test
    public void testCurlyUnclosed() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("a{3");
        });
    }

    @Test
    public void testCurlyZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("a{0}");
        });
    }

    @Test
    public void testCurlyNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("a{-1}");
        });
    }

    @Test
    public void testParentheses() {
        PreparedRegularStatement r = new PreparedRegularStatement("(a)");
        assertEquals("(a)", r.getRegularStatement());
    }

    @Test
    public void testUnclosedParentheses() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("(a");
        });
    }

    @Test
    public void testExtraClosingParentheses() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("a)");
        });
    }

    @Test
    public void testStarEscaped() {
        PreparedRegularStatement r = new PreparedRegularStatement("*");
        assertEquals("\\*", r.getRegularStatement());
    }

    @Test
    public void testComplex1() {
        PreparedRegularStatement r = new PreparedRegularStatement("a(b|c)");
        assertEquals("a.(b|c)", r.getRegularStatement());
    }

    @Test
    public void testComplex2() {
        PreparedRegularStatement r = new PreparedRegularStatement("(a|b)c");
        assertEquals("(a|b).c", r.getRegularStatement());
    }

    @Test
    public void testCopyCurly() {
        PreparedRegularStatement r = new PreparedRegularStatement("(ab){2}");
        assertEquals("(a.b){2}", r.getRegularStatement());
    }

    @Test
    public void testSingleCopyCurly() {
        PreparedRegularStatement r = new PreparedRegularStatement("ab{2}");
        assertEquals("a.b{2}", r.getRegularStatement());
    }

    @Test
    public void testDeepNesting() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("((((a))))");
        assertEquals("((((a))))", r.getRegularStatement());
    }

    @Test
    public void testAlternationComplex() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("(a|b)(c|d)");
        assertEquals("(a|b).(c|d)", r.getRegularStatement());
    }

    @Test
    public void testNestedAlternation() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("((a|b)|c)");
        assertEquals("((a|b)|c)", r.getRegularStatement());
    }

    @Test
    public void testOptionalChains() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("a?b?c?");
        assertEquals("a?.b?.c?", r.getRegularStatement());
    }

    @Test
    public void testMixedOperators() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("(a|b)c?d");
        assertEquals("(a|b).c?.d", r.getRegularStatement());
    }

    @Test
    public void testLongLinear() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("abcdefg");
        assertEquals("a.b.c.d.e.f.g", r.getRegularStatement());
    }

    @Test
    public void testCurlyComplex1() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("a{3}b{2}");
        assertEquals("a{3}.b{2}", r.getRegularStatement());
    }

    @Test
    public void testCurlyWithGroup() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("(ab){3}");
        assertEquals("(a.b){3}", r.getRegularStatement());
    }

    @Test
    public void testChaos1() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("a(b|c{2})d?");
        assertEquals("a.(b|c{2}).d?", r.getRegularStatement());
    }

    @Test
    public void testChaos2() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("(a|b{2}(c|d))e");
        assertEquals("(a|b{2}.(c|d)).e", r.getRegularStatement());
    }

    @Test
    public void testDeepCurlyCombo() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("((ab){2}c){2}");
        assertEquals("((a.b){2}.c){2}", r.getRegularStatement());
    }

    @Test
    public void testChaos3() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("(a|b{2})c(d|e{3})f?");
        assertEquals(
                "(a|b{2}).c.(d|e{3}).f?",
                r.getRegularStatement()
        );
    }

    @Test
    public void testCurlyOne() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("a{1}");
        assertEquals("a{1}", r.getRegularStatement());
    }

    @Test
    public void testWhitespace() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("a b");
        assertEquals("a. .b",  r.getRegularStatement());
    }

    @Test
    public void testCurlyWithoutOperand() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("{2}");
        });
    }

    @Test
    public void testDoubleOperators() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("a||b");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("a..b");
        });
    }

    @Test
    public void testStartsWithOperator() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("|a");
        });
    }

    @Test
    public void testEndsWithOperator() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("a|");
        });
    }

    @Test
    public void testQuestionAfterGroup() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("(a)?");
        assertEquals("(a)?", r.getRegularStatement());
    }

    @Test
    public void testEmptyGroupInExpression() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("a()b");
        assertEquals("a.().b", r.getRegularStatement());
    }

    @Test
    public void testInsertOrConcat() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("a|.c");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("a.|c");
        });
    }

    @Test
    public void testEscapedOperatorInExpression() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("a\\|b");
        assertEquals("a.\\|.b", r.getRegularStatement());
    }

    @Test
    public void testEscapeAtEnd() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PreparedRegularStatement("a\\");
        });
    }

    @Test
    public void testMultipleCurly() {
        PreparedRegularStatement r =
            new PreparedRegularStatement("a{2}{3}");
        assertEquals("a{2}{3}", r.getRegularStatement());
    }

    @Test
    public void testWeirdButValid() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("(a|b?c{2})d");
        assertEquals("(a|b?.c{2}).d", r.getRegularStatement());
    }

    @Test
    public void testOperatorChain() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("a?b{2}(c|d)e?f");
        assertEquals("a?.b{2}.(c|d).e?.f", r.getRegularStatement());
    }

    @Test
    public void testDeepMixedNesting() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("((a|b?){2}c)?");
        assertEquals("((a|b?){2}.c)?", r.getRegularStatement());
    }

    @Test
    public void testConcatAfterComplex() {
        PreparedRegularStatement r =
                new PreparedRegularStatement("(a|b{2})c(d)");
        assertEquals("(a|b{2}).c.(d)", r.getRegularStatement());
    }
}
