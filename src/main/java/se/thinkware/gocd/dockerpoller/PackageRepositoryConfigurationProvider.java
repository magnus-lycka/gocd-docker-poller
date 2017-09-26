package se.thinkware.gocd.dockerpoller;

import se.thinkware.gocd.dockerpoller.message.PackageMaterialProperties;
import se.thinkware.gocd.dockerpoller.message.PackageMaterialProperty;
import se.thinkware.gocd.dockerpoller.message.ValidationResultMessage;

class PackageRepositoryConfigurationProvider {

    public PackageMaterialProperties repositoryConfiguration() {
        PackageMaterialProperties repositoryConfigurationResponse = new PackageMaterialProperties();
        repositoryConfigurationResponse.addPackageMaterialProperty(
                Constants.DOCKER_REGISTRY_URL,
                new PackageMaterialProperty()
                        .withDisplayName("Docker Registry URL")
                        .withDisplayOrder("0")
                        .withRequired(true)
        );
        repositoryConfigurationResponse.addPackageMaterialProperty(
                Constants.DOCKER_REGISTRY_NAME,
                new PackageMaterialProperty()
                        .withDisplayName("Docker Registry Name")
                        .withDisplayOrder("1")
                        .withPartOfIdentity(true)
                        .withRequired(true)
        );
        return repositoryConfigurationResponse;
    }

    public PackageMaterialProperties packageConfiguration() {
        PackageMaterialProperties packageConfigurationResponse = new PackageMaterialProperties();
        packageConfigurationResponse.addPackageMaterialProperty(
                Constants.DOCKER_IMAGE,
                new PackageMaterialProperty()
                        .withDisplayName("Docker Image")
                        .withDisplayOrder("0")
                        .withPartOfIdentity(true)
                        .withRequired(true)
        );
        packageConfigurationResponse.addPackageMaterialProperty(
                Constants.DOCKER_TAG_FILTER,
                new PackageMaterialProperty()
                        .withDisplayName("Docker Tag Filter Regular Expression")
                        .withDisplayOrder("1")
        );
        return packageConfigurationResponse;
    }

    public ValidationResultMessage validateRepositoryConfiguration(PackageMaterialProperties configurationProvidedByUser) {
        return new ValidationResultMessage();
    }

    public ValidationResultMessage validatePackageConfiguration(PackageMaterialProperties configurationProvidedByUser) {
        return new ValidationResultMessage();
    }

}