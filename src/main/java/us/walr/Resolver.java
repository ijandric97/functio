package us.walr;

import us.walr.grammar.Expression;
import us.walr.grammar.Statement;
import us.walr.internal.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {
    /**
     * Reference to the current interpreter we wish to send scope resolution information to
     */
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    /**
     * Creates a new instance of scope resolver
     *
     * @param interpreter Instance of the interpreter that will be used to interpret
     */
    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Resolve a list of statements (this is the default method)
     *
     * @param statements List of statements
     */
    void resolve(List<Statement> statements) {
        for (Statement statement : statements) {
            resolve(statement);
        }
    }

    /**
     * Resolve a single statement
     *
     * @param statement Single statement
     */
    private void resolve(Statement statement) {
        statement.accept(this);
    }

    /**
     * Resolve a single expression
     *
     * @param expression Single expression
     */
    private void resolve(Expression expression) {
        expression.accept(this);
    }

    /**
     * Resolves which environment function should use
     *
     * @param function Function to resolve
     * @param type Type of the function (method or function)
     */
    private void resolveFunction(Statement.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : function.getParams()) {
            declare(param);
            define(param);
        }
        resolve(function.getBody());
        endScope();
        currentFunction = enclosingFunction;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme())) {
            Walrus.error(name, "Already a variable with this name in this scope.");
        }

        scope.put(name.lexeme(), false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme(), true);
    }

    private void resolveLocal(Expression expression, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme())) {
                interpreter.resolveDepth(expression, scopes.size() - 1 - i);
                return;
            }
        }
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        beginScope();
        resolve(statement.getStatements());
        endScope();
        return null;
    }

    @Override
    public Void visitClassStatement(Statement.Class statement) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(statement.getName());
        define(statement.getName());

        if (statement.getSuperclass() != null && statement.getName().lexeme().equals(statement.getSuperclass().getName().lexeme())) {
            Walrus.error(statement.getSuperclass().getName(), "A class can't inherit from itself.");
        }

        if (statement.getSuperclass() != null) {
            currentClass = ClassType.SUBCLASS;
            resolve(statement.getSuperclass());
        }

        if (statement.getSuperclass() != null) {
            beginScope();
            scopes.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("this", true);

        for (Statement.Function method : statement.getMethods()) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.getName().lexeme().equals("init")) {
                declaration = FunctionType.INITIALIZER;
            }

            resolveFunction(method, declaration);
        }

        endScope();

        if (statement.getSuperclass() != null) endScope();

        currentClass = enclosingClass;
        return null;
    }

    @Override
    public Void visitExpressionStatement(Statement.Expression statement) {
        resolve(statement.getExpression());
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement) {
        declare(statement.getName());
        define(statement.getName());

        // resolveFunction(stmt);
        resolveFunction(statement, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        resolve(statement.getCondition());
        resolve(statement.getThenBranch());
        if (statement.getElseBranch() != null) resolve(statement.getElseBranch());
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        resolve(statement.getExpression());
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        if (currentFunction == FunctionType.NONE) {
            Walrus.error(statement.getKeyword(), "Can't return from top-level code.");
        }

        if (statement.getValue() != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                Walrus.error(statement.getKeyword(), "Can't return a literal from an initializer.");
            }

            resolve(statement.getValue());
        }

        return null;
    }

    @Override
    public Void visitVariableStatement(Statement.Variable statement) {
        declare(statement.getName());
        if (statement.getInitializer() != null) {
            resolve(statement.getInitializer());
        }
        define(statement.getName());
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        resolve(statement.getCondition());
        resolve(statement.getBody());
        return null;
    }

    @Override
    public Void visitAssignExpression(Expression.Assign expression) {
        resolve(expression.getValue());
        resolveLocal(expression, expression.getName());
        return null;
    }

    @Override
    public Void visitBinaryExpression(Expression.Binary expression) {
        resolve(expression.getLeft());
        resolve(expression.getRight());
        return null;
    }

    @Override
    public Void visitCallExpression(Expression.Call expression) {
        resolve(expression.getCallee());

        for (Expression argument : expression.getArguments()) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGetExpression(Expression.Get expression) {
        resolve(expression.getObject());
        return null;
    }

    @Override
    public Void visitGroupingExpression(Expression.Grouping expression) {
        resolve(expression.getExpression());
        return null;
    }

    @Override
    public Void visitLiteralExpression(Expression.Literal expression) {
        return null;
    }

    @Override
    public Void visitLogicalExpression(Expression.Logical expression) {
        resolve(expression.getLeft());
        resolve(expression.getRight());
        return null;
    }

    @Override
    public Void visitSetExpression(Expression.Set expression) {
        resolve(expression.getValue());
        resolve(expression.getObject());
        return null;
    }

    @Override
    public Void visitSuperExpression(Expression.Super expression) {
        if (currentClass == ClassType.NONE) {
            Walrus.error(expression.getKeyword(), "Can't use 'super' outside of a class.");
        } else if (currentClass != ClassType.SUBCLASS) {
            Walrus.error(expression.getKeyword(), "Can't use 'super' in a class with no superclass.");
        }

        resolveLocal(expression, expression.getKeyword());
        return null;
    }

    @Override
    public Void visitThisExpression(Expression.This expression) {
        if (currentClass == ClassType.NONE) {
            Walrus.error(expression.getKeyword(),
                    "Can't use 'this' outside of a class.");
            return null;
        }

        resolveLocal(expression, expression.getKeyword());
        return null;
    }

    @Override
    public Void visitUnaryExpression(Expression.Unary expression) {
        resolve(expression.getRight());
        return null;
    }

    @Override
    public Void visitVariableExpression(Expression.Variable expression) {
        if (!scopes.isEmpty() &&
                scopes.peek().get(expression.getName().lexeme()) == Boolean.FALSE) {
            Walrus.error(expression.getName(), "Can't read local variable in its own initializer.");
        }

        resolveLocal(expression, expression.getName());
        return null;
    }

    private enum FunctionType {
        NONE,
        // FUNCTION
        FUNCTION,
        INITIALIZER,
        METHOD
    }

    private enum ClassType {
        NONE,
        // CLASS
        CLASS,
        SUBCLASS
    }
}
