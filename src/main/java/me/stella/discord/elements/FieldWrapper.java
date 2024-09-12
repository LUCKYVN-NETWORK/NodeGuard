package me.stella.discord.elements;

import org.json.simple.JSONObject;

public class FieldWrapper {

    private final String name;
    private final String value;
    private final boolean inLine;

    public FieldWrapper(String name, String value, boolean inline) {
        this.name = name;
        this.value = value;
        this.inLine = inline;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public boolean isInLine() {
        return this.inLine;
    }

    public String toString() {
        return "Field{" + getName() + ";" + getValue() + ";" + isInLine() + "}";
    }

    public JSONObject getJSONMapping() {
        JSONObject object = new JSONObject();
        object.put("name", getName());
        object.put("value", getValue());
        object.put("inline", isInLine());
        return object;
    }


}
