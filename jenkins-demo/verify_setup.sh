#!/bin/bash

###############################################################################
# Verify SonarCloud Setup
# Quick check that everything is configured correctly
###############################################################################

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "🔍 Verifying SonarCloud Setup..."
echo ""

ERRORS=0

# Check 1: .env file exists
if [ -f "../.env" ]; then
    echo -e "${GREEN}✅ .env file exists${NC}"
else
    echo -e "${RED}❌ .env file not found${NC}"
    echo "   Copy .env.example to .env and fill in your values"
    ERRORS=$((ERRORS + 1))
fi

# Check 2: .env is gitignored
if git check-ignore ../.env > /dev/null 2>&1; then
    echo -e "${GREEN}✅ .env is properly gitignored${NC}"
else
    echo -e "${RED}❌ .env is NOT gitignored${NC}"
    echo "   This is a security risk - add it to .gitignore"
    ERRORS=$((ERRORS + 1))
fi

# Check 3: Load and validate environment variables
if [ -f "../.env" ]; then
    export $(cat ../.env | grep -v '^#' | xargs)
    
    if [ "$SONAR_TOKEN" == "your_sonar_token_here" ] || [ -z "$SONAR_TOKEN" ]; then
        echo -e "${YELLOW}⚠️  SONAR_TOKEN not configured (still using placeholder)${NC}"
        echo "   Get your token from: https://sonarcloud.io/account/security"
        ERRORS=$((ERRORS + 1))
    else
        echo -e "${GREEN}✅ SONAR_TOKEN is configured${NC}"
    fi
    
    if [ "$SONAR_ORGANIZATION" == "your-org-name" ] || [ -z "$SONAR_ORGANIZATION" ]; then
        echo -e "${YELLOW}⚠️  SONAR_ORGANIZATION not configured${NC}"
        ERRORS=$((ERRORS + 1))
    else
        echo -e "${GREEN}✅ SONAR_ORGANIZATION is set: ${SONAR_ORGANIZATION}${NC}"
    fi
fi

# Check 4: sonar-project.properties exists
if [ -f "../sonar-project.properties" ]; then
    echo -e "${GREEN}✅ sonar-project.properties exists${NC}"
else
    echo -e "${RED}❌ sonar-project.properties not found${NC}"
    ERRORS=$((ERRORS + 1))
fi

# Check 5: Jenkinsfile has Sonar stage
if grep -q "SonarCloud Analysis" ../Jenkinsfile; then
    echo -e "${GREEN}✅ Jenkinsfile has SonarCloud stage${NC}"
else
    echo -e "${RED}❌ Jenkinsfile missing SonarCloud stage${NC}"
    ERRORS=$((ERRORS + 1))
fi

# Check 6: Build works
echo ""
echo "Testing build..."
cd ..
if ./gradlew clean test > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Build and tests pass${NC}"
else
    echo -e "${RED}❌ Build failed${NC}"
    echo "   Run: ./gradlew clean test"
    ERRORS=$((ERRORS + 1))
fi

# Summary
echo ""
echo "════════════════════════════════════════"
if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✅ All checks passed!${NC}"
    echo ""
    echo "Ready to test:"
    echo "  cd jenkins-demo && ./test_sonar_local.sh"
else
    echo -e "${RED}❌ Found $ERRORS issue(s)${NC}"
    echo ""
    echo "Fix the issues above before running SonarCloud analysis"
fi
echo "════════════════════════════════════════"
