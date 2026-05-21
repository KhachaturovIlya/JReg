package JReg.DFA;

import java.util.List;

public record Edge(List<Character> transitionChars, Node to) {}