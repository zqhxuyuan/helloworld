package rushmore.zbus.common.json.parser.deserializer;

import java.lang.reflect.Type;

import rushmore.zbus.common.json.JSONArray;
import rushmore.zbus.common.json.parser.DefaultJSONParser;
import rushmore.zbus.common.json.parser.JSONToken;

public class JSONArrayDeserializer implements ObjectDeserializer {
    public final static JSONArrayDeserializer instance = new JSONArrayDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        JSONArray array = new JSONArray();
        parser.parseArray(array);
        return (T) array;
    }

    public int getFastMatchToken() {
        return JSONToken.LBRACKET;
    }
}
