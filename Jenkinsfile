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
                withCredentials([file(credentialsId: 'application-yml', variable: 'APP_CONFIG')]) {
                    sh 'mkdir -p BE/src/main/resources && cp $APP_CONFIG BE/src/main/resources/application.yml'
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
                    # 기존 컨테이너 중지 및 삭제
                    docker stop redis postgres ocr-app be-spring-container || true
                    docker rm redis postgres ocr-app be-spring-container || true

                    # 기존 네트워크와 orphan 컨테이너 정리
                    docker-compose -p yobi-be -f $COMPOSE_FILE_1 --env-file $ENV_FILE down --remove-orphans

                    # 컨테이너 재생성 및 재배포
                    docker-compose -p yobi-be -f $COMPOSE_FILE_1 --env-file $ENV_FILE \\
                        up -d --build --force-recreate redis postgres backend ocr
                        """
            }
        }
        stage('Deploy to EC2-2') {
            steps {
                sh """
                    # 기존 컨테이너 중지 및 삭제
                    docker stop ai-app || true
                    docker rm ai-app || true

                    # 기존 네트워크와 orphan 컨테이너 정리
                    docker-compose -p yobi-ai -f $COMPOSE_FILE_2 --env-file $ENV_FILE down --remove-orphans

                    # 컨테이너 재생성 및 재배포
                    docker-compose -p yobi-ai -f $COMPOSE_FILE_2 --env-file $ENV_FILE \\
                        up -d --build --force-recreate ai
                """
            }
        }
    }
}
