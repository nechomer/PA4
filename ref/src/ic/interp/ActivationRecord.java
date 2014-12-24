package ic.interp;

import java.util.HashMap;
import java.util.Map;

public class ActivationRecord {
    Map<String, Object> variables;

    public ActivationRecord() {
        variables = new HashMap<>();
    }

    public void addNewVariable(String name, Object var) {
        variables.put(name, var);
    }

    public boolean variableExist(String name) {
        return variables.containsKey(name);
    }

    public Object getVariableValue(String name) {
        return variables.get(name);
    }
}
