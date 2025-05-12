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
 #                   docker stop redis postgres ocr-app be-spring-container || true
 #                   docker rm redis postgres ocr-app be-spring-container || true
 #                   docker-compose -f $COMPOSE_FILE_1 --env-file $ENV_FILE up -d --build redis postgres backend ocr

                    docker-compose -f $COMPOSE_FILE_1 --env-file $ENV_FILE \\
                          down --remove-orphans

                    docker-compose -f $COMPOSE_FILE_1 --env-file $ENV_FILE \\
                          up -d --build --force-recreate redis postgres backend ocr
                """
            }
        }
        stage('Deploy to EC2-2') {
            steps {
                sh """
                    #docker stop ai-service || true
                    #docker rm ai-service || true
                    #docker-compose -f $COMPOSE_FILE_2 --env-file $ENV_FILE up -d --build
                    
                    docker-compose -f $COMPOSE_FILE_2 --env-file $ENV_FILE \\
                          down --remove-orphans

                    docker-compose -f $COMPOSE_FILE_2 --env-file $ENV_FILE \\
                          up -d --build --force-recreate ai-service
                """
            }
        }
    }
}
