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
                        ssh -o StrictHostKeyChecking=no ubuntu@${EC2_AI_IP} bash -c "
                        set -e

                        echo 📦 Pulling Docker Image: ${DOCKER_IMAGE}
                        docker image inspect ${DOCKER_IMAGE} > /dev/null || docker pull ${DOCKER_IMAGE}

                        sudo chown -R ubuntu:ubuntu /srv/models /mnt/data/huggingface

                        if [ ! -f ${BASE_MODEL_PATH}/config.json ]; then
                            echo ⬇️ Downloading Models
                            docker run --rm \\
                                -e HF_TOKEN=${HF_TOKEN} \\
                                -e HF_HOME=/root/.cache/huggingface \
                                -e BASE_MODEL_PATH=/mnt/data/models/base \
                                -e ADAPTER_PATH=/mnt/data/models/adapter \
                                -v /mnt/data/models:/mnt/data/models \
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
