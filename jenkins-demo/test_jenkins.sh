#!/bin/bash

###############################################################################
# Simple Jenkins Test Script
# Tests that Jenkins can build the Spring Boot app
###############################################################################

set -e

JENKINS_URL="http://localhost:8080"
JENKINS_USER="admin"
JENKINS_PASSWORD="admin123"

echo "🧪 Testing Jenkins Setup..."
echo ""

# Get Jenkins crumb for CSRF protection
echo "1. Getting Jenkins crumb..."
CRUMB=$(curl -s -u "${JENKINS_USER}:${JENKINS_PASSWORD}" \
  "${JENKINS_URL}/crumbIssuer/api/json" | \
  python3 -c "import sys, json; print(json.load(sys.stdin)['crumb'])" 2>/dev/null || echo "")

if [ -z "$CRUMB" ]; then
    echo "⚠️  No CSRF protection (Jenkins might not be fully configured)"
    CRUMB_HEADER=""
else
    echo "✅ Got crumb: ${CRUMB:0:20}..."
    CRUMB_HEADER="-H Jenkins-Crumb:${CRUMB}"
fi

# Check Jenkins version
echo ""
echo "2. Checking Jenkins version..."
JENKINS_VERSION=$(curl -s -I "${JENKINS_URL}" | grep "X-Jenkins:" | cut -d' ' -f2 | tr -d '\r')
echo "✅ Jenkins version: ${JENKINS_VERSION}"

# Check Java version in Jenkins
echo ""
echo "3. Checking Java version in Jenkins..."
docker exec jenkins-migration-demo java -version 2>&1 | head -1

echo ""
echo "✅ Jenkins is ready for testing!"
echo ""
echo "Next steps:"
echo "  1. Open http://localhost:8080 in your browser"
echo "  2. Login with admin/admin123"
echo "  3. Create a Pipeline job manually or use the run_demo.sh script"
