package se.thinkware.gocd.dockerpoller;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class DockerTagsList {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("tags")
    @Expose
    private List<String> tags = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

}