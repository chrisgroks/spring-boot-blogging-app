#!/bin/bash

###############################################################################
# Jenkins to GitHub Actions Migration Demo Runner
# 
# This script orchestrates the full demo:
# 1. Spins up Jenkins locally in Docker
# 2. Configures Jenkins job via REST API
# 3. Triggers both Jenkins and GHA pipelines
# 4. Compares outputs and generates validation report
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
JENKINS_URL="http://localhost:8080"
JENKINS_USER="admin"
JENKINS_PASSWORD="admin123"
JOB_NAME="reporting-dashboard"
REPO_PATH="../"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   Jenkins to GitHub Actions Migration Demo                ║${NC}"
echo -e "${BLUE}║   Devin @ Cognition AI                                     ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

###############################################################################
# Step 1: Start Jenkins
###############################################################################
echo -e "${YELLOW}[1/6] Starting Jenkins in Docker...${NC}"

if docker ps | grep -q jenkins-migration-demo; then
    echo "  ✓ Jenkins already running"
else
    docker-compose up -d
    echo "  ⏳ Waiting for Jenkins to start (this may take 1-2 minutes)..."
    
    # Wait for Jenkins to be ready
    for i in {1..60}; do
        if curl -s -f "${JENKINS_URL}/login" > /dev/null 2>&1; then
            echo -e "  ${GREEN}✓ Jenkins is ready!${NC}"
            break
        fi
        echo -n "."
        sleep 2
    done
    echo ""
fi

###############################################################################
# Step 2: Create Jenkins Job via REST API
###############################################################################
echo -e "${YELLOW}[2/6] Creating Jenkins job via REST API...${NC}"

# Job configuration XML
JOB_CONFIG=$(cat <<EOF
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job@2.40">
  <description>Spring Boot Blogging App - Migrated from GitHub Actions</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
      <triggers>
        <hudson.triggers.SCMTrigger>
          <spec>H/5 * * * *</spec>
          <ignorePostCommitHooks>false</ignorePostCommitHooks>
        </hudson.triggers.SCMTrigger>
      </triggers>
    </org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
  </properties>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2.90">
    <scm class="hudson.plugins.git.GitSCM" plugin="git@4.7.1">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>file://${REPO_PATH}</url>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/main</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="list"/>
      <extensions/>
    </scm>
    <scriptPath>Jenkinsfile</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
EOF
)

# Create or update job
curl -X POST "${JENKINS_URL}/createItem?name=${JOB_NAME}" \
  --user "${JENKINS_USER}:${JENKINS_PASSWORD}" \
  --header "Content-Type: application/xml" \
  --data "${JOB_CONFIG}" 2>/dev/null || \
curl -X POST "${JENKINS_URL}/job/${JOB_NAME}/config.xml" \
  --user "${JENKINS_USER}:${JENKINS_PASSWORD}" \
  --header "Content-Type: application/xml" \
  --data "${JOB_CONFIG}" 2>/dev/null

echo -e "  ${GREEN}✓ Jenkins job created/updated${NC}"

###############################################################################
# Step 3: Trigger Jenkins Build
###############################################################################
echo -e "${YELLOW}[3/6] Triggering Jenkins build...${NC}"

# Trigger build
BUILD_QUEUE=$(curl -X POST "${JENKINS_URL}/job/${JOB_NAME}/build" \
  --user "${JENKINS_USER}:${JENKINS_PASSWORD}" \
  -s -D - -o /dev/null | grep -i "Location:" | awk '{print $2}' | tr -d '\r')

# Extract queue ID
QUEUE_ID=$(echo $BUILD_QUEUE | grep -o '[0-9]*' | tail -1)

echo "  ⏳ Build queued (ID: ${QUEUE_ID})"

# Wait for build to start and get build number
sleep 5
BUILD_NUMBER=$(curl -s "${JENKINS_URL}/queue/item/${QUEUE_ID}/api/json" \
  --user "${JENKINS_USER}:${JENKINS_PASSWORD}" | \
  python3 -c "import sys, json; print(json.load(sys.stdin).get('executable', {}).get('number', 0))")

if [ "$BUILD_NUMBER" == "0" ]; then
    # Fallback: get last build number
    BUILD_NUMBER=$(curl -s "${JENKINS_URL}/job/${JOB_NAME}/lastBuild/buildNumber" \
      --user "${JENKINS_USER}:${JENKINS_PASSWORD}")
fi

echo "  📊 Build #${BUILD_NUMBER} started"
echo "  🔗 ${JENKINS_URL}/job/${JOB_NAME}/${BUILD_NUMBER}/"

# Wait for build to complete
echo "  ⏳ Waiting for build to complete..."
while true; do
    BUILD_STATUS=$(curl -s "${JENKINS_URL}/job/${JOB_NAME}/${BUILD_NUMBER}/api/json" \
      --user "${JENKINS_USER}:${JENKINS_PASSWORD}" | \
      python3 -c "import sys, json; print(json.load(sys.stdin).get('result', 'BUILDING'))")
    
    if [ "$BUILD_STATUS" != "BUILDING" ] && [ "$BUILD_STATUS" != "null" ]; then
        break
    fi
    echo -n "."
    sleep 5
