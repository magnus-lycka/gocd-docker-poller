package se.thinkware.gocd.dockerpoller.message;

import com.google.gson.annotations.Expose;

public class ValidationError {

    @Expose
    private final String key;

    @Expose
    private final String message;

    private ValidationError(String key, String message) {
        this.key = key;
        this.message = message;
    }

    public static ValidationError create(String message) {
        return new ValidationError("", message);
    }

    public static ValidationError create(String key, String message) {
        return new ValidationError(key, message);
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValidationError that = (ValidationError) o;

        return (key != null ? key.equals(that.key) : that.key == null) &&
                (message != null ? message.equals(that.message) : that.message == null);
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}