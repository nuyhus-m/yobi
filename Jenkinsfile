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
                // Jenkins 크레덴셜 저장소에서 .env 파일 로드
                withCredentials([file(credentialsId: 'env-secret', variable: 'LOADED_ENV')]) {
                    // .env 파일을 로컬로 복사
                    sh 'cp $LOADED_ENV $ENV_FILE'
                }
            }
        }
        stage('Deploy to EC2-1') {
            steps {
                // EC2-1에 배포 (docker-compose 명령어 실행)
                sh "docker compose -f $COMPOSE_FILE_1 --env-file $ENV_FILE up -d --build"
            }
        }
        stage('Deploy to EC2-2') {
            steps {
                // EC2-2에 배포 (docker-compose 명령어 실행)
                sh "docker compose -f $COMPOSE_FILE_2 --env-file $ENV_FILE up -d --build"
            }
        }
    }
}
