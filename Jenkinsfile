pipeline {
    agent {
        kubernetes {
            yaml '''
                apiVersion: v1
                kind: Pod
                spec:
                  serviceAccountName: jenkins-admin
                  containers:
                  - name: maven
                    image: maven:3.8.4-openjdk-17
                    command: ["cat"]
                    tty: true
                    volumeMounts:
                    - mountPath: /root/.m2
                      name: maven-repo
                    - mountPath: /home/jenkins/agent
                      name: workspace-volume
                    workingDir: /home/jenkins/agent
                  - name: docker
                    image: docker:23-dind
                    privileged: true
                    securityContext:
                      privileged: true
                    env:
                    - name: DOCKER_HOST
                      value: tcp://localhost:2375
                    - name: DOCKER_TLS_CERTDIR
                      value: ""
                    - name: DOCKER_BUILDKIT
                      value: "1"
                    volumeMounts:
                    - mountPath: /home/jenkins/agent
                      name: workspace-volume
                  volumes:
                  - name: maven-repo
                    emptyDir: {}
                  - name: workspace-volume
                    emptyDir: {}
            '''
            defaultContainer 'maven'
            inheritFrom 'default'
        }
    }
    options {
        timestamps()
        disableConcurrentBuilds()
    }
    environment {
        DOCKER_IMAGE = 'papakao/ty-multiverse-backend'
        DOCKER_TAG = "${BUILD_NUMBER}"
        SERVER_PORT = "8080"
        LOGGING_LEVEL = "INFO"
        LOGGING_LEVEL_SPRINGFRAMEWORK = "INFO"
    }
    stages {
        stage('Clone and Setup') {
            steps {
                script {
                    container('maven') {
                        sh '''
                            # 確認 Dockerfile 存在
                            ls -la
                            if [ ! -f "Dockerfile" ]; then
                                echo "Error: Dockerfile not found!"
                                exit 1
                            fi
                            # 創建配置目錄
                            mkdir -p src/main/resources/env
                        '''
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
                                cat > src/main/resources/env/platform.properties <<EOL
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
                            '''
                        }
                    }
                }
            }
        }

        stage('Build') {
            steps {
                container('maven') {
                    sh 'mvn clean package -P platform -DskipTests'
                }
            }
        }

        stage('Test') {
            steps {
                container('maven') {
                    sh 'mvn test -P platform'
                }
            }
        }

        stage('Build Docker Image with BuildKit') {
            steps {
                container('docker') {
                    script {
                        withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                            sh '''
                                cd /home/jenkins/agent
                                echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin
                                # 確認 Dockerfile 存在
                                ls -la
                                if [ ! -f "Dockerfile" ]; then
                                    echo "Error: Dockerfile not found!"
                                    exit 1
                                fi
                                # 構建 Docker 鏡像
                                docker build \
                                    --build-arg BUILDKIT_INLINE_CACHE=1 \
                                    --cache-from ${DOCKER_IMAGE}:latest \
                                    -t ${DOCKER_IMAGE}:${DOCKER_TAG} \
                                    -t ${DOCKER_IMAGE}:latest \
                                    .
                                docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                                docker push ${DOCKER_IMAGE}:latest
                            '''
                        }
                    }
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