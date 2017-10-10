package se.thinkware.gocd.dockerpoller;

import se.thinkware.gocd.dockerpoller.message.PackageMaterialProperties;
import se.thinkware.gocd.dockerpoller.message.PackageMaterialProperty;
import se.thinkware.gocd.dockerpoller.message.ValidationError;
import se.thinkware.gocd.dockerpoller.message.ValidationResultMessage;

class PackageRepositoryConfigurationProvider {

    public PackageMaterialProperties repositoryConfiguration() {
        PackageMaterialProperties repositoryConfigurationResponse = new PackageMaterialProperties();
        repositoryConfigurationResponse.addPackageMaterialProperty(
                Constants.DOCKER_REGISTRY_URL,
                new PackageMaterialProperty()
                        .withDisplayName("Docker Registry URL")
                        .withDisplayOrder("0")
                        .withPartOfIdentity(false)
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
                        .withPartOfIdentity(false)
                        .withRequired(false)
        );
        return packageConfigurationResponse;
    }

    public ValidationResultMessage validateRepositoryConfiguration(PackageMaterialProperties configurationProvidedByUser) {

        ValidationResultMessage validationResultMessage = new ValidationResultMessage();
        PackageMaterialProperty registryUrl = configurationProvidedByUser.getProperty(Constants.DOCKER_REGISTRY_URL);
        PackageMaterialProperty registryName = configurationProvidedByUser.getProperty(Constants.DOCKER_REGISTRY_NAME);

        if (registryUrl == null) {
            validationResultMessage.addError(
                    ValidationError.create(Constants.DOCKER_REGISTRY_URL, "Docker Registry url not specified")
            );
            return validationResultMessage;
        }
        if (registryName == null) {
            validationResultMessage.addError(
                    ValidationError.create(Constants.DOCKER_REGISTRY_NAME, "Docker Registry name not specified")
            );
            return validationResultMessage;
        }
        return validationResultMessage;

    }

    public ValidationResultMessage validatePackageConfiguration(PackageMaterialProperties configurationProvidedByUser) {
        ValidationResultMessage validationResultMessage = new ValidationResultMessage();
        PackageMaterialProperty imageConfig = configurationProvidedByUser.getProperty(Constants.DOCKER_IMAGE);
        if (imageConfig == null) {
            validationResultMessage.addError(ValidationError.create(Constants.DOCKER_IMAGE, "Docker image not specified"));
            return validationResultMessage;
        }
        String image = imageConfig.value();
        if (image == null) {
            validationResultMessage.addError(ValidationError.create(Constants.DOCKER_IMAGE, "Docker image is null"));
            return validationResultMessage;
        }
        if (image.trim().isEmpty()) {
            validationResultMessage.addError(ValidationError.create(Constants.DOCKER_IMAGE, "Docker image is empty"));
            return validationResultMessage;
        }
        return validationResultMessage;
    }

}