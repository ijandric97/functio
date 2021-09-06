package io.funct.tools;

import io.funct.grammar.Expression;
import io.funct.grammar.Statement;
import io.funct.internal.Token;

import java.util.List;

/**
 * Pretty print the AST nodes. The result looks like polish notation (prefix).
 */
class AstPrinter implements Expression.Visitor<String>, Statement.Visitor<String> {
    private int notation = 1; // 0 - Postfix (RPN), 1 - Prefix (PN)

    /**
     * Runs the ASTPrinter with the example expression.
     * Used to verify that the AST Printer is working as expected.
     *
     * @param args Command line parameters - WILL BE IGNORED
     */
    public static void main(String[] args) {
        var astPrinter = new AstPrinter();

        // Example expression
        Expression expression = new Expression.Binary(
                new Expression.Unary(
                        new Token(Token.Type.MINUS, "-", null, 1),
                        new Expression.Literal(123)
                ),
                new Token(Token.Type.STAR, "*", null, 1),
                new Expression.Grouping(
                        new Expression.Literal(45.67)
                )
        );

        System.out.println("Testing the test expression...");
        System.out.println(astPrinter.print(expression));

        expression = new Expression.Binary(
                new Expression.Binary(
                        new Expression.Grouping(
                                new Expression.Binary(
                                        new Expression.Literal(2),
                                        new Token(Token.Type.STAR, "+", null, 1),
                                        new Expression.Literal(1)
                                )
                        ),
                        new Token(Token.Type.LESS_EQUAL, "<=", null, 1),
                        new Expression.Literal(3)
                ),
                new Token(Token.Type.EQUAL_EQUAL, "==", null, 1),
                new Expression.Literal(true)
        );


        System.out.println("Testing the test expression...");
        System.out.println(astPrinter.print(expression));

        // Test different notations
        expression = new Expression.Binary(
                new Expression.Binary(
                        new Expression.Literal(1),
                        new Token(Token.Type.PLUS, "+", null, 1),
                        new Expression.Literal(2)
                ),
                new Token(Token.Type.STAR, "*", null, 1),
                new Expression.Binary(
                        new Expression.Literal(4),
                        new Token(Token.Type.MINUS, "-", null, 1),
                        new Expression.Literal(3)
                )
        );

        System.out.println("Testing ASTPrinter notations...");
        System.out.println(" prefix: " + astPrinter.print(expression)); // Print prefix - PN
        System.out.println("postfix: " + astPrinter.printReverse(expression)); // Print postfix - RPN
    }

    /**
     * Returns infix-like representation of the provided AST expression.
     *
     * @param expression AST nodes
     * @return Infix (Polish-notation) like representation of the provided Abstract Syntax Tree (AST).
     */
    public String print(Expression expression) {
        notation = 1;
        return expression.accept(this);
    }

    /**
     * Returns infix-like representation of the provided AST statement.
     *
     * @param statement AST nodes
     * @return Infix (Polish-notation) like representation of the provided Abstract Syntax Tree (AST).
     */
    String print(Statement statement) {
        notation = 1;
        return statement.accept(this);
    }

    /**
     * Returns postfix-like representation of the provided AST expression.
     *
     * @param expression AST nodes
     * @return Postfix (Reverse Polish Notation) like representation of the provided Abstract Syntax Tree (AST).
     */
    public String printReverse(Expression expression) {
        notation = 0;
        return expression.accept(this);
    }

    /**
     * Returns postfix-like representation of the provided AST statement.
     *
     * @param statement AST nodes
     * @return Postfix (Reverse Polish Notation) like representation of the provided Abstract Syntax Tree (AST).
     */
    String printReverse(Statement statement) {
        notation = 0;
        return statement.accept(this);
    }

    /**
     * Pretty prints the block statement ("{", "}") and its subtree.
     *
     * @param statement The block ("{" declaration "}") subtree.
     * @return Infix or postfix representation of the Block statement subtree
     */
    @Override
    public String visitBlockStatement(Statement.Block statement) {
        StringBuilder builder = new StringBuilder();
        builder.append("(block ");

        for (Statement subStatement : statement.getStatements()) {
            builder.append(subStatement.accept(this));
        }

        builder.append(")");
        return builder.toString();
    }

