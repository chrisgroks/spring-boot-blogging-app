#!/usr/bin/env python3
"""
Jenkins to GitHub Actions Migration Validator

Compares outputs from Jenkins and GitHub Actions pipelines to ensure equivalence.
This is the core of the demo's "trust builder" - proving the migration is accurate.
"""

import json
import hashlib
import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Dict, List, Tuple
from dataclasses import dataclass, asdict
import requests
from datetime import datetime


@dataclass
class TestResults:
    """Test execution results for comparison"""
    total_tests: int
    passed: int
    failed: int
    skipped: int
    execution_time: float
    test_cases: List[Dict]


@dataclass
class BuildArtifact:
    """Build artifact metadata"""
    name: str
    size: int
    checksum: str
    path: str


@dataclass
class ValidationResult:
    """Overall validation result"""
    timestamp: str
    jenkins_build: str
    gha_run: str
    tests_match: bool
    artifacts_match: bool
    exit_codes_match: bool
    differences: List[str]
    confidence_score: float


class JenkinsClient:
    """Client for Jenkins REST API"""
    
    def __init__(self, base_url: str, username: str, password: str):
        self.base_url = base_url.rstrip('/')
        self.auth = (username, password)
    
    def get_build_info(self, job_name: str, build_number: int) -> Dict:
        """Get build information"""
        url = f"{self.base_url}/job/{job_name}/{build_number}/api/json"
        response = requests.get(url, auth=self.auth)
        response.raise_for_status()
        return response.json()
    
    def get_test_results(self, job_name: str, build_number: int) -> TestResults:
        """Parse JUnit XML test results from Jenkins"""
        url = f"{self.base_url}/job/{job_name}/{build_number}/testReport/api/json"
        response = requests.get(url, auth=self.auth)
        
        if response.status_code == 404:
            return TestResults(0, 0, 0, 0, 0.0, [])
        
        response.raise_for_status()
        data = response.json()
        
        test_cases = []
        for suite in data.get('suites', []):
            for case in suite.get('cases', []):
                test_cases.append({
                    'name': case['name'],
                    'className': case['className'],
                    'status': case['status'],
                    'duration': case['duration']
                })
        
        return TestResults(
            total_tests=data.get('totalCount', 0),
            passed=data.get('passCount', 0),
            failed=data.get('failCount', 0),
            skipped=data.get('skipCount', 0),
            execution_time=data.get('duration', 0.0),
            test_cases=test_cases
        )
    
    def get_artifacts(self, job_name: str, build_number: int) -> List[BuildArtifact]:
        """Get build artifacts with checksums"""
        build_info = self.get_build_info(job_name, build_number)
        artifacts = []
        
        for artifact in build_info.get('artifacts', []):
            url = f"{self.base_url}/job/{job_name}/{build_number}/artifact/{artifact['relativePath']}"
            response = requests.get(url, auth=self.auth)
            response.raise_for_status()
            
            checksum = hashlib.sha256(response.content).hexdigest()
            artifacts.append(BuildArtifact(
                name=artifact['fileName'],
                size=len(response.content),
                checksum=checksum,
                path=artifact['relativePath']
            ))
        
        return artifacts
    
    def get_console_output(self, job_name: str, build_number: int) -> str:
        """Get console output"""
        url = f"{self.base_url}/job/{job_name}/{build_number}/consoleText"
        response = requests.get(url, auth=self.auth)
        response.raise_for_status()
        return response.text


