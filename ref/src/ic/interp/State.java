package ic.interp;

import java.util.ListIterator;
import java.util.Stack;

public class State {
    Stack<ActivationRecord> a_stack = new Stack<>();

    State() {
        a_stack.push(new ActivationRecord());
    }

    /**
     * Find a variable by its name (dynamic lookup).
     */
    Object lookup(String byName) {
        ListIterator<ActivationRecord> iter =
                a_stack.listIterator(a_stack.size());
        while (iter.hasPrevious()) {
            ActivationRecord ar = iter.previous();
            if (ar.variableExist(byName))
                return ar.getVariableValue(byName);
        }
        throw new Interpreter.RuntimeError("Undefined variable '" + byName + "'");
    }

    public boolean varExist(String byName) {
        ListIterator<ActivationRecord> iter = a_stack.listIterator(a_stack.size());
        while (iter.hasPrevious()) {
            ActivationRecord ar = iter.previous();
            if (ar.variableExist(byName))
                return true;
        }
        return false;
    }
}
