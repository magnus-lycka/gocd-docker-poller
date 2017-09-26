package se.thinkware.gocd.dockerpoller;

import com.google.gson.GsonBuilder;

class JsonUtil {

    public static String toJsonString(Object object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return gsonBuilder.excludeFieldsWithoutExposeAnnotation().create().toJson(object);
    }

    public static <T> T fromJsonString(String json, Class<T> type) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return gsonBuilder.create().fromJson(json, type);
    }
}