class GitHubActionsClient:
    """Client for GitHub Actions API"""
    
    def __init__(self, token: str, repo: str):
        self.token = token
        self.repo = repo  # format: owner/repo
        self.base_url = "https://api.github.com"
        self.headers = {
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github.v3+json"
        }
    
    def get_workflow_run(self, run_id: int) -> Dict:
        """Get workflow run information"""
        url = f"{self.base_url}/repos/{self.repo}/actions/runs/{run_id}"
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()
        return response.json()
    
    def get_test_results(self, run_id: int) -> TestResults:
        """Download and parse test artifacts from GHA"""
        # This would download artifacts and parse JUnit XML
        # Simplified for demo purposes
        artifacts_url = f"{self.base_url}/repos/{self.repo}/actions/runs/{run_id}/artifacts"
        response = requests.get(artifacts_url, headers=self.headers)
        response.raise_for_status()
        
        # Parse test results from artifacts
        # Implementation depends on how tests are uploaded
        return TestResults(0, 0, 0, 0, 0.0, [])
    
    def get_artifacts(self, run_id: int) -> List[BuildArtifact]:
        """Get workflow run artifacts"""
        url = f"{self.base_url}/repos/{self.repo}/actions/runs/{run_id}/artifacts"
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()
        
        artifacts = []
        for artifact in response.json().get('artifacts', []):
            # Download and checksum each artifact
            download_url = artifact['archive_download_url']
            download_response = requests.get(download_url, headers=self.headers)
            
            checksum = hashlib.sha256(download_response.content).hexdigest()
            artifacts.append(BuildArtifact(
                name=artifact['name'],
                size=artifact['size_in_bytes'],
                checksum=checksum,
                path=artifact['name']
            ))
        
        return artifacts


