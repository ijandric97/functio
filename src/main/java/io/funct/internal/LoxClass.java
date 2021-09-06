package io.funct.internal;

import io.funct.Interpreter;

import java.util.List;
import java.util.Map;

public record LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) implements LoxCallable {
    /**
     * @param name Name of the method we are searching for
     * @return Returns the method identifier (callee) which we can then execute
     */
    public LoxFunction findMethod(String name) {
        // Search in current object
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        // If not found and parent exist, search in the parent
        if (superclass != null) {
            return superclass.findMethod(name);
        }

        return null;
    }

    /**
     * @return Prints the name of the class
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Calls the constructor
     *
     * @param interpreter Instance of the interpreter
     * @param arguments   List of arguments for the constructor
     * @return The reference to the user object
     */
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    /**
     * @return Number of arguments needed for constructor
     */
    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }
}
