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
        stage('Load Application Config') {
            steps {
                withCredentials([file(credentialsId: 'application-prod-yml', variable: 'APP_CONFIG')]) {
                    sh 'mkdir -p BE/src/main/resources && cp $APP_CONFIG BE/src/main/resources/application-prod.yml'
                }
            }
        }
        

        stage('Build backend jar')   {
            steps {
                sh '''
                  cd BE
                  chmod +x gradlew 
                  ./gradlew clean bootJar -x test   # 선택: test 제외
                '''
            }
        }


        stage('Deploy to EC2-1') {
            steps {
                sh """
                    # 실행 중인 모든 컨테이너 중지 및 제거
                    docker ps -a | grep -E 'redis|postgres|ocr-app|be-spring-container|s209cicd|s209cicd2' | awk '{print \$1}' | xargs -r docker stop
                    docker ps -a | grep -E 'redis|postgres|ocr-app|be-spring-container|s209cicd|s209cicd2' | awk '{print \$1}' | xargs -r docker rm -f
                    
                    # 포트를 사용 중인 프로세스 확인 및 종료
                    lsof -ti:5432 | xargs -r kill -9
                    lsof -ti:6379 | xargs -r kill -9
                    lsof -ti:8081 | xargs -r kill -9
                    lsof -ti:7000 | xargs -r kill -9
                    
                    # Docker Compose 정리 및 재시작
                    docker-compose -f $COMPOSE_FILE_1 --env-file $ENV_FILE down -v --remove-orphans || true
                    docker network prune -f || true
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
