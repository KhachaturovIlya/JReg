package JReg.AST;

import lombok.Getter;

public class PreparedRegularStatement {
    @Getter
    String regularStatement;

    public PreparedRegularStatement(String regularStatement) {
        StringBuilder correctExpr = new StringBuilder();

        int openedParenthesis = 0;

        boolean endThought = false;

        for (int i = 0; i < regularStatement.length(); ++i) {
            char c = regularStatement.charAt(i);

            boolean isUnaryOrClose = (c == ')' || c == '>' || c == '}' || c == '?' || c == '|' || c == '{' ||
                (i < regularStatement.length() - 2 &&
                        c == '.' && regularStatement.charAt(i+1) == '.' && regularStatement.charAt(i+2) == '.'));

            if (endThought && !isUnaryOrClose) correctExpr.append('.');

            switch (c) {
                case '\\' -> {
                    if (i == regularStatement.length() - 1) throw new IllegalArgumentException("Bad regular expression");
                    correctExpr.append('\\');
                    correctExpr.append(regularStatement.charAt(i + 1));
                    ++i;
                    endThought = true;
                }
                case '.' -> {
                    if (i == regularStatement.length() - 1) throw new IllegalArgumentException("Bad regular expression");

                    if (regularStatement.charAt(i + 1) != '.') {
                        if (!endThought) throw new IllegalArgumentException("Bad regular expression");
                        correctExpr.append('.');
                        endThought = false;
                        continue;
                    }

                    if (i == regularStatement.length() - 2 || regularStatement.charAt(i + 2) != '.')
                        throw new IllegalArgumentException("Bad regular expression");

                    if (!endThought) throw new IllegalArgumentException("Bad regular expression");

                    correctExpr.append('*');

                    i += 2; // Need to seek i, because we captured 3 chars at once
                }
                case '|' -> {
                    if (!endThought) throw new IllegalArgumentException("Bad regular expression");
                    correctExpr.append('|');
                    endThought = false;
                }
                case '?' -> {
                    if (!endThought) throw new IllegalArgumentException("Bad regular expression");
                    correctExpr.append('?');
                }
                case '(' -> {
                    ++openedParenthesis;
                    correctExpr.append('(');
                    endThought = false;
                }
                case ')' -> {
                    if (openedParenthesis == 0) throw new IllegalArgumentException("Bad regular expression");
                    --openedParenthesis;
                    correctExpr.append(')');
                    endThought = true;
                }
                case '{' -> {
                    if (i == 0 || !endThought) throw new IllegalArgumentException("Bad regular expression");
                    correctExpr.append('{');
                    c = regularStatement.charAt(++i);
                    if (c == '0') throw new IllegalArgumentException("Bad regular expression");
                    while (c != '}') {
                        if (i == regularStatement.length() - 1 || c < '0' || c > '9') throw new IllegalArgumentException("Bad regular expression");

                        correctExpr.append(c);

                        c = regularStatement.charAt(++i);
                    }
                    correctExpr.append('}');
                }
                case '}' -> throw new IllegalArgumentException("Bad curly expression");
                case '<' -> {
                    if (i == 0 || regularStatement.charAt(i-1) != '(') throw new IllegalArgumentException("Bad angle expression");
                    correctExpr.append('<');
                    c = regularStatement.charAt(++i);
                    while (c != '>') {
                        if (i == regularStatement.length() - 1) throw new IllegalArgumentException("Bad angle expression");
                        correctExpr.append(c);

                        c = regularStatement.charAt(++i);
                    }
                    correctExpr.append('>');

                    endThought = false;
                }
                case '>' -> throw new  IllegalArgumentException("Bad angle expression");
                case '*' -> {
                    correctExpr.append('\\');
                    correctExpr.append('*');
                    endThought = true;
                }
                default -> {
                    endThought = true;
                    correctExpr.append(regularStatement.charAt(i));
                }
            }
        }
        if (openedParenthesis != 0 || !endThought) throw new IllegalArgumentException("Bad regular expression");

        this.regularStatement = correctExpr.toString();
    }
}
