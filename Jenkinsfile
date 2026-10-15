pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
        ansiColor('xterm')
    }

    environment {
        DOCKER_IMAGE = 'grab-store'
        APP_CONTAINER_NAME = 'grab-store-dev'
        APP_PORT = '8080'
        ENV_FILE = 'docker/env/dev.env'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code from repository...'
                checkout scm
            }
        }

        stage('Compile & Test') {
            steps {
                echo 'Running unit and integration tests with Maven wrapper...'
                sh '''
                    chmod +x ./mvnw
                    ./mvnw clean test -B -ntp
                '''
            }
        }

        stage('Docker Build Image') {
            steps {
                echo "Building Docker image ${DOCKER_IMAGE}:${BUILD_NUMBER} and tagging latest..."
                sh '''
                    docker build \
                        -t ${DOCKER_IMAGE}:${BUILD_NUMBER} \
                        -t ${DOCKER_IMAGE}:latest \
                        .
                '''
            }
        }

        stage('Deploy to Local Docker Containers') {
            steps {
                echo 'Deploying application to local Docker stack via Docker Compose...'
                sh '''
                    # Ensure supporting infrastructure (PostgreSQL, SeaweedFS) is running
                    docker compose --env-file ${ENV_FILE} up -d postgres seaweedfs seaweedfs-init

                    # Recreate the application container using the newly built image
                    docker compose --env-file ${ENV_FILE} up -d --no-deps --force-recreate app
                '''
            }
        }

        stage('Health & Sanity Check') {
            steps {
                echo 'Verifying application container health...'
                sh '''
                    MAX_RETRIES=24
                    COUNT=0
                    STATUS=""
                    
                    echo "Waiting for container ${APP_CONTAINER_NAME} to become healthy..."
                    until [ "$STATUS" = '"healthy"' ] || [ $COUNT -ge $MAX_RETRIES ]; do
                        STATUS=$(docker inspect --format="{{json .State.Health.Status}}" ${APP_CONTAINER_NAME} 2>/dev/null || echo '"unknown"')
                        echo "Status: $STATUS (Check $((COUNT+1))/$MAX_RETRIES)"
                        
                        if [ "$STATUS" = '"healthy"' ]; then
                            break
                        fi
                        
                        sleep 5
                        COUNT=$((COUNT+1))
                    done

                    if [ "$STATUS" != '"healthy"' ]; then
                        echo "Health check failed: application container did not report healthy status in time."
                        echo "=== Container Logs (last 100 lines) ==="
                        docker logs --tail 100 ${APP_CONTAINER_NAME}
                        exit 1
                    fi

                    echo "Application deployed and verified healthy on port ${APP_PORT}!"
                '''
            }
        }
    }

    post {
        always {
            echo 'Pipeline execution completed.'
        }
        success {
            echo "Successfully built and deployed Grab Store build #${BUILD_NUMBER}."
            sh 'docker image prune -f --filter "label=application=grab-store" || true'
        }
        failure {
            echo "Pipeline run failed. Inspecting recent container logs:"
            sh "docker logs --tail 100 ${APP_CONTAINER_NAME} || true"
        }
    }
}
