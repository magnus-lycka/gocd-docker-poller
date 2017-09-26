package se.thinkware.gocd.dockerpoller;


import com.google.api.client.http.*;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import se.thinkware.gocd.dockerpoller.message.CheckConnectionResultMessage;
import se.thinkware.gocd.dockerpoller.message.PackageMaterialProperties;
import se.thinkware.gocd.dockerpoller.message.PackageRevisionMessage;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


class PackageRepositoryPoller {

    private final HttpTransport transport;

    public PackageRepositoryPoller(PackageRepositoryConfigurationProvider configurationProvider) {
        this.transport = new NetHttpTransport();
    }

    // This is used for testing, so that we can mock the HttpTransport
    public PackageRepositoryPoller(HttpTransport transport) {

        this.transport = transport;
    }

    private CheckConnectionResultMessage UrlChecker(GenericUrl url, String what) {
        try {
            HttpRequest request = transport.createRequestFactory().buildRequest("get", url, null);
            HttpResponse response = request.execute();
            HttpHeaders headers = response.getHeaders();
            String dockerHeader = "docker-distribution-api-version";
            String message;
            CheckConnectionResultMessage.STATUS status;
            if (headers.containsKey(dockerHeader)) {
                if (headers.get(dockerHeader).toString().startsWith("[registry/2.")) {
                    message = "Docker " + what + " found.";
                    status = CheckConnectionResultMessage.STATUS.SUCCESS;
                } else {
                    status = CheckConnectionResultMessage.STATUS.FAILURE;
                    message = "Unknown value " + headers.get(dockerHeader).toString() + " for header " + dockerHeader;
                }
            } else {
                status = CheckConnectionResultMessage.STATUS.FAILURE;
                message = "Missing header: " + dockerHeader + " found only: " + headers.keySet();
            }
            return new CheckConnectionResultMessage(status, Collections.singletonList(message));
        } catch (IOException ex) {
            return new CheckConnectionResultMessage(
                    CheckConnectionResultMessage.STATUS.FAILURE,
                    Collections.singletonList("Could not find docker " + what + ". [" + ex.getMessage() + "]"));
        }
    }

    List<String> TagFetcher(GenericUrl url) {
        try {
            HttpRequest request = transport.createRequestFactory().buildRequest("get", url, null);
            HttpResponse response = request.execute();
            Gson gson = new GsonBuilder().create();
            Reader tagListJson = new InputStreamReader(response.getContent());
            DockerTagsList tagsList = gson.fromJson(tagListJson, DockerTagsList.class);
            return tagsList.getTags();
        } catch (IOException ex) {
            return Collections.emptyList();
        }
    }

    public CheckConnectionResultMessage checkConnectionToRepository(
            PackageMaterialProperties repositoryConfiguration
    ) {
        String dockerRegistryUrl = repositoryConfiguration.getProperty(Constants.DOCKER_REGISTRY_URL).value();
        return UrlChecker(new GenericUrl(dockerRegistryUrl), "registry");
    }

    public CheckConnectionResultMessage checkConnectionToPackage(
            PackageMaterialProperties packageConfiguration,
            PackageMaterialProperties repositoryConfiguration
    ) {
        String dockerPackageUrl =
                getDockerPackageUrl(packageConfiguration, repositoryConfiguration);
        return UrlChecker(new GenericUrl(dockerPackageUrl), "image");
    }

    private List<String> getDockerTags(
            PackageMaterialProperties packageConfiguration,
            PackageMaterialProperties repositoryConfiguration
    ) {
        String dockerPackageUrl =
                getDockerPackageUrl(packageConfiguration, repositoryConfiguration);
        return Collections.singletonList("TODO");

    }

    private String getDockerPackageUrl(
            PackageMaterialProperties packageConfiguration,
            PackageMaterialProperties repositoryConfiguration
    ) {
        return repositoryConfiguration.getProperty(Constants.DOCKER_REGISTRY_URL).value() +
                packageConfiguration.getProperty(Constants.DOCKER_IMAGE).value() +
                "/tags/list";
    }

    static String expandNums(String versionString) {
        Pattern p = Pattern.compile("[0-9]+");
        Matcher m = p.matcher(versionString);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String match = String.format("%06d", Integer.parseInt(m.group()));

            m.appendReplacement(sb, match);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String biggest(String first, String second) {
        String firstComp = expandNums(first);
        String secondComp = expandNums(second);
        if (firstComp.compareTo(secondComp) > 0) {
            return first;
        } else {
            return second;
        }
    }

    public PackageRevisionMessage getLatestRevision(
            PackageMaterialProperties packageConfiguration,
            PackageMaterialProperties repositoryConfiguration
    ) {

        GenericUrl url = new GenericUrl(getDockerPackageUrl(packageConfiguration, repositoryConfiguration));
        List<String> tags = TagFetcher(url);
        String filter = packageConfiguration.getProperty(Constants.DOCKER_TAG_FILTER).value();
        if (filter.equals("")) {
            filter = ".*";
        }
        Pattern pattern = Pattern.compile(filter);

        List<Object> matching = tags.stream().filter(pattern.asPredicate()).collect(Collectors.toList());

        if (matching.isEmpty()) {
            return new PackageRevisionMessage();
        }

        String latest = "";
        for (Object tag: matching) {
            latest = biggest(latest, tag.toString());
        }

        return new PackageRevisionMessage(latest,null,null, null,null);
    }

    public PackageRevisionMessage getLatestRevisionSince(
            PackageMaterialProperties packageConfiguration,
            PackageMaterialProperties repositoryConfiguration,
            PackageRevisionMessage previous
    ) {
        PackageRevisionMessage latest = getLatestRevision(packageConfiguration, repositoryConfiguration);
        if (biggest(previous.getRevision(), latest.getRevision()).equals(latest.getRevision())) {
            return latest;
        } else {
            return new PackageRevisionMessage();
        }
    }

}