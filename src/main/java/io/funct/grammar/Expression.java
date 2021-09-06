package io.funct.grammar;

import io.funct.internal.Token;

import java.util.List;

public abstract class Expression {
    public abstract <R> R accept(Visitor<R> visitor);

    public interface Visitor<R> {
        R visitAssignExpression(Assign expression);

        R visitBinaryExpression(Binary expression);

        R visitCallExpression(Call expression);

        R visitGetExpression(Get expression);

        R visitGroupingExpression(Grouping expression);

        R visitLiteralExpression(Literal expression);

        R visitLogicalExpression(Logical expression);

        R visitSetExpression(Set expression);

        R visitSuperExpression(Super expression);

        R visitThisExpression(This expression);

        R visitUnaryExpression(Unary expression);

        R visitVariableExpression(Variable expression);
    }

    public static class Assign extends Expression {
        final Token name;
        final Expression value;

        public Assign(Token name, Expression value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpression(this);
        }

        public Token getName() {
            return name;
        }

        public Expression getValue() {
            return value;
        }
    }

    public static class Binary extends Expression {
        final Expression left;
        final Token operator;
        final Expression right;

        public Binary(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpression(this);
        }

        public Expression getLeft() {
            return left;
        }

        public Token getOperator() {
            return operator;
        }

        public Expression getRight() {
            return right;
        }
    }

    public static class Call extends Expression {
        final Expression callee;
        final Token paren;
        final List<Expression> arguments;

        public Call(Expression callee, Token paren, List<Expression> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpression(this);
        }

        public Expression getCallee() {
            return callee;
        }

        public Token getParen() {
            return paren;
        }

        public List<Expression> getArguments() {
            return arguments;
        }
    }

    public static class Get extends Expression {
        final Expression object;
        final Token name;

        public Get(Expression object, Token name) {
            this.object = object;
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpression(this);
        }

        public Expression getObject() {
            return object;
        }

        public Token getName() {
            return name;
        }
    }

    public static class Grouping extends Expression {
        final Expression expression;

        public Grouping(Expression expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpression(this);
        }

        public Expression getExpression() {
            return expression;
        }
    }

    public static class Literal extends Expression {
        final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpression(this);
        }

        public Object getValue() {
            return value;
        }
    }

    public static class Logical extends Expression {
        final Expression left;
        final Token operator;
        final Expression right;

        public Logical(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpression(this);
        }

        public Expression getLeft() {
            return left;
        }

        public Token getOperator() {
            return operator;
        }

        public Expression getRight() {
            return right;
        }
    }

    public static class Set extends Expression {
        final Expression object;
        final Token name;
        final Expression value;

        public Set(Expression object, Token name, Expression value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpression(this);
        }

        public Expression getObject() {
            return object;
        }

        public Token getName() {
            return name;
        }

        public Expression getValue() {
            return value;
        }
    }

    public static class Super extends Expression {
        final Token keyword;
        final Token method;

        public Super(Token keyword, Token method) {
            this.keyword = keyword;
            this.method = method;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSuperExpression(this);
        }

        public Token getKeyword() {
            return keyword;
        }

        public Token getMethod() {
            return method;
        }
    }

    public static class This extends Expression {
        final Token keyword;

        public This(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisExpression(this);
        }

        public Token getKeyword() {
            return keyword;
        }
    }

    public static class Unary extends Expression {
        final Token operator;
        final Expression right;

        public Unary(Token operator, Expression right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpression(this);
        }

        public Token getOperator() {
            return operator;
        }

        public Expression getRight() {
            return right;
        }
    }

    public static class Variable extends Expression {
        final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpression(this);
        }

        public Token getName() {
            return name;
        }
    }
}
