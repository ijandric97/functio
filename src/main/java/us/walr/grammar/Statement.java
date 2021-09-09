package us.walr.grammar;

import us.walr.internal.Token;

import java.util.List;

public abstract class Statement {
    public abstract <R> R accept(Visitor<R> visitor);

    public interface Visitor<R> {
        R visitBlockStatement(Block statement);

        R visitClassStatement(Class statement);

        R visitExpressionStatement(Expression statement);

        R visitFunctionStatement(Function statement);

        R visitIfStatement(If statement);

        R visitPrintStatement(Print statement);

        R visitReturnStatement(Return statement);

        R visitVariableStatement(Variable statement);

        R visitWhileStatement(While statement);
    }

    public static class Block extends Statement {
        final List<Statement> statements;

        public Block(List<Statement> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStatement(this);
        }

        public List<Statement> getStatements() {
            return statements;
        }
    }

    public static class Class extends Statement {
        final Token name;
        final us.walr.grammar.Expression.Variable superclass;
        final List<Statement.Function> methods;

        public Class(Token name, us.walr.grammar.Expression.Variable superclass, List<Statement.Function> methods) {
            this.name = name;
            this.superclass = superclass;
            this.methods = methods;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassStatement(this);
        }

        public Token getName() {
            return name;
        }

        public us.walr.grammar.Expression.Variable getSuperclass() {
            return superclass;
        }

        public List<Statement.Function> getMethods() {
            return methods;
        }
    }

    public static class Expression extends Statement {
        final us.walr.grammar.Expression expression;

        public Expression(us.walr.grammar.Expression expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStatement(this);
        }

        public us.walr.grammar.Expression getExpression() {
            return expression;
        }
    }

    public static class Function extends Statement {
        final Token name;
        final List<Token> params;
        final List<Statement> body;

        public Function(Token name, List<Token> params, List<Statement> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStatement(this);
        }

        public Token getName() {
            return name;
        }

        public List<Token> getParams() {
            return params;
        }

        public List<Statement> getBody() {
            return body;
        }
    }

    public static class If extends Statement {
        final us.walr.grammar.Expression condition;
        final Statement thenBranch;
        final Statement elseBranch;

        public If(us.walr.grammar.Expression condition, Statement thenBranch, Statement elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStatement(this);
        }

        public us.walr.grammar.Expression getCondition() {
            return condition;
        }

        public Statement getThenBranch() {
            return thenBranch;
        }

        public Statement getElseBranch() {
            return elseBranch;
        }
    }

    public static class Print extends Statement {
        final us.walr.grammar.Expression expression;

        public Print(us.walr.grammar.Expression expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStatement(this);
        }

        public us.walr.grammar.Expression getExpression() {
            return expression;
        }
    }

    public static class Return extends Statement {
        final Token keyword;
        final us.walr.grammar.Expression value;

        public Return(Token keyword, us.walr.grammar.Expression value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStatement(this);
        }

        public Token getKeyword() {
            return keyword;
        }

        public us.walr.grammar.Expression getValue() {
            return value;
        }
    }

    public static class Variable extends Statement {
        final Token name;
        final us.walr.grammar.Expression initializer;

        public Variable(Token name, us.walr.grammar.Expression initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableStatement(this);
        }

        public Token getName() {
            return name;
        }

        public us.walr.grammar.Expression getInitializer() {
            return initializer;
        }
    }

    public static class While extends Statement {
        final us.walr.grammar.Expression condition;
        final Statement body;

        public While(us.walr.grammar.Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStatement(this);
        }

        public us.walr.grammar.Expression getCondition() {
            return condition;
        }

        public Statement getBody() {
            return body;
        }
    }
}
