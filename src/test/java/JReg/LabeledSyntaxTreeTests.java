package JReg;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class LabeledSyntaxTreeTests {

    private static LabeledSyntaxTree getLabeledTree(String pattern) {
        return new LabeledSyntaxTree(new SyntaxTree(new PreparedRegularStatement(pattern)));
    }

    @Test
    void testSimpleConcat() {
        SyntaxTree tree = new SyntaxTree(new PreparedRegularStatement("ab"));
        LabeledSyntaxTree labeled = new LabeledSyntaxTree(tree);
        
        assertEquals(Set.of(2), labeled.getFollowPos(1));
        assertEquals(Set.of(3), labeled.getFollowPos(2));
    }

    @Test
    void testStarOperator() {
        SyntaxTree tree = new SyntaxTree(new PreparedRegularStatement("a..."));
        LabeledSyntaxTree labeled = new LabeledSyntaxTree(tree);

        assertEquals(Set.of(1, 2), labeled.getFollowPos(1));
    }

    @Test
    void testOrOperator() {
        SyntaxTree tree = new SyntaxTree(new PreparedRegularStatement("a|b"));
        LabeledSyntaxTree labeled = new LabeledSyntaxTree(tree);

        assertEquals(Set.of(3), labeled.getFollowPos(1));
        assertEquals(Set.of(3), labeled.getFollowPos(2));
    }

    @Test
    void testComplexExpression() {
        SyntaxTree tree = new SyntaxTree(new PreparedRegularStatement("(a|b)...abb"));
        LabeledSyntaxTree labeled = new LabeledSyntaxTree(tree);

        assertEquals(Set.of(1, 2, 3), labeled.getFollowPos(1));
        assertEquals(Set.of(1, 2, 3), labeled.getFollowPos(2));

        assertEquals(Set.of(4), labeled.getFollowPos(3));
        assertEquals(Set.of(5), labeled.getFollowPos(4));
        assertEquals(Set.of(6), labeled.getFollowPos(5));
    }

    @Test
    void testMultipleOptional() {
        LabeledSyntaxTree l = getLabeledTree("a?b?c");
        assertEquals(Set.of(2, 3), l.getFollowPos(1));
        assertEquals(Set.of(3), l.getFollowPos(2));
    }

    @Test
    void testNestedStars() {
        LabeledSyntaxTree l = getLabeledTree("(a...)...");
        assertEquals(Set.of(1, 2), l.getFollowPos(1));
    }

    @Test
    void testStarFollowedBySameChar() {
        LabeledSyntaxTree l = getLabeledTree("a...a");
        assertEquals(Set.of(1, 2), l.getFollowPos(1));
        assertEquals(Set.of(3), l.getFollowPos(2));
    }

    @Test
    void testOrWithSnap() {
        LabeledSyntaxTree l = getLabeledTree("(a...|b)c");
        assertEquals(Set.of(1, 3), l.getFollowPos(1));
        assertEquals(Set.of(3), l.getFollowPos(2));
    }

    @Test
    void testRepetitionStatic() {
        LabeledSyntaxTree l = getLabeledTree("a{3}");
        assertEquals(Set.of(2), l.getFollowPos(1));
        assertEquals(Set.of(3), l.getFollowPos(2));
        assertEquals(Set.of(4), l.getFollowPos(3));
    }

    @Test
    void testRepetitionOfGroup() {
        LabeledSyntaxTree l = getLabeledTree("(ab){2}");
        assertEquals(Set.of(2), l.getFollowPos(1));
        assertEquals(Set.of(3), l.getFollowPos(2));
        assertEquals(Set.of(4), l.getFollowPos(3));
        assertEquals(Set.of(5), l.getFollowPos(4));
    }

    @Test
    void testComplexBranching() {
        LabeledSyntaxTree l = getLabeledTree("a(b|c|d)e");
        assertEquals(Set.of(2, 3, 4), l.getFollowPos(1));
        assertEquals(Set.of(5), l.getFollowPos(2));
        assertEquals(Set.of(5), l.getFollowPos(3));
        assertEquals(Set.of(5), l.getFollowPos(4));
    }

    @Test
    void testSnapInsideOptional() {
        LabeledSyntaxTree l = getLabeledTree("(a...)?b");
        assertEquals(Set.of(1, 2), l.getFollowPos(1));
    }

    @Test
    void testLeadingOptional() {
        LabeledSyntaxTree l = getLabeledTree("a?bc");
        assertEquals(Set.of(2), l.getFollowPos(1));
        assertEquals(Set.of(3), l.getFollowPos(2));
    }

    @Test
    void testEscapeCharacters() {
        LabeledSyntaxTree l = getLabeledTree("a\\*b");
        assertEquals(Set.of(2), l.getFollowPos(1));
        assertEquals(Set.of(3), l.getFollowPos(2));
        assertEquals(Set.of(4), l.getFollowPos(3));
    }

    @Test
    void testTwoConsecutiveStars() {
        LabeledSyntaxTree l = getLabeledTree("a...b...");
        assertEquals(Set.of(1, 2, 3), l.getFollowPos(1));
        assertEquals(Set.of(2, 3), l.getFollowPos(2));
    }

    @Test
    void testDeeplyNestedOptional() {
        LabeledSyntaxTree l = getLabeledTree("((a?))?b");
        assertEquals(Set.of(2), l.getFollowPos(1));
    }

    @Test
    void testAllNullableBeforeEnd() {
        LabeledSyntaxTree l = getLabeledTree("a...b?");
        assertEquals(Set.of(1, 2, 3), l.getFollowPos(1));
        assertEquals(Set.of(3), l.getFollowPos(2));
    }
}
