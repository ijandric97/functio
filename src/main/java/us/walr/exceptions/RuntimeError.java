package us.walr.exceptions;

import us.walr.internal.Token;

/**
 * Thrown when interpreter cannot interpret something
 */
public class RuntimeError extends RuntimeException {
    /**
     * The token it happened at
     */
    final Token token;

    /**
     * Creates a new runtime error instance which should be thrown to reset the interpreter
     *
     * @param token   Token it happened at
     * @param message Message user should see
     */
    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }

    /**
     * @return Get the encapsulated token
     */
    public Token getToken() {
        return token;
    }
}
