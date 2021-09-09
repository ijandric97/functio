package us.walr.internal;

import us.walr.exceptions.RuntimeError;

import java.util.HashMap;
import java.util.Map;

public class WalrusInstance {
    private final WalrusClass walrusClass;
    private final Map<String, Object> fields = new HashMap<>();

    WalrusInstance(WalrusClass klass) {
        this.walrusClass = klass;
    }

    /**
     * Get the value of attribute
     *
     * @param name Name of the attribute
     * @return Value of the requested attribute (field, property)
     */
    public Object get(Token name) {
        if (fields.containsKey(name.lexeme())) {
            return fields.get(name.lexeme());
        }

        WalrusFunction method = walrusClass.findMethod(name.lexeme());
        if (method != null) return method.bind(this);

        throw new RuntimeError(name, "Undefined property '" + name.lexeme() + "'.");
    }

    /**
     * Write a value to the attribute
     *
     * @param name  name of the attribute
     * @param value Value we wish to write to that attribute
     */
    public void set(Token name, Object value) {
        fields.put(name.lexeme(), value);
    }

    /**
     * @return Prints name of the class and instance
     */
    @Override
    public String toString() {
        return walrusClass.name() + " instance";
    }
}
