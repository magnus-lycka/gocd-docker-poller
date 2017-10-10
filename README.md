# Docker Package plugin for GoCD


## What's this?

This plugin provides docker images in a docker registry as package material in GoCD. It only runs on the GoCD server.

It polls a docker registry through the `Docker Registry HTTP API V2` to find whether a docker image has changed. When a pipeline with docker package material runs, the GoCD server will populate certain environment variables which can be used in pipeline tasks.

Note that there is a bit of confusion about terminology between GoCD and Docker. Imagine that we have a Docker image you'd pull like this:

    docker pull dockerregistry.mydomain/backends/database-x:1.2.3

The part `dockerregistry.mydomain` is called **repository** by GoCD and **registry** by Docker.

The part `backends/database-x` is called **package** by GoCD and **repository** by Docker.

The part `1.2.3` is called **label** by GoCD and **tag** by Docker.

Note in particular that both GoCD and Docker use the word **repository** but for different things.


## Building the Plugin

Run `mvn verify` to build the jar file and run tests.


## Installing the Plugin

To use this plugin, drop the generated jar-file in `/var/lib/go-server/plugins/external/` on the GoCD server and restart `go-server` service.


## Configuring a docker registry as a GoCD package repository

Go to `Admin -> Package Repositories` in the GoCD web UI to add a new package repository.

`Name` is the identifier GoCD uses to keep track of its Package Repositories. This README assumes that you use the name *Docker*.

In the drop-down list for `Type`, select `docker-registry` which indicates this Plugin.

You will now be asked to provide `Docker Registry URL` and `Docker Registry Name`.

The URL should be the `Docker Registry HTTP API V2` URL. It might be something like `http://dockerregistry.mydomain/v2/`. It should end with `/v2/`.

The Name should be the prefix to the image name that you use with e.g. `docker pull`. It's not actually used by the plugin, but it will be provided as an environment variable in the pipeline runs. It's probably identical to the part of the URL after `http://` and before `/v2/`.

Press `CHECK CONNECTION`. You should get the message: *Connection OK. Docker registry found.*

Press `SAVE`.

*Docker* should now appear in the list of Package Repositories on the left.


## Configuring a docker image as pipeline material

In the `Material` section of the configuation for a pipeline, select `Add material`, and select `Package`.
In the `Add Material - Package` form, select the repository *Docker* (or whatever you called it).

For `Package` select `Define New`. Fill in `Package Name`, `Docker Image` and possibly `Docker Tag Filter Regular Expression`.

The `Package Name` is used by GoCD together with the `Repository Name` (e.g. *Docker*) as a unique name for this particular material. You will typically use the docker image name.
*(You can use the same material in another pipeline by using `Choose Existing` instead of `Define New` in the `Edit Material - Package` Form.)*


## Using docker material in pipeline

As you run a pipeline with Docker Package material, the following important environment variables should become visible in the build:

    GO_REPO_<docker registry name>_<package name>_DOCKER_REGISTRY_NAME
    GO_PACKAGE_<docker registry name>_<package name>_DOCKER_IMAGE
    GO_PACKAGE_<docker registry name>_<package name>_LABEL
 
 Assuming the values used in the examples above, it could for instance be:

    GO_REPO_DOCKER_BACKENDS_DATABASE_X_DOCKER_REGISTRY_NAME=dockerregistry.mydomain
    GO_PACKAGE_DOCKER_BACKENDS_DATABASE_X_DOCKER_IMAGE=backends/database
    GO_PACKAGE_DOCKER_BACKENDS_DATABASE_X_LABEL=1.2.3

You use whatever you need of these values in your GoCD tasks.

For instance, if you build a new docker image based on the docker you depend on, you might create a `Dockerfile` from a template where you insert the value from `GO_PACKAGE_<docker registry name>_<package name>_LABEL` in the end of the FROM statement.

If you for instance want to pull all the docker images a pipelines depends on in a task, you could use something like this Python script. 

    #!/usr/bin/env python
    
    import os
    import re
    import subprocess
    
    
    def docker_pull(subprocess, image):
        subprocess.call(['docker', 'pull', image])
    
    
    def provided_packages(env):
        pattern = r"GO_REPO_([^ ]+)_DOCKER_REGISTRY_NAME"
        return re.findall(pattern, " ".join(env))
    
    
    def image_name(env, package):
        registry = env['GO_REPO_{}_DOCKER_REGISTRY_NAME'.format(package)]
        repository = env['GO_PACKAGE_{}_DOCKER_IMAGE'.format(package)]
        tag = env['GO_PACKAGE_{}_LABEL'.format(package)]
        return registry + '/' + repository + ':' + tag
    
    
    def main(env=os.environ, subprocess=subprocess):
        for package in provided_packages(env):
            docker_pull(subprocess, image_name(env, package))
    
    
    if __name__ == '__main__':
        main()
