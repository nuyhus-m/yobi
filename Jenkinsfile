
pipeline {
    agent any

        environment {
            // 로컬(워크스페이스) .env → 원격 .env
            ENV_FILE     = ".env"

            // 원격 AI 서버 기본 경로
            REMOTE_PATH  = "/home/ubuntu/ai-app"

            // Docker Hub 이미지
            DOCKER_IMAGE = "your-dockerhub-id/ai-app:latest"

            /* 모델 경로 (컨테이너·호스트 공통) */
            BASE_MODEL_PATH = "/srv/models/base"
            ADAPTER_PATH    = "/srv/models/mistral_lora_adapter"
            HF_CACHE_DIR    = "/srv/models/cache"

            /* Jenkins Credentials */
            HF_TOKEN        = credentials('hf_token')
            DOCKERHUB_USER  = credentials('docker-hub-user')
            DOCKERHUB_PASS  = credentials('docker-hub-pass')
            EC2_AI_IP       = "43.203.38.182"            // 2번 서버 IP
        }
    }

    stages {

        /* 0. 작업 경로 확인 (디버깅용) */
        stage('Check Workspace') {
            steps {
                sh 'echo "[Workspace] $WORKSPACE"'
                sh 'pwd && ls -al'
            }
        }

        /* 1. 브랜치 검사 (ai-dev 전용) */
        stage('Branch Check') {
            steps {
                script {
                    def branch = env.BRANCH_NAME ?: env.GIT_BRANCH?.replaceFirst(/^origin\//,'') ?: 'unknown'
                    echo "현재 브랜치: ${branch}"
                    if (!branch.contains('ai-dev')) {
                        currentBuild.result = 'SUCCESS'
                        error('ai-dev 브랜치가 아니므로 파이프라인 중단')
                    }
                }
            }
        }

        /* 2. 코드 체크아웃 */
        stage('Checkout') { steps { checkout scm } }

        /* 3. .env 파일 로드 (Jenkins 파일 타입 credential) */
        stage('Prepare .env') {
            steps {
                withCredentials([file(credentialsId: 'ai-env-secret', variable: 'ENV_SRC')]) {
                    sh 'cp $ENV_SRC .env'
                }
            }
        }

        /* 4. Docker 이미지 빌드 (모델 없이) */
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE .'
            }
        }

        /* 5. Docker Hub Push */
        stage('Push Docker Image') {
            steps {
                sh """
                    echo "$DOCKERHUB_PASS" | docker login -u "$DOCKERHUB_USER" --password-stdin
                    docker push $DOCKER_IMAGE
                    docker logout
                """
            }
        }

        /* 6. .env & 배포 스크립트 전송 + 모델 확인·배포 */
        stage('Deploy to AI Server') {
            steps {
                sshagent (credentials: ['ec2-2-pem-key-id']) {
                    withCredentials([string(credentialsId: 'hf_token', variable: 'HF')]) {

                        /* (6-1) .env 복사 */
                        sh """
                        scp -o StrictHostKeyChecking=no ${ENV_FILE} \
                            ubuntu@${EC2_AI_IP}:${REMOTE_PATH}/.env
                        """

                        /* (6-2) 원격 배포 스크립트 실행 */
                        sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${EC2_AI_IP} '
                          set -e

                          echo "▶ 모델 디렉터리 준비"
                          sudo mkdir -p /srv/models/base /srv/models/mistral_lora_adapter /srv/models/cache
                          sudo chown -R ubuntu:ubuntu /srv/models

                          echo "▶ 모델 존재 여부 확인"
                          if [ ! -f /srv/models/base/config.json ] || [ ! -f /srv/models/mistral_lora_adapter/adapter_model.bin ]; then
                              echo "🔍 모델 없음 → 다운로드"
                              docker run --rm \
                                -e HF_TOKEN=${HF} \
                                -e BASE_MODEL_PATH=${BASE_MODEL_PATH} \
                                -e ADAPTER_PATH=${ADAPTER_PATH} \
                                -e HF_HOME=${HF_CACHE_DIR} \
                                -v /srv/models:/srv/models \
                                ${DOCKER_IMAGE} \
                                python app/ai_model/download_models.py
                          else
                              echo "✅ 모델 이미 존재"
                          fi

                          echo "▶ 최신 이미지 Pull & 재배포"
                          cd ${REMOTE_PATH}
                          docker pull ${DOCKER_IMAGE}
                          docker-compose -f docker-compose.ec2-2.yml --env-file .env up -d --build --force-recreate
                        '
                        """
                    }
                }
            }
        }
    }
}