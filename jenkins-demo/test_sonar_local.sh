#!/bin/bash

###############################################################################
# Local SonarCloud Test Script
# Tests SonarCloud analysis locally using .env file
###############################################################################

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   SonarCloud Local Test                                   ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Load .env file
if [ ! -f "../.env" ]; then
    echo -e "${RED}❌ .env file not found!${NC}"
    echo "Create a .env file in the project root with:"
    echo "  SONAR_TOKEN=your_token_here"
    echo "  SONAR_ORGANIZATION=your-org-name"
    echo "  SONAR_PROJECT_KEY=reporting-dashboard"
    exit 1
fi

echo -e "${YELLOW}[1/5] Loading environment variables...${NC}"
export $(cat ../.env | grep -v '^#' | xargs)

# Check if token is set
if [ "$SONAR_TOKEN" == "your_sonar_token_here" ] || [ -z "$SONAR_TOKEN" ]; then
    echo -e "${RED}❌ SONAR_TOKEN not configured!${NC}"
    echo "Update .env file with your actual SonarCloud token"
    echo "Get it from: https://sonarcloud.io/account/security"
    exit 1
fi

echo -e "${GREEN}✅ Environment variables loaded${NC}"
echo "   Organization: ${SONAR_ORGANIZATION}"
echo "   Project Key: ${SONAR_PROJECT_KEY}"
echo "   Token: ${SONAR_TOKEN:0:10}..."
echo ""

# Run tests first
echo -e "${YELLOW}[2/5] Running tests...${NC}"
cd ..
./gradlew clean test
echo -e "${GREEN}✅ Tests passed${NC}"
echo ""

# Download SonarScanner if needed
cd jenkins-demo
SONAR_SCANNER_VERSION="5.0.1.3006"

# Detect OS
if [[ "$OSTYPE" == "darwin"* ]]; then
    SONAR_SCANNER_PLATFORM="macosx"
else
    SONAR_SCANNER_PLATFORM="linux"
fi

SONAR_SCANNER_DIR="sonar-scanner-${SONAR_SCANNER_VERSION}-${SONAR_SCANNER_PLATFORM}"

echo -e "${YELLOW}[3/5] Setting up SonarScanner...${NC}"
if [ ! -d "$SONAR_SCANNER_DIR" ]; then
    echo "   Downloading SonarScanner for ${SONAR_SCANNER_PLATFORM}..."
    curl -L -o "sonar-scanner-cli-${SONAR_SCANNER_VERSION}-${SONAR_SCANNER_PLATFORM}.zip" \
      "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-${SONAR_SCANNER_VERSION}-${SONAR_SCANNER_PLATFORM}.zip"
    unzip -q "sonar-scanner-cli-${SONAR_SCANNER_VERSION}-${SONAR_SCANNER_PLATFORM}.zip"
    rm "sonar-scanner-cli-${SONAR_SCANNER_VERSION}-${SONAR_SCANNER_PLATFORM}.zip"
    echo -e "${GREEN}✅ SonarScanner downloaded${NC}"
else
    echo -e "${GREEN}✅ SonarScanner already present${NC}"
fi
echo ""

# Update sonar-project.properties with env vars
echo -e "${YELLOW}[4/5] Updating sonar-project.properties...${NC}"
cd ..
cp sonar-project.properties sonar-project.properties.backup

sed -i.tmp "s/sonar.organization=.*/sonar.organization=${SONAR_ORGANIZATION}/" sonar-project.properties
sed -i.tmp "s/sonar.projectKey=.*/sonar.projectKey=${SONAR_PROJECT_KEY}/" sonar-project.properties
rm sonar-project.properties.tmp

echo -e "${GREEN}✅ Configuration updated${NC}"
echo ""

# Run SonarScanner
echo -e "${YELLOW}[5/5] Running SonarCloud analysis...${NC}"
cd jenkins-demo
./${SONAR_SCANNER_DIR}/bin/sonar-scanner \
  -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
  -Dsonar.organization=${SONAR_ORGANIZATION} \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=${SONAR_TOKEN} \
  -Dsonar.verbose=true

echo ""
echo -e "${GREEN}✅ SonarCloud analysis complete!${NC}"
echo ""
echo "View results at:"
echo "https://sonarcloud.io/dashboard?id=${SONAR_PROJECT_KEY}"
echo ""

# Restore backup
cd ..
mv sonar-project.properties.backup sonar-project.properties

echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}Test completed successfully! 🎉${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
