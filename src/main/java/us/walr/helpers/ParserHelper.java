package us.walr.helpers;

import us.walr.Walrus;
import us.walr.exceptions.ParseError;
import us.walr.internal.Token;

import java.util.List;
import java.util.ListIterator;

public class ParserHelper {
    /**
     * List of Tokens as returned by Lexer
     */
    private final List<Token> tokens;

    /**
     * Iterator for the list of Tokens
     */
    private final ListIterator<Token> it;

    /**
     * ParserHelper object which is used to iterate over the tokens returned by Lexer
     *
     * @param tokens List of Tokens as returned by Lexer
     */
    public ParserHelper(List<Token> tokens) {
        this.tokens = tokens;
        this.it = tokens.listIterator();
    }

    /**
     * Checks if the current Token is of the specified type
     *
     * @param type The token type to check the current one with
     * @return True if the current Token is of the specified Type
     */
    public boolean match(Token.Type type) {
        if (isEOF()) return false;
        return current().type() == type;
    }

    /**
     * Checks if the current Token is one of the specified types
     *
     * @param types The token types to check the current one with
     * @return True if the current Token is one of the specified Types
     */
    public boolean match(Token.Type... types) {
        for (Token.Type type : types) {
            if (match(type)) return true;
        }

        return false;
    }

    /**
     * Returns the current token and advances to the next one.
     *
     * @return The next token
     */
    public Token advance() {
        if (!isEOF()) it.next();
        return peekPrevious();
    }

    /**
     * Returns the current token and advances to the next one but only if the current token is of the exact specified
     * type. Otherwise, throws an error.
     *
     * @param type    The token type to check the current one with
     * @param message The error message to be thrown
     * @return The current token
     */
    public Token advance(Token.Type type, String message) {
        if (match(type)) return advance();

        throw error(current(), message);
    }

    /**
     * Checks if the current Token is in specified types and moves to the next token if it is.
     *
     * @param types The token types to check the current one with
     * @return True if the match and advancement happened
     */
    public boolean matchAndAdvance(Token.Type... types) {
        if (match(types)) {
            advance();
            return true;
        }

        return false;
    }

    /**
     * Checks if End Of File (EOF) token has been reached.
     *
     * @return Boolean literal that is true if EOF token was reached.
     */
    public boolean isEOF() {
        return current().type() == Token.Type.EOF;
    }

    /**
     * Returns the current token in the list.
     *
     * @return The currently iterated Token.
     */
    public Token current() {
        return tokens.get(it.previousIndex() + 1);
    }

    /**
     * Returns the previous token without moving the iterator to it.
     *
     * @return Token at a previous position
     */
    public Token peekPrevious() {
        return tokens.get(it.previousIndex());
    }

    /**
     * Report an error to the user and the internal Walrus error system.
     *
     * @param token   Token at which the error happened.
     * @param message The error message.
     * @return An instance of the ParseError exception which we can decide to throw (and crash the parser in process) or
     * ignore it and try to find a synchronization point which will allow us to continue parsing.
     */
    public ParseError error(Token token, String message) {
        Walrus.error(token, message);
        return new ParseError();
    }
}
