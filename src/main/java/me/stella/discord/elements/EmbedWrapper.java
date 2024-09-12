package me.stella.discord.elements;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;

public class EmbedWrapper {

    private final String title;
    private final String description;
    private final int color;
    private final List<FieldWrapper> fields;

    public EmbedWrapper(String title, String description, int color, List<FieldWrapper> fields) {
        this.title = title;
        this.description = description;
        this.color = color;
        this.fields = fields;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getColor() {
        return color;
    }

    public List<FieldWrapper> getFields() {
        return fields;
    }

    public JSONObject getJSONMapping() {
        JSONObject object = new JSONObject();
        object.put("title", getTitle());
        object.put("type", "rich");
        object.put("description",  getDescription());
        object.put("color", getColor());
        JSONArray fields = new JSONArray();
        getFields().forEach(field -> {
            fields.add(field.getJSONMapping());
        });
        object.put("fields", fields);
        return object;
    }
}
