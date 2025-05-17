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

        stage('Debug psycopg2 Trace') {
            steps {
                sh """
                    docker build --no-cache -t debug-psycopg2 ./AI
                """
            }
        }

        stage('Build Docker Image (Real)') {
            when {
                expression { false } // 디버그 끝나면 여기 true로 바꿔서 빌드할 것
            }
            steps {
                sh """
                    docker build --build-arg HF_TOKEN=${HF_TOKEN} -t ${DOCKER_IMAGE} .
                """
            }
        }

        stage('Push Docker Image') {
            when {
                expression { false } // 디버그 끝나면 true로 변경
            }
            steps {
                sh """
                    echo "$DOCKERHUB_PASS" | docker login -u "$DOCKERHUB_USER" --password-stdin
                    docker push ${DOCKER_IMAGE}
                    docker logout
                """
            }
        }

        stage('Deploy to AI Server') {
            when {
                expression { false } // 디버그 끝나면 true로 변경
            }
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
