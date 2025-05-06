pipeline {
    agent any

    environment {
        IMAGE_NAME = "papakao/ty-multiverse-backend:latest"
        SERVER_PORT = "8080"
        LOGGING_LEVEL = "INFO"
        LOGGING_LEVEL_SPRINGFRAMEWORK = "INFO"
    }

    stages {
        stage('Clone repository') {
            steps {
                git url: 'https://github.com/Vinskao/TY-Multiverse-Backend.git', branch: 'main'
            }
        }

        stage('Check Git Version') {
            steps {
                sh 'git log -1'
                sh 'ls -R src/main/resources/env'
            }
        }

        stage('Setup Environment') {
            steps {
                withCredentials([
                    string(credentialsId: 'TYB/SPRING_DATASOURCE_URL', variable: 'SPRING_DATASOURCE_URL'),
                    string(credentialsId: 'TYB/SPRING_DATASOURCE_USERNAME', variable: 'SPRING_DATASOURCE_USERNAME'),
                    string(credentialsId: 'TYB/SPRING_DATASOURCE_PASSWORD', variable: 'SPRING_DATASOURCE_PASSWORD'),
                    string(credentialsId: 'TYB/URL_ADDRESS', variable: 'URL_ADDRESS'),
                    string(credentialsId: 'TYB/URL_FRONTEND', variable: 'URL_FRONTEND'),
                    string(credentialsId: 'TYB/KEYCLOAK_AUTH_SERVER_URL', variable: 'KEYCLOAK_AUTH_SERVER_URL'),
                    string(credentialsId: 'TYB/KEYCLOAK_REALM', variable: 'KEYCLOAK_REALM'),
                    string(credentialsId: 'TYB/KEYCLOAK_CLIENT_ID', variable: 'KEYCLOAK_CLIENT_ID'),
                    string(credentialsId: 'TYB/KEYCLOAK_CREDENTIALS_SECRET', variable: 'KEYCLOAK_CREDENTIALS_SECRET'),
                    string(credentialsId: 'TYB/PROJECT_ENV', variable: 'PROJECT_ENV')
                ]) {
                    sh '''
                        mkdir -p src/main/resources/env
                        cat > src/main/resources/env/platform.properties << EOL
                        env: platform
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
                        cat src/main/resources/env/platform.properties
                    '''
                }
            }
        }

        stage('Maven Build') {
            steps {
                sh './mvnw clean install -P platform -X'
            }
        }

        stage('Build Docker image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker build -t $IMAGE_NAME .
                    '''
                }
            }
        }

        stage('Push Docker image') {
            steps {
                sh 'docker push $IMAGE_NAME'
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
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
