package us.walr;

import us.walr.helpers.LexerIterator;
import us.walr.internal.Token;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static us.walr.internal.Token.Type.*;

/**
 * Lexer (also called tokenizer or scanner), takes the input string and splits it into pieces we call tokens.
 */
public class Lexer {
    /**
     * A list of reserved keywords.
     * Used to differentiate out statements from identifiers.
     */
    private static final Map<String, Token.Type> keywords = Map.ofEntries(
            new AbstractMap.SimpleEntry<>("class", CLASS),
            new AbstractMap.SimpleEntry<>("else", ELSE),
            new AbstractMap.SimpleEntry<>("extends", EXTENDS),
            new AbstractMap.SimpleEntry<>("false", FALSE),
            new AbstractMap.SimpleEntry<>("for", FOR),
            new AbstractMap.SimpleEntry<>("function", FUNCTION),
            new AbstractMap.SimpleEntry<>("if", IF),
            new AbstractMap.SimpleEntry<>("null", NULL),
            new AbstractMap.SimpleEntry<>("print", PRINT),
            new AbstractMap.SimpleEntry<>("return", RETURN),
            new AbstractMap.SimpleEntry<>("super", SUPER),
            new AbstractMap.SimpleEntry<>("this", THIS),
            new AbstractMap.SimpleEntry<>("true", TRUE),
            new AbstractMap.SimpleEntry<>("var", VAR),
            new AbstractMap.SimpleEntry<>("while", WHILE)
    );

    /**
     * Source code as a raw string
     */
    private final String source;
    /**
     * A list of tokens that will be generated from the raw source code string
     */
    private final List<Token> tokens = new ArrayList<>();
    /**
     * The modified StringCharacterIterator which we use for... iterating :) over the input string.
     */
    private final LexerIterator it;

    /**
     * Index of the first character of the current Lexeme
     */
    private int lexemeStartIndex = 0;
    /**
     * Index of the currently scanned LINE
     */
    private int line = 1;

    /**
     * Create a new instance of the Lexer.
     *
     * @param source String source code which we will run lexical analysis on to produce a set of tokens.
     */
    Lexer(String source) {
        this.source = source;
        this.it = new LexerIterator(source);
    }

    /**
     * Scan tokens and add them to tokens list until the EOF is reached.
     *
     * @return Returns a list of found tokens
     */
    List<Token> run() {
        // Parse characters until we reached the EOF
        while (!it.isEOF()) {
            // We are at the beginning of the next lexeme, therefore "reset" the start index
            lexemeStartIndex = it.getIndex();

            // Parse the current character
            switch (it.current()) {
                // Single-character tokens
                case '[' -> addToken(LEFT_BRACKET);
                case ']' -> addToken(RIGHT_BRACKET);
                case '(' -> addToken(LEFT_PAREN);
                case ')' -> addToken(RIGHT_PAREN);
                case '{' -> addToken(LEFT_BRACE);
                case '}' -> addToken(RIGHT_BRACE);
                case ',' -> addToken(COMMA);
                case '.' -> addToken(DOT);
                case ';' -> addToken(SEMICOLON);
                case '%' -> addToken(PERCENT);

                // One or two character tokens.
                case '*' -> addToken(it.matchNextAndAdvance('*') ? STAR_STAR : STAR);
                case '-' -> addToken(it.matchNextAndAdvance('-') ? MINUS_MINUS : MINUS);
                case '+' -> addToken(it.matchNextAndAdvance('+') ? PLUS_PLUS : PLUS);
                case '!' -> addToken(it.matchNextAndAdvance('=') ? BANG_EQUAL : BANG);
                case '=' -> addToken(it.matchNextAndAdvance('=') ? EQUAL_EQUAL : EQUAL);
                case '<' -> addToken(it.matchNextAndAdvance('=') ? LESS_EQUAL : LESS);
                case '>' -> addToken(it.matchNextAndAdvance('=') ? GREATER_EQUAL : GREATER);
                case '&' -> {
                    if (it.matchNextAndAdvance('&')) {
                        addToken(AND);
                    } else {
                        Walrus.error(line, "Unexpected character.");
                    }
                }
                case '|' -> {
                    if (it.matchNextAndAdvance('|')) {
                        addToken(OR);
                    } else {
                        Walrus.error(line, "Unexpected character.");
                    }
                }
                case '/' -> {
                    if (it.matchNextAndAdvance('*')) {
                        multiLineComment();
                    } else if (it.matchNextAndAdvance('/')) {
                        // This is a single-line comment
                        // Advance until either EOF or newline has been reached
                        while (!it.isEOF() && it.peekNext() != '\n') it.next();
                    } else if (it.matchNextAndAdvance('%')) {
                        addToken(SLASH_PERCENT);
                    } else {
                        addToken(SLASH);
                    }
                }

                // Ignore whitespace
                case ' ', '\r', '\t' -> {
                }
                case '\n' -> line++; // Newline. Increase the current line number.

                // Literals
                case '"' -> string();
                default -> {
                    if (it.isDigit()) {
                        number();
                    } else if (it.isAlpha()) {
                        identifier();
                    } else {
                        // Something went wrong, throw an ERROR
                        Walrus.error(line, "Unexpected character.");
                    }
                }
            }

            // Iterate over to the next character
            it.next();
        }

        // Add the EOF token since the EOF has been reached
        tokens.add(new Token(EOF, "", null, line));

        // Return the found tokens
        return tokens;
    }

