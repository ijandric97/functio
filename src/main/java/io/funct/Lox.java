package io.funct;

import io.funct.exceptions.RuntimeError;
import io.funct.grammar.Statement;
import io.funct.internal.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();
    /**
     * Any error. This usually means in lexing parsing or while resolving variables (resolver).
     */
    static boolean hadError = false;
    /**
     * Error during the interpreter stage
     */
    static boolean hadRuntimeError = false;

    private static boolean isREPL = false;

    /**
     * The Main function of the Lox programming language. It either runs a
     * REPL mode or executes a file that is passed as CMD param.
     *
     * @param args Filename to run
     * @throws IOException Something went wrong while parsing a FILE or Input
     */
    public static void main(String[] args) throws IOException {
        switch (args.length) {
            case 0 -> runPrompt(); // REPL Mode
            case 1 -> runFile(args[0]); // Try to run the provided file
            default -> {
                // We passed too many arguments; display help
                System.out.println("Usage: jLox [script]");
                Lox.exit(64);
            }
        }
    }

    /**
     * Reads and executes a provided source code file
     *
     * @param path Path to the source code file
     * @throws IOException Something went wrong while parsing the FILE
     */
    private static void runFile(String path) throws IOException {
        isREPL = false;

        // Read and run the source code
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error in the exit code.
        if (hadError) Lox.exit(65);
        if (hadRuntimeError) Lox.exit(70);
    }

    /**
     * Runs an interactive CMD prompt, also called REPL
     *
     * @throws IOException Something went wrong while parsing a LINE
     */
    private static void runPrompt() throws IOException {
        isREPL = true;
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            // Read the user input
            System.out.print("> ");
            String line = reader.readLine();

            if (line == null) break; // This should not happen, abort
            if (line.equals("")) { // If there was no user input, show the user how to end it
                System.out.println("Use \"exit();\" to exit out of the REPL.");
            }

            run(line);
            hadError = false;
        }
    }

    /**
     * Runs the provided source code which is either read from a LINE or a FILE
     *
     * @param source The source code to parse and execute
     */
    private static void run(String source) {
        // Run the lexical analysis of the code
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.run();
        if (!isREPL) System.out.println("Finished Lexical analysis.");
        // printTokens(tokens); // Print out the tokens that the Lexer found

        // Run the syntax analysis of the code
        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.run();
        if (!isREPL) System.out.println("Finished Syntax analysis.");
        if (hadError) return; // Stop if there was a syntax error.

        // Run the binding scope resolution
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);
        if (!isREPL) System.out.println("Finished Resolving scopes.");
        if (hadError) return; // Stop if there was a resolution error.

        // Interpret the code
        if (!isREPL) System.out.println("Interpreting...");
        interpreter.run(statements);
    }

    /**
     * A wrapper around System.exit() method.
     *
     * @param status Status code to exit with.
     * @return An Object to satisfy the interpreter.
     */
    public static Object exit(int status) {
        System.exit(status);
        return null;
    }

    /**
     * @param tokens List of tokens returned by Lexer
     */
    @SuppressWarnings("unused")
    private static void printTokens(List<Token> tokens) {
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    /**
     * Print the error message then report it to the internal error handling system
     *
     * @param line    The line number on which the error occurred
     * @param message The error message
     */
    static void error(int line, String message) {
        error(line, "", message);
    }

    /**
     * Print the error message then report it to the internal error handling system
     *
     * @param token   Token error happened at
     * @param message The error message
     */
    public static void error(Token token, String message) {
        if (token.type() == Token.Type.EOF) {
            error(token.line(), " at end", message);
        } else {
            error(token.line(), " at '" + token.lexeme() + "'", message);
        }
    }

    /**
     * Print the error message then report it to the internal error handling system
     *
     * @param line    The line number on which the error occurred
     * @param where   Where it happened at end of the file or whatever
     * @param message The error message
     */
    private static void error(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    /**
     * Print the runtime error message then report it to the internal error handling system
     *
     * @param error The raised RuntimeError object
     */
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.getToken().line() + "]");
        hadRuntimeError = true;
    }
}