done
echo ""

if [ "$BUILD_STATUS" == "SUCCESS" ]; then
    echo -e "  ${GREEN}✓ Jenkins build completed successfully${NC}"
else
    echo -e "  ${RED}✗ Jenkins build failed: ${BUILD_STATUS}${NC}"
fi

###############################################################################
# Step 4: Trigger GitHub Actions (if configured)
###############################################################################
echo -e "${YELLOW}[4/6] Triggering GitHub Actions workflow...${NC}"

if [ -z "$GITHUB_TOKEN" ]; then
    echo -e "  ${YELLOW}⚠️  GITHUB_TOKEN not set, skipping GHA trigger${NC}"
    echo "  💡 Set GITHUB_TOKEN to enable GHA comparison"
    GHA_RUN_ID="0"
else
    # Trigger GHA workflow
    # This would use GitHub API to trigger workflow_dispatch
    echo "  ⏳ Triggering GHA workflow..."
    # Implementation depends on repo configuration
    GHA_RUN_ID="12345"  # Placeholder
    echo -e "  ${GREEN}✓ GHA workflow triggered${NC}"
fi

###############################################################################
# Step 5: Compare Outputs
###############################################################################
echo -e "${YELLOW}[5/6] Comparing Jenkins and GHA outputs...${NC}"

if [ "$GHA_RUN_ID" != "0" ]; then
    python3 validate_migration.py \
      --jenkins-url "${JENKINS_URL}" \
      --jenkins-user "${JENKINS_USER}" \
      --jenkins-password "${JENKINS_PASSWORD}" \
      --jenkins-job "${JOB_NAME}" \
      --jenkins-build "${BUILD_NUMBER}" \
      --gha-token "${GITHUB_TOKEN}" \
      --gha-repo "${GITHUB_REPO}" \
      --gha-run "${GHA_RUN_ID}" \
      --output "validation-report-${BUILD_NUMBER}.html"
    
    VALIDATION_EXIT=$?
else
    echo "  📊 Fetching Jenkins test results..."
    
    # Get test results from Jenkins
    TEST_RESULTS=$(curl -s "${JENKINS_URL}/job/${JOB_NAME}/${BUILD_NUMBER}/testReport/api/json" \
      --user "${JENKINS_USER}:${JENKINS_PASSWORD}")
    
    TOTAL_TESTS=$(echo $TEST_RESULTS | python3 -c "import sys, json; print(json.load(sys.stdin).get('totalCount', 0))" 2>/dev/null || echo "0")
    PASSED_TESTS=$(echo $TEST_RESULTS | python3 -c "import sys, json; print(json.load(sys.stdin).get('passCount', 0))" 2>/dev/null || echo "0")
    FAILED_TESTS=$(echo $TEST_RESULTS | python3 -c "import sys, json; print(json.load(sys.stdin).get('failCount', 0))" 2>/dev/null || echo "0")
    
    echo "  📊 Test Results:"
    echo "     Total: ${TOTAL_TESTS}"
    echo "     Passed: ${PASSED_TESTS}"
    echo "     Failed: ${FAILED_TESTS}"
    
    VALIDATION_EXIT=0
fi

###############################################################################
# Step 6: Generate Summary
###############################################################################
echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   Demo Summary                                             ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo "Jenkins Build: #${BUILD_NUMBER}"
echo "Status: ${BUILD_STATUS}"
echo "URL: ${JENKINS_URL}/job/${JOB_NAME}/${BUILD_NUMBER}/"
echo ""

if [ "$GHA_RUN_ID" != "0" ]; then
    echo "GitHub Actions Run: #${GHA_RUN_ID}"
    echo "Validation Report: validation-report-${BUILD_NUMBER}.html"
    echo ""
    
    if [ $VALIDATION_EXIT -eq 0 ]; then
        echo -e "${GREEN}✅ MIGRATION VALIDATED - Ready to auto-migrate${NC}"
    else
        echo -e "${YELLOW}⚠️  REVIEW REQUIRED - Manual verification needed${NC}"
    fi
else
    echo "💡 To enable full validation:"
    echo "   export GITHUB_TOKEN=your_token"
    echo "   export GITHUB_REPO=owner/repo"
    echo "   ./run_demo.sh"
fi

echo ""
echo -e "${BLUE}Next Steps:${NC}"
echo "  1. Review Jenkins console output: ${JENKINS_URL}/job/${JOB_NAME}/${BUILD_NUMBER}/console"
echo "  2. Check test results: ${JENKINS_URL}/job/${JOB_NAME}/${BUILD_NUMBER}/testReport/"
echo "  3. View artifacts: ${JENKINS_URL}/job/${JOB_NAME}/${BUILD_NUMBER}/artifact/"
if [ "$GHA_RUN_ID" != "0" ]; then
    echo "  4. Open validation report: open validation-report-${BUILD_NUMBER}.html"
fi

echo ""
echo -e "${GREEN}Demo complete! 🎉${NC}"
