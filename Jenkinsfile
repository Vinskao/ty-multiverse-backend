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
                  - name: kubectl
                    image: bitnami/kubectl:1.30.7
                    command: ["/bin/sh"]
                    args: ["-c", "while true; do sleep 30; done"]
                    imagePullPolicy: Always
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
                            string(credentialsId: 'PUBLIC_TYMB_URL', variable: 'PUBLIC_TYMB_URL'),
                            string(credentialsId: 'PUBLIC_FRONTEND_URL', variable: 'PUBLIC_FRONTEND_URL'),
                            string(credentialsId: 'KEYCLOAK_AUTH_SERVER_URL', variable: 'KEYCLOAK_AUTH_SERVER_URL'),
                            string(credentialsId: 'PUBLIC_REALM', variable: 'PUBLIC_REALM'),
                            string(credentialsId: 'PUBLIC_CLIENT_ID', variable: 'PUBLIC_CLIENT_ID'),
                            string(credentialsId: 'KEYCLOAK_CREDENTIALS_SECRET', variable: 'KEYCLOAK_CREDENTIALS_SECRET')
                        ]) {
                            sh '''
                                cat > src/main/resources/env/platform.properties <<EOL
                                env=platform
                                spring.profiles.active=platform
                                PROJECT_ENV=platform
                                # Primary datasource configuration
                                SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}
                                SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
                                SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
                                # People datasource configuration
                                SPRING_PEOPLE_DATASOURCE_URL=${SPRING_PEOPLE_DATASOURCE_URL}
                                SPRING_PEOPLE_DATASOURCE_USERNAME=${SPRING_PEOPLE_DATASOURCE_USERNAME}
                                SPRING_PEOPLE_DATASOURCE_PASSWORD=${SPRING_PEOPLE_DATASOURCE_PASSWORD}
                                server.port=8080
                                logging.level.root=DEBUG
                                logging.level.org.springframework=DEBUG
                                PUBLIC_TYMB_URL=${PUBLIC_TYMB_URL}
                                PUBLIC_FRONTEND_URL=${PUBLIC_FRONTEND_URL}
                                KEYCLOAK_AUTH_SERVER_URL=${KEYCLOAK_AUTH_SERVER_URL}
                                PUBLIC_REALM=${PUBLIC_REALM}
                                PUBLIC_CLIENT_ID=${PUBLIC_CLIENT_ID}
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

        stage('Build and Push Image (Jib)') {
            steps {
                container('maven') {
                    script {
                        withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                            sh '''
                                cd "${WORKSPACE}"
                                # 使用 Jib 直接建置並推送（無需 Docker Daemon）
                                mvn -B -P platform -DskipTests \
                                  com.google.cloud.tools:jib-maven-plugin:3.4.2:build \
                                  -Djib.to.image=${DOCKER_IMAGE}:${DOCKER_TAG} \
                                  -Djib.to.tags=latest \
                                  -Djib.to.auth.username=${DOCKER_USERNAME} \
                                  -Djib.to.auth.password=${DOCKER_PASSWORD}
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
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                container('kubectl') {
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
                        string(credentialsId: 'PUBLIC_TYMB_URL', variable: 'PUBLIC_TYMB_URL'),
                        string(credentialsId: 'PUBLIC_FRONTEND_URL', variable: 'PUBLIC_FRONTEND_URL'),
                        string(credentialsId: 'KEYCLOAK_AUTH_SERVER_URL', variable: 'KEYCLOAK_AUTH_SERVER_URL'),
                        string(credentialsId: 'PUBLIC_REALM', variable: 'PUBLIC_REALM'),
                        string(credentialsId: 'PUBLIC_CLIENT_ID', variable: 'PUBLIC_CLIENT_ID'),
                        string(credentialsId: 'KEYCLOAK_CREDENTIALS_SECRET', variable: 'KEYCLOAK_CREDENTIALS_SECRET')
                    ]) {
                        script {
                            try {
                                sh '''
                                    set -e

                                    # Ensure envsubst is available (try Debian then Alpine)
                                    if ! command -v envsubst >/dev/null 2>&1; then
                                      (apt-get update && apt-get install -y --no-install-recommends gettext-base ca-certificates) >/dev/null 2>&1 || true
                                      command -v envsubst >/dev/null 2>&1 || (apk add --no-cache gettext ca-certificates >/dev/null 2>&1 || true)
                                    fi

                                    # In-cluster auth via ServiceAccount (serviceAccountName: jenkins-admin)
                                    kubectl cluster-info

                                    # Inspect manifest directory
                                    ls -la k8s/

                                    echo "Recreating deployment ..."
                                    echo "=== Effective sensitive env values ==="
                                    echo "SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}"
                                    echo "SPRING_PEOPLE_DATASOURCE_URL=${SPRING_PEOPLE_DATASOURCE_URL}"
                                    echo "KEYCLOAK_AUTH_SERVER_URL=${KEYCLOAK_AUTH_SERVER_URL}"
                                    echo "REDIS_HOST=${REDIS_HOST}:${REDIS_CUSTOM_PORT}"

                                    kubectl delete deployment ty-multiverse-backend -n default --ignore-not-found
                                    envsubst < k8s/deployment.yaml | kubectl apply -f -
                                    kubectl set image deployment/ty-multiverse-backend ty-multiverse-backend=${DOCKER_IMAGE}:${DOCKER_TAG} -n default
                                    kubectl rollout status deployment/ty-multiverse-backend -n default
                                '''

                                // 檢查部署狀態
                                sh 'kubectl get deployments -n default'
                                sh 'kubectl rollout status deployment/ty-multiverse-backend -n default'
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