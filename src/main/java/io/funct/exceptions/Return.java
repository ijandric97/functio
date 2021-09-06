package io.funct.exceptions;

public class Return extends RuntimeException {
    /**
     * Encapsulated value
     */
    final Object value;

    /**
     * @param value Value to encapsulate in the return exception
     */
    public Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }

    /**
     * @return Get the encapsulated value
     */
    public Object getValue() {
        return value;
    }
}
