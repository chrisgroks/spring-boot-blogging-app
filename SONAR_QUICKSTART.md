# SonarCloud Quick Start

## ✅ Current Status

Everything is configured and ready! Just need your SonarCloud credentials.

### What's Set Up
- ✅ `.env` file created (gitignored)
- ✅ `sonar-project.properties` configured
- ✅ Jenkinsfile has SonarCloud stage
- ✅ Build and tests pass
- ✅ Test scripts ready

### What's Needed
- ⚠️ Your actual SonarCloud token
- ⚠️ Your organization name

---

## 🚀 Quick Start (3 steps)

### 1. Get SonarCloud Token
```bash
# 1. Go to https://sonarcloud.io
# 2. Sign in with GitHub
# 3. Click "+" → "Analyze new project"
# 4. Select this repository
# 5. Go to My Account → Security → Generate Token
# 6. Copy the token
```

### 2. Update .env File
```bash
# Edit .env in project root:
SONAR_TOKEN=your_actual_token_here          # Paste your token
SONAR_ORGANIZATION=your-actual-org-name     # Your org key from SonarCloud
SONAR_PROJECT_KEY=spring-boot-blogging-app  # Keep this
```

### 3. Test Locally
```bash
cd jenkins-demo
./verify_setup.sh    # Verify everything is configured
./test_sonar_local.sh  # Run actual SonarCloud analysis
```

That's it! Results will appear at:
`https://sonarcloud.io/dashboard?id=spring-boot-blogging-app`

---

## 📁 Files Overview

| File | Purpose | Tracked |
|------|---------|---------|
| `.env` | Your credentials (local) | ❌ No (gitignored) |
| `.env.example` | Template with instructions | ✅ Yes |
| `sonar-project.properties` | Sonar configuration | ✅ Yes |
| `Jenkinsfile` | Jenkins pipeline with Sonar | ✅ Yes |
| `jenkins-demo/verify_setup.sh` | Check configuration | ✅ Yes |
| `jenkins-demo/test_sonar_local.sh` | Run Sonar locally | ✅ Yes |

---

## 🤖 For Devin

Devin can access environment variables from `.env` as:
```bash
$SONAR_TOKEN
$SONAR_ORGANIZATION
$SONAR_PROJECT_KEY
```

To run Sonar analysis:
```bash
cd jenkins-demo && ./test_sonar_local.sh
```

The script will:
1. Load `.env` variables
2. Run tests
3. Download SonarScanner CLI
4. Execute analysis
5. Show results URL

---

## 🔒 Security

✅ **Safe**: `.env` is gitignored and never committed  
✅ **Safe**: Tokens stored locally only  
✅ **Safe**: Jenkins uses credentials plugin (not .env)  

---

## 🐛 Troubleshooting

### "SONAR_TOKEN not configured"
Edit `.env` and replace `your_sonar_token_here` with your actual token

### ".env file not found"
The file exists but might be hidden. Run: `ls -la` to see it

### "Build failed"
Run: `./gradlew clean test` to see the error

### "sonar-scanner not found"
The script downloads it automatically - check your internet connection

---

## 📊 What Gets Analyzed

- ✅ **Code Quality**: Bugs, vulnerabilities, code smells
- ✅ **Security**: Security hotspots and vulnerabilities
- ✅ **Maintainability**: Code complexity, duplications
- ✅ **Java 17**: Configured for Java 17 source
- ❌ **Coverage**: Not enabled (requires Gradle 8+ upgrade)

---

## 🎯 Next Steps

After local testing works:

1. **For Jenkins**: Add `sonar-token` credential in Jenkins UI
2. **For GHA**: Add `SONAR_TOKEN` secret in GitHub settings
3. **Quality Gates**: Configure in SonarCloud UI
4. **Coverage**: Upgrade to Gradle 8+ to enable JaCoCo

---

## 📚 Full Documentation

See `jenkins-demo/SONAR_SETUP.md` for:
- Detailed setup instructions
- Jenkins configuration
- Code coverage setup
- Troubleshooting guide