    /**
     * Pretty prints the class statement and its subtree.
     *
     * @param statement The class statement subtree (contains sub functions)
     * @return Infix or postfix representation of the Class statement and its subtree
     */
    @Override
    public String visitClassStatement(Statement.Class statement) {
        StringBuilder builder = new StringBuilder();
        builder.append("(class ").append(statement.getName().lexeme());

        if (statement.getSuperclass() != null) {
            builder.append(" < ").append(print(statement.getSuperclass()));
        }

        for (Statement.Function method : statement.getMethods()) {
            builder.append(" ").append(print(method));
        }

        builder.append(")");
        return builder.toString();
    }

    /**
     * Pretty print the expression statement
     *
     * @param statement Expression subtree
     * @return Infix or postfix representation of the expression.
     */
    @Override
    public String visitExpressionStatement(Statement.Expression statement) {
        return parenthesize(";", statement.getExpression());
    }

    /**
     * Pretty print the function statement
     *
     * @param statement Function definition statement
     * @return Pretty print representation of the function definition
     */
    @Override
    public String visitFunctionStatement(Statement.Function statement) {
        StringBuilder builder = new StringBuilder();
        builder.append("(fun ").append(statement.getName().lexeme()).append("(");

        for (Token param : statement.getParams()) {
            if (param != statement.getParams().get(0)) builder.append(" ");
            builder.append(param.lexeme());
        }

        builder.append(") ");

        for (Statement body : statement.getBody()) {
            builder.append(body.accept(this));
        }

        builder.append(")");
        return builder.toString();
    }

    /**
     * Pretty print the if statement
     *
     * @param statement If statement node and subtree
     * @return Infix or postfix representation of the if statement and its choices
     */
    @Override
    public String visitIfStatement(Statement.If statement) {
        if (statement.getElseBranch() == null)
            return parenthesize("if", statement.getCondition(), statement.getThenBranch());

        return parenthesize("if-else", statement.getCondition(), statement.getThenBranch(), statement.getElseBranch());
    }

    /**
     * Pretty print the print statement.
     *
     * @param statement Print statement expression tree
     * @return Infix or postfix representation of the print subtree
     */
    @Override
    public String visitPrintStatement(Statement.Print statement) {
        return parenthesize("print", statement.getExpression());
    }

    /**
     * Print method return statement.
     *
     * @param statement Method return statement subtree.
     * @return Infix or postfix representation of the method return.
     */
    @Override
    public String visitReturnStatement(Statement.Return statement) {
        if (statement.getValue() == null) return "(return)";
        return parenthesize("return", statement.getValue());
    }

    /**
     * Pretty prints variable initialization.
     *
     * @param statement Variable initialization statement.
     * @return Infix or postfix representation of the variable initialization.
     */
    @Override
    public String visitVariableStatement(Statement.Variable statement) {
        if (statement.getInitializer() == null) {
            return parenthesize("var", statement.getName());
        }

        return parenthesize("var", statement.getName(), "=", statement.getInitializer());
    }

    /**
     * Pretty prints while loop and its content.
     *
     * @param statement While statement and its subtree.
     * @return Infix or postfix representation of the while loop.
     */
    @Override
    public String visitWhileStatement(Statement.While statement) {
        return parenthesize("while", statement.getCondition(), statement.getBody());
    }

    /**
     * Pretty print the variable assignment subtree.
     *
     * @param expression Assignment subtree.
     * @return Infix or postfix representation of the assignment expression subtree.
     */
    @Override
    public String visitAssignExpression(Expression.Assign expression) {
        return parenthesize("=", expression.getName().lexeme(), expression.getValue());
    }

    /**
     * Visitor for pretty printing the binary operation subtree.
     * <p>
     * (For example "2+2" is a binary expression).
     *
     * @param expression The binary (expression operator expression) AST subtree.
     * @return Infix or postfix representation of the Binary expression subtree
     */
    @Override
    public String visitBinaryExpression(Expression.Binary expression) {
        return parenthesize(expression.getOperator().lexeme(), expression.getLeft(), expression.getRight());
    }

    /**
     * Pretty print a function call subtree.
     *
     * @param expression Function call expression subtree.
     * @return Infix or postfix representation of the function call.
     */
    @Override
    public String visitCallExpression(Expression.Call expression) {
        return parenthesize("call", expression.getCallee(), expression.getArguments());
    }

    /**
     * Pretty prints a getter method.
     *
     * @param expression A getter subtree.
     * @return Infix or postfix representation of the getter method.
     */
    @Override
    public String visitGetExpression(Expression.Get expression) {
        return parenthesize(".", expression.getObject(), expression.getName().lexeme());
    }

