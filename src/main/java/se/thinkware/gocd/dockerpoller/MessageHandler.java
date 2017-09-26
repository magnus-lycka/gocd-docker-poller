package se.thinkware.gocd.dockerpoller;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

interface MessageHandler {
    GoPluginApiResponse handle(GoPluginApiRequest request);
}
