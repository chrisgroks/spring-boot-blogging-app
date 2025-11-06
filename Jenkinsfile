pipeline {
    agent any
    
    tools {
        // Configure JDK 23 (matches build.gradle sourceCompatibility)
        jdk 'JDK-23'
    }
    
    triggers {
        // Trigger on all branch pushes and PRs (matches GHA on.push and on.pull_request)
        pollSCM('H/5 * * * *')
    }
    
    options {
        // Build timeout to prevent hanging builds
        timeout(time: 30, unit: 'MINUTES')
        // Keep last 10 builds
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Timestamps in console output
        timestamps()
    }
    
    stages {
        stage('Checkout') {
            steps {
                // Checkout code (equivalent to actions/checkout@v2)
                checkout scm
            }
        }
        
        stage('Setup Gradle Cache') {
            steps {
                // Jenkins caching approach - uses workspace caching
                // This is a key difference from GHA's cache action
                script {
                    echo "Gradle cache location: ${env.HOME}/.gradle"
                    // Ensure gradle wrapper is executable
                    sh 'chmod +x gradlew'
                }
            }
        }
        
        stage('Test with Gradle') {
            steps {
                // Matches GHA: ./gradlew clean test
                sh './gradlew clean test'
            }
            post {
                always {
                    // Publish JUnit test results (critical for comparison)
                    junit '**/build/test-results/test/*.xml'
                    
                    // Archive test reports
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'build/reports/tests/test',
                        reportFiles: 'index.html',
                        reportName: 'Test Report'
                    ])
                }
                success {
                    echo 'Tests passed successfully!'
                }
                failure {
                    echo 'Tests failed!'
                }
            }
        }
        
        stage('Checkstyle Analysis') {
            steps {
                // BONUS: Run checkstyle (configured in build.gradle but not in GHA)
                // This could be a "catch" moment in the demo
                sh './gradlew checkstyleMain checkstyleTest'
            }
            post {
                always {
                    // Publish checkstyle results
                    recordIssues(
                        enabledForFailure: true,
                        tool: checkStyle(pattern: '**/build/reports/checkstyle/*.xml')
                    )
                }
            }
        }
        
        stage('SonarCloud Analysis') {
            when {
                // Only run on PRs and main branch
                anyOf {
                    branch 'main'
                    branch 'master'
                    changeRequest()
                }
            }
            environment {
                // Uses SONAR_TOKEN from environment (.env file or Jenkins credentials)
                // In Jenkins: Set as environment variable or use credentials('sonar-token')
                // Locally: Loaded from .env file
                SONAR_SCANNER_VERSION = '5.0.1.3006'
            }
            steps {
                // Download and run SonarScanner CLI
                sh '''
                    # Download SonarScanner if not already present
                    if [ ! -d "sonar-scanner-${SONAR_SCANNER_VERSION}-linux" ]; then
                        wget -q https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-${SONAR_SCANNER_VERSION}-linux.zip
                        unzip -q sonar-scanner-cli-${SONAR_SCANNER_VERSION}-linux.zip
                    fi
                    
                    # Run SonarScanner using SONAR_TOKEN from environment
                    ./sonar-scanner-${SONAR_SCANNER_VERSION}-linux/bin/sonar-scanner \
                      -Dsonar.projectKey=chrisgroks_spring-boot-blogging-app \
                      -Dsonar.organization=chrisgroks \
                      -Dsonar.host.url=https://sonarcloud.io \
                      -Dsonar.token=${SONAR_TOKEN}
                '''
            }
        }
        
        stage('Archive Artifacts') {
            steps {
                // Archive build artifacts for comparison
                archiveArtifacts artifacts: '**/build/libs/*.jar', 
                                 fingerprint: true,
                                 allowEmptyArchive: true
            }
        }
    }
    
    post {
        always {
            // Cleanup workspace to save disk space (optional)
            cleanWs(
                deleteDirs: true,
                patterns: [
                    [pattern: 'build/', type: 'INCLUDE'],
                    [pattern: '.gradle/', type: 'INCLUDE']
                ]
            )
        }
        success {
            echo "Pipeline completed successfully! Build: ${env.BUILD_NUMBER}"
        }
        failure {
            echo "Pipeline failed! Build: ${env.BUILD_NUMBER}"
        }
    }
}
