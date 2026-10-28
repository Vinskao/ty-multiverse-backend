from pathlib import Path
import re
import unittest


ROOT = Path(__file__).resolve().parents[1]
PIPELINE = (ROOT / 'Jenkinsfile').read_text(encoding='utf-8')


def container_cpu(name):
    match = re.search(
        rf'                  - name: {re.escape(name)}\n(.*?)(?=\n                  - name:|\n                  volumes:)',
        PIPELINE,
        re.DOTALL,
    )
    if not match:
        raise AssertionError(f'missing Jenkins agent container: {name}')
    block = match.group(1)
    request = re.search(r'requests:\s+cpu:\s+"(\d+)m"', block)
    limit = re.search(r'limits:\s+cpu:\s+"(\d+)m"', block)
    if not request or not limit:
        raise AssertionError(f'missing CPU request or limit for {name}')
    return int(request.group(1)), int(limit.group(1))


class JenkinsPipelinePerformanceTests(unittest.TestCase):
    # Speed comes from the limits: the agent may burst to them while the node is idle.
    def test_maven_container_has_build_cpu_headroom(self):
        _, limit = container_cpu('maven')
        self.assertGreaterEqual(limit, 500)

    def test_docker_container_has_layer_unpack_cpu_headroom(self):
        _, limit = container_cpu('docker')
        self.assertGreaterEqual(limit, 300)

    # Requests are reserved before the pod can start. Each OKE node has 840m allocatable with ~535-695m
    # already requested, so a larger reservation leaves the agent Pending ("Insufficient cpu") forever.
    def test_agent_pod_cpu_requests_fit_the_cluster(self):
        total = sum(container_cpu(name)[0] for name in ('maven', 'docker', 'kubectl', 'jnlp'))
        self.assertLessEqual(total, 140)

    def test_rollout_is_waited_for_once(self):
        self.assertEqual(PIPELINE.count('kubectl rollout status deployment/ty-multiverse-backend'), 1)

    def test_docker_context_excludes_repo_and_keeps_only_runtime_jar(self):
        ignore = (ROOT / '.dockerignore').read_text(encoding='utf-8').splitlines()
        self.assertIn('**', ignore)
        self.assertIn('!Dockerfile', ignore)
        self.assertIn('!target/', ignore)
        self.assertIn('!target/ty-multiverse-backend.jar', ignore)


if __name__ == '__main__':
    unittest.main()
