pipeline {
    agent any

    environment {
        COMPOSE_FILE_1 = "docker-compose.ec2-1.yml"
        COMPOSE_FILE_2 = "docker-compose.ec2-2.yml"
        ENV_FILE = ".env"
        REMOTE_PATH = "/home/ubuntu/S12P31S209"  // 현재는 의미 없음 (참고용)
    }

    stages {
        stage('Load .env File') {
            steps {
                withCredentials([file(credentialsId: 'env-secret', variable: 'LOADED_ENV')]) {
                    sh 'cp $LOADED_ENV $ENV_FILE'
                }
            }
        }

        stage('Deploy to EC2-1') {
            steps {
                sh "docker compose -f $COMPOSE_FILE_1 --env-file $ENV_FILE up -d --build"
            }
        }

        stage('Deploy to EC2-2') {
            steps {
                sh "docker compose -f $COMPOSE_FILE_2 --env-file $ENV_FILE up -d --build"
            }
        }
    }
}
