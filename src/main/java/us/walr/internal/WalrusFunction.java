package us.walr.internal;

import us.walr.Interpreter;
import us.walr.exceptions.Return;
import us.walr.grammar.Statement;

import java.util.List;

public record WalrusFunction(Statement.Function declaration, Environment closure, boolean isInitializer) implements WalrusCallable {

    /**
     * Bind user object to an object (or sub environment)
     *
     * @param instance User Object instance
     * @return This same function except bound to the user defined object
     */
    public WalrusFunction bind(WalrusInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new WalrusFunction(declaration, environment, isInitializer);
    }

    @Override
    public String toString() {
        return "<fn " + declaration.getName().lexeme() + ">";
    }

    /**
     * @return Number of required arguments
     */
    @Override
    public int numberOfArguments() {
        return declaration.getParams().size();
    }

    /**
     * Execute a user defined function
     *
     * @param interpreter Instance of the interpreter
     * @param arguments   List of arguments
     * @return Result of the function execution
     */
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // Create a local environment for the function
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.getParams().size(); i++) {
            environment.define(declaration.getParams().get(i).lexeme(), arguments.get(i));
        }

        try {
            // Execute the function body (which is a list of statements)
            interpreter.executeBlock(declaration.getBody(), environment);
        } catch (Return returnValue) {
            // Catch the return value "exception"
            // If this is object method, search for it in closure environment
            if (isInitializer) return closure.getAt(0, "this");
            return returnValue.getValue();
        }

        // Return nothing
        if (isInitializer) return closure.getAt(0, "this");
        return null;
    }
}