class MigrationValidator:
    """Main validator that compares Jenkins and GHA outputs"""
    
    def __init__(self, jenkins_client: JenkinsClient, gha_client: GitHubActionsClient):
        self.jenkins = jenkins_client
        self.gha = gha_client
    
    def compare_test_results(self, jenkins_tests: TestResults, gha_tests: TestResults) -> Tuple[bool, List[str]]:
        """Compare test results between Jenkins and GHA"""
        differences = []
        
        if jenkins_tests.total_tests != gha_tests.total_tests:
            differences.append(
                f"Test count mismatch: Jenkins={jenkins_tests.total_tests}, GHA={gha_tests.total_tests}"
            )
        
        if jenkins_tests.passed != gha_tests.passed:
            differences.append(
                f"Passed tests mismatch: Jenkins={jenkins_tests.passed}, GHA={gha_tests.passed}"
            )
        
        if jenkins_tests.failed != gha_tests.failed:
            differences.append(
                f"Failed tests mismatch: Jenkins={jenkins_tests.failed}, GHA={gha_tests.failed}"
            )
        
        # Compare individual test cases
        jenkins_test_names = {tc['name'] for tc in jenkins_tests.test_cases}
        gha_test_names = {tc['name'] for tc in gha_tests.test_cases}
        
        missing_in_gha = jenkins_test_names - gha_test_names
        missing_in_jenkins = gha_test_names - jenkins_test_names
        
        if missing_in_gha:
            differences.append(f"Tests missing in GHA: {missing_in_gha}")
        
        if missing_in_jenkins:
            differences.append(f"Tests missing in Jenkins: {missing_in_jenkins}")
        
        return len(differences) == 0, differences
    
    def compare_artifacts(self, jenkins_artifacts: List[BuildArtifact], 
                         gha_artifacts: List[BuildArtifact]) -> Tuple[bool, List[str]]:
        """Compare build artifacts using checksums"""
        differences = []
        
        jenkins_by_name = {a.name: a for a in jenkins_artifacts}
        gha_by_name = {a.name: a for a in gha_artifacts}
        
        all_names = set(jenkins_by_name.keys()) | set(gha_by_name.keys())
        
        for name in all_names:
            if name not in jenkins_by_name:
                differences.append(f"Artifact '{name}' only in GHA")
            elif name not in gha_by_name:
                differences.append(f"Artifact '{name}' only in Jenkins")
            else:
                jenkins_artifact = jenkins_by_name[name]
                gha_artifact = gha_by_name[name]
                
                if jenkins_artifact.checksum != gha_artifact.checksum:
                    differences.append(
                        f"Artifact '{name}' checksum mismatch:\n"
                        f"  Jenkins: {jenkins_artifact.checksum}\n"
                        f"  GHA: {gha_artifact.checksum}"
                    )
                
                if jenkins_artifact.size != gha_artifact.size:
                    differences.append(
                        f"Artifact '{name}' size mismatch: "
                        f"Jenkins={jenkins_artifact.size}, GHA={gha_artifact.size}"
                    )
        
        return len(differences) == 0, differences
    
    def calculate_confidence_score(self, validation: ValidationResult) -> float:
        """Calculate confidence score (0-100) for migration"""
        score = 100.0
        
        # Deduct points for each type of mismatch
        if not validation.tests_match:
            score -= 40.0
        if not validation.artifacts_match:
            score -= 30.0
        if not validation.exit_codes_match:
            score -= 30.0
        
        return max(0.0, score)
    
    def validate(self, jenkins_job: str, jenkins_build: int, 
                gha_run_id: int) -> ValidationResult:
        """
        Perform full validation between Jenkins build and GHA run
        
        This is the core validation logic that proves equivalence
        """
        print(f"🔍 Validating Jenkins build {jenkins_build} vs GHA run {gha_run_id}")
        
        differences = []
        
        # 1. Compare test results
        print("  📊 Comparing test results...")
        jenkins_tests = self.jenkins.get_test_results(jenkins_job, jenkins_build)
        gha_tests = self.gha.get_test_results(gha_run_id)
        tests_match, test_diffs = self.compare_test_results(jenkins_tests, gha_tests)
        differences.extend(test_diffs)
        
        # 2. Compare artifacts
        print("  📦 Comparing artifacts...")
        jenkins_artifacts = self.jenkins.get_artifacts(jenkins_job, jenkins_build)
        gha_artifacts = self.gha.get_artifacts(gha_run_id)
        artifacts_match, artifact_diffs = self.compare_artifacts(jenkins_artifacts, gha_artifacts)
        differences.extend(artifact_diffs)
        
        # 3. Compare exit codes
        print("  ✅ Comparing exit codes...")
        jenkins_info = self.jenkins.get_build_info(jenkins_job, jenkins_build)
        gha_info = self.gha.get_workflow_run(gha_run_id)
        
        jenkins_success = jenkins_info['result'] == 'SUCCESS'
        gha_success = gha_info['conclusion'] == 'success'
        exit_codes_match = jenkins_success == gha_success
        
        if not exit_codes_match:
            differences.append(
                f"Exit code mismatch: Jenkins={'SUCCESS' if jenkins_success else 'FAILURE'}, "
                f"GHA={'success' if gha_success else 'failure'}"
            )
        
        # Create validation result
        result = ValidationResult(
            timestamp=datetime.now().isoformat(),
            jenkins_build=f"{jenkins_job}#{jenkins_build}",
            gha_run=str(gha_run_id),
            tests_match=tests_match,
            artifacts_match=artifacts_match,
            exit_codes_match=exit_codes_match,
            differences=differences,
            confidence_score=0.0  # Will be calculated
        )
        
        result.confidence_score = self.calculate_confidence_score(result)
        
        return result
    
    def generate_report(self, result: ValidationResult, output_path: Path):
        """Generate HTML report for visualization"""
        html = f"""
<!DOCTYPE html>
<html>
<head>
    <title>Migration Validation Report</title>
    <style>
        body {{ font-family: Arial, sans-serif; margin: 40px; }}
        .header {{ background: #2c3e50; color: white; padding: 20px; border-radius: 5px; }}
        .score {{ font-size: 48px; font-weight: bold; }}
        .score.high {{ color: #27ae60; }}
        .score.medium {{ color: #f39c12; }}
        .score.low {{ color: #e74c3c; }}
        .section {{ margin: 20px 0; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }}
        .pass {{ color: #27ae60; font-weight: bold; }}
        .fail {{ color: #e74c3c; font-weight: bold; }}
        .diff {{ background: #fff3cd; padding: 10px; margin: 10px 0; border-left: 4px solid #ffc107; }}
    </style>
</head>
<body>
    <div class="header">
        <h1>Migration Validation Report</h1>
        <p>Jenkins Build: {result.jenkins_build} | GHA Run: {result.gha_run}</p>
        <p>Timestamp: {result.timestamp}</p>
    </div>
    
    <div class="section">
        <h2>Confidence Score</h2>
        <div class="score {'high' if result.confidence_score >= 90 else 'medium' if result.confidence_score >= 70 else 'low'}">
            {result.confidence_score:.1f}%
        </div>
    </div>
    
    <div class="section">
        <h2>Validation Results</h2>
        <p>Tests Match: <span class="{'pass' if result.tests_match else 'fail'}">
            {'✓ PASS' if result.tests_match else '✗ FAIL'}
        </span></p>
        <p>Artifacts Match: <span class="{'pass' if result.artifacts_match else 'fail'}">
            {'✓ PASS' if result.artifacts_match else '✗ FAIL'}
        </span></p>
        <p>Exit Codes Match: <span class="{'pass' if result.exit_codes_match else 'fail'}">
            {'✓ PASS' if result.exit_codes_match else '✗ FAIL'}
        </span></p>
    </div>
    
    <div class="section">
        <h2>Differences Found</h2>
        {''.join(f'<div class="diff">{diff}</div>' for diff in result.differences) if result.differences else '<p>No differences found! ✓</p>'}
    </div>
</body>
</html>
        """
        
        output_path.write_text(html)
        print(f"📄 Report generated: {output_path}")


