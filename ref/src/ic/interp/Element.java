package ic.interp;

public class Element {
    String type = null;
    String name = null;
    Object value;

    public Element(Object value, String type) {
        this.value = value;
        this.type = type;
    }

    public Element(Object value, String name, String type) {
        this.value = value;
        this.name = name;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        String stringValue = "";
        if (value instanceof Element[]) {
            for (Element element : (Element[]) value) {
                stringValue += element.getValue().toString() + "\n";
            }
        } else
            stringValue += value.toString();
        return stringValue;
    }
}
