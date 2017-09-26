package se.thinkware.gocd.dockerpoller.message;

import java.util.ArrayList;
import java.util.List;

public class ValidationResultMessage {
    private final List<ValidationError> validationErrors = new ArrayList<>();

    public void addError(ValidationError validationError) {
        validationErrors.add(validationError);
    }

    public boolean failure() {
        return !validationErrors.isEmpty();
    }

    public List<String> getMessages() {
        List<String> errorMessages = new ArrayList<>();
        for (ValidationError error : validationErrors) {
            errorMessages.add(error.getMessage());
        }
        return errorMessages;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public Boolean success() {
        return !failure();
    }
}
