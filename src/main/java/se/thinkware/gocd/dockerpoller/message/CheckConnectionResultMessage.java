package se.thinkware.gocd.dockerpoller.message;

/**
 * Created by magnusl on 2017-09-21.
 */

import com.google.gson.annotations.Expose;
import java.util.List;

public class CheckConnectionResultMessage {

    public enum STATUS {SUCCESS, FAILURE}

    @Expose
    private final STATUS status;

    @Expose
    private final List<String> messages;

    public CheckConnectionResultMessage(STATUS status, List<String> messages) {
        this.status = status;
        this.messages = messages;
    }

    public boolean success() {
        return STATUS.SUCCESS.equals(status);
    }

    public List<String> getMessages() {
        return messages;
    }
}
