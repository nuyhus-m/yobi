pipeline {
    agent any

    environment {
        DOCKER_CTX   = "AI"
        DOCKERFILE   = "AI/Dockerfile"
        REMOTE_PATH  = "/home/ubuntu/S12P31S209"
        COMPOSE_FILE = "/home/ubuntu/S12P31S209/docker-compose.ec2-2.yml"
        DOCKER_IMAGE = "mundevelop/ai-app:latest"

        BASE_MODEL_PATH = "/mnt/data/models/base"
        ADAPTER_PATH    = "/mnt/data/models/adapter"
        HF_HOME="/mnt/data/huggingface"
        HF_CACHE_DIR    = "/srv/models/cache"

        EC2_AI_IP       = "43.203.38.182"
    }

    stages {
        stage('Check Workspace') {
            steps {
                sh 'echo "[Workspace] $WORKSPACE"'
                sh 'pwd && ls -al'
            }
        }

        stage('Branch Check') {
            steps {
                script {
                    def branch = env.BRANCH_NAME ?: env.GIT_BRANCH?.replaceFirst(/^origin\//,'') ?: 'unknown'
                    if (!branch.contains('ai-dev')) {
                        echo "❌ ai-dev 전용 파이프라인. 현재: ${branch}"
                        currentBuild.result = 'SUCCESS'
                        error('ai-dev 가 아니라서 중단')
                    }
                    echo "✅ ai-dev 브랜치 확인 완료"
                }
            }
        }

        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Patch requirements (drop psycopg2)') {
            steps {
                sh '''
                  sed -i -E '/^psycopg2(-binary)?([[:space:]]*==.*)?[[:space:]]*$/Id' AI/requirements.txt
                  echo "== after patch =="
                  grep -n psycopg2 AI/requirements.txt || echo "✔ no psycopg2 lines"
                '''
            }
        }

        stage('Prepare .env') {
            steps {
                withCredentials([file(credentialsId: 'ai-env-secret', variable: 'ENV_SRC')]) {
                    sh 'cp $ENV_SRC .env'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                docker build -f ${DOCKERFILE} -t ${DOCKER_IMAGE} ${DOCKER_CTX}
                """
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'docker-hub-creds',
                    usernameVariable: 'HUB_USER',
                    passwordVariable: 'HUB_PASS'
                )]) {
                    sh '''
                        echo "$HUB_PASS" | docker login -u "$HUB_USER" --password-stdin
                        docker push ${DOCKER_IMAGE}
                        docker logout
                    '''
                }
            }
        }
        
        stage('Deploy to AI Server') {
            steps {
                sshagent(credentials: ['ec2-2-pem-key-id']) {
                    withCredentials([string(credentialsId: 'hf_token', variable: 'HF_TOKEN')]) {
                        sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${EC2_AI_IP} bash -c \"
                        set -e
                                
                        # 기존 컨테이너 정리
                        echo '🧹 기존 컨테이너 정리'
                        docker-compose -f /home/ubuntu/S12P31S209/docker-compose.ec2-2.yml down || true
                        
                        # 기존 이미지 삭제
                        echo '🗑️ 기존 이미지 삭제 중...'
                        docker image rm mundevelop/ai-app:latest || true
                        
                        # 새 이미지 받기
                        echo '📦 새 이미지 풀 받는 중...'
                        docker pull mundevelop/ai-app:latest

                        sudo chown -R ubuntu:ubuntu /srv/models /mnt/data/huggingface

                        # Docker 네트워크 생성 - 이미 존재하면 무시
                        echo '🌐 Docker 네트워크 확인 중...'
                        if ! docker network inspect s12p31s209_ai-network &>/dev/null; then
                            echo '🆕 네트워크가 없으므로 생성합니다'
                            docker network create s12p31s209_ai-network
                        else
                            echo '✅ 네트워크가 이미 존재합니다'
                        fi

                        # 모델 다운로드 부분 수정 - python 명령 전체를 Docker 컨테이너 내에서 실행
                        if [ ! -f /mnt/data/models/base/config.json ]; then
                            echo '⬇️ 모델 다운로드 중...'
                            docker run --rm \\\\
                                -e HF_TOKEN=${HF_TOKEN} \\\\
                                -e HF_HOME=/root/.cache/huggingface \\\\
                                -e BASE_MODEL_PATH=/mnt/data/models/base \\\\
                                -e ADAPTER_PATH=/mnt/data/models/adapter \\\\
                                -v /mnt/data/models:/mnt/data/models \\\\
                                -v /mnt/data/huggingface:/root/.cache/huggingface \\\\
                                mundevelop/ai-app:latest \\\\
                                /bin/bash -c \\\"pip install peft && python app/ai_model/download_models.py\\\"
                        fi

                        # 디렉토리 확인 및 이동
                        cd /home/ubuntu/S12P31S209
                        pwd
                        ls -la
                        
                        # 환경 파일 확인
                        if [ ! -f .env ] && [ -f /home/ubuntu/S12P31S209/.env ]; then
                            echo '⚠️ .env 파일을 현재 디렉토리로 복사합니다'
                            cp /home/ubuntu/S12P31S209/.env .
                        fi
                        
                        echo '🚀 Docker Compose로 배포 중...'
                        docker-compose -f docker-compose.ec2-2.yml --env-file .env up -d --build --force-recreate
                        \"
                        """
                    }
                }
            }
        }
    }
}
