package JReg;

import java.util.List;

public record Edge(List<Character> transitionChars, Node to) {}