    /**
     * Scans and tokenizes an identifier and checks if it is a reserved keyword.
     */
    private void identifier() {
        // Iterate till the end of the identifier (Which consists of AlphaNumeric characters)
        while (it.isAlphaNumeric(it.peekNext())) it.next();

        // Extract the name of the identifier
        String text = source.substring(lexemeStartIndex, it.getIndex() + 1);

        // Check if that name is a reserved keyword
        Token.Type type = keywords.get(text);
        if (type == null) type = IDENTIFIER;

        addToken(type);
    }

    /**
     * Scans and tokenizes a number and converts it into double.
     * Support decimal dot.
     */
    private void number() {
        // Loop until the end of the number sequence
        while (it.isDigit(it.peekNext())) it.next();

        // Check if the next character is a fractional dot
        if (it.matchNext('.') && it.isDigit(it.getIndex() + 2)) {
            // Go forward to skip the dot
            it.next();

            // Loop until the end of the number sequence
            while (it.isDigit(it.peekNext())) it.next();
        }

        double value = Double.parseDouble(source.substring(lexemeStartIndex, it.getIndex() + 1));
        addToken(NUMBER, value);
    }

    /**
     * Scans and tokenizes a string literal and throws an error if it is not closed.
     */
    private void string() {
        // Loop while either EOF or closing comment tag is reached
        while (!it.isEOF() && !(it.matchNext('"'))) {
            if (it.isNewLine()) line++; // Increase the current line if newline was found
            it.next();
        }

        // EOF was reached which means we did not close the string
        if (it.isEOF()) {
            Walrus.error(line, "Unterminated string.");
            return;
        }

        it.next(); // Consume the closing "

        // Trim the surrounding quotes so we can get the actual literal value
        String value = source.substring(lexemeStartIndex + 1, it.getIndex());
        addToken(STRING, value);
    }

    /**
     * Scans and ignores a multi-line comment and throws an error if it is not closed.
     * Multi-line comments cannot be nested, because that would greatly increase the complexity.
     * Also, nesting is only useful for commenting out code, which is a bad practice.
     */
    private void multiLineComment() {
        // Advance until EOF or  */ was found!
        while (!it.isEOF() && !(it.matchNext('*') && it.match(it.getIndex() + 2, '/'))) {
            if (it.isNewLine()) line++; // Increase the current line if newline was found
            it.next();
        }

        // EOF was reached which means we did not close the comment, throw an error
        if (it.isEOF()) {
            Walrus.error(line, "Unterminated multi-line comment.");
        }

        it.next(2); // Everything is ok, consume the end */ characters
    }

    /**
     * Add a token to the token list. The literal value will be null.
     *
     * @param type A supported token type from Token.Type enum
     */
    private void addToken(Token.Type type) {
        addToken(type, null);
    }

    /**
     * Add a token to the token list, and its literal value
     *
     * @param type    A supported token type from Token.Type enum
     * @param literal The literal value object, can be null
     */
    private void addToken(Token.Type type, Object literal) {
        // Extract the lexeme from the original source string
        int lexemeEndIndex = it.getIndex() + 1;
        String lexeme = source.substring(lexemeStartIndex, lexemeEndIndex);

        // Add a token to the tokens list
        tokens.add(new Token(type, lexeme, literal, line));
    }
}
