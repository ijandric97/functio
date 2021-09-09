package us.walr;

import us.walr.exceptions.ParseError;
import us.walr.grammar.Expression;
import us.walr.grammar.Statement;
import us.walr.helpers.ParserHelper;
import us.walr.internal.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static us.walr.internal.Token.Type.*;

class Parser {
    /**
     * An Instance of the ParserHelper object which is used to iterate over the tokens returned by Lexer
     */
    private final ParserHelper ph;

    /**
     * Initializes a Parser instance.
     *
     * @param tokens List of Tokens as returned by Lexer
     */
    Parser(List<Token> tokens) {
        this.ph = new ParserHelper(tokens);
    }

    /**
     * Runs the Recursive Descent Parsing and returns a Syntax Tree representation of the code.
     *
     * @return An Abstract Syntax Tree (AST).
     */
    List<Statement> run() {
        List<Statement> statements = new ArrayList<>();
        while (!ph.isEOF()) statements.add(declaration());
        return statements;
    }

    /**
     * Skip over to the next statement (line in code) to ensure safe continuation of parsing.
     * Called after ParseError exception resets the parser.
     */
    private void synchronize() {
        ph.advance();

        // Advance while we have not passed the next ";" character (which indicates the next statement) or one of the
        // statement declarations (which indicate start of the new statement).
        while (!ph.isEOF() &&
                ph.peekPrevious().type() != SEMICOLON &&
                !ph.match(CLASS, FUNCTION, VAR, FOR, IF, WHILE, PRINT, RETURN)) {
            ph.advance();
        }
    }

    // GRAMMAR METHODS

    /**
     * The lowest priority grammar nonterminal. A program is a series of declarations, which are the statements that
     * bind new identifiers or any of the other statement types.
     * <p>
     * Grammar:
     * declaration -> classDeclaration | functionDeclaration | variableDeclaration | statement ;
     * <p>
     * Catches ParseError and synchronizes if it happens.
     *
     * @return Syntax tree
     */
    private Statement declaration() {
        try {
            if (ph.matchAndAdvance(CLASS)) return classDeclaration();
            if (ph.matchAndAdvance(FUNCTION)) return function("function");
            if (ph.matchAndAdvance(VAR)) return variableDeclaration();

            return statement();
        } catch (ParseError error) {
            // Something went wrong and Parser raised an exception, synchronize (a.k.a. skip to next statement)
            synchronize();
            return null;
        }
    }

    /**
     * Grammar:
     * classDeclaration -> "class" IDENTIFIER ( "<" IDENTIFIER )? "{" function* "}" ;
     *
     * @return Syntax tree
     */
    private Statement classDeclaration() {
        // If the next token is not an identifier, crash
        Token name = ph.advance(IDENTIFIER, "Expect class name.");

        // Check if there is inheritance
        Expression.Variable superclass = null;
        if (ph.matchAndAdvance(EXTENDS)) {
            ph.advance(IDENTIFIER, "Expect superclass name.");
            superclass = new Expression.Variable(ph.peekPrevious());
        }

        // Parse the class body which consists of methods (attributes are defined in the init() function
        ph.advance(LEFT_BRACE, "Expect '{' before class body.");
        List<Statement.Function> methods = new ArrayList<>();
        while (!ph.match(RIGHT_BRACE) && !ph.isEOF()) methods.add(function("method"));
        ph.advance(RIGHT_BRACE, "Expect '}' after class body.");

        return new Statement.Class(name, superclass, methods);
    }

