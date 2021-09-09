package us.walr.internal;

import us.walr.Interpreter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WalrusClass extends WalrusInstance implements WalrusCallable {
    private final String name;
    private final WalrusClass superclass;
    private final Map<String, WalrusFunction> methods;

    public WalrusClass(WalrusClass metaclass, String name, WalrusClass superclass, Map<String, WalrusFunction> methods) {
        super(metaclass);
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

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

    public String name() {
        return name;
    }

    public WalrusClass superclass() {
        return superclass;
    }

    public Map<String, WalrusFunction> methods() {
        return methods;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WalrusClass) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.superclass, that.superclass) &&
                Objects.equals(this.methods, that.methods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, superclass, methods);
    }

}
