package io.funct.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * A standalone tool to generate Expression.java and Statement.java classes that are used to represent the
 * abstract syntax tree (AST) nodes. Our Context-Free grammar (Metasyntax) is essentially a blueprint of our language
 * rules, and that metasyntax will be converted to the AST.
 */
public class GenerateGrammarAST {
    public static void main(String[] args) throws IOException {
        // If we did not provide the output file, throw an error
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        // Generate the Expression.java file
        generateGrammarASTClass(outputDir, "Expression", Arrays.asList(
                "Assign   : Token name, Expression value",
                "Binary   : Expression left, Token operator, Expression right",
                "Call     : Expression callee, Token paren, List<Expression> arguments",
                "Get      : Expression object, Token name",
                "Grouping : Expression expression",
                "Literal  : Object value",
                "Logical  : Expression left, Token operator, Expression right",
                "Set      : Expression object, Token name, Expression value",
                "Super    : Token keyword, Token method",
                "This     : Token keyword",
                "Unary    : Token operator, Expression right",
                "Variable : Token name"
        ));

        // Generate the Statement.java file
        generateGrammarASTClass(outputDir, "Statement", Arrays.asList(
                "Block      : List<Statement> statements",
                "Class      : Token name, io.funct.grammar.Expression.Variable superclass," + " List<Statement.Function> methods",
                "Expression : io.funct.grammar.Expression expression",
                "Function   : Token name, List<Token> params," + " List<Statement> body",
                "If         : io.funct.grammar.Expression condition, Statement thenBranch," + " Statement elseBranch",
                "Print      : io.funct.grammar.Expression expression",
                "Return     : Token keyword, io.funct.grammar.Expression value",
                "Variable   : Token name, io.funct.grammar.Expression initializer",
                "While      : io.funct.grammar.Expression condition, Statement body"
        ));
    }

    /**
     * Generates AST syntax tree nodes definition java class file.
     *
     * @param outputDir Path to the output direction
     * @param baseName  Name of the class and therefore the file
     * @param types     List of AST node types in format ["Name : VarType VarName, VarType...", ...]
     * @throws IOException Output directory could not be found.
     */
    private static void generateGrammarASTClass(String outputDir, String baseName, List<String> types) throws IOException {
        // Setup our writer and the output dir
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        // Write package, imports and class name
        writer.print("""
                package io.funct.grammar;
                                
                import io.funct.records.Token;
                                
                import java.util.List;
                                
                public abstract class %s {
                """.formatted(baseName));

        // The base accept() method.
        writer.println("\tpublic abstract <R> R accept(Visitor<R> visitor);");
        writer.println();

        // Generate the visitor interface
        generateVisitor(writer, baseName, types);

        // The AST nodes classes
        for (int i = 0, typesSize = types.size(); i < typesSize; i++) {
            String type = types.get(i);
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            generateType(writer, baseName, className, fields);

            if (i != typesSize - 1) writer.println();
        }

        // The closing bracket and close our writer
        writer.println("}");
        writer.close();
    }

    /**
     * Generates the visitor interface, visitor method for each type and abstract accept method to accept the visiting
     * class method.
     * <p>
     * This is our solution to the "expression problem". The visitor design pattern allows us to define a new operation
     * for classes without changing the classes.
     *
     * @param writer   Reference to the main PrintWriter object
     * @param baseName Name of the class and therefore the file
     * @param types    List of AST node types in format ["Name : VarType VarName, VarType...", ...]
     */
    private static void generateVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("\tpublic interface Visitor<R> {");

        for (int i = 0, typesSize = types.size(); i < typesSize; i++) {
            String type = types.get(i);
            String typeName = type.split(":")[0].trim();
            writer.println("\t\tR visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");

            if (i != typesSize-1) writer.println();
        }

        writer.println("\t}");
        writer.println();
    }

    /**
     * Generates AST syntax tree node types (subclasses)
     *
     * @param writer    Reference to the main PrintWriter object
     * @param baseName  Name of the class and therefore the file
     * @param className Name of the subclass and therefore the node type
     * @param fieldList List of fields that this AST node type holds
     */
    private static void generateType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("\tpublic static class " + className + " extends " + baseName + " {");

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");

        // Fields.
        for (String field : fields) {
            writer.println("\t\tfinal " + field + ";");
        }

        // Constructor.
        writer.println("\n\t\tpublic " + className + "(" + fieldList + ") {");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("\t\t\tthis." + name + " = " + name + ";");
        }
        writer.println("\t\t}\n");

        // Visitor pattern.
        writer.println("\t\t@Override");
        writer.println("\t\tpublic <R> R accept(Visitor<R> visitor) {");
        writer.println("\t\t\treturn visitor.visit" + className + baseName + "(this);");
        writer.println("\t\t}\n");

        // Access methods
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            String type = field.split(" ")[0];
            String name = field.split(" ")[1];
            String capitalized = name.substring(0, 1).toUpperCase() + name.substring(1);

            writer.println("\t\tpublic " + type + " get" + capitalized + "() {");
            writer.println("\t\t\treturn " + name + ";");
            writer.println("\t\t}");

            if (i != fields.length - 1) writer.println();
        }

        writer.println("\t}");
    }
}