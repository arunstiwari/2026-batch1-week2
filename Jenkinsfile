// CI pipeline for week2 (Spring Boot 4.1.1 / Java 21 / Maven wrapper)
//
// Build -> Test -> Coverage -> Package -> SonarQube quality gate
//   -> OWASP Dependency-Check -> Docker image -> Trivy image scan -> container smoke test
//
// Required Jenkins credentials (Manage Jenkins > Credentials):
//   sonarqube-token   Secret text  - SonarQube user token with "Execute Analysis" on the project
//   nvd-api-key       Secret text  - NVD API key for Dependency-Check (https://nvd.nist.gov/developers/request-an-api-key)
//
// Agent requirements: JDK 21, a Docker daemon the agent can talk to, git.
// If the controller has a JDK tool configured, uncomment the `tools` block.

def mvn(String args) {
    if (isUnix()) {
        sh "./mvnw ${args}"
    } else {
        bat "mvnw.cmd ${args}"
    }
}

// Same as mvn() but hands back the exit code rather than failing the stage, so the
// caller can tell an infrastructure error apart from a genuine finding. Maven output
// still streams to the console.
def mvnStatus(String args) {
    if (isUnix()) {
        return sh(script: "./mvnw ${args}", returnStatus: true)
    }
    return bat(script: "mvnw.cmd ${args}", returnStatus: true)
}

