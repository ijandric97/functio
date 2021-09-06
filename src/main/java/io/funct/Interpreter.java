package io.funct;

import io.funct.exceptions.Return;
import io.funct.exceptions.RuntimeError;
import io.funct.grammar.Expression;
import io.funct.grammar.Statement;
import io.funct.helpers.StandardLibrary;
import io.funct.internal.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.funct.helpers.InterpreterHelper.*;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {
    /**
     * The global environment, containing global function definitions and variables
     */
    final Environment globals = new Environment();
    /**
     * A hashmap that stores the depth information (how nested current expression is) of expressions.
     * It is used for correct local environment resolution
     */
    private final Map<Expression, Integer> locals = new HashMap<>();
    /**
     * The currently used environment, by default it is global, but when inside nested function blocks
     * it can be changed to a temporary child environment which serves as a local environment
     */
    private Environment environment = globals;

    Interpreter() {
        // Default functions
        globals.define("clock", new StandardLibrary.clock());
        globals.define("exit", new StandardLibrary.exit());
        globals.define("println", new StandardLibrary.println());
        globals.define("abs", new StandardLibrary.abs());
        globals.define("rand", new StandardLibrary.rand());
        globals.define("pow", new StandardLibrary.pow());
    }

    /**
     * Run the interpreter on the provided list of statements
     *
     * @param statements List of statements
     */
    void run(List<Statement> statements) {
        try {
            for (Statement statement : statements) {
                executeStatement(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    /**
     * Run the interpreter visitor on the provided statement
     *
     * @param statement Statement to visit
     */
    private void executeStatement(Statement statement) {
        statement.accept(this);
    }

    /**
     * Run the interpreter visitor on the provided expression
     *
     * @param expression Expression to visit
     * @return The result of the interpretation, returned as generic Object
     */
    private Object executeExpression(Expression expression) {
        return expression.accept(this);
    }

    /**
     * Executes a nested list of statements (statements between "{" and "}".
     *
     * @param statements  List of statements to execute
     * @param environment The local environment to use
     */
    public void executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.environment; // Save the reference to the current environment

        try {
            this.environment = environment; // Enter new local environment
            for (Statement statement : statements) executeStatement(statement); // Execute statements
        } finally {
            this.environment = previous; // Reset the environment back to the original (usually global)
        }
    }

    /**
     * Temporarily log the depth level of the current environment so that the correct local environment can be
     * stored and gathered from.
     * <p>
     * Associate the current depth with the unique expression id.
     *
     * @param expression Unique expression id
     * @param depth      The current depth information
     */
    void resolveDepth(Expression expression, int depth) {
        locals.put(expression, depth);
    }

    /**
     * Searches local or global environment to find the requested variable.
     *
     * @param name       Name of the variable
     * @param expression Reference to the expression id
     * @return Global or local variable as specified by name
     */
    private Object lookUpVariable(Token name, Expression expression) {
        Integer depth = locals.get(expression); // How nested we are

        if (depth != null) {
            // Depth is not 0, therefore this is a local variable, traverse through the
            // environment children
            return environment.getAt(depth, name.lexeme());
        } else {
            // Depth is 0, therefore this is a global variable, fetch it
            return globals.get(name);
        }
    }

    /**
     * Execute a block node (which contains statements between "{" and "}").
     *
     * @param statement A block statement
     * @return Nothing (NOTE: Statements can produce side effects).
     */
    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        executeBlock(statement.getStatements(), new Environment(environment));
        return null;
    }

    /**
     * Execute a class statement
     *
     * @param statement A class statement
     * @return Nothing (NOTE: It's statements produce side effects).
     */
    @Override
    public Void visitClassStatement(Statement.Class statement) {
        // Do inheritance
        Object superclass = null;
        // Check if we should inherit from a parent class
        if (statement.getSuperclass() != null) {
            // We do have a superclass defined, therefore grab it's variable
            superclass = executeExpression(statement.getSuperclass());
            // If superclass has not been defined, or it is not a class, throw an error
            if (!(superclass instanceof LoxClass)) {
                throw new RuntimeError(statement.getSuperclass().getName(), "Superclass must be a class.");
            }
        }

        // Define a class reference in the current environment
        environment.define(statement.getName().lexeme(), null);

        // If there is a parent class, create a new local environment just for this class, and reference the constructor
        // of the parent class in it
        if (statement.getSuperclass() != null) {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }

        // Map the method definitions to a hashmap
        Map<String, LoxFunction> methods = new HashMap<>();
        for (Statement.Function method : statement.getMethods()) {
            LoxFunction function = new LoxFunction(method, environment, method.getName().lexeme().equals("init"));
            methods.put(method.getName().lexeme(), function);
        }

        // Finally, create an internal Lox Class object
        LoxClass loxClass = new LoxClass(statement.getName().lexeme(), (LoxClass) superclass, methods);

        // If a superclass exists, that means that we had to create a local environment to reference the superclass
        // therefore we should "pop" that environment and get back to the original one
        if (superclass != null) {
            environment = environment.getEnclosing();
        }

        // Add class to the environment
        environment.assign(statement.getName(), loxClass);
        return null;
    }

    /**
     * Executes (evaluates) an expression statement.
     *
     * @param statement Expression statement
     * @return Nothing, discards its return value
     */
    @Override
    public Void visitExpressionStatement(Statement.Expression statement) {
        executeExpression(statement.getExpression());
        return null;
    }

    /**
     * Executes a function declaration node.
     * Create a new function reference in the current environment
     *
     * @param statement A function statement
     * @return Nothing
     */
    @Override
    public Void visitFunctionStatement(Statement.Function statement) {
        LoxFunction function = new LoxFunction(statement, environment, false);
        environment.define(statement.getName().lexeme(), function);
        return null;
    }

    /**
     * Interprets an if node.
     *
     * @param statement If statement
     * @return Nothing, (but there may be side effects).
     */
    @Override
    public Void visitIfStatement(Statement.If statement) {
        // Pretty self-explanatory, if the condition is true, execute then branch
        // otherwise, search for the else branch (which may also contain if ).
        if (isTruthy(executeExpression(statement.getCondition()))) {
            executeStatement(statement.getThenBranch());
        } else if (statement.getElseBranch() != null) {
            executeStatement(statement.getElseBranch());
        }
        return null;
    }

    /**
     * Execute a print statement, which prints something on the screen.
     *
     * @param statement The print statement
     * @return Nothing, but produces a side effect on the standard output terminal
     */
    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        Object value = executeExpression(statement.getExpression()); // Get the value
        System.out.println(stringify(value)); // Print the string representation of the value
        return null;
    }

    /**
     * Evaluates the return value, then throws a Return exception so that the function can be
     * exited. (this effectively, aborts the current level of interpretation and its state, effectively returning us
     * out of the function block scope).
     *
     * @param statement Return statement
     * @return Nothing
     */
    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        // Get the value
        Object value = null;
        if (statement.getValue() != null) value = executeExpression(statement.getValue());

        // Encapsulate the return value in an exception which is thrown to get us out of the function scope block
        throw new Return(value);
    }

    /**
     * Visit variable declaration statement (e.g. var ...).
     * Defines or overwrites a variable with defined name.
     *
     * @param statement Variable statement
     * @return Nothing, but produces a side effect (Variable declaration)
     */
    @Override
    public Void visitVariableStatement(Statement.Variable statement) {
        Object value = null;

        // If there is a value associated with this call e.g. (var identifier = value);
        // We want to calculate that value so that it can be stored, otherwise defaults to null (e.g. var identifier;)
        if (statement.getInitializer() != null) value = executeExpression(statement.getInitializer());

        environment.define(statement.getName().lexeme(), value); // Add variable to the current environment
        return null;
    }

    /**
     * Executes a while block of statements
     *
     * @param statement While statement
     * @return Nothing, but statements inside while can produce side effects
     */
    @Override
    public Void visitWhileStatement(Statement.While statement) {
        // While condition is true, execute the statements inside while
        while (isTruthy(executeExpression(statement.getCondition()))) {
            executeStatement(statement.getBody());
        }
        return null;
    }

    /**
     * Execute assignment expression (e.g. identifier = ...).
     * Identifier has to exist otherwise runtime error will be thrown.
     *
     * @param expression Assign expression
     * @return The newly assigned value
     */
    @Override
    public Object visitAssignExpression(Expression.Assign expression) {
        Object value = executeExpression(expression.getValue()); // Get the value

        // Assign the variable at the correct environment, it is either global (depth: 0) or one of the local ones
        Integer depth = locals.get(expression);
        if (depth != null) {
            environment.assignAt(depth, expression.getName(), value);
        } else {
            globals.assign(expression.getName(), value);
        }

        // Returns the newly assigned value
        return value;
    }

    /**
     * Interpret a binary expression
     *
     * @param expression Binary expression
     * @return Object result of interpreting a binary expression
     */
    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        Object left = executeExpression(expression.getLeft()); // Get left value
        Object right = executeExpression(expression.getRight()); // Get right value

        // Apply the correct operation
        switch (expression.getOperator().type()) {
            case BANG_EQUAL -> {
                return !isEqual(left, right);
            }
            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }
            case GREATER -> {
                // We can only do comparisons with numbers
                checkNumberOperands(expression.getOperator(), left, right);
                return (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expression.getOperator(), left, right);
                return (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expression.getOperator(), left, right);
                return (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expression.getOperator(), left, right);
                return (double) left <= (double) right;
            }
            case MINUS -> {
                checkNumberOperands(expression.getOperator(), left, right);
                return (double) left - (double) right;
            }
            case PLUS -> {
                // We are trying to add two numbers together
                if (left instanceof Double) {
                    if (right instanceof Double) return (double) left + (double) right;
                    if (right instanceof String) return stringify(left) + right;
                }

                // We are trying to concatenate two strings together
                if (left instanceof String) {
                    if (right instanceof String) return left + (String) right;
                    if (right instanceof Double) return left + stringify(right);
                }

                throw new RuntimeError(expression.getOperator(), "Operands must be numbers or strings.");
            }
            case SLASH -> {
                // If dividing by Zero, it will report infinity
                checkNumberOperands(expression.getOperator(), left, right);
                return (double) left / (double) right;
            }
            case STAR -> {
                checkNumberOperands(expression.getOperator(), left, right);
                return (double) left * (double) right;
            }
        }

        // This should be unreachable.
        return null;
    }

    /**
     * Execute a function or method call
     *
     * @param expression Function or method call node
     * @return Result of executing a method or a function
     */
    @Override
    public Object visitCallExpression(Expression.Call expression) {
        // Evaluate the callee definition and check if its result is an identifier that points to a
        // LoxCallable object (e.g. example: a returning pointer to b, being callable as a()();)
        // If callee does not reference a function, throw an error
        Object callee = executeExpression(expression.getCallee());
        if (!(callee instanceof LoxCallable function)) {
            throw new RuntimeError(expression.getParen(), "Can only call functions and classes.");
        }

        // Get arguments and store in temporary list
        List<Object> arguments = new ArrayList<>();
        for (Expression argument : expression.getArguments()) {
            arguments.add(executeExpression(argument));
        }

        // Check if number of arguments does not match the requested number of arguments
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expression.getParen(), "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }

        return function.call(this, arguments); // Execute the function and return its value
    }

    /**
     * Execute a node that gets attribute from an object
     *
     * @param expression Get expression
     * @return Value of the requested attribute.
     */
    @Override
    public Object visitGetExpression(Expression.Get expression) {
        // Execute expression ot get the name of the object
        Object object = executeExpression(expression.getObject());
        // If that name is an object, then return the value
        if (object instanceof LoxInstance) {
            return ((LoxInstance) object).get(expression.getName());
        }
        // Turns out that it is not an object, throw an error
        throw new RuntimeError(expression.getName(), "Only instances have properties.");
    }

    /**
     * Execute an expression inside parenthesis "(" and ")"
     *
     * @param expression Grouping node
     * @return Value of the evaluated expression
     */
    @Override
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return executeExpression(expression.getExpression());
    }

    /**
     * Return the value of the literal node.
     *
     * @param expression A literal node
     * @return Value of the literal node (not converted to anything, returned as a raw Object)
     */
    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return expression.getValue();
    }

    /**
     * Execute a logical "or", "and" expressions.
     *
     * @param expression Logical expression node.
     * @return Result of the logical expression as a raw Object
     */
    @Override
    public Object visitLogicalExpression(Expression.Logical expression) {
        Object left = executeExpression(expression.getLeft()); // Get left value

        if (expression.getOperator().type() == Token.Type.OR) {
            // Apply logical OR
            // If left is true then we know we can return left (true) since right doesn't matter
            if (isTruthy(left)) return left;
        } else {
            // Apply logical AND
            // If left is false then we know we can return left (false) since right doesn't matter
            if (!isTruthy(left)) return left;
        }

        // We couldn't "short circuit", examine the right side to determine the correct outcome
        return executeExpression(expression.getRight());
    }

    /**
     * Execute a node that sets attribute on an object
     *
     * @param expression Set node
     * @return Value of the newly set attribute
     */
    @Override
    public Object visitSetExpression(Expression.Set expression) {
        // Get the name of the object
        Object object = executeExpression(expression.getObject());

        // If that object is not actually an object or perhaps a variable, throw an error
        if (!(object instanceof LoxInstance)) {
            throw new RuntimeError(expression.getName(), "Only instances have fields.");
        }

        // Add the attribute value to the object
        Object value = executeExpression(expression.getValue());
        ((LoxInstance) object).set(expression.getName(), value);
        return value;
    }

    /**
     * Execute a call to the super class
     *
     * @param expression Super node
     * @return Result of a call to the super class method or attribute
     */
    @Override
    public Object visitSuperExpression(Expression.Super expression) {
        // Get the reference to the superclass from the current local environment
        int depth = locals.get(expression);
        LoxClass superclass = (LoxClass) environment.getAt(depth, "super");

        // Get the reference to the object (hence, depth-1)
        LoxInstance object = (LoxInstance) environment.getAt(depth - 1, "this");

        // Get reference to the method we want to call
        LoxFunction method = superclass.findMethod(expression.getMethod().lexeme());

        // If that method does not exist, throw an error
        if (method == null) {
            throw new RuntimeError(expression.getMethod(),
                    "Undefined property '" + expression.getMethod().lexeme() + "'.");
        }

        // Execute the method using the object scope
        return method.bind(object);
    }

    /**
     * Return the object referenced by "this" keyword
     *
     * @param expression This node
     * @return Variable referenced by "this" keyword (Depends on the current scope)
     */
    @Override
    public Object visitThisExpression(Expression.This expression) {
        return lookUpVariable(expression.getKeyword(), expression);
    }

    /**
     * Evaluate and complete the unary expression
     *
     * @param expression Unary expression
     * @return A result of the unary operation
     */
    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        Object right = executeExpression(expression.getRight());

        switch (expression.getOperator().type()) {
            case BANG -> {
                return !isTruthy(right);
            }
            case MINUS -> {
                checkNumberOperand(expression.getOperator(), right);
                return -(double) right;
            }
            case MINUS_MINUS -> {
                checkNumberOperand(expression.getOperator(), right);
                double converted = (double) right;
                return --converted;
            }
            case PLUS_PLUS -> {
                checkNumberOperand(expression.getOperator(), right);
                double converted = (double) right;
                return ++converted;
            }
        }

        // This should be unreachable.
        return null;
    }

    /**
     * Execute a Variable expression
     *
     * @param expression Var expression node
     * @return Value of the expression
     */
    @Override
    public Object visitVariableExpression(Expression.Variable expression) {
        return lookUpVariable(expression.getName(), expression);
    }
}