def main():
    """Main entry point for validation"""
    import argparse
    
    parser = argparse.ArgumentParser(description='Validate Jenkins to GHA migration')
    parser.add_argument('--jenkins-url', required=True, help='Jenkins URL')
    parser.add_argument('--jenkins-user', default='admin', help='Jenkins username')
    parser.add_argument('--jenkins-password', default='admin123', help='Jenkins password')
    parser.add_argument('--jenkins-job', required=True, help='Jenkins job name')
    parser.add_argument('--jenkins-build', type=int, required=True, help='Jenkins build number')
    parser.add_argument('--gha-token', required=True, help='GitHub token')
    parser.add_argument('--gha-repo', required=True, help='GitHub repo (owner/repo)')
    parser.add_argument('--gha-run', type=int, required=True, help='GHA run ID')
    parser.add_argument('--output', default='validation-report.html', help='Output report path')
    
    args = parser.parse_args()
    
    # Initialize clients
    jenkins = JenkinsClient(args.jenkins_url, args.jenkins_user, args.jenkins_password)
    gha = GitHubActionsClient(args.gha_token, args.gha_repo)
    
    # Validate
    validator = MigrationValidator(jenkins, gha)
    result = validator.validate(args.jenkins_job, args.jenkins_build, args.gha_run)
    
    # Generate report
    validator.generate_report(result, Path(args.output))
    
    # Print summary
    print("\n" + "="*60)
    print(f"Confidence Score: {result.confidence_score:.1f}%")
    print(f"Tests Match: {'✓' if result.tests_match else '✗'}")
    print(f"Artifacts Match: {'✓' if result.artifacts_match else '✗'}")
    print(f"Exit Codes Match: {'✓' if result.exit_codes_match else '✗'}")
    
    if result.confidence_score >= 95:
        print("\n✅ READY TO AUTO-MIGRATE")
    elif result.confidence_score >= 80:
        print("\n⚠️  REVIEW REQUIRED")
    else:
        print("\n❌ MANUAL INTERVENTION NEEDED")
    
    print("="*60)
    
    return 0 if result.confidence_score >= 95 else 1


if __name__ == '__main__':
    exit(main())
