pipeline {
    agent any

    /* ───────────── 공통 환경변수 ───────────── */
    environment {
        /* Git 저장소 내부 경로 */
        DOCKER_CTX   = "AI"              // build context
        DOCKERFILE   = "AI/Dockerfile"   // Dockerfile 경로

        /* 원격(2번) 경로 */
        REMOTE_PATH  = "/home/ubuntu/S12P31S209"    // compose·.env 위치
        COMPOSE_FILE = "/home/ubuntu/S12P31S209/docker-compose.ec2-2.yml"

        /* Docker Hub 이미지 */
        DOCKER_IMAGE = "mundevelop/ai-app:latest"

        /* 모델 경로 (컨테이너·호스트 공통) */
        BASE_MODEL_PATH = "/srv/models/base"
        ADAPTER_PATH    = "/srv/models/mistral_lora_adapter"
        HF_CACHE_DIR    = "/srv/models/cache"

        /* Jenkins Credentials */
        EC2_AI_IP       = "43.203.38.182"           // 2번 서버 IP
    }

    stages {
        /* 0. 워크스페이스 확인 (옵션) */
        stage('Check Workspace') {
            steps {
                sh 'echo "[Workspace] $WORKSPACE"'
                sh 'pwd && ls -al'
            }
        }

        /* 1. ai-dev 브랜치인지 확인 */
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

        /* 2. 코드 체크아웃 */
        stage('Checkout') {
            steps { checkout scm }
        }

        /* 2-A. requirements.txt 에서 psycopg2 제거 */
        stage('Patch requirements (drop psycopg2)') {
            steps {
                sh '''
                  sed -i -E '/^psycopg2(-binary)?([[:space:]]*==.*)?[[:space:]]*$/Id' AI/requirements.txt
                  echo "== after patch =="
                  grep -n psycopg2 AI/requirements.txt || echo "✔ no psycopg2 lines"
                '''
            }
        }

        /* 3. .env 파일 준비 (Jenkins 파일-credential) */
        stage('Prepare .env') {
            steps {
                withCredentials([file(credentialsId: 'ai-env-secret', variable: 'ENV_SRC')]) {
                    sh 'cp $ENV_SRC .env'
                }
            }
        }

        /* 4. Docker 이미지 빌드 */
        stage('Build Docker Image') {
            steps {
                sh """
                docker build -f ${DOCKERFILE} -t ${DOCKER_IMAGE} ${DOCKER_CTX}
                """
            }
        }

        /* 5. Docker Hub Push */
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
                        ssh -o StrictHostKeyChecking=no ubuntu@${EC2_AI_IP} bash -c "
                        set -e

                        echo 📦 Pulling Docker Image: ${DOCKER_IMAGE}
                        docker image inspect ${DOCKER_IMAGE} > /dev/null || docker pull ${DOCKER_IMAGE}


                        sudo mkdir -p ${BASE_MODEL_PATH} ${ADAPTER_PATH} ${HF_CACHE_DIR}
                        sudo chown -R ubuntu:ubuntu /srv/models

                        if [ ! -f ${BASE_MODEL_PATH}/config.json ] || [ ! -f ${ADAPTER_PATH}/adapter_model.bin ]; then
                            echo ⬇️ Downloading Models
                            docker run --rm \\
                                -e HF_TOKEN=${HF_TOKEN} \\
                                -v /srv/models:/srv/models \\
                                -v /mnt/data/huggingface:/root/.cache/huggingface \\
                                ${DOCKER_IMAGE} \\
                                python app/ai_model/download_models.py
                        fi

                        echo 🚀 Deploying via docker-compose
                        docker-compose -f ${COMPOSE_FILE} --env-file ${REMOTE_PATH}/.env up -d --build --force-recreate"
                        """
                    }
                }
            }
        }
    }
}