pipeline {
    agent any

    // tools {
    //     jdk 'jdk-21'
    // }

    options {
        timestamps()
        timeout(time: 90, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '5'))
    }

    parameters {
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: 'Package without running the test suite (not recommended).'
        )
        booleanParam(
            name: 'RUN_COVERAGE',
            defaultValue: true,
            description: 'Generate a JaCoCo coverage report and feed it to SonarQube.'
        )
        booleanParam(
            name: 'RUN_SONAR',
            defaultValue: true,
            description: 'Analyse the project in SonarQube and break the build if the quality gate fails.'
        )
        booleanParam(
            name: 'RUN_DEPENDENCY_CHECK',
            defaultValue: true,
            description: 'Run OWASP Dependency-Check against the declared dependencies.'
        )
        string(
            name: 'DEPENDENCY_CHECK_FAIL_ON_CVSS',
            defaultValue: '7.0',
            description: 'Fail the build when a dependency has a CVSS score at or above this value. Use 11 to report only.'
        )
        booleanParam(
            name: 'PUBLISH_DEPENDENCY_CHECK_TRENDS',
            defaultValue: false,
            description: 'Publish Dependency-Check trend graphs. Requires the OWASP ' +
                         'Dependency-Check Jenkins plugin; reports are archived either way.'
        )
        booleanParam(
            name: 'RUN_IMAGE_SCAN',
            defaultValue: true,
            description: 'Scan the built container image with Trivy.'
        )
        string(
            name: 'IMAGE_SCAN_SEVERITIES',
            defaultValue: 'HIGH,CRITICAL',
            description: 'Trivy severities that break the build.'
        )
    }

    environment {
        // Quiet, reproducible Maven output plus a per-workspace local repository so
        // parallel jobs on one agent cannot corrupt each other's downloads.
        MVN_FLAGS = '--batch-mode --no-transfer-progress -Dmaven.repo.local=.m2/repository'
        MAVEN_OPTS = '-Xmx1g'

        // Pinned tool versions. JaCoCo, Sonar and Dependency-Check are not declared in
        // the pom, so their plugins are invoked directly by coordinate.
        JACOCO_VERSION           = '0.8.13'
        SONAR_PLUGIN_VERSION     = '5.8.0.7211'
        DEPENDENCY_CHECK_VERSION = '12.2.2'
        TRIVY_IMAGE              = 'aquasec/trivy:0.74.0'

        SONAR_HOST_URL      = 'http://sonar.fil.lab:9000'
        SONAR_PROJECT_KEY   = 'week2'
        SONAR_PROJECT_NAME  = 'week2'

        // Dependency-Check keeps a ~240MB local mirror of the NVD. It must live outside
        // the workspace so `cleanWs` cannot delete it, AND on a path that is actually
        // persisted. /var/lib/jenkins does not exist in the Jenkins container image, so
        // it lands on the container's overlay layer and vanishes whenever the container
        // is recreated; JENKINS_HOME is the bind-mounted volume that survives.
        DEPENDENCY_CHECK_DATA_DIR = '/var/jenkins_home/caches/dependency-check'
        // Optional. Missing or blank means the scan runs unauthenticated (slow).
        NVD_CREDENTIALS_ID        = 'nvd-api-key'
        DC_SUPPRESSION_FILE       = 'dependency-check-suppressions.xml'

        IMAGE_NAME = 'week2'
        // Set to e.g. 'registry.fil.lab/platform' to namespace the image for a future push.
        IMAGE_REGISTRY = ''
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw'
                    }
                    env.GIT_SHORT_SHA = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()
                    // Read the version straight from Maven so the pipeline does not
                    // depend on the Pipeline Utility Steps plugin for readMavenPom().
                    env.APP_VERSION = sh(
                        script: "./mvnw -q ${env.MVN_FLAGS} help:evaluate " +
                                "-Dexpression=project.version -DforceStdout",
                        returnStdout: true
                    ).trim().readLines().last().trim()
                    env.IMAGE_TAG = "${env.APP_VERSION}-${env.BUILD_NUMBER}-${env.GIT_SHORT_SHA}"
                    env.IMAGE_REF = env.IMAGE_REGISTRY
                        ? "${env.IMAGE_REGISTRY}/${env.IMAGE_NAME}:${env.IMAGE_TAG}"
                        : "${env.IMAGE_NAME}:${env.IMAGE_TAG}"
                }
                echo "Building ${env.BRANCH_NAME ?: 'local'} @ ${env.GIT_SHORT_SHA} -> ${env.IMAGE_REF}"
            }
        }

        stage('Tooling') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'java -version'
                        sh 'docker version --format "docker {{.Server.Version}}"'
                    } else {
                        bat 'java -version'
                    }
                    mvn "${env.MVN_FLAGS} --version"
                }
            }
        }

        stage('Compile') {
            steps {
                script {
                    mvn "${env.MVN_FLAGS} clean compile"
                }
            }
        }

        stage('Test') {
            when {
                expression { !params.SKIP_TESTS }
            }
            steps {
                script {
                    // JaCoCo is not declared in the pom, so the agent is attached and the
                    // report rendered by invoking the plugin directly in one reactor run.
                    if (params.RUN_COVERAGE) {
                        mvn "${env.MVN_FLAGS} " +
                            "org.jacoco:jacoco-maven-plugin:${env.JACOCO_VERSION}:prepare-agent " +
                            "test " +
                            "org.jacoco:jacoco-maven-plugin:${env.JACOCO_VERSION}:report"
                    } else {
                        mvn "${env.MVN_FLAGS} test"
                    }
                }
            }
            post {
                always {
                    junit(
                        testResults: 'target/surefire-reports/*.xml',
                        allowEmptyResults: true,
                        skipPublishingChecks: true
                    )
                }
            }
        }

        stage('Coverage Report') {
            when {
                allOf {
                    expression { !params.SKIP_TESTS }
                    expression { params.RUN_COVERAGE }
                }
            }
            steps {
                // Publishing coverage is informational: a missing plugin or report must
                // not turn a green build red.
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    recordCoverage(
                        tools: [[parser: 'JACOCO', pattern: 'target/site/jacoco/jacoco.xml']],
                        sourceCodeRetention: 'MODIFIED'
                    )
                }
                archiveArtifacts(
                    artifacts: 'target/site/jacoco/**',
                    allowEmptyArchive: true,
                    onlyIfSuccessful: false
                )
            }
        }

        stage('SonarQube Analysis') {
            when {
                expression { params.RUN_SONAR }
            }
            steps {
                // `sonar.qualitygate.wait` makes the scanner poll SonarQube for the gate
                // result and exit non-zero when it fails, so the build breaks here. This
                // needs no inbound webhook from SonarQube back to Jenkins.
                //
                // If a SonarQube server is configured under Manage Jenkins > System, you
                // can replace the withCredentials block with:
                //     withSonarQubeEnv('sonar.fil.lab') { ... }   // drops -Dsonar.host.url/-Dsonar.token
                withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                    script {
                        def coverageArg = params.RUN_COVERAGE && !params.SKIP_TESTS
                            ? '-Dsonar.coverage.jacocoReportPaths=target/site/jacoco/jacoco.xml'
                            : ''
                        mvn "${env.MVN_FLAGS} " +
                            "org.sonarsource.scanner.maven:sonar-maven-plugin:${env.SONAR_PLUGIN_VERSION}:sonar " +
                            "-Dsonar.host.url=${env.SONAR_HOST_URL} " +
                            "-Dsonar.token=\$SONAR_TOKEN " +
                            "-Dsonar.projectKey=${env.SONAR_PROJECT_KEY} " +
                            "-Dsonar.projectName=${env.SONAR_PROJECT_NAME} " +
                            "-Dsonar.projectVersion=${env.APP_VERSION} " +
                            "-Dsonar.scm.revision=${env.GIT_SHORT_SHA} " +
                            "-Dsonar.junit.reportPaths=target/surefire-reports " +
                            "${coverageArg} " +
                            "-Dsonar.qualitygate.wait=true " +
                            "-Dsonar.qualitygate.timeout=600"
                        // Branch/PR decoration is a Developer Edition feature. On Community
                        // Build, passing -Dsonar.branch.name makes the scanner fail, so add
                        // it only if this SonarQube is licensed for it:
                        //     -Dsonar.branch.name=${env.BRANCH_NAME}
                    }
                }
            }
            post {
                failure {
                    echo "SonarQube quality gate failed or analysis errored. " +
                         "Dashboard: ${env.SONAR_HOST_URL}/dashboard?id=${env.SONAR_PROJECT_KEY}"
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    // Tests already ran in their own stage; do not run them twice.
                    mvn "${env.MVN_FLAGS} package -DskipTests"
                }
            }
            post {
                success {
                    archiveArtifacts(
                        artifacts: 'target/*.jar',
                        excludes: 'target/*-sources.jar',
                        fingerprint: true,
                        onlyIfSuccessful: true
                    )
                }
            }
        }

        stage('Dependency Vulnerability Scan') {
            when {
                expression { params.RUN_DEPENDENCY_CHECK }
            }
            options {
                // Bounded separately from the pipeline timeout: an unauthenticated NVD
                // download can run for an hour, and it should not consume the budget the
                // later Docker and scan stages need.
                timeout(time: 40, unit: 'MINUTES')
            }
            steps {
                script {
                    if (isUnix()) {
                        sh "mkdir -p ${env.DEPENDENCY_CHECK_DATA_DIR}"
                    }

                    def baseArgs = "${env.MVN_FLAGS} " +
                        "org.owasp:dependency-check-maven:${env.DEPENDENCY_CHECK_VERSION}:check " +
                        "-DdataDirectory=${env.DEPENDENCY_CHECK_DATA_DIR} " +
                        "-DfailBuildOnCVSS=${params.DEPENDENCY_CHECK_FAIL_ON_CVSS} " +
                        "-Dformats=HTML,XML,JSON " +
                        "-DskipProvidedScope=true " +
                        "-DskipTestScope=true " +
                        // Documented, narrowly-scoped false positives. Each entry in the
                        // file records why it is not a real finding. Stale rules that stop
                        // matching are reported by the Unused Suppression Rule analyzer in
                        // the Maven output (failBuildOnUnusedSuppressionRule defaults to
                        // false, so they warn rather than break the build).
                        "-DsuppressionFiles=${env.DC_SUPPRESSION_FILE}"

                    // The NVD API key is optional. Probe for the credential instead of
                    // letting withCredentials abort the build when it is not configured:
                    // a missing key should slow the scan down, not break the pipeline.
                    def hasNvdKey = false
                    if (env.NVD_CREDENTIALS_ID) {
                        try {
                            withCredentials([string(credentialsId: env.NVD_CREDENTIALS_ID,
                                                    variable: 'NVD_API_KEY_PROBE')]) {
                                hasNvdKey = true
                            }
                        } catch (ignored) {
                            hasNvdKey = false
                        }
                    }

                    def rc
                    if (hasNvdKey) {
                        // Passed by environment variable name, not -D, so the key cannot
                        // leak via the process table or the build log.
                        withCredentials([string(credentialsId: env.NVD_CREDENTIALS_ID,
                                                variable: 'NVD_API_KEY')]) {
                            rc = mvnStatus("${baseArgs} -DnvdApiKeyEnvironmentVariable=NVD_API_KEY")
                        }
                    } else {
                        def wantedId = env.NVD_CREDENTIALS_ID ?: 'nvd-api-key'
                        echo "WARNING: no NVD API key credential found (looked for id " +
                             "'${wantedId}').\n" +
                             "Dependency-Check will fall back to unauthenticated NVD access, which " +
                             "is heavily rate limited: the first database build can take well over " +
                             "an hour and often fails outright.\n" +
                             "To fix, request a free key at " +
                             "https://nvd.nist.gov/developers/request-an-api-key and add it to " +
                             "Jenkins as a 'Secret text' credential with ID '${wantedId}'.\n" +
                             "To skip this stage instead, re-run with RUN_DEPENDENCY_CHECK unchecked."

                        // A longer inter-request delay is what keeps the unauthenticated NVD
                        // API from returning 403/429 partway through the download.
                        rc = mvnStatus("${baseArgs} -DnvdApiDelay=8000")
                    }

                    // Dependency-Check exits non-zero both when it finds something and when
                    // it could not run at all. The report is the discriminator: the plugin
                    // only writes one once analysis actually completed.
                    def reportWritten = fileExists('target/dependency-check-report.xml')
                    env.DC_REPORT_WRITTEN = reportWritten ? 'true' : 'false'

                    if (rc == 0) {
                        echo "Dependency-Check passed: nothing at or above CVSS " +
                             "${params.DEPENDENCY_CHECK_FAIL_ON_CVSS}."
                    } else if (reportWritten) {
                        error "Dependency-Check found dependencies at or above CVSS " +
                              "${params.DEPENDENCY_CHECK_FAIL_ON_CVSS}. " +
                              "See the archived dependency-check-report.html."
                    } else {
                        // No report means the scan never completed, which is an
                        // infrastructure problem and not a statement about this codebase.
                        // Flag it loudly but do not gate the build on it.
                        unstable "Dependency-Check did not complete (Maven exit ${rc}) and wrote " +
                                 "no report, so no vulnerability verdict was reached. Usual " +
                                 "causes: unauthenticated NVD download rate limited or timed " +
                                 "out, or ${env.DEPENDENCY_CHECK_DATA_DIR} not writable by the " +
                                 "Jenkins user. See the Maven output above for the decisive error."
                    }
                }
            }
            post {
                always {
                    archiveArtifacts(
                        artifacts: 'target/dependency-check-report.*',
                        allowEmptyArchive: true,
                        onlyIfSuccessful: false
                    )
                    // Optional trend graphs. Off by default because the step only exists
                    // when the OWASP Dependency-Check Jenkins plugin is installed, and an
                    // unknown DSL step raises NoSuchMethodError - an Error, not an
                    // Exception, so `catch (e)` will not hold it and catchError dumps a
                    // ~60-line CPS trace that buries the real failure. The reports are
                    // archived above regardless, so nothing is lost by leaving this off.
                    script {
                        if (!params.PUBLISH_DEPENDENCY_CHECK_TRENDS) {
                            echo 'Dependency-Check trend publishing disabled.'
                        } else if (!fileExists('target/dependency-check-report.xml')) {
                            echo 'No dependency-check report to publish.'
                        } else {
                            try {
                                dependencyCheckPublisher(
                                    pattern: 'target/dependency-check-report.xml',
                                    failedTotalCritical: 0,
                                    unstableTotalHigh: 0
                                )
                            } catch (Throwable t) {
                                echo "Skipped Dependency-Check trend publishing: ${t.message}"
                            }
                        }
                    }
                }
                failure {
                    echo "Dependency-Check gate failed on real findings " +
                         "(report written: ${env.DC_REPORT_WRITTEN}). " +
                         "Open the archived dependency-check-report.html, or re-run with a " +
                         "higher DEPENDENCY_CHECK_FAIL_ON_CVSS (11 reports without gating)."
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    if (!isUnix()) {
                        error 'Docker stages require a unix agent.'
                    }
                    // Note: `\\` so the shell receives a real line continuation. A single
                    // backslash would be consumed by Groovy's triple-quoted string.
                    sh """
                        docker build \\
                          --build-arg APP_VERSION=${env.APP_VERSION} \\
                          --build-arg GIT_COMMIT=${env.GIT_SHORT_SHA} \\
                          --build-arg BUILD_TIME=\$(date -u +%Y-%m-%dT%H:%M:%SZ) \\
                          -t ${env.IMAGE_REF} \\
                          -t ${env.IMAGE_NAME}:latest \\
                          .
                    """
                    sh "docker image inspect ${env.IMAGE_REF} --format 'image={{.Id}} size={{.Size}}'"
                }
            }
        }

        stage('Image Scan') {
            when {
                expression { params.RUN_IMAGE_SCAN }
            }
            steps {
                script {
                    // Trivy runs as a container against the agent's Docker socket. The
                    // named volume caches the vulnerability database between builds.
                    //
                    // Reports come back on stdout and are written to the workspace by
                    // Jenkins, NOT via `-v $WORKSPACE/target:/out`. Bind-mount paths are
                    // resolved by the Docker daemon, so when Jenkins itself runs in a
                    // container the workspace path does not exist on the host: the daemon
                    // silently creates an empty directory, Trivy writes into it, and the
                    // files never appear in the workspace. Trivy logs to stderr, so
                    // stdout is the report alone.
                    def trivy = "docker run --rm " +
                                "-v /var/run/docker.sock:/var/run/docker.sock " +
                                "-v trivy-cache:/root/.cache/ " +
                                "${env.TRIVY_IMAGE} image --scanners vuln --no-progress"

                    // Pass 1: full report of everything found, never breaks the build.
                    writeFile file: 'target/trivy-report.json', text: sh(
                        script: "${trivy} --format json --exit-code 0 ${env.IMAGE_REF}",
                        returnStdout: true
                    )
                    def table = sh(
                        script: "${trivy} --format table --exit-code 0 ${env.IMAGE_REF}",
                        returnStdout: true
                    )
                    writeFile file: 'target/trivy-report.txt', text: table
                    echo table

                    // Pass 2: the gate. Only fixable findings at the configured severities
                    // break the build, so unpatched upstream CVEs cannot wedge the pipeline.
                    sh "${trivy} --severity ${params.IMAGE_SCAN_SEVERITIES} --ignore-unfixed --exit-code 1 ${env.IMAGE_REF}"
                }
            }
            post {
                always {
                    archiveArtifacts(
                        artifacts: 'target/trivy-report.*',
                        allowEmptyArchive: true,
                        onlyIfSuccessful: false
                    )
                }
                failure {
                    echo "Trivy found fixable ${params.IMAGE_SCAN_SEVERITIES} vulnerabilities in " +
                         "${env.IMAGE_REF}. See the archived trivy-report.txt."
                }
            }
        }

        stage('Container Smoke Test') {
            steps {
                // Prove the image actually boots and serves traffic before it is promoted.
                //
                // Every probe here is made by the Docker daemon or from inside the
                // container, never from the Jenkins process. `docker run -P` publishes on
                // the *host*, so when Jenkins runs in a container `localhost:<port>` from
                // the pipeline reaches nothing - that is why the earlier curl returned 000
                // even though the application had started. The runtime image is Alpine
                // based, so busybox wget is available for an in-container probe.
                sh """
                    set -e
                    CONTAINER=week2-smoke-${env.BUILD_NUMBER}
                    PROBE_PATH=/customers

                    docker rm -f \$CONTAINER >/dev/null 2>&1 || true
                    docker run -d --name \$CONTAINER ${env.IMAGE_REF} >/dev/null
                    trap 'docker logs \$CONTAINER > target/container-smoke.log 2>&1 || true; docker rm -f \$CONTAINER >/dev/null 2>&1 || true' EXIT

                    for i in \$(seq 1 30); do
                        if docker logs \$CONTAINER 2>&1 | grep -q 'Started Week2Application'; then
                            echo 'Application started.'
                            break
                        fi
                        if [ "\$(docker inspect -f '{{.State.Running}}' \$CONTAINER)" != "true" ]; then
                            echo 'Container exited before starting:'
                            docker logs \$CONTAINER
                            exit 1
                        fi
                        sleep 2
                        if [ \$i -eq 30 ]; then
                            echo 'Application did not start within 60s:'
                            docker logs \$CONTAINER
                            exit 1
                        fi
                    done

                    # This project has no actuator dependency, so probe a real endpoint.
                    # busybox wget exits non-zero on any non-2xx response.
                    set +e
                    PROBE=\$(docker exec \$CONTAINER wget -q -S -O /dev/null "http://localhost:8080\$PROBE_PATH" 2>&1)
                    RC=\$?
                    set -e
                    echo "GET \$PROBE_PATH -> \$(echo "\$PROBE" | grep -m1 'HTTP/' | sed 's/^[[:space:]]*//')"
                    if [ \$RC -ne 0 ]; then
                        echo "Probe failed (wget exit \$RC). Response:"
                        echo "\$PROBE"
                        echo '--- container logs ---'
                        docker logs \$CONTAINER
                        exit 1
                    fi
                    echo 'Container smoke test passed.'
                """
            }
            post {
                always {
                    archiveArtifacts(
                        artifacts: 'target/container-smoke.log',
                        allowEmptyArchive: true,
                        onlyIfSuccessful: false
                    )
                }
            }
        }
    }

    post {
        success {
            echo "CI passed for ${env.GIT_SHORT_SHA} - image ${env.IMAGE_REF} (build #${env.BUILD_NUMBER})."
        }
        unstable {
            echo 'CI finished with test failures or an unstable stage.'
        }
        failure {
            echo "CI failed for ${env.GIT_SHORT_SHA} - see ${env.BUILD_URL}console"
        }
        always {
            script {
                // Do not leave build images behind on the agent; keep the :latest tag.
                if (isUnix()) {
                    sh "docker rm -f week2-smoke-${env.BUILD_NUMBER} >/dev/null 2>&1 || true"
                    sh "docker rmi ${env.IMAGE_REF} >/dev/null 2>&1 || true"
                }
            }
            // Keep the cached local Maven repository, discard everything else.
            cleanWs(
                deleteDirs: true,
                notFailBuild: true,
                patterns: [[pattern: '.m2/**', type: 'EXCLUDE']]
            )
        }
    }
}
