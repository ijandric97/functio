package io.funct.internal;

import io.funct.exceptions.RuntimeError;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * Get the value of the variable from this or enclosing environment
     *
     * @param name Name of the variable
     * @return Value of the variable
     */
    public Object get(Token name) {
        if (values.containsKey(name.lexeme())) {
            return values.get(name.lexeme());
        }

        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }

    /**
     * Gets the value of the variable from a local environment related to this one
     *
     * @param depth The depth (Distance) at which the environment we want to search in is located
     * @param name  Name of the variable
     * @return Value of the variable
     */
    public Object getAt(int depth, String name) {
        return ancestor(depth).values.get(name);
    }

    /**
     * Define a variable. If it exists it will be overwritten.
     *
     * @param name  The name of the variable.
     * @param value The value of the variable.
     */
    public void define(String name, Object value) {
        values.put(name, value);
    }

    /**
     * Redefine a variable. If it doesn't exist will throw an error.
     *
     * @param name  The name of the existing variable.
     * @param value The value of the variable.
     */
    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme())) {
            values.put(name.lexeme(), value); // Everything is ok, redefine it
        } else if (enclosing != null) {
            enclosing.assign(name, value);
        } else {
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
        }
    }

    /**
     * Redefine a variable value at a local environment related to this one
     *
     * @param depth The depth (Distance) at which the environment we want to search in is located
     * @param name  Name of the variable
     * @param value Value which we wish to assign to the variable
     */
    public void assignAt(int depth, Token name, Object value) {
        ancestor(depth).values.put(name.lexeme(), value);
    }


    /**
     * Find a local environment related to this one
     *
     * @param depth Depth of the ancestor we want to find
     * @return The environment which we wanted to find
     */
    Environment ancestor(int depth) {
        Environment environment = this;
        for (int i = 0; i < depth; i++) {
            if (environment != null) {
                environment = environment.enclosing;
            }
        }

        return environment;
    }

    /**
     * @return Returns enclosing environment
     */
    public Environment getEnclosing() {
        return enclosing;
    }
}
