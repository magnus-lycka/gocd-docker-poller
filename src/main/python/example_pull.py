import re
import os
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
