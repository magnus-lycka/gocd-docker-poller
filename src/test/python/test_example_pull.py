import unittest
import example_pull


class ComposeImageTests(unittest.TestCase):
    def test_docker_pull(self):
        class Subprocess:
            def call(self, *args):
                self.args = args

        subprocess = Subprocess()
        example_pull.docker_pull(subprocess, 'x/y:z')

        self.assertEqual((['docker', 'pull', 'x/y:z'],), subprocess.args)

    def test_provided_packages(self):
        env = {
            'GO_REPO_REG_FOO_DOCKER_REGISTRY_NAME': 'x',
            'GO_REPO_REG_FOO_DOCKER_BOINK': 'y',
            'GO_REPO_REG_BAR_BAZ_DOCKER_REGISTRY_NAME': 'z'
        }
        pkgs = example_pull.provided_packages(env)

        self.assertEqual(sorted(pkgs), ['REG_BAR_BAZ', 'REG_FOO'])

    def test_image_name(self):
        env = {
            'GO_REPO_DKR_PK_G_DOCKER_REGISTRY_NAME': 'reg',
            'GO_PACKAGE_DKR_PK_G_DOCKER_IMAGE': 'pk/g',
            'GO_PACKAGE_DKR_PK_G_LABEL': '1',
        }
        im = example_pull.image_name(env, 'DKR_PK_G')

        self.assertEqual(im, 'reg/pk/g:1')

    def test_main(self):
        class Subprocess:
            args = []
            def call(self, *args):
                self.args.append(args)
        subprocess = Subprocess()

        env = {
            'GO_REPO_DKR_PK_G_DOCKER_REGISTRY_NAME': 'reg',
            'GO_PACKAGE_DKR_PK_G_DOCKER_IMAGE': 'pk/g',
            'GO_PACKAGE_DKR_PK_G_LABEL': '1.1',
            'GO_REPO_DKR_PK_H_DOCKER_REGISTRY_NAME': 'reg',
            'GO_PACKAGE_DKR_PK_H_DOCKER_IMAGE': 'pk/h',
            'GO_PACKAGE_DKR_PK_H_LABEL': '1.0',
        }
        expected = [
            (['docker', 'pull', 'reg/pk/g:1.1'],),
            (['docker', 'pull', 'reg/pk/h:1.0'],)
        ]

        example_pull.main(env, subprocess=subprocess)

        self.assertEqual(expected, sorted(subprocess.args))


if __name__ == '__main__':
    unittest.main()