pipeline {
    agent any

    environment {
        IMAGE_NAME = "papakao/ty-multiverse-backend:latest"
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

        stage('Maven Build') {
            steps {
                sh './mvnw clean install -P platform'
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
