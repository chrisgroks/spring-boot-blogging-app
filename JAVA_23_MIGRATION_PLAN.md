# Java 23 Migration Plan for Spring Boot Blogging Application

## Executive Summary

This document provides a comprehensive, step-by-step migration plan for upgrading the **chrisgroks/spring-boot-blogging-app** repository from Java 17 to Java 23. This migration requires significant framework upgrades, including Spring Boot 2.6.3 → 3.2+ and package namespace changes from `javax.*` to `jakarta.*`.

**Estimated Timeline**: 2-3 days  
**Risk Level**: HIGH (Major version upgrades with breaking changes)  
**Rollback Complexity**: MEDIUM (Well-defined rollback points)

---

## Table of Contents

1. [Pre-Migration Checklist](#pre-migration-checklist)
2. [Step-by-Step Migration Sequence](#step-by-step-migration-sequence)
3. [Rollback Plan](#rollback-plan)
4. [Validation Steps](#validation-steps)
5. [Risk Assessment](#risk-assessment)
6. [Appendix](#appendix)

---

## Pre-Migration Checklist

### 1. Environment Preparation

- [ ] **Backup Repository**: Create a full backup of the current codebase
  ```bash
  git clone https://github.com/chrisgroks/spring-boot-blogging-app.git backup-java17
  cd backup-java17
  git checkout master
  ```

- [ ] **Verify Current State**: Ensure all tests pass on Java 17
  ```bash
  ./gradlew clean test
  # Expected: All 20 test suites (68 tests) pass
  ```

- [ ] **Document Current Configuration**:
  - Java Version: 17 (build.gradle lines 10-11)
  - Spring Boot: 2.6.3 (build.gradle line 2)
  - Gradle: 7.4 (gradle/wrapper/gradle-wrapper.properties line 3)
  - MyBatis: 2.2.2 (build.gradle line 41)
  - Netflix DGS: 4.9.21 (build.gradle line 42)
  - JJWT: 0.11.2 (build.gradle lines 44-46)

- [ ] **Install Java 23**: Ensure JDK 23 is available on development and CI environments
  ```bash
  # For local development
  sdk install java 23-open
  sdk use java 23-open
  
  # Verify installation
  java -version  # Should show version 23
  ```

- [ ] **Create Migration Branch**:
  ```bash
  git checkout -b migration/java-23
  ```

### 2. Dependency Audit

Run dependency analysis to identify potential compatibility issues:

```bash
./gradlew dependencies > dependencies-before.txt
./gradlew dependencyInsight --dependency spring-boot-starter-web
```

### 3. Identify All javax.* Usage

**Files with javax.* imports** (21 files identified):

**Validation Package (javax.validation.* → jakarta.validation.*)**:
- `src/main/java/io/spring/application/article/NewArticleParam.java` (line 5)
- `src/main/java/io/spring/application/article/ArticleCommandService.java` (line 6)
- `src/main/java/io/spring/application/user/DuplicatedUsernameValidator.java` (lines 4-5)
- `src/main/java/io/spring/application/user/DuplicatedEmailValidator.java` (lines 4-5)
- `src/main/java/io/spring/application/user/DuplicatedEmailConstraint.java` (lines 5-6)
- `src/main/java/io/spring/application/user/UpdateUserParam.java` (line 4)
- `src/main/java/io/spring/application/user/DuplicatedUsernameConstraint.java` (lines 5-6)
- `src/main/java/io/spring/application/user/RegisterParam.java` (lines 4-5)
- `src/main/java/io/spring/application/article/DuplicatedArticleValidator.java` (lines 5-6)
- `src/main/java/io/spring/application/article/DuplicatedArticleConstraint.java` (lines 5-6)
- `src/main/java/io/spring/application/user/UserService.java`
- `src/main/java/io/spring/graphql/UserMutation.java`
- `src/main/java/io/spring/api/ArticleApi.java`
- `src/main/java/io/spring/api/UsersApi.java`
- `src/main/java/io/spring/graphql/exception/GraphQLCustomizeExceptionHandler.java`
- `src/main/java/io/spring/api/ArticlesApi.java`
- `src/main/java/io/spring/api/CommentsApi.java`
- `src/main/java/io/spring/api/CurrentUserApi.java`
- `src/main/java/io/spring/api/exception/CustomizeExceptionHandler.java`

**Servlet Package (javax.servlet.* → jakarta.servlet.*)**:
- `src/main/java/io/spring/api/security/JwtTokenFilter.java` (lines 8-11)

**Crypto Package (javax.crypto.* - No change required)**:
- `src/main/java/io/spring/infrastructure/service/DefaultJwtService.java` (lines 11-12)
  - Note: javax.crypto is part of JDK, not Jakarta EE, so no migration needed

### 4. Communication Plan

- [ ] Notify team of upcoming migration window
- [ ] Schedule code freeze period for migration
- [ ] Prepare rollback communication channels

---

## Step-by-Step Migration Sequence

### Phase 1: Gradle Upgrade (Foundation)

**Objective**: Upgrade Gradle wrapper to support Java 23

#### Step 1.1: Update Gradle Wrapper

**File**: `gradle/wrapper/gradle-wrapper.properties`

**Current (line 3)**:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-7.4-bin.zip
```

**Target**:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
```

**Commands**:
```bash
./gradlew wrapper --gradle-version 8.5
./gradlew --version  # Verify upgrade
```

**Rationale**: Gradle 8.5+ is required for Java 23 support. Current Gradle 7.4 has known issues with Java 17 bytecode (documented in jenkins-demo/SONAR_SETUP.md, lines 14-16).

**Validation**:
```bash
./gradlew tasks  # Should complete without errors
```

---

### Phase 2: Java Version Update

**Objective**: Update Java version in build configuration and CI/CD

#### Step 2.1: Update build.gradle

**File**: `build.gradle`

**Current (lines 10-11)**:
```groovy
sourceCompatibility = '17'
targetCompatibility = '17'
```

**Target**:
```groovy
sourceCompatibility = '23'
targetCompatibility = '23'
```

#### Step 2.2: Update GitHub Actions

**File**: `.github/workflows/gradle.yml`

**Current (lines 20-24)**:
```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v2
  with:
    distribution: zulu
    java-version: '17'
```

**Target**:
```yaml
- name: Set up JDK 23
  uses: actions/setup-java@v2
  with:
    distribution: zulu
    java-version: '23'
```

#### Step 2.3: Update Jenkins Configuration

**File**: `Jenkinsfile`

**Current (lines 5-6)**:
```groovy
// Configure JDK 17 (matches build.gradle sourceCompatibility)
jdk 'JDK-17'
```

**Target**:
```groovy
// Configure JDK 23 (matches build.gradle sourceCompatibility)
jdk 'JDK-23'
```

**Note**: Jenkins administrator must configure JDK-23 tool in Jenkins Global Tool Configuration before this change.

**Validation**:
```bash
# Local validation
java -version  # Should show Java 23
./gradlew clean build -x test  # Build without running tests yet
```

---

### Phase 3: Spring Boot Upgrade

**Objective**: Upgrade Spring Boot 2.6.3 → 3.2.x (latest stable)

#### Step 3.1: Update Spring Boot Version

**File**: `build.gradle`

**Current (lines 1-3)**:
```groovy
plugins {
    id 'org.springframework.boot' version '2.6.3'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
```

**Target**:
```groovy
plugins {
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
```

**Breaking Changes**:
- Spring Boot 3.x requires Java 17 minimum (Java 23 supported)
- Jakarta EE 9+ (javax.* → jakarta.*)
- Spring Security 6.x (configuration changes required)

---

### Phase 4: Dependency Updates

**Objective**: Update all dependencies for Java 23 and Spring Boot 3.2 compatibility

#### Step 4.1: Update MyBatis Spring Boot Starter

**File**: `build.gradle`

**Current (line 41)**:
```groovy
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.2.2'
```

**Target**:
```groovy
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
```

**Also update test dependency (line 59)**:
```groovy
testImplementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.3'
```

#### Step 4.2: Update Netflix DGS (GraphQL)

**File**: `build.gradle`

**Current (line 42)**:
```groovy
implementation 'com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter:4.9.21'
```

**Target**:
```groovy
implementation 'com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter:8.1.1'
```

**Breaking Changes**:
- DGS 8.x has API changes for error handling
- Review GraphQL exception handler: `src/main/java/io/spring/graphql/exception/GraphQLCustomizeExceptionHandler.java`

#### Step 4.3: Update DGS Codegen Plugin

**File**: `build.gradle`

**Current (line 5)**:
```groovy
id "com.netflix.dgs.codegen" version "5.0.6"
```

**Target**:
```groovy
id "com.netflix.dgs.codegen" version "6.2.1"
```

#### Step 4.4: Update JJWT (JWT Library)

**File**: `build.gradle`

**Current (lines 44-46)**:
```groovy
implementation 'io.jsonwebtoken:jjwt-api:0.11.2'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.2',
            'io.jsonwebtoken:jjwt-jackson:0.11.2'
```

**Target**:
```groovy
implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.5',
            'io.jsonwebtoken:jjwt-jackson:0.12.5'
```

**Breaking Changes**:
- JJWT 0.12.x has API changes in key handling
- Review JWT service: `src/main/java/io/spring/infrastructure/service/DefaultJwtService.java`

#### Step 4.5: Update Checkstyle

**File**: `build.gradle`

**Current (line 14)**:
```groovy
toolVersion = '10.12.4'
```

**Target**:
```groovy
toolVersion = '10.12.5'
```

**Note**: Checkstyle 10.12.5+ adds Java 23 support.

#### Step 4.6: Verify Lombok Compatibility

**File**: `build.gradle` (lines 50-51)

**Current**:
```groovy
compileOnly 'org.projectlombok:lombok:1.18.30'
annotationProcessor 'org.projectlombok:lombok:1.18.30'
```

**Action**: Lombok 1.18.30 supports Java 23. No change required, but verify in testing phase.

#### Step 4.7: Update REST Assured (Test Dependency)

**File**: `build.gradle`

**Current (lines 53-56)**:
```groovy
testImplementation 'io.rest-assured:rest-assured:4.5.1'
testImplementation 'io.rest-assured:json-path:4.5.1'
testImplementation 'io.rest-assured:xml-path:4.5.1'
testImplementation 'io.rest-assured:spring-mock-mvc:4.5.1'
```

**Target**:
```groovy
testImplementation 'io.rest-assured:rest-assured:5.4.0'
testImplementation 'io.rest-assured:json-path:5.4.0'
testImplementation 'io.rest-assured:xml-path:5.4.0'
testImplementation 'io.rest-assured:spring-mock-mvc:5.4.0'
```

---

### Phase 5: Package Migration (javax.* → jakarta.*)

**Objective**: Replace all javax.* imports with jakarta.* equivalents

#### Step 5.1: Automated Package Replacement

**Option A: Using find-and-edit command (Recommended)**

Use IDE or automated tools to replace:
- `javax.validation` → `jakarta.validation`
- `javax.servlet` → `jakarta.servlet`

**Option B: Manual replacement using sed (Backup approach)**

```bash
# Validation package
find src/main/java -name "*.java" -type f -exec sed -i 's/import javax\.validation\./import jakarta.validation./g' {} +

# Servlet package  
find src/main/java -name "*.java" -type f -exec sed -i 's/import javax\.servlet\./import jakarta.servlet./g' {} +

# Verify changes
git diff src/main/java/
```

#### Step 5.2: Update Specific Files

**Files requiring javax.validation → jakarta.validation migration** (19 files):

1. `src/main/java/io/spring/application/article/NewArticleParam.java`
2. `src/main/java/io/spring/application/article/ArticleCommandService.java`
3. `src/main/java/io/spring/application/user/DuplicatedUsernameValidator.java`
4. `src/main/java/io/spring/application/user/DuplicatedEmailValidator.java`
5. `src/main/java/io/spring/application/user/DuplicatedEmailConstraint.java`
6. `src/main/java/io/spring/application/user/UpdateUserParam.java`
7. `src/main/java/io/spring/application/user/DuplicatedUsernameConstraint.java`
8. `src/main/java/io/spring/application/user/RegisterParam.java`
9. `src/main/java/io/spring/application/article/DuplicatedArticleValidator.java`
10. `src/main/java/io/spring/application/article/DuplicatedArticleConstraint.java`
11. `src/main/java/io/spring/application/user/UserService.java`
12. `src/main/java/io/spring/graphql/UserMutation.java`
13. `src/main/java/io/spring/api/ArticleApi.java`
14. `src/main/java/io/spring/api/UsersApi.java`
15. `src/main/java/io/spring/graphql/exception/GraphQLCustomizeExceptionHandler.java`
16. `src/main/java/io/spring/api/ArticlesApi.java`
17. `src/main/java/io/spring/api/CommentsApi.java`
18. `src/main/java/io/spring/api/CurrentUserApi.java`
19. `src/main/java/io/spring/api/exception/CustomizeExceptionHandler.java`

**Files requiring javax.servlet → jakarta.servlet migration** (1 file):

1. `src/main/java/io/spring/api/security/JwtTokenFilter.java`

**Note**: `src/main/java/io/spring/infrastructure/service/DefaultJwtService.java` uses `javax.crypto.*` which is part of JDK, not Jakarta EE, so NO migration required.

---

### Phase 6: Spring Security 6.x Migration

**Objective**: Migrate from deprecated WebSecurityConfigurerAdapter to component-based security

#### Step 6.1: Refactor WebSecurityConfig

**File**: `src/main/java/io/spring/api/security/WebSecurityConfig.java`

**Current approach** (lines 11, 23, 35-65):
```java
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
  
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // Configuration here
  }
}
```

**Target approach**:
```java
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
  
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        .cors()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeHttpRequests()
        .requestMatchers(HttpMethod.OPTIONS).permitAll()
        .requestMatchers("/graphiql").permitAll()
        .requestMatchers("/graphql").permitAll()
        .requestMatchers(HttpMethod.GET, "/articles/feed").authenticated()
        .requestMatchers(HttpMethod.POST, "/users", "/users/login").permitAll()
        .requestMatchers(HttpMethod.GET, "/articles/**", "/profiles/**", "/tags").permitAll()
        .anyRequest().authenticated();

    http.addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
  }
  
  // Keep other beans unchanged
}
```

**Key Changes**:
1. Remove `extends WebSecurityConfigurerAdapter`
2. Change `configure(HttpSecurity http)` to `SecurityFilterChain filterChain(HttpSecurity http)`
3. Add `@Bean` annotation to the method
4. Replace `.authorizeRequests()` with `.authorizeHttpRequests()`
5. Replace `.antMatchers()` with `.requestMatchers()`
6. Add `return http.build();` at the end

---

### Phase 7: Code Quality and Build Tools

#### Step 7.1: Update Checkstyle Configuration (if needed)

Verify that `config/checkstyle/checkstyle.xml` is compatible with Checkstyle 10.12.5. No changes expected, but review warnings.

#### Step 7.2: Regenerate GraphQL Code

After updating DGS plugin, regenerate GraphQL types:

```bash
./gradlew clean generateJava
```

This generates Java types from `src/main/resources/schema/schema.graphqls` into the `io.spring.graphql` package.

---

### Phase 8: Testing and Validation

#### Step 8.1: Compile Code

```bash
./gradlew clean build -x test
```

Expected: Code compiles without errors.

#### Step 8.2: Run Unit Tests

```bash
./gradlew test
```

Expected: All 20 test suites (68 tests) pass.

#### Step 8.3: Run Checkstyle

```bash
./gradlew checkstyleMain checkstyleTest
```

Expected: No violations (or only pre-existing violations).

#### Step 8.4: Test JWT Token Generation

Pay special attention to JWT-related tests due to JJWT 0.12.x API changes:
- Test user login/registration flows
- Verify token validation in `JwtTokenFilter`
- Check `DefaultJwtService.java` key handling

#### Step 8.5: Test REST API

```bash
./gradlew bootRun
# In another terminal:
curl http://localhost:8080/tags
```

Expected: Application starts successfully, API responds correctly.

#### Step 8.6: Test GraphQL API

Navigate to http://localhost:8080/graphiql and execute sample queries:

```graphql
query {
  tags
}
```

Expected: GraphQL endpoint responds correctly.

#### Step 8.7: Run Integration Tests

Focus on:
- REST Assured integration tests
- MyBatis repository tests
- Spring Security authentication tests

---

## Rollback Plan

### Rollback Decision Points

**Decision Point 1: After Phase 2 (Java Version Update)**
- **Trigger**: Build fails after Java version update
- **Action**: Revert changes, investigate compatibility issues

**Decision Point 2: After Phase 5 (Package Migration)**
- **Trigger**: Compilation errors persist after package migration
- **Action**: Review migration completeness, check for missed imports

**Decision Point 3: After Phase 8 (Testing)**
- **Trigger**: Critical tests fail or application doesn't start
- **Action**: Analyze root cause, potentially rollback to pre-migration state

### Rollback Procedure

#### Quick Rollback (During Migration)

```bash
# Discard all changes
git checkout .
git clean -fd

# Switch back to main branch
git checkout master
```

#### Full Rollback (After Merge)

```bash
# Create revert branch
git checkout master
git checkout -b revert/java-23-migration

# Revert the merge commit
git revert -m 1 <merge-commit-hash>

# Create PR to revert
git push origin revert/java-23-migration
```

#### Partial Rollback

If only specific phases need rollback:

```bash
# Revert specific files
git checkout master -- build.gradle
git checkout master -- src/main/java/io/spring/api/security/WebSecurityConfig.java

# Commit partial revert
git commit -m "Partial rollback: Revert security configuration"
```

### Rollback Verification

After rollback:
1. Verify Java 17 is active: `java -version`
2. Run tests: `./gradlew clean test`
3. Confirm all 68 tests pass
4. Test application startup: `./gradlew bootRun`

---

## Validation Steps

### Pre-Migration Validation

- [ ] **Baseline Tests**: All 68 tests pass on Java 17
  ```bash
  ./gradlew clean test
  ```

- [ ] **Baseline Build**: Application builds successfully
  ```bash
  ./gradlew clean build
  ```

- [ ] **Baseline Runtime**: Application runs without errors
  ```bash
  ./gradlew bootRun
  curl http://localhost:8080/tags
  ```

### Post-Migration Validation

#### Phase-by-Phase Validation

**After Phase 1 (Gradle Upgrade)**:
```bash
./gradlew --version  # Should show Gradle 8.5
./gradlew tasks      # Should complete without errors
```

**After Phase 2 (Java Version Update)**:
```bash
java -version        # Should show Java 23
./gradlew clean build -x test
```

**After Phase 3-4 (Spring Boot & Dependencies)**:
```bash
./gradlew clean build -x test
./gradlew dependencies | grep "spring-boot-starter-web"  # Should show 3.2.x
```

**After Phase 5 (Package Migration)**:
```bash
# Verify no javax.validation or javax.servlet imports remain
grep -r "import javax.validation" src/main/java/  # Should return nothing
grep -r "import javax.servlet" src/main/java/     # Should return nothing
./gradlew clean build -x test
```

**After Phase 6 (Spring Security Migration)**:
```bash
./gradlew clean build -x test
# Verify no deprecated WebSecurityConfigurerAdapter usage
grep -r "WebSecurityConfigurerAdapter" src/main/java/  # Should return nothing
```

**After Phase 8 (Complete Migration)**:
```bash
# Full test suite
./gradlew clean test

# Checkstyle validation
./gradlew checkstyleMain checkstyleTest

# Runtime validation
./gradlew bootRun
# In another terminal:
curl http://localhost:8080/tags
curl http://localhost:8080/articles

# GraphQL validation
# Navigate to http://localhost:8080/graphiql
# Execute: query { tags }
```

#### CI/CD Validation

**GitHub Actions**:
1. Push migration branch to GitHub
2. Verify GitHub Actions workflow runs with JDK 23
3. Check that all tests pass in CI
4. Review action logs for warnings

**Jenkins**:
1. Ensure Jenkins administrator has configured JDK-23 tool
2. Trigger Jenkins build manually
3. Verify build succeeds with JDK 23
4. Check SonarCloud analysis completes (if configured)

#### Functional Validation

Test critical user flows:

1. **User Registration**:
   ```bash
   curl -X POST http://localhost:8080/users \
     -H "Content-Type: application/json" \
     -d '{"user":{"username":"test","email":"test@test.com","password":"password"}}'
   ```

2. **User Login**:
   ```bash
   curl -X POST http://localhost:8080/users/login \
     -H "Content-Type: application/json" \
     -d '{"user":{"email":"test@test.com","password":"password"}}'
   ```

3. **JWT Authentication**:
   - Verify token is returned
   - Use token to access protected endpoints

4. **Article CRUD**:
   - Create, read, update, delete articles
   - Test with authenticated and unauthenticated requests

5. **GraphQL Operations**:
   - Test user mutations (createUser, login, updateUser)
   - Test article queries and mutations
   - Verify error handling

### Performance Validation

Compare performance metrics before and after migration:

```bash
# Startup time
time ./gradlew bootRun

# Test execution time
time ./gradlew test

# Build time
time ./gradlew clean build
```

Expected: Minimal performance impact (±5-10% variance acceptable).

---

## Risk Assessment

### High-Risk Areas

#### 1. Spring Security 6.x Breaking Changes

**Risk Level**: HIGH

**Description**: Spring Security 6.x removes `WebSecurityConfigurerAdapter` and changes authorization API.

**Impact**:
- JWT authentication filter may fail
- Endpoint authorization rules may not work correctly
- CORS configuration may be affected

**Mitigation**:
1. Thoroughly test all authentication flows
2. Verify JWT token generation and validation
3. Test CORS with frontend clients
4. Review Spring Security 6.x migration guide
5. Keep old implementation commented for reference during transition

**Affected Files**:
- `src/main/java/io/spring/api/security/WebSecurityConfig.java`
- `src/main/java/io/spring/api/security/JwtTokenFilter.java`

**Test Coverage**:
- `src/test/java/io/spring/api/UsersApiTest.java` (login/registration)
- `src/test/java/io/spring/api/CurrentUserApiTest.java` (authenticated endpoints)
- All API tests using `TestWithCurrentUser` base class

#### 2. JJWT 0.12.x API Changes

**Risk Level**: MEDIUM-HIGH

**Description**: JJWT 0.12.x changes key handling APIs and signature algorithms.

**Impact**:
- Existing JWT tokens may become invalid
- Key generation/parsing may fail
- Token validation may throw new exceptions

**Mitigation**:
1. Review JJWT 0.12.x changelog for breaking changes
2. Test token generation with new API
3. Verify backward compatibility with existing tokens (if needed)
4. Update secret key handling if required

**Affected Files**:
- `src/main/java/io/spring/infrastructure/service/DefaultJwtService.java`

**Test Coverage**:
- JWT-related unit tests
- Integration tests for login/registration

#### 3. MyBatis 3.0.x Compatibility

**Risk Level**: MEDIUM

**Description**: MyBatis Spring Boot Starter 3.0.x is designed for Spring Boot 3.x.

**Impact**:
- SQL mapper configurations may need updates
- Type handlers may require changes
- Transaction management may behave differently

**Mitigation**:
1. Review MyBatis 3.0 release notes
2. Test all repository operations thoroughly
3. Verify transaction boundaries in service layer
4. Check mapper XML files for deprecated syntax

**Affected Files**:
- All files in `src/main/java/io/spring/infrastructure/mybatis/`
- All files in `src/main/java/io/spring/infrastructure/repository/`
- Mapper XML files in `src/main/resources/mapper/`

**Test Coverage**:
- `src/test/java/io/spring/infrastructure/MyBatisUserRepositoryTest.java`
- `src/test/java/io/spring/infrastructure/ArticleRepositoryTransactionTest.java`
- All repository integration tests

#### 4. Netflix DGS 8.x Breaking Changes

**Risk Level**: MEDIUM

**Description**: DGS 8.x introduces API changes for error handling and data fetching.

**Impact**:
- GraphQL error responses may change format
- Data fetcher APIs may be incompatible
- Schema generation may differ

**Mitigation**:
1. Review DGS 8.x migration guide
2. Regenerate GraphQL types with new codegen version
3. Test all GraphQL mutations and queries
4. Verify error handling matches expectations

**Affected Files**:
- `src/main/java/io/spring/graphql/UserMutation.java`
- `src/main/java/io/spring/graphql/exception/GraphQLCustomizeExceptionHandler.java`
- All DGS data fetchers

**Test Coverage**:
- GraphQL integration tests (if present)
- Manual testing via GraphiQL

#### 5. Package Migration (javax.* → jakarta.*)

**Risk Level**: MEDIUM

**Description**: Missing imports or incomplete migration can cause compilation failures.

**Impact**:
- Compilation errors
- Runtime ClassNotFoundException
- Validation annotations may not work

**Mitigation**:
1. Use automated tools for package replacement
2. Perform comprehensive grep search before and after
3. Compile frequently during migration
4. Run all tests after migration

**Verification**:
```bash
# Should return no results:
grep -r "import javax.validation" src/main/java/
grep -r "import javax.servlet" src/main/java/
```

#### 6. Gradle 8.x Compatibility

**Risk Level**: LOW-MEDIUM

**Description**: Gradle 8.x has breaking changes in plugin APIs and build script syntax.

**Impact**:
- Build may fail with deprecated APIs
- Plugin configurations may need updates
- Checkstyle plugin behavior may change

**Mitigation**:
1. Test build immediately after Gradle upgrade
2. Review Gradle 8.x release notes for breaking changes
3. Update plugin configurations as needed

**Affected Files**:
- `build.gradle`

### Medium-Risk Areas

#### 7. Test Framework Updates

**Risk Level**: MEDIUM

**Description**: REST Assured 5.x may have API changes affecting tests.

**Impact**:
- Test compilation errors
- Different DSL syntax required
- Matcher changes

**Mitigation**:
- Review REST Assured 5.x changelog
- Update test code incrementally
- Maintain test coverage during updates

#### 8. Flyway Database Migrations

**Risk Level**: LOW-MEDIUM

**Description**: Spring Boot 3.x uses newer Flyway version.

**Impact**:
- Migration scripts may need updates
- Checksum validation may fail

**Mitigation**:
- Test database migrations on clean database
- Verify migration history table compatibility
- Test rollback migrations if implemented

**Note**: Using SQLite (`dev.db`) simplifies this risk since database can be easily recreated.

### Low-Risk Areas

#### 9. Lombok Compatibility

**Risk Level**: LOW

**Description**: Lombok 1.18.30 supports Java 23.

**Impact**: Minimal, already verified compatible.

**Mitigation**: Monitor for annotation processing warnings.

#### 10. Checkstyle Rules

**Risk Level**: LOW

**Description**: Checkstyle 10.12.5 adds Java 23 support.

**Impact**: May detect new code style issues in Java 23 syntax.

**Mitigation**: Review and fix any new violations.

### Environmental Risks

#### 11. CI/CD Environment Setup

**Risk Level**: MEDIUM

**Description**: CI/CD environments need JDK 23 configuration.

**Impact**:
- GitHub Actions may fail if JDK 23 not available with Zulu distribution
- Jenkins requires manual JDK-23 tool configuration

**Mitigation**:
1. **GitHub Actions**: Verify Zulu distribution supports Java 23
   - Fallback: Use 'temurin' or 'oracle' distribution
2. **Jenkins**: Coordinate with Jenkins administrator
   - Install JDK 23 on Jenkins server
   - Configure JDK-23 tool in Global Tool Configuration
   - Test configuration before merging migration

#### 12. SQLite Driver Compatibility

**Risk Level**: LOW

**Description**: SQLite JDBC driver (3.36.0.3) may need update for Java 23.

**Impact**: Database operations may fail at runtime.

**Mitigation**:
- Test database operations early in migration
- Update driver if needed: `org.xerial:sqlite-jdbc:3.44.1.0`

---

## Appendix

### A. Dependency Version Matrix

| Dependency | Current Version | Target Version | Notes |
|------------|----------------|----------------|-------|
| Java | 17 | 23 | Minimum for Spring Boot 3.2 is Java 17 |
| Gradle | 7.4 | 8.5 | Required for Java 23 support |
| Spring Boot | 2.6.3 | 3.2.0 | Major version upgrade |
| Spring Dependency Management | 1.0.11.RELEASE | 1.1.4 | Updated for Spring Boot 3.x |
| MyBatis Spring Boot | 2.2.2 | 3.0.3 | Spring Boot 3.x compatible |
| Netflix DGS | 4.9.21 | 8.1.1 | Latest stable for Spring Boot 3.x |
| DGS Codegen Plugin | 5.0.6 | 6.2.1 | Updated for DGS 8.x |
| JJWT | 0.11.2 | 0.12.5 | Latest stable with API changes |
| Checkstyle | 10.12.4 | 10.12.5+ | Java 23 support |
| Lombok | 1.18.30 | 1.18.30 | No change needed |
| REST Assured | 4.5.1 | 5.4.0 | Spring Boot 3.x compatible |
| SQLite JDBC | 3.36.0.3 | 3.36.0.3 (or 3.44.1.0) | Test compatibility |

### B. Useful Commands Reference

```bash
# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Run specific test class
./gradlew test --tests io.spring.api.UsersApiTest

# Run application
./gradlew bootRun

# Generate GraphQL code
./gradlew generateJava

# Run checkstyle
./gradlew checkstyleMain checkstyleTest

# View dependencies
./gradlew dependencies

# Update Gradle wrapper
./gradlew wrapper --gradle-version 8.5

# Build Docker image
./gradlew bootBuildImage --imageName spring-boot-blogging-app
```

### C. Reference Documentation

- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Spring Security 6.0 Migration Guide](https://docs.spring.io/spring-security/reference/6.0/migration/index.html)
- [Jakarta EE 9 Specification](https://jakarta.ee/specifications/platform/9/)
- [Gradle 8.5 Release Notes](https://docs.gradle.org/8.5/release-notes.html)
- [MyBatis Spring Boot 3.0 Documentation](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
- [Netflix DGS Framework Documentation](https://netflix.github.io/dgs/)
- [JJWT 0.12.x Documentation](https://github.com/jwtk/jjwt#install)

### D. Troubleshooting Guide

#### Issue: "UnsupportedClassVersionError"

**Cause**: Running with wrong JDK version.

**Solution**:
```bash
java -version  # Verify JDK 23 is active
export JAVA_HOME=/path/to/jdk-23
```

#### Issue: "javax.* class not found"

**Cause**: Incomplete package migration.

**Solution**:
```bash
# Find remaining javax.* imports
grep -r "import javax.validation" src/
grep -r "import javax.servlet" src/
# Replace with jakarta.* equivalents
```

#### Issue: "WebSecurityConfigurerAdapter is deprecated"

**Cause**: Using old Spring Security API.

**Solution**: Follow Phase 6 migration steps to refactor to `SecurityFilterChain`.

#### Issue: Tests fail with JWT validation errors

**Cause**: JJWT 0.12.x API changes.

**Solution**:
1. Review `DefaultJwtService.java` implementation
2. Check key handling API changes
3. Verify signature algorithm configuration

#### Issue: GraphQL schema generation fails

**Cause**: DGS codegen plugin incompatibility.

**Solution**:
```bash
# Clean generated code
rm -rf build/generated/sources/dgs-codegen/
# Regenerate
./gradlew clean generateJava
```

#### Issue: MyBatis mapper errors

**Cause**: MyBatis 3.0 configuration differences.

**Solution**:
1. Review `application.properties` MyBatis configuration
2. Check mapper XML syntax
3. Verify type handler registrations

### E. Success Metrics

Migration is considered successful when:

- [ ] All 68 tests pass on Java 23
- [ ] Application starts successfully with Java 23
- [ ] REST API responds correctly (tested via curl)
- [ ] GraphQL API responds correctly (tested via GraphiQL)
- [ ] JWT authentication works for all flows
- [ ] No compilation warnings related to deprecated APIs
- [ ] Checkstyle reports no new violations
- [ ] GitHub Actions workflow passes with JDK 23
- [ ] Jenkins build passes with JDK-23
- [ ] Database operations work correctly
- [ ] GraphQL schema generates without errors
- [ ] No javax.* imports remain in codebase
- [ ] Code coverage remains at current level (per SonarCloud)
- [ ] Application performance within 10% of baseline

### F. Timeline Estimate

| Phase | Estimated Duration | Dependencies |
|-------|-------------------|--------------|
| Pre-Migration Setup | 2 hours | None |
| Phase 1: Gradle Upgrade | 1 hour | Pre-Migration |
| Phase 2: Java Version Update | 1 hour | Phase 1 |
| Phase 3: Spring Boot Upgrade | 2 hours | Phase 2 |
| Phase 4: Dependency Updates | 3 hours | Phase 3 |
| Phase 5: Package Migration | 2 hours | Phase 4 |
| Phase 6: Spring Security Migration | 4 hours | Phase 5 |
| Phase 7: Code Quality Updates | 1 hour | Phase 6 |
| Phase 8: Testing & Validation | 4 hours | Phase 7 |
| **Total Estimated Time** | **20 hours** | - |

**Notes**:
- Timeline assumes no major blockers
- Additional time needed for issue resolution
- Code review and approval time not included
- Recommend spreading over 2-3 days for proper testing

---

## Conclusion

This migration plan provides a comprehensive, step-by-step approach to upgrading the Spring Boot blogging application from Java 17 to Java 23. The plan addresses all critical areas including framework upgrades, package migrations, dependency updates, and CI/CD configuration changes.

**Key Success Factors**:
1. Follow phases sequentially
2. Validate after each phase
3. Test thoroughly before merging
4. Maintain clear rollback points
5. Document any deviations from this plan

**Next Steps**:
1. Review this plan with the team
2. Schedule migration window
3. Prepare development and CI/CD environments
4. Execute migration following this plan
5. Document lessons learned for future reference

For questions or issues during migration, refer to:
- Reference Documentation (Appendix C)
- Troubleshooting Guide (Appendix D)
- Spring Boot/Spring Security official migration guides
