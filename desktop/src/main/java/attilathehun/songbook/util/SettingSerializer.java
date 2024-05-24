package attilathehun.songbook.util;

import attilathehun.songbook.environment.Setting;
import com.google.gson.*;

import java.lang.reflect.Type;

public class SettingSerializer implements JsonSerializer<Setting<?>> {

    @Override
    public JsonElement serialize(final Setting<?> setting, final Type type, final JsonSerializationContext jsonSerializationContext) {
        switch (setting.getType()) {
            case Setting.TYPE_DOUBLE-> new JsonPrimitive((Double) setting.getValue());
            case Setting.TYPE_INTEGER-> new JsonPrimitive((Integer) setting.getValue());
        }

        return new JsonPrimitive((String) setting.getValue());
    }
}
