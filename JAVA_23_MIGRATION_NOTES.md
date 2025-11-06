# Java 23 Migration - Compatibility Issues

## Overview
This document outlines the configuration changes made to migrate from Java 17 to Java 23, and the compatibility issues discovered during the migration.

## Files Updated

### Configuration Files (Updated Successfully)
1. **build.gradle** (lines 10-11)
   - `sourceCompatibility = '23'`
   - `targetCompatibility = '23'`

2. **.github/workflows/gradle.yml** (lines 20-24)
   - `java-version: '23'`

3. **Jenkinsfile** (lines 4-7)
   - `jdk 'JDK-23'`

4. **sonar-project.properties** (lines 27-28)
   - `sonar.java.source=23`
   - `sonar.java.target=23`

5. **jenkins-demo/TESTING.md** (line 9)
   - Updated Docker image reference to `jenkins/jenkins:lts-jdk23`

### Additional Files Updated for Consistency
- jenkins-demo/docker-compose.yml
- jenkins-demo/jenkins-init/02-configure-jdk.groovy
- SONAR_QUICKSTART.md
- jenkins-demo/SONAR_SETUP.md

## Compatibility Issues Discovered

### Critical: Gradle 7.4 Does Not Support Java 23

**Error Message:**
```
> Task :compileJava FAILED
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':compileJava'.
> error: invalid source release: 21
```

**Root Cause:**
- Gradle 7.4 was released in February 2022
- Officially supports Java 8 through Java 17
- Java 21 (LTS) was released in September 2023
- Java 23 was released in September 2024
- Gradle 7.4 does not recognize Java 23 syntax/features

**Gradle Version Requirements:**
- Java 21 support: Requires Gradle 8.5+
- Java 23 support: Requires Gradle 8.10+ (or latest)

### Spring Boot 2.6.3 Compatibility

The project currently uses Spring Boot 2.6.3, which:
- Was released in January 2022
- Officially supports Java 11-17
- Does NOT support Java 21 or Java 23
- Spring Boot 3.x is required for Java 17+ support (with Java 21+ requiring 3.2+)

### Other Dependencies

The following dependencies may also need updates:
- MyBatis Spring Boot Starter 2.2.2
- Netflix DGS GraphQL 4.9.21
- JJWT 0.11.2
- Checkstyle 10.12.4

## Required Changes to Make Build Work

### 1. Upgrade Gradle (Required)
```bash
./gradlew wrapper --gradle-version=8.10
```

### 2. Upgrade Spring Boot (Required)
Update `build.gradle`:
```gradle
plugins {
    id 'org.springframework.boot' version '3.3.5'  // or latest 3.x
    id 'io.spring.dependency-management' version '1.1.6'
    // ... other plugins
}
```

### 3. Verify/Update Dependencies
All dependencies must be compatible with:
- Java 23
- Spring Boot 3.x
- Gradle 8.10+

### 4. Update Application Code
Spring Boot 3.x migration may require:
- Updating javax.* imports to jakarta.*
- Reviewing deprecated API usage
- Updating configuration properties
- Testing all functionality

### 5. Update CI/CD Environment
- GitHub Actions: Ensure `actions/setup-java@v2` supports Java 23 (may need v3 or v4)
- Jenkins: Configure JDK-23 installation
- Docker: Verify `jenkins/jenkins:lts-jdk23` image availability

## Testing Strategy

Since the build currently fails due to Gradle incompatibility, the following testing approach is recommended:

1. **Phase 1: Upgrade Gradle**
   - Update to Gradle 8.10+
   - Verify build compiles with Java 17 settings
   - Test that all existing tests pass

2. **Phase 2: Upgrade Spring Boot**
   - Update to Spring Boot 3.3.x
   - Migrate javax.* to jakarta.*
   - Fix any breaking changes
   - Verify tests pass

3. **Phase 3: Update to Java 23**
   - Change sourceCompatibility/targetCompatibility to 23
   - Install JDK 23 in local environment
   - Run full test suite
   - Verify all functionality

4. **Phase 4: Update CI/CD**
   - Update GitHub Actions workflows
   - Configure Jenkins with JDK-23
   - Verify CI builds pass

## Current Status

✅ **Completed:**
- Updated all configuration files to reference Java 23
- Documented compatibility issues
- Created migration notes

⚠️ **Blocked:**
- Build fails with Gradle 7.4
- Cannot run tests until Gradle is upgraded
- Cannot verify Java 23 compatibility

❌ **Not Started:**
- Gradle upgrade
- Spring Boot upgrade
- Dependency updates
- Application code migration

## Recommendations

### Option 1: Full Migration (Recommended for Production)
1. Upgrade Gradle to 8.10+
2. Upgrade Spring Boot to 3.3.x
3. Update all dependencies
4. Migrate application code
5. Comprehensive testing
6. Gradual rollout

**Estimated Effort:** 2-4 weeks depending on codebase size and test coverage

### Option 2: Incremental Migration
1. Keep Java 17 for now
2. Upgrade Gradle first (7.4 → 8.10)
3. Upgrade Spring Boot (2.6.3 → 3.3.x)
4. Then upgrade to Java 23
5. Test at each step

**Estimated Effort:** 3-6 weeks with lower risk

### Option 3: Stay on Java 17 (Lowest Risk)
- Java 17 is LTS (supported until 2029)
- Current stack is stable
- Defer Java 23 migration until business need arises

## References

- [Gradle Compatibility Matrix](https://docs.gradle.org/current/userguide/compatibility.html)
- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Java 21 Release Notes](https://openjdk.org/projects/jdk/21/)
- [Java 23 Release Notes](https://openjdk.org/projects/jdk/23/)

## Contact

For questions about this migration, contact:
- Migration requested by: @woutermesker
- Session: https://hsbc.devinenterprise.com/sessions/aa15ac1447934dcea64cb33f8c35b999
