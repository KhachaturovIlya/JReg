package JReg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DFATests {
    private boolean matches(String pattern, String input) {
        PreparedRegularStatement prs = new PreparedRegularStatement(pattern);
        SyntaxTree st = new SyntaxTree(prs);
        LabeledSyntaxTree lst = new LabeledSyntaxTree(st);
        DFA dfa = new DFA(lst);
        return dfa.match(input);
    }

    @Test
    void testBasicMatch() {
        assertTrue(matches("abc", "abc"));
        assertFalse(matches("abc", "abcd"));
        assertFalse(matches("abc", "ab"));
    }

    @Test
    void testComplexStarAndOr() {
        String pattern = "(a|b)...abb";
        assertTrue(matches(pattern, "abb"));
        assertTrue(matches(pattern, "aaabbbabb"));
        assertTrue(matches(pattern, "baaabb"));
        assertFalse(matches(pattern, "baaaab"));
    }

    @Test
    void testOptional() {
        String pattern = "colou?r";
        assertTrue(matches(pattern, "color"));
        assertTrue(matches(pattern, "colour"));
        assertFalse(matches(pattern, "colouur"));
    }

    @Test
    void testFixedRepetition() {
        String pattern = "a{3}";
        assertTrue(matches(pattern, "aaa"));
        assertFalse(matches(pattern, "aa"));
        assertFalse(matches(pattern, "aaaa"));
    }

    @Test
    void testNestedStructures() {
        String pattern = "(ab|c)...d";
        assertTrue(matches(pattern, "d"));
        assertTrue(matches(pattern, "abd"));
        assertTrue(matches(pattern, "ababccd"));
        assertFalse(matches(pattern, "abc"));
    }

    @Test
    void testEscapedStar() {
        assertTrue(matches("a\\*b", "a*b"));
        assertFalse(matches("a\\*b", "aaab"));
    }

    @Test
    void testNestedQuantifiers() {
        String pattern = "(a?)...";
        assertTrue(matches(pattern, ""));
        assertTrue(matches(pattern, "aaa"));
    }

    @Test
    void testComplexAlternationWithGroups() {
        String pattern = "(abc|ade)f";
        assertTrue(matches(pattern, "abcf"));
        assertTrue(matches(pattern, "adef"));
        assertFalse(matches(pattern, "abce"));
        assertFalse(matches(pattern, "ade"));
    }

    @Test
    void testOverlappingPatterns() {
        String pattern = "a...ab";
        assertTrue(matches(pattern, "ab"));
        assertTrue(matches(pattern, "aaab"));
        assertFalse(matches(pattern, "aaaaa"));
    }

    @Test
    void testHardCurlyRepetitions() {
        String pattern = "(a|b){2}c";
        assertTrue(matches(pattern, "aac"));
        assertTrue(matches(pattern, "abc"));
        assertTrue(matches(pattern, "bac"));
        assertTrue(matches(pattern, "bbc"));
        assertFalse(matches(pattern, "ac"));
        assertFalse(matches(pattern, "aaac"));
    }

    @Test
    void testLongChainOfOptionables() {
        String pattern = "a?b?c?d?e";
        assertTrue(matches(pattern, "e"));
        assertTrue(matches(pattern, "abcde"));
        assertTrue(matches(pattern, "ace"));
        assertFalse(matches(pattern, "abcd"));
    }

    @Test
    void testMultipleStarsInSequence() {
        // a*b*c*
        String pattern = "a...b...c...";
        assertTrue(matches(pattern, ""));
        assertTrue(matches(pattern, "aaabbcccc"));
        assertTrue(matches(pattern, "ac"));
        assertFalse(matches(pattern, "ba"));
    }

    @Test
    void testBackslashInGroup() {
        String pattern = "(a|\\*|b)...";
        assertTrue(matches(pattern, "a*b*aa"));
        assertTrue(matches(pattern, "***"));
        assertFalse(matches(pattern, "a+b"));
    }

    @Test
    void testRecursiveLikeStructure() {
        String pattern = "(a(b(c)?)?)?";
        assertTrue(matches(pattern, ""));
        assertTrue(matches(pattern, "a"));
        assertTrue(matches(pattern, "ab"));
        assertTrue(matches(pattern, "abc"));
        assertFalse(matches(pattern, "ac"));
    }

    @Test
    void testStarAfterCurly() {
        String pattern = "a{2}...";
        assertTrue(matches(pattern, "aa"));
        assertTrue(matches(pattern, "aaaaaa"));
        assertFalse(matches(pattern, "aaaaaaa"));
    }

    @Test
    void testLargeAlphabetStress() {
        String pattern = "qwerty|12345";
        assertTrue(matches(pattern, "qwerty"));
        assertTrue(matches(pattern, "12345"));
        assertFalse(matches(pattern, "qwe123"));
    }

    @Test
    void testEmptyStringMatch() {
        assertTrue(matches("a...", ""));
        assertTrue(matches("a?", ""));
        assertTrue(matches("(a|b)...", ""));
        assertFalse(matches("a", ""));
    }

    @Test
    void testNullablesBeforeRequiredChar() {
        String pattern = "a...b?c?d";
        assertTrue(matches(pattern, "d"));
        assertTrue(matches(pattern, "ad"));
        assertTrue(matches(pattern, "abcd"));
        assertFalse(matches(pattern, "abc"));
    }

    @Test
    void testStarOfStar() {
        String pattern = "(a...)...";
        assertTrue(matches(pattern, ""));
        assertTrue(matches(pattern, "a"));
        assertTrue(matches(pattern, "aaaaa"));
    }

    @Test
    void testNestedOptionalWithRequired() {
        String pattern = "((a?)?)?b";
        assertTrue(matches(pattern, "b"));
        assertTrue(matches(pattern, "ab"));
        assertFalse(matches(pattern, "a"));
    }

    @Test
    void testEpsilonTransitionsInOr() {
        String pattern = "(a?|b)c";
        assertTrue(matches(pattern, "ac"));
        assertTrue(matches(pattern, "bc"));
        assertTrue(matches(pattern, "c"));
    }

    @Test
    void testInternalRepetitionIdentity() {
        assertTrue(matches("a{1}b", "ab"));
    }

    @Test
    void testLargeRepetitionStep() {
        String pattern = "a{10}b";
        assertTrue(matches(pattern, "aaaaaaaaaab"));
        assertFalse(matches(pattern, "aaaaaaaaab"));
    }

    @Test
    void testCatastrophicBacktrackingScenario() {
        String pattern = "(a|a)...b";
        assertTrue(matches(pattern, "aaaaaaaaaaaaaaaaaaaaab"));
    }
}
