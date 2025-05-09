pipeline {
    agent any
    environment {
        COMPOSE_FILE_1 = "docker-compose.ec2-1.yml"
        COMPOSE_FILE_2 = "docker-compose.ec2-2.yml"
        ENV_FILE = ".env"
        REMOTE_PATH = "/home/ubuntu/S12P31S209"
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Load .env File') {
            steps {
                withCredentials([file(credentialsId: 'env-secret', variable: 'LOADED_ENV')]) {
                    sh 'rm -f $ENV_FILE && cp $LOADED_ENV $ENV_FILE'
                }
            }
        }
        stage('Deploy to EC2-1') {
            steps {
                sh """
                    # 젠킨스 제외하고 다른 컨테이너만 중지/삭제
                    docker stop redis postgres ocr-app be-spring-container || true
                    docker rm redis postgres ocr-app be-spring-container || true
                    # 젠킨스 서비스 제외하고 docker-compose 실행
                    docker-compose -f $COMPOSE_FILE_1 --env-file $ENV_FILE up -d --build redis postgres backend ocr
                """
            }
        }
        stage('Deploy to EC2-2') {
            steps {
                sh """
                    docker stop ai-service || true
                    docker rm ai-service || true
                    docker-compose -f $COMPOSE_FILE_2 --env-file $ENV_FILE up -d --build
                """
            }
        }
    }
}