    /**
     * Grammar:
     * functionDeclaration -> "function" function ; // This part already consumed by the declaration().
     * function -> IDENTIFIER "(" parameters? ")" block ;
     * parameters -> IDENTIFIER ( "," IDENTIFIER )* ;
     *
     * @param kind The kind of function. Used for error message. Should be either "function" or "method".
     * @return Syntax tree
     */
    private Statement.Function function(String kind) {
        Token name = ph.advance(IDENTIFIER, "Expect " + kind + " name.");
        ph.advance(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();

        if (!ph.match(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) throw ph.error(ph.current(), "Can't have more than 255 parameters.");

                parameters.add(ph.advance(IDENTIFIER, "Expect parameter name."));
            } while (ph.matchAndAdvance(COMMA));
        }
        ph.advance(RIGHT_PAREN, "Expect ')' after parameters.");

        ph.advance(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Statement> body = block();
        return new Statement.Function(name, parameters, body);
    }

    /**
     * Grammar:
     * variableDeclaration -> "var" IDENTIFIER ( "=" expression )? ";" ;
     *
     * @return Syntax tree
     */
    private Statement variableDeclaration() {
        Token name = ph.advance(IDENTIFIER, "Expect variable name.");

        // Check if the variable also has a value assigned
        Expression initializer = null;
        if (ph.matchAndAdvance(EQUAL)) initializer = expression();

        ph.advance(SEMICOLON, "Expect ';' after variable declaration.");
        return new Statement.Variable(name, initializer);
    }

    /**
     * Statements produce side effects just like declarations, but they do not introduce data bindings.
     * <p>
     * Grammar:
     * statement -> expressionStatement | forStatement | ifStatement | printStatement | returnStatement | whileStatement | block ;
     *
     * @return Syntax tree
     */
    private Statement statement() {
        if (ph.matchAndAdvance(FOR)) return forStatement();
        if (ph.matchAndAdvance(IF)) return ifStatement();
        if (ph.matchAndAdvance(PRINT)) return printStatement();
        if (ph.matchAndAdvance(RETURN)) return returnStatement();
        if (ph.matchAndAdvance(WHILE)) return whileStatement();
        if (ph.matchAndAdvance(LEFT_BRACE)) return new Statement.Block(block());

        return expressionStatement();
    }

    /**
     * Grammar:
     * expressionStatement -> expression ";" ;
     *
     * @return Syntax tree
     */
    private Statement expressionStatement() {
        Expression expression = expression();

        ph.advance(SEMICOLON, "Expect ';' after expression.");
        return new Statement.Expression(expression);
    }

    /**
     * Syntactic sugar around while loops :)
     * <p>
     * Grammar:
     * forStatement -> "for" "(" ( variableDeclaration | expressionStatement | ";" ) expression? ";" expression? ")" statement ;
     *
     * @return Syntax tree
     */
    private Statement forStatement() {
        ph.advance(LEFT_PAREN, "Expect '(' after 'for'.");

        // Parse the initializer (for example var i = 0)
        Statement initializer;
        if (ph.matchAndAdvance(SEMICOLON)) {
            initializer = null;
        } else if (ph.matchAndAdvance(VAR)) {
            initializer = variableDeclaration();
        } else {
            initializer = expressionStatement();
        }

        // Parse the condition (for example: i < 10)
        Expression condition = null;
        if (!ph.match(SEMICOLON)) {
            condition = expression();
        }
        ph.advance(SEMICOLON, "Expect ';' after loop condition.");

        // Parse the increment or step (for example: i = i + 1);
        Expression increment = null;
        if (!ph.match(RIGHT_PAREN)) {
            increment = expression();
        }
        ph.advance(RIGHT_PAREN, "Expect ')' after for clauses.");

        // Parse the body of the for loop
        Statement body = statement();

        // We now convert the FOR into WHILE
        // If there is increment defined, add it to the end of the body
        if (increment != null) body = new Statement.Block(Arrays.asList(body, new Statement.Expression(increment)));
        // Create the while statement with the defined condition
        if (condition == null) condition = new Expression.Literal(true);
        body = new Statement.While(condition, body);
        // Add initializer to the top of the body if it exists
        if (initializer != null) body = new Statement.Block(Arrays.asList(initializer, body));

        return body;
    }

    /**
     * Grammar:
     * ifStatement -> "if" "(" expression ")" statement ( "else" statement )? ;
     *
     * @return Syntax tree
     */
    private Statement ifStatement() {
        // Match the if condition
        ph.advance(LEFT_PAREN, "Expect '(' after 'if'.");
        Expression condition = expression();
        ph.advance(RIGHT_PAREN, "Expect ')' after if condition.");

        // Evaluate the branch
        Statement thenBranch = statement();
        Statement elseBranch = null;

        // If the next token is ELSE, then add else branch to the execution
        // Else is appended to the first IF preceding it to solve the "dangling else problem"
        if (ph.matchAndAdvance(ELSE)) elseBranch = statement();

        return new Statement.If(condition, thenBranch, elseBranch);
    }

    /**
     * Grammar:
     * printStatement -> "print" expression ";" ;
     *
     * @return Syntax tree
     */
    private Statement printStatement() {
        Expression value = expression(); // Evaluate what we want to print

        ph.advance(SEMICOLON, "Expect ';' after literal.");
        return new Statement.Print(value);
    }

    /**
     * Grammar:
     * returnStatement -> "return" expression? ";" ;
     *
     * @return Syntax tree
     */
    private Statement returnStatement() {
        // The keyword here is used for displaying error message to the user
        Token keyword = ph.peekPrevious();
        Expression value = null;

        // Check if return value is not present and throw an error if it isn't
        if (!ph.match(SEMICOLON)) value = expression();
        ph.advance(SEMICOLON, "Expect ';' after return literal.");

        return new Statement.Return(keyword, value);
    }

    /**
     * Grammar:
     * whileStatement -> "while" "(" expression ")" statement ;
     *
     * @return Syntax tree
     */
    private Statement whileStatement() {
        // Parse the while condition
        ph.advance(LEFT_PAREN, "Expect '(' after 'while'.");
        Expression condition = expression();
        ph.advance(RIGHT_PAREN, "Expect ')' after condition.");

        // Parse the while body
        Statement body = statement();

        return new Statement.While(condition, body);
    }

    /**
     * A block of code (code between "{" and "}").
     * <p>
     * Grammar:
     * block -> "{" declaration* "}" ;
     *
     * @return Syntax tree
     */
    private List<Statement> block() {
        // Block of code contains a list of statements, so declare an array
        List<Statement> statements = new ArrayList<>();

        // Parse statements as long as the closing "}" is not reached
        while (!ph.match(RIGHT_BRACE) && !ph.isEOF()) statements.add(declaration());

        ph.advance(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    /**
     * A wrapper around assignment.
     * <p>
     * Grammar:
     * expression -> assignment ;
     *
     * @return Syntax tree
     */
    private Expression expression() {
        return assignment();
    }


    /**
     * Assignment is either a class
     * <p>
     * Grammar:
     * assignment -> ( call "." )? IDENTIFIER "=" assignment | logic_or ;
     *
     * @return Syntax tree
     */
    private Expression assignment() {
        Expression expression = logic_or();

        if (ph.matchAndAdvance(EQUAL)) {
            Token equals = ph.peekPrevious();
            Expression value = assignment();

            if (expression instanceof Expression.Variable) {
                Token name = ((Expression.Variable) expression).getName();
                return new Expression.Assign(name, value);
            } else if (expression instanceof Expression.Get get) {
                return new Expression.Set(get.getObject(), get.getName(), value);
            }

            throw ph.error(equals, "Invalid assignment target.");
        }

        return expression;
    }

    /**
     * A logical or operator (for example: var1 or var2)
     * <p>
     * Grammar:
     * logic_or -> logic_and ( "||" logic_and )* ;
     *
     * @return Syntax tree
     */
    private Expression logic_or() {
        Expression expression = logic_and(); // Check if there are higher priority operations first

        // Match the "or" keyword
        while (ph.matchAndAdvance(OR)) {
            Token operator = ph.peekPrevious(); // Save it as operator
            Expression right = logic_and(); // Evaluate the right side
            expression = new Expression.Logical(expression, operator, right); //
        }

        return expression;
    }

    /**
     * Grammar:
     * logic_and -> equality ( "&&" equality )* ;
     *
     * @return Syntax tree
     */
    private Expression logic_and() {
        Expression expression = equality(); // Check if there are higher priority operations first

        // Match the "and" keyword
        while (ph.matchAndAdvance(AND)) {
            Token operator = ph.peekPrevious(); // Save "and" as operator
            Expression right = equality(); // Evaluate the right side
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    /**
     * Takes care of the left associated Equality operators ("==", "!", "=")
     * <p>
     * Grammar:
     * equality -> comparison ( ( "!=" | "==" ) comparison )* ;
     *
     * @return Syntax tree
     */
    private Expression equality() {
        // Check if comparisons exist on the left side since they have higher priority
        Expression expression = comparison();

        // While the current token is either "!=" or "==" go to the next token
        while (ph.matchAndAdvance(BANG_EQUAL, EQUAL_EQUAL)) {
            // Get the previous token (which should now be "!=" or "==")
            Token operator = ph.peekPrevious();

            // Check if comparisons exists on the right side since they have higher priority
            Expression right = comparison();

            // Create a new binary tree where left operand is the existing expression, while the right one is the
            // one we are handling in the current iteration
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    /**
     * Take care of the left associated comparison operators (">", ">=", "<", "<=").
     * <p>
     * Grammar:
     * comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
     *
     * @return Syntax tree
     */
    private Expression comparison() {
        // Check if there are higher priority things on the left side
        Expression expression = term();

        // While either ">", ">=", "<", "<="
        while (ph.matchAndAdvance(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            // Get the previous token
            Token operator = ph.peekPrevious();

            // Check if there are higher priority things on the current token
            Expression right = term();

            // Create a new binary tree where left operand is the existing expression, while the right one is the
            // one we are handling in the current iteration
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    /**
     * Take care of the left associated Terms (objects associated in additions and subtractions) operators ("+", "-").
     * <p>
     * Grammar:
     * term -> factor ( ( "-" | "+" ) factor )* ;
     *
     * @return Syntax tree
     */
    private Expression term() {
        Expression expression = factor(); // Evaluate the higher priority operations

        while (ph.matchAndAdvance(MINUS, PLUS)) {
            Token operator = ph.peekPrevious(); // Save "+" or "-"
            Expression right = factor(); // Evaluate the right side
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    /**
     * Takes care of the left associated Factor ("/", "*") objects.
     * <p>
     * Grammar:
     * factor -> power ( ( "/" | "*" | "%" ) power )* ;
     *
     * @return Syntax tree
     */
    private Expression factor() {
        Expression expression = power(); // Evaluate the higher priority operations

        while (ph.matchAndAdvance(SLASH, STAR, SLASH_PERCENT, PERCENT)) {
            Token operator = ph.peekPrevious(); // Save the operator
            Expression right = power();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    /**
     * Takes care of the left associated Power ("**") objects.
     *
     * Grammar:
     * power -> unary ( ("**") unary )* ;
     *
     * @return Syntax tree
     */
    private Expression power() {
        Expression expression = unary(); // Evaluate the higher priority operations

        while (ph.matchAndAdvance(STAR_STAR)) {
            Token operator = ph.peekPrevious(); // Save the operator
            Expression right = unary();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    /**
     * Takes care of the right associated Unary ("!", "-", "++", "--") operations.
     * <p>
     * Grammar:
     * unary -> ( "!" | "-" | "++" | "--" ) unary | call ;
     *
     * @return Syntax tree
     */
    private Expression unary() {
        if (ph.matchAndAdvance(BANG, MINUS, MINUS_MINUS, PLUS_PLUS)) {
            Token operator = ph.peekPrevious(); // Save the operator
            Expression right = unary(); // Evaluate the right side
            return new Expression.Unary(operator, right);
        }

        return call();
    }

    /**
     * Parses a call to a function or a method. This is needed because functions can return functions.
     * For example: function a() return b, which is a function variable (pointer) itself, therefore it should be
     * possible to execute the whole sequence by typing in a()().
     * <p>
     * Grammar:
     * call -> primary ( "(" arguments? ")" | "." IDENTIFIER )* ;
     * arguments -> expression ( "," expression )* ;
     *
     * @return Syntax tree
     */
    private Expression call() {
        Expression expression = primary(); // Evaluate the higher order

        // While
        while (true) {
            if (ph.matchAndAdvance(LEFT_PAREN)) { // We detected a "(", therefore parse the function declaration
                // Parse the function arguments
                List<Expression> arguments = new ArrayList<>();
                if (!ph.match(RIGHT_PAREN)) { // If there are indeed arguments
                    do { // Loop and add them to the list
                        // Java limits arguments to 255, therefore We inherited this in Walrus too
                        if (arguments.size() >= 255)
                            throw ph.error(ph.current(), "Can't have more than 255 arguments.");
                        arguments.add(expression());
                    } while (ph.matchAndAdvance(COMMA));
                }
                // Right parenthesis is used for reporting the error at the correct location
                Token right_paren = ph.advance(RIGHT_PAREN, "Expect ')' after arguments.");

                expression = new Expression.Call(expression, right_paren, arguments);

                //expression = finishCall(expression);
            } else if (ph.matchAndAdvance(DOT)) {
                // We detected that this is actually a method
                Token name = ph.advance(IDENTIFIER, "Expect property name after '.'.");
                expression = new Expression.Get(expression, name);
            } else {
                break;
            }
        }

        return expression;
    }

    /**
     * Highest precedence part of the Recursive Descent Parsing which takes care of the primary objects which contain
     * all literals (NUMBER, STRING, BOOLEAN, NULL) and grouping operators ("(", ")").
     * <p>
     * Grammar:
     * primary -> "true" | "false" | "null" | "this" | NUMBER | STRING | IDENTIFIER | "(" expression ")" | "super" "." IDENTIFIER ;
     *
     * @return Syntax tree
     */
    private Expression primary() {
        // Match the boolean and null values
        if (ph.matchAndAdvance(FALSE)) return new Expression.Literal(false);
        if (ph.matchAndAdvance(TRUE)) return new Expression.Literal(true);
        if (ph.matchAndAdvance(NULL)) return new Expression.Literal(null);

        // Match numbers and strings
        if (ph.matchAndAdvance(NUMBER, STRING)) return new Expression.Literal(ph.peekPrevious().literal());

        // Match a "super" keyword
        if (ph.matchAndAdvance(SUPER)) {
            Token keyword = ph.peekPrevious(); // For error messages
            ph.advance(DOT, "Expect '.' after 'super'."); // check that dot exists
            Token method = ph.advance(IDENTIFIER, "Expect superclass method name."); // method name

            return new Expression.Super(keyword, method);
        }

        // Match a "this" keyword
        if (ph.matchAndAdvance(THIS)) return new Expression.This(ph.peekPrevious());

        // Match a variable
        if (ph.matchAndAdvance(IDENTIFIER)) return new Expression.Variable(ph.peekPrevious());

        // Match an expression inside grouping "(" and ")"
        if (ph.matchAndAdvance(LEFT_PAREN)) {
            Expression expression = expression();

            ph.advance(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expression);
        }

        // We should not be here
        throw ph.error(ph.current(), "Expect expression.");
    }
}
