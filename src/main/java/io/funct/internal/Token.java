package io.funct.internal;

/**
 * A Token data class. Contains Token type, its parsed lexeme, literal literal and source code line.
 */
public record Token(Type type, String lexeme, Object literal, int line) {
    /**
     * Available Token Types, grouped by their representation and meaning.
     */
    public enum Type {
        // Single-character tokens.
        LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
        COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

        // One or two character tokens.
        BANG, BANG_EQUAL,
        EQUAL, EQUAL_EQUAL,
        GREATER, GREATER_EQUAL,
        LESS, LESS_EQUAL,
        MINUS_MINUS, PLUS_PLUS,

        // Literals.
        IDENTIFIER, STRING, NUMBER,

        // Keywords.
        AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
        PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

        EOF
    }

    /**
     * @return A string representation of the Token
     */
    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
