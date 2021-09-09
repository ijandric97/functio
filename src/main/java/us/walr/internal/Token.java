package us.walr.internal;

/**
 * A Token data class. Contains Token type, its parsed lexeme, literal literal and source code line.
 */
public record Token(Type type, String lexeme, Object literal, int line) {
    /**
     * Available Token Types, grouped by their representation and meaning.
     */
    public enum Type {
        // Single-character tokens.
        LEFT_BRACKET, RIGHT_BRACKET, LEFT_PAREN, RIGHT_PAREN,
        LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, SEMICOLON, SLASH,

        // One or two character tokens.
        BANG, BANG_EQUAL,
        EQUAL, EQUAL_EQUAL,
        GREATER, GREATER_EQUAL,
        LESS, LESS_EQUAL,
        MINUS, MINUS_MINUS,
        PLUS, PLUS_PLUS,
        STAR, STAR_STAR,
        PERCENT, SLASH_PERCENT,
        AND, OR,

        // Literals.
        IDENTIFIER, STRING, NUMBER,

        // Keywords.
        CLASS, ELSE, EXTENDS, FALSE, FUNCTION, FOR, IF, NULL,
        PRINT, RETURN, STATIC, SUPER, THIS, TRUE, VAR, WHILE,

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