    /**
     * Visitor for pretty printing the expression subtree inside the "(...)" brackets, which we call the grouping
     * AST node and its subtree.
     *
     * @param expression The Grouping (literally subexpression inside "( ...)" ) expression subtree
     * @return Infix or postfix representation of the Grouping Expression subtree
     */
    @Override
    public String visitGroupingExpression(Expression.Grouping expression) {
        return parenthesize("group", expression.getExpression());
    }

    /**
     * Visitor for pretty printing the literal value tree node.
     *
     * @param expression The Literal (number, string, boolean, nil) AST node.
     * @return Infix or postfix representation of the Literal Value
     */
    @Override
    public String visitLiteralExpression(Expression.Literal expression) {
        if (expression.getValue() == null) return "nil";
        return expression.getValue().toString();
    }

    /**
     * Pretty prints the logical expression subtree.
     *
     * @param expression Logical expression (bool algebra) subtree
     * @return Infix or postfix representation of the logical expression
     */
    @Override
    public String visitLogicalExpression(Expression.Logical expression) {
        return parenthesize(expression.getOperator().lexeme(), expression.getLeft(), expression.getRight());
    }

    /**
     * Pretty prints a setter method.
     *
     * @param expression A setter subtree.
     * @return Infix or postfix representation of the setter method.
     */
    @Override
    public String visitSetExpression(Expression.Set expression) {
        return parenthesize("=", expression.getObject(), expression.getName().lexeme(), expression.getValue());
    }

    /**
     * Pretty prints the call to a super().* methods.
     *
     * @param expression Super class expression subtree.
     * @return Infix or postfix representation of the superclass expression subtree
     */
    @Override
    public String visitSuperExpression(Expression.Super expression) {
        return parenthesize("super", expression.getMethod());
    }

    /**
     * Prints out the "this" keyword
     *
     * @param expression "This" keyword expression
     * @return "this" string
     */
    @Override
    public String visitThisExpression(Expression.This expression) {
        return "this";
    }

    /**
     * Visitor for pretty printing the unary expression subtree.
     *
     * @param expression The Unary ("-" and "!") expression tree
     * @return Infix or postfix representation of the Unary Expression subtree
     */
    @Override
    public String visitUnaryExpression(Expression.Unary expression) {
        return parenthesize(expression.getOperator().lexeme(), expression.getRight());
    }

    /**
     * Prints out the lexeme that is used as a name of the variable
     *
     * @param expression The variable expression tree
     * @return Name of the variable
     */
    @Override
    public String visitVariableExpression(Expression.Variable expression) {
        return expression.getName().lexeme();
    }

    /**
     * Encloses the given Object parts in parentheses "(", ")" to better represent the pretty print structure.
     *
     * @param name  Name of the node we want to print (e.g. grouping)
     * @param parts Attributes or children of the node
     * @return Infix or postfix representation of the given Objects.
     */
    private String parenthesize(String name, Object... parts) {
        StringBuilder builder = new StringBuilder();

        builder.append("(");
        if (notation == 1) builder.append(name);

        transform(builder, parts);  // The actual parentheses applying logic

        if (notation == 0) builder.append(name);
        builder.append(")");

        return builder.toString();
    }

    /**
     * A helper method to parenthesize() method. It contains interpreter pattern inside it that decides how to properly
     * apply parentheses depending on the type of the Node currently traversed.
     *
     * @param builder Instance to the existing string builder defined in parent method
     * @param parts   Attributes or children of the node
     */
    private void transform(StringBuilder builder, Object... parts) {
        for (Object part : parts) {
            if (notation == 1) builder.append(" ");

            // Sort-of interpreter pattern
            if (part instanceof Expression) {
                // Just visit the defined visitor for the expression
                builder.append(((Expression) part).accept(this));
            } else if (part instanceof Statement) {
                // Just visit the defined visitor for the statement
                builder.append(((Statement) part).accept(this));
            } else if (part instanceof Token) {
                // Print out the lexeme of the Token in question
                builder.append(((Token) part).lexeme());
            } else if (part instanceof List) {
                // A subtree, recursively run transform on it
                //noinspection rawtypes
                transform(builder, ((List) part).toArray());
            } else {
                // Unknown territory, print the string representation of the object
                builder.append(part);
            }

            if (notation == 0) builder.append(" ");
        }
    }
}
