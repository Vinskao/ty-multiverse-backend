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
                  - name: kubectl
                    image: bitnami/kubectl:1.30.7
                    command: ["/bin/sh"]
                    args: ["-c", "while true; do sleep 30; done"]
                    alwaysPull: true
                    securityContext:
                      runAsUser: 0
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
                            string(credentialsId: 'SPRING_PEOPLE_DATASOURCE_URL', variable: 'SPRING_PEOPLE_DATASOURCE_URL'),
                            string(credentialsId: 'SPRING_PEOPLE_DATASOURCE_USERNAME', variable: 'SPRING_PEOPLE_DATASOURCE_USERNAME'),
                            string(credentialsId: 'SPRING_PEOPLE_DATASOURCE_PASSWORD', variable: 'SPRING_PEOPLE_DATASOURCE_PASSWORD'),
                            string(credentialsId: 'REDIS_HOST', variable: 'REDIS_HOST'),
                            string(credentialsId: 'REDIS_CUSTOM_PORT', variable: 'REDIS_CUSTOM_PORT'),
                            string(credentialsId: 'REDIS_PASSWORD', variable: 'REDIS_PASSWORD'),
                            string(credentialsId: 'REDIS_QUEUE_TYMB', variable: 'REDIS_QUEUE_TYMB'),
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
                                PROJECT_ENV=${PROJECT_ENV}
                                SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}
                                SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
                                SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
                                SPRING_PEOPLE_DATASOURCE_URL=${SPRING_PEOPLE_DATASOURCE_URL}
                                SPRING_PEOPLE_DATASOURCE_USERNAME=${SPRING_PEOPLE_DATASOURCE_USERNAME}
                                SPRING_PEOPLE_DATASOURCE_PASSWORD=${SPRING_PEOPLE_DATASOURCE_PASSWORD}
                                SERVER_PORT=${SERVER_PORT}
                                LOGGING_LEVEL=${LOGGING_LEVEL}
                                LOGGING_LEVEL_SPRINGFRAMEWORK=${LOGGING_LEVEL_SPRINGFRAMEWORK}
                                URL_ADDRESS=${URL_ADDRESS}
                                URL_FRONTEND=${URL_FRONTEND}
                                KEYCLOAK_AUTH_SERVER_URL=${KEYCLOAK_AUTH_SERVER_URL}
                                KEYCLOAK_REALM=${KEYCLOAK_REALM}
                                KEYCLOAK_CLIENT_ID=${KEYCLOAK_CLIENT_ID}
                                KEYCLOAK_CREDENTIALS_SECRET=${KEYCLOAK_CREDENTIALS_SECRET}
                                REDIS_HOST=${REDIS_HOST}
                                REDIS_CUSTOM_PORT=${REDIS_CUSTOM_PORT}
                                REDIS_PASSWORD=${REDIS_PASSWORD}
                                REDIS_QUEUE_TYMB=${REDIS_QUEUE_TYMB}
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
                                cd /home/jenkins/agent/workspace/TYB/ty-multiverse-backend-deploy
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

        stage('Debug Environment') {
            steps {
                container('kubectl') {
                    script {
                        echo "=== Listing all environment variables ==="
                        sh 'printenv | sort'
                        
                        echo "=== Checking Jenkins environment variables ==="
                        sh '''
                            echo "BUILD_NUMBER: ${BUILD_NUMBER}"
                            echo "BUILD_ID: ${BUILD_ID}"
                            echo "BUILD_URL: ${BUILD_URL}"
                            echo "JOB_NAME: ${JOB_NAME}"
                            echo "JOB_BASE_NAME: ${JOB_BASE_NAME}"
                            echo "WORKSPACE: ${WORKSPACE}"
                            echo "JENKINS_HOME: ${JENKINS_HOME}"
                            echo "JENKINS_URL: ${JENKINS_URL}"
                            echo "EXECUTOR_NUMBER: ${EXECUTOR_NUMBER}"
                            echo "NODE_NAME: ${NODE_NAME}"
                            echo "NODE_LABELS: ${NODE_LABELS}"
                            echo "JAVA_HOME: ${JAVA_HOME}"
                            echo "PATH: ${PATH}"
                            echo "SHELL: ${SHELL}"
                            echo "HOME: ${HOME}"
                            echo "USER: ${USER}"
                            echo "DOCKER_IMAGE: ${DOCKER_IMAGE}"
                            echo "DOCKER_TAG: ${DOCKER_TAG}"
                            echo "SERVER_PORT: ${SERVER_PORT}"
                            echo "LOGGING_LEVEL: ${LOGGING_LEVEL}"
                            echo "LOGGING_LEVEL_SPRINGFRAMEWORK: ${LOGGING_LEVEL_SPRINGFRAMEWORK}"
                        '''
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                container('kubectl') {
                    withKubeConfig([credentialsId: 'kubeconfig-secret']) {
                        script {
                            try {
                                // 測試集群連接
                                sh 'kubectl cluster-info'
                                
                                // 檢查 deployment.yaml 文件
                                sh 'ls -la k8s/'
                                
                                // 檢查 Deployment 是否存在
                                sh '''
                                    echo "Recreating deployment ..."
                                        # Ensure envsubst is available
                                        apk add --no-cache gettext >/dev/null 2>&1 || true

                                        # debug: show key credentials pulled in (mask passwords)
                                        echo "=== Effective sensitive env values ==="
                                        echo "SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}"
                                        echo "KEYCLOAK_AUTH_SERVER_URL=${KEYCLOAK_AUTH_SERVER_URL}"
                                        echo "REDIS_HOST=${REDIS_HOST}:${REDIS_CUSTOM_PORT}"

                                        kubectl delete deployment ty-multiverse-backend -n default --ignore-not-found
                                        envsubst < k8s/deployment.yaml | kubectl apply -f -
                                        kubectl set image deployment/ty-multiverse-backend ty-multiverse-backend=${DOCKER_IMAGE}:${DOCKER_TAG} -n default
                                        kubectl rollout status deployment/ty-multiverse-backend
                                '''
                                
                                // 檢查部署狀態
                                sh 'kubectl get deployments -n default'
                                sh 'kubectl rollout status deployment/ty-multiverse-backend'
                            } catch (Exception e) {
                                echo "Error during deployment: ${e.message}"
                                throw e
                            }
                        }
                    }
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