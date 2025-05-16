pipeline {
    agent any

    environment {
        ENV_FILE = ".env"
        REMOTE_PATH = "/home/ubuntu/ai-app"
        DOCKER_IMAGE = "your-dockerhub-id/ai-app:latest"
    }

    stages {

        // ✅ [1] 현재 브랜치가 ai-dev 인지 체크 (아니면 빌드 중단)
        stage('Branch Check') {
            steps {
                script {
                    def branchName = env.BRANCH_NAME ?: env.GIT_BRANCH?.replaceFirst(/^origin\//, '') ?: 'unknown'
                    echo "현재 브랜치입니다: ${branchName}"

                    if (!branchName || !branchName.contains('ai-dev')) {
                        echo "❌ 이 잡은 ai-dev 브랜치에서만 동작합니다. 현재 브랜치: ${branchName}"
                        currentBuild.result = 'SUCCESS'
                        error('브랜치가 ai-dev가 아니므로 빌드를 중단합니다.')
                    } else {
                        echo "✅ ai-dev 브랜치 감지됨. 계속 진행합니다."
                    }
                }
            }
        }

        // ✅ [2] GitLab 저장소 Checkout (코드 가져오기)
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // ✅ [3] Jenkins Credentials에 있는 .env 파일 로드 (.env로 복사)
        stage('Load .env File') {
            steps {
                withCredentials([file(credentialsId: 'ai-env-secret', variable: 'LOADED_ENV')]) {
                    sh 'rm -f .env && cp $LOADED_ENV .env'
                }
            }
        }

        // ✅ [4] 모델 파일이 없는 경우 HuggingFace에서 다운로드 후 저장
        stage('Check & Download Mistral LoRA') {
            steps {
                script {
                    def modelCheck = sh(script: "[ -f ${BASE_MODEL_PATH}/config.json ] && [ -f ${ADAPTER_PATH}/adapter_model.bin ]", returnStatus: true)

                    if (modelCheck != 0) {
                        echo "🔍 모델이 없음. download_models.py 실행"
                        sh """
                            export HF_TOKEN=${HF_TOKEN}
                            export BASE_MODEL_PATH=${BASE_MODEL_PATH}
                            export ADAPTER_PATH=${ADAPTER_PATH}
                            export HF_HOME=${HF_CACHE_DIR}
                            export TEST_MODEL_LOADING=true
                            python3 scripts/download_models.py
                        """
                    } else {
                        echo "✅ 모델이 이미 존재합니다. 다운로드 스킵"
                    }
                }
            }
        }

        // ✅ [5] Docker 이미지 빌드 (AI App 기준)
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t your-dockerhub-id/ai-app:latest .'
            }
        }

        // ✅ [6] 빌드한 Docker 이미지를 DockerHub에 Push
        stage('Push Docker Image to Docker Hub') {
            steps {
                sh """
                    echo "$DOCKERHUB_PASS" | docker login -u "$DOCKERHUB_USER" --password-stdin
                    docker push your-dockerhub-id/ai-app:latest
                    docker logout
                """
            }
        }

        // ✅ [7] 2번 서버(AI 서버)에 접속 → 최신 이미지 pull → docker-compose로 배포
        stage('Deploy to AI Server (2번 서버)') {
            steps {
                sshagent (credentials: ['ec2-2-pem-key-id']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@2번서버IP '
                            cd /home/ubuntu/your-app-directory &&
                            docker pull your-dockerhub-id/ai-app:latest &&
                            docker-compose -f docker-compose.ec2-2.yml --env-file .env up -d --build --force-recreate
                        '
                    """
                }
            }
        }
    }
}
