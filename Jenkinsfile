pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: build
    image: papakao/maven-docker-agent:latest
    command:
    - cat
    tty: true
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
      type: Socket
"""
        }
    }

    environment {
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_IMAGE = 'papakao/ty-multiverse-backend'
        SERVER_PORT = "8080"
        LOGGING_LEVEL = "INFO"
        LOGGING_LEVEL_SPRINGFRAMEWORK = "INFO"
    }

    stages {
        stage('Clone repository') {
            steps {
                container('build') {
                    git url: 'https://github.com/Vinskao/TY-Multiverse-Backend.git', branch: 'main'
                }
            }
        }

        stage('Check Git Version') {
            steps {
                container('build') {
                    sh 'git log -1'
                    sh 'ls -R src/main/resources/env'
                }
            }
        }

        stage('Setup Environment') {
            steps {
                container('build') {
                    withCredentials([
                        string(credentialsId: 'SPRING_DATASOURCE_URL', variable: 'SPRING_DATASOURCE_URL'),
                        string(credentialsId: 'SPRING_DATASOURCE_USERNAME', variable: 'SPRING_DATASOURCE_USERNAME'),
                        string(credentialsId: 'SPRING_DATASOURCE_PASSWORD', variable: 'SPRING_DATASOURCE_PASSWORD'),
                        string(credentialsId: 'URL_ADDRESS', variable: 'URL_ADDRESS'),
                        string(credentialsId: 'URL_FRONTEND', variable: 'URL_FRONTEND'),
                        string(credentialsId: 'KEYCLOAK_AUTH_SERVER_URL', variable: 'KEYCLOAK_AUTH_SERVER_URL'),
                        string(credentialsId: 'KEYCLOAK_REALM', variable: 'KEYCLOAK_REALM'),
                        string(credentialsId: 'KEYCLOAK_CLIENT_ID', variable: 'KEYCLOAK_CLIENT_ID'),
                        string(credentialsId: 'KEYCLOAK_CREDENTIALS_SECRET', variable: 'KEYCLOAK_CREDENTIALS_SECRET'),
                        string(credentialsId: 'PROJECT_ENV', variable: 'PROJECT_ENV')
                    ]) {
                        sh '''
                            mkdir -p src/main/resources/env
                            cat > src/main/resources/env/platform.properties << 'EOL'
                            env=platform
                            spring.profiles.active=platform
                            spring.datasource.url=${SPRING_DATASOURCE_URL}
                            spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
                            spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
                            server.port=${SERVER_PORT}
                            logging.level.root=${LOGGING_LEVEL}
                            logging.level.org.springframework=${LOGGING_LEVEL_SPRINGFRAMEWORK}
                            url.address=${URL_ADDRESS}
                            url.frontend=${URL_FRONTEND}
                            keycloak.auth-server-url=${KEYCLOAK_AUTH_SERVER_URL}
                            keycloak.realm=${KEYCLOAK_REALM}
                            keycloak.clientId=${KEYCLOAK_CLIENT_ID}
                            keycloak.credentials.secret=${KEYCLOAK_CREDENTIALS_SECRET}
                            project.env=${PROJECT_ENV}
                            EOL
                            chmod 644 src/main/resources/env/platform.properties
                            ls -la src/main/resources/env/
                            cat src/main/resources/env/platform.properties
                        '''
                    }
                }
            }
        }

        stage('Debug Env') {
            steps {
                container('build') {
                    withCredentials([
                        string(credentialsId: 'SPRING_DATASOURCE_URL', variable: 'SPRING_DATASOURCE_URL'),
                        string(credentialsId: 'SPRING_DATASOURCE_USERNAME', variable: 'SPRING_DATASOURCE_USERNAME'),
                        string(credentialsId: 'SPRING_DATASOURCE_PASSWORD', variable: 'SPRING_DATASOURCE_PASSWORD'),
                        string(credentialsId: 'URL_ADDRESS', variable: 'URL_ADDRESS'),
                        string(credentialsId: 'URL_FRONTEND', variable: 'URL_FRONTEND'),
                        string(credentialsId: 'KEYCLOAK_AUTH_SERVER_URL', variable: 'KEYCLOAK_AUTH_SERVER_URL'),
                        string(credentialsId: 'KEYCLOAK_REALM', variable: 'KEYCLOAK_REALM'),
                        string(credentialsId: 'KEYCLOAK_CLIENT_ID', variable: 'KEYCLOAK_CLIENT_ID'),
                        string(credentialsId: 'KEYCLOAK_CREDENTIALS_SECRET', variable: 'KEYCLOAK_CREDENTIALS_SECRET'),
                        string(credentialsId: 'PROJECT_ENV', variable: 'PROJECT_ENV')
                    ]) {
                        sh '''
                            echo "Checking environment variables:"
                            echo "SPRING_DATASOURCE_URL: $SPRING_DATASOURCE_URL"
                            echo "SPRING_DATASOURCE_USERNAME: $SPRING_DATASOURCE_USERNAME"
                            echo "URL_ADDRESS: $URL_ADDRESS"
                            echo "URL_FRONTEND: $URL_FRONTEND"
                            echo "KEYCLOAK_AUTH_SERVER_URL: $KEYCLOAK_AUTH_SERVER_URL"
                            echo "KEYCLOAK_REALM: $KEYCLOAK_REALM"
                            echo "KEYCLOAK_CLIENT_ID: $KEYCLOAK_CLIENT_ID"
                            echo "PROJECT_ENV: $PROJECT_ENV"
                        '''
                    }
                }
            }
        }

        stage('Maven Build') {
            steps {
                container('build') {
                    sh './mvnw clean install -P platform -X'
                }
            }
        }

        stage('Build and Push Docker image') {
            steps {
                container('build') {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh '''
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                            docker buildx build --platform linux/arm64 -t $DOCKER_IMAGE:latest --push .
                        '''
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                container('build') {
                    withKubeConfig([credentialsId: 'kubeconfig-secret']) {
                        sh '''
                            kubectl apply -f k8s/deployment.yaml
                            kubectl rollout restart deployment ty-multiverse-backend
                        '''
                    }
                }
            }
        }
    }
}
