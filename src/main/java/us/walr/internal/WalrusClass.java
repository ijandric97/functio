package us.walr.internal;

import us.walr.Interpreter;

import java.util.List;
import java.util.Map;

public record WalrusClass(String name, WalrusClass superclass, Map<String, WalrusFunction> methods) implements WalrusCallable {
    /**
     * @param name Name of the method we are searching for
     * @return Returns the method identifier (callee) which we can then execute
     */
    public WalrusFunction findMethod(String name) {
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
        WalrusInstance instance = new WalrusInstance(this);
        WalrusFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    /**
     * @return Number of arguments needed for constructor
     */
    @Override
    public int numberOfArguments() {
        WalrusFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.numberOfArguments();
    }
}
