package se.thinkware.gocd.dockerpoller;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;

import org.junit.jupiter.api.Test;
import se.thinkware.gocd.dockerpoller.message.CheckConnectionResultMessage;
import se.thinkware.gocd.dockerpoller.message.PackageMaterialProperties;
import se.thinkware.gocd.dockerpoller.message.PackageMaterialProperty;
import se.thinkware.gocd.dockerpoller.message.PackageRevisionMessage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class PackageRepositoryPollerTests {

    private final HttpTransport mockTransport404 = new MockHttpTransport() {
        @Override
        public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return new MockLowLevelHttpRequest() {
                @Override
                public LowLevelHttpResponse execute() throws IOException {
                    MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
                    response.setStatusCode(404);
                    return response;
                }
            };
        }
    };

    private final HttpTransport mockTransport200 = new MockHttpTransport() {
        @Override
        public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return new MockLowLevelHttpRequest() {
                @Override
                public LowLevelHttpResponse execute() throws IOException {
                    MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
                    response.setStatusCode(200);
                    response.addHeader("Content-Type", "application/json; charset=utf-8");
                    response.addHeader("Docker-Distribution-Api-Version", "registry/2.0");
                    response.addHeader("Server", "nginx/1.4.6 (Ubuntu)");
                    return response;
                }
            };
        }
    };

    private final HttpTransport mockTransportMissingHeader = new MockHttpTransport() {
        @Override
        public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return new MockLowLevelHttpRequest() {
                @Override
                public LowLevelHttpResponse execute() throws IOException {
                    MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
                    response.setStatusCode(200);
                    return response;
                }
            };
        }
    };

    private final HttpTransport mockTransportTags = new MockHttpTransport() {
        @Override
        public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return new MockLowLevelHttpRequest() {
                @Override
                public LowLevelHttpResponse execute() throws IOException {
                    MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
                    response.setStatusCode(200);
                    response.setContentType("application/json");
                    String dockerImage = "{\n"
                            + "\"name\":\"my_docker\",\n"
                            + "\"tags\":[\n"
                            + "    \"1.1\",\n"
                            + "    \"1.11\",\n"
                            + "    \"1.100\",\n"
                            + "    \"1.2\",\n"
                            + "    \"1.3\"\n"
                            + "]}";
                    response.setContent(dockerImage);
                    return response;
                }
            };
        }
    };

    @Test
    void RepositoryNotFoundTest() {

        PackageRepositoryPoller poller = new PackageRepositoryPoller(
                new PackageRepositoryConfigurationProvider(),
                mockTransport404
        );
        PackageMaterialProperties repositoryConfiguration = new PackageMaterialProperties();
        PackageMaterialProperty url = new PackageMaterialProperty().withValue("http://xxx/v2/");
        repositoryConfiguration.addPackageMaterialProperty(Constants.DOCKER_REGISTRY_URL, url);
        PackageMaterialProperty name = new PackageMaterialProperty().withValue("registry/name");
        repositoryConfiguration.addPackageMaterialProperty(Constants.DOCKER_REGISTRY_NAME, name);
        CheckConnectionResultMessage status = poller.checkConnectionToRepository(repositoryConfiguration);

        assertFalse(status.success());
        assertEquals(Collections.singletonList("Could not find docker registry. [404]"), status.getMessages());
    }

    @Test
    void RepositoryMissingHeaderTest() {

        PackageRepositoryPoller poller = new PackageRepositoryPoller(
                new PackageRepositoryConfigurationProvider(),
                mockTransportMissingHeader
        );
        PackageMaterialProperties repositoryConfiguration = new PackageMaterialProperties();
        PackageMaterialProperty url = new PackageMaterialProperty().withValue("http://xxx/v2/");
        repositoryConfiguration.addPackageMaterialProperty(Constants.DOCKER_REGISTRY_URL, url);
        PackageMaterialProperty name = new PackageMaterialProperty().withValue("registry/name");
        repositoryConfiguration.addPackageMaterialProperty(Constants.DOCKER_REGISTRY_NAME, name);
        CheckConnectionResultMessage status = poller.checkConnectionToRepository(repositoryConfiguration);

        assertFalse(status.success());
        assertEquals(
                Collections.singletonList("Missing header: docker-distribution-api-version found only: []"),
                status.getMessages());
    }

    @Test
    void RepositoryFoundTest() {

        PackageRepositoryPoller poller = new PackageRepositoryPoller(
                new PackageRepositoryConfigurationProvider(),
                mockTransport200);
        PackageMaterialProperties repositoryConfiguration = new PackageMaterialProperties();
        PackageMaterialProperty url = new PackageMaterialProperty().withValue("http://xxx/v2/");
        repositoryConfiguration.addPackageMaterialProperty(Constants.DOCKER_REGISTRY_URL, url);
        PackageMaterialProperty name = new PackageMaterialProperty().withValue("registry/name");
        repositoryConfiguration.addPackageMaterialProperty(Constants.DOCKER_REGISTRY_NAME, name);
        CheckConnectionResultMessage status = poller.checkConnectionToRepository(repositoryConfiguration);

        assert(status.success());
        assertEquals(Collections.singletonList("Docker registry found."), status.getMessages());
    }

    @Test
    void PackageNotFoundTest() {

        PackageRepositoryPoller poller = new PackageRepositoryPoller(
                new PackageRepositoryConfigurationProvider(),
                mockTransport404
        );

        PackageMaterialProperties packageConfiguration = new PackageMaterialProperties();
        PackageMaterialProperty image = new PackageMaterialProperty().withValue("my_docker");
        packageConfiguration.addPackageMaterialProperty(Constants.DOCKER_IMAGE, image);

        PackageMaterialProperties repositoryConfiguration = new PackageMaterialProperties();
        PackageMaterialProperty url = new PackageMaterialProperty().withValue("http://xxx/v2/");
        repositoryConfiguration.addPackageMaterialProperty(Constants.DOCKER_REGISTRY_URL, url);
        PackageMaterialProperty name = new PackageMaterialProperty().withValue("registry/name");
        repositoryConfiguration.addPackageMaterialProperty(Constants.DOCKER_REGISTRY_NAME, name);


        CheckConnectionResultMessage status = poller.checkConnectionToPackage(
                packageConfiguration,
                repositoryConfiguration
        );

        assertFalse(status.success());
        assertEquals(Collections.singletonList("Could not find docker image. [404]"), status.getMessages());
    }

    @Test
    void PackageFoundTest() {

        PackageRepositoryPoller poller = new PackageRepositoryPoller(
                new PackageRepositoryConfigurationProvider(),
                mockTransport200
        );

        PackageMaterialProperties packageConfiguration = new PackageMaterialProperties();
        PackageMaterialProperty image = new PackageMaterialProperty().withValue("my_docker");
        packageConfiguration.addPackageMaterialProperty(Constants.DOCKER_IMAGE, image);

        PackageMaterialProperties repositoryConfiguration = new PackageMaterialProperties();
        PackageMaterialProperty url = new PackageMaterialProperty().withValue("http://xxx/v2/");
        repositoryConfiguration.addPackageMaterialProperty(Constants.DOCKER_REGISTRY_URL, url);


        CheckConnectionResultMessage status = poller.checkConnectionToPackage(
                packageConfiguration,
                repositoryConfiguration
        );

        assert(status.success());
        assertEquals(Collections.singletonList("Docker image found."), status.getMessages());
    }

    @Test
    void TagFetcherTest() {

        PackageRepositoryPoller poller = new PackageRepositoryPoller(
                new PackageRepositoryConfigurationProvider(),
                mockTransportTags
        );

        GenericUrl url = new GenericUrl("http://xxx/v2/my_docker/tags/list");

        List<String> tags = poller.TagFetcher(url);

        List<String> expected = Arrays.asList("1.1", "1.11", "1.100", "1.2", "1.3");
        assertEquals(expected, tags);
    }

    @Test
    void getLatestTest() {

        PackageRepositoryPoller poller = new PackageRepositoryPoller(
                new PackageRepositoryConfigurationProvider(),
                mockTransportTags
        );

        PackageMaterialProperties packageConfiguration = new PackageMaterialProperties();
        PackageMaterialProperty image = new PackageMaterialProperty().withValue("my_docker");
        packageConfiguration.addPackageMaterialProperty(Constants.DOCKER_IMAGE, image);
        PackageMaterialProperty filter = new PackageMaterialProperty().withValue(".*");
        packageConfiguration.addPackageMaterialProperty(Constants.DOCKER_TAG_FILTER, filter);

        PackageMaterialProperties repositoryConfiguration = new PackageMaterialProperties();
        PackageMaterialProperty url = new PackageMaterialProperty().withValue("http://xxx/v2/");
        repositoryConfiguration.addPackageMaterialProperty(Constants.DOCKER_REGISTRY_URL, url);


        PackageRevisionMessage dockerImage = poller.getLatestRevision(
                packageConfiguration,
                repositoryConfiguration
        );

        assertEquals("1.100", dockerImage.getRevision());
    }

    @Test
    void getLatestUsingInvalidFilterTest() {

        PackageRepositoryPoller poller = new PackageRepositoryPoller(
                new PackageRepositoryConfigurationProvider(),
                mockTransportTags
        );

        PackageMaterialProperties packageConfiguration = new PackageMaterialProperties();
        String dockerImage = "my_docker";
        PackageMaterialProperty image = new PackageMaterialProperty().withValue(dockerImage);
        packageConfiguration.addPackageMaterialProperty(Constants.DOCKER_IMAGE, image);
        String dockerTagFilter = "*.starDotIsAnInvalidFilter";
        PackageMaterialProperty filter = new PackageMaterialProperty().withValue(dockerTagFilter);
        packageConfiguration.addPackageMaterialProperty(Constants.DOCKER_TAG_FILTER, filter);

        PackageMaterialProperties repositoryConfiguration = new PackageMaterialProperties();
        String dockerRegistryUrl = "http://xxx/v2/";
        PackageMaterialProperty url = new PackageMaterialProperty().withValue(dockerRegistryUrl);
        repositoryConfiguration.addPackageMaterialProperty(Constants.DOCKER_REGISTRY_URL, url);


        Exception thrown = assertThrows(PatternSyntaxException.class, ()->{
            poller.getLatestRevision(
                    packageConfiguration,
                    repositoryConfiguration
            );
        });

        String expectedTypeOfPatternSyntaxException = "Dangling meta character '*' near index 0";
        assertTrue(thrown.getMessage().contains(expectedTypeOfPatternSyntaxException));
        assertTrue(thrown.getMessage().contains(dockerImage));
        assertTrue(thrown.getMessage().contains(dockerTagFilter));
        assertTrue(thrown.getMessage().contains(dockerRegistryUrl));
    }

    @Test
    void getLatestEndsWith1Test() {

        PackageRepositoryPoller poller = new PackageRepositoryPoller(
                new PackageRepositoryConfigurationProvider(),
                mockTransportTags
        );

        PackageMaterialProperties packageConfiguration = new PackageMaterialProperties();
        PackageMaterialProperty image = new PackageMaterialProperty().withValue("my_docker");
        packageConfiguration.addPackageMaterialProperty(Constants.DOCKER_IMAGE, image);
        PackageMaterialProperty filter = new PackageMaterialProperty().withValue("1$");
        packageConfiguration.addPackageMaterialProperty(Constants.DOCKER_TAG_FILTER, filter);

        PackageMaterialProperties repositoryConfiguration = new PackageMaterialProperties();
        PackageMaterialProperty url = new PackageMaterialProperty().withValue("http://xxx/v2/");
        repositoryConfiguration.addPackageMaterialProperty(Constants.DOCKER_REGISTRY_URL, url);


        PackageRevisionMessage dockerImage = poller.getLatestRevision(
                packageConfiguration,
                repositoryConfiguration
        );

        assertEquals("1.11", dockerImage.getRevision());
    }

    @Test
    void expandNumsTest() {
        String expected = "000123.000001-X";
        String actual = PackageRepositoryPoller.expandNums("123.1-X");
        assertEquals(expected, actual);
    }

    @Test
    void getLatestSinceHitTest() {

        PackageRepositoryPoller poller = new PackageRepositoryPoller(
                new PackageRepositoryConfigurationProvider(),
                mockTransportTags
        );

        PackageMaterialProperties packageConfiguration = new PackageMaterialProperties();
        PackageMaterialProperty image = new PackageMaterialProperty().withValue("my_docker");
        packageConfiguration.addPackageMaterialProperty(Constants.DOCKER_IMAGE, image);
        PackageMaterialProperty filter = new PackageMaterialProperty().withValue(".*");
        packageConfiguration.addPackageMaterialProperty(Constants.DOCKER_TAG_FILTER, filter);

        PackageMaterialProperties repositoryConfiguration = new PackageMaterialProperties();
        PackageMaterialProperty url = new PackageMaterialProperty().withValue("http://xxx/v2/");
        repositoryConfiguration.addPackageMaterialProperty(Constants.DOCKER_REGISTRY_URL, url);

        PackageRevisionMessage oldRev = new PackageRevisionMessage(
                "1.23", null, null, null, null
        );


        PackageRevisionMessage dockerImage = poller.getLatestRevisionSince(
                packageConfiguration,
                repositoryConfiguration,
                oldRev
        );

        assertEquals("1.100", dockerImage.getRevision());
    }

    @Test
    void getLatestSinceMissTest() {

        PackageRepositoryPoller poller = new PackageRepositoryPoller(
                new PackageRepositoryConfigurationProvider(),
                mockTransportTags
        );

        PackageMaterialProperties packageConfiguration = new PackageMaterialProperties();
        PackageMaterialProperty image = new PackageMaterialProperty().withValue("my_docker");
        packageConfiguration.addPackageMaterialProperty(Constants.DOCKER_IMAGE, image);
        PackageMaterialProperty filter = new PackageMaterialProperty().withValue(".*");
        packageConfiguration.addPackageMaterialProperty(Constants.DOCKER_TAG_FILTER, filter);

        PackageMaterialProperties repositoryConfiguration = new PackageMaterialProperties();
        PackageMaterialProperty url = new PackageMaterialProperty().withValue("http://xxx/v2/");
        repositoryConfiguration.addPackageMaterialProperty(Constants.DOCKER_REGISTRY_URL, url);

        PackageRevisionMessage oldRev = new PackageRevisionMessage(
                "2.0", null, null, null, null
        );


        PackageRevisionMessage dockerImage = poller.getLatestRevisionSince(
                packageConfiguration,
                repositoryConfiguration,
                oldRev
        );

        assertEquals(null, dockerImage.getRevision());
    }

}

