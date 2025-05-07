pipeline {
    agent {
        kubernetes {
            yaml '''
                apiVersion: v1
                kind: Pod
                spec:
                  containers:
                  - name: maven
                    image: maven:3.8.4-openjdk-17
                    command:
                    - cat
                    tty: true
                    volumeMounts:
                    - mountPath: /root/.m2
                      name: maven-repo
                  - name: kaniko
                    image: gcr.io/kaniko-project/executor:debug
                    command:
                    - cat
                    tty: true
                    volumeMounts:
                    - mountPath: /kaniko/.docker
                      name: kaniko-secret
                    - mountPath: /workspace
                      name: workspace
                  volumes:
                  - name: maven-repo
                    emptyDir: {}
                  - name: kaniko-secret
                    secret:
                      secretName: kaniko-secret
                  - name: workspace
                    emptyDir: {}
            '''
        }
    }
    environment {
        DOCKER_IMAGE = 'papakao/ty-multiverse-backend'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        SERVER_PORT = "8080"
        LOGGING_LEVEL = "INFO"
        LOGGING_LEVEL_SPRINGFRAMEWORK = "INFO"
    }
    stages {
        stage('Clone repository') {
            steps {
                container('maven') {
                    git url: 'https://github.com/Vinskao/TY-Multiverse-Backend.git', branch: 'main'
                }
            }
        }

        stage('Setup Environment') {
            steps {
                container('maven') {
                    withCredentials([
                        string(credentialsId: 'TYB_SPRING_DATASOURCE_URL', variable: 'SPRING_DATASOURCE_URL'),
                        string(credentialsId: 'TYB_SPRING_DATASOURCE_USERNAME', variable: 'SPRING_DATASOURCE_USERNAME'),
                        string(credentialsId: 'TYB_SPRING_DATASOURCE_PASSWORD', variable: 'SPRING_DATASOURCE_PASSWORD'),
                        string(credentialsId: 'TYB_URL_ADDRESS', variable: 'URL_ADDRESS'),
                        string(credentialsId: 'TYB_URL_FRONTEND', variable: 'URL_FRONTEND'),
                        string(credentialsId: 'TYB_KEYCLOAK_AUTH_SERVER_URL', variable: 'KEYCLOAK_AUTH_SERVER_URL'),
                        string(credentialsId: 'TYB_KEYCLOAK_REALM', variable: 'KEYCLOAK_REALM'),
                        string(credentialsId: 'TYB_KEYCLOAK_CLIENT_ID', variable: 'KEYCLOAK_CLIENT_ID'),
                        string(credentialsId: 'TYB_KEYCLOAK_CREDENTIALS_SECRET', variable: 'KEYCLOAK_CREDENTIALS_SECRET'),
                        string(credentialsId: 'TYB_PROJECT_ENV', variable: 'PROJECT_ENV')
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
                        '''
                    }
                }
            }
        }

        stage('Build') {
            steps {
                container('maven') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                container('kaniko') {
                    sh '''
                        cp -r . /workspace/
                        /kaniko/executor \
                            --context=/workspace \
                            --dockerfile=/workspace/Dockerfile \
                            --destination=${DOCKER_IMAGE}:${DOCKER_TAG} \
                            --destination=${DOCKER_IMAGE}:latest \
                            --cache=true \
                            --verbosity=info \
                            --insecure \
                            --skip-tls-verify
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withKubeConfig([credentialsId: 'kubeconfig-secret']) {
                    sh '''
                        kubectl set image deployment/ty-multiverse-backend ty-multiverse-backend=${DOCKER_IMAGE}:${DOCKER_TAG} -n default
                        kubectl rollout restart deployment ty-multiverse-backend
                    '''
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
