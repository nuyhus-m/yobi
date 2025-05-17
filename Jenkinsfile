pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "mundevelop/ai-app:latest"
        REMOTE_PATH = "/home/ubuntu/ai-app"
        HF_TOKEN = credentials('hf_token')
        DOCKERHUB_USER = credentials('docker-hub-user')
        DOCKERHUB_PASS = credentials('docker-hub-pass')
        EC2_AI_IP = "43.203.38.182"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image (Real)') {
            steps {
                sh """
                    docker build --build-arg HF_TOKEN=${HF_TOKEN} -t ${DOCKER_IMAGE} ./AI
                """
            }
        }

        stage('Push Docker Image') {
            steps {
                sh """
                    echo "$DOCKERHUB_PASS" | docker login -u "$DOCKERHUB_USER" --password-stdin
                    docker push ${DOCKER_IMAGE}
                    docker logout
                """
            }
        }

        stage('Deploy to AI Server') {
            steps {
                sshagent (credentials: ['ec2-2-pem-key-id']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${EC2_AI_IP} "
                            cd /home/ubuntu/S12P31S209 &&
                            docker pull ${DOCKER_IMAGE} &&
                            docker-compose -f docker-compose.ec2-2.yml --env-file .env up -d --build --force-recreate
                        "
                    """
                }
            }
        }
    }
}
