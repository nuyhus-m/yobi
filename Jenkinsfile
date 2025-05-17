pipeline {
    agent any

    environment {
        // 도커 이미지 이름
        DOCKER_IMAGE = "mundevelop/ai-app:latest"

        // 배포 서버에서 사용하는 경로 (EC2 내부 경로)
        REMOTE_PATH = "/home/ubuntu/ai-app"

        // Jenkins Credentials에서 가져오는 시크릿 값들
        HF_TOKEN = credentials('hf_token') // huggingface token
        DOCKERHUB_USER = credentials('docker-hub-user') // dockerhub 아이디
        DOCKERHUB_PASS = credentials('docker-hub-pass') // dockerhub 비밀번호
        EC2_AI_IP = "43.203.38.182" // AI 서버 IP
    }

    stages {
        stage('Checkout') {
            steps {
                // ✅ [1] GitLab 저장소 코드 가져오기
                checkout scm
            }
        }

        stage('Debug Workspace') {
            steps {
                // ✅ [2] Jenkins가 실제로 어디서 작업하는지 로그 확인
                sh 'echo "=== Current Workspace Directory ==="'
                sh 'pwd'
                sh 'echo "=== List Workspace Files ==="'
                sh 'ls -alR'
            }
        }

        stage('Build Docker Image with HF_TOKEN') {
            steps {
                // ✅ [3] 도커 이미지 빌드 (모델 다운로드 포함)
                // HF_TOKEN을 build-arg로 넘겨서 Dockerfile에서 모델 다운로드 시 사용
                sh """
                    docker build --build-arg HF_TOKEN=${HF_TOKEN} -t ${DOCKER_IMAGE} .
                """
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                // ✅ [4] 도커 허브 로그인 → 이미지 푸시 → 로그아웃
                sh """
                    echo "$DOCKERHUB_PASS" | docker login -u "$DOCKERHUB_USER" --password-stdin
                    docker push ${DOCKER_IMAGE}
                    docker logout
                """
            }
        }

        stage('Deploy to AI Server (2번 서버)') {
            steps {
                // ✅ [5] SSH를 통해 EC2 서버 접속 → 도커 이미지 최신 pull → 배포
                sshagent (credentials: ['ec2-2-pem-key-id']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${EC2_AI_IP} "
                            cd /home/ubuntu/S12P31S209 &&
                            docker pull ${DOCKER_IMAGE} &&
                            docker-compose -f docker-compose.ec2-2.yml --env-file .env up -d --build --force-recreate
                        "
                    """
                }
            }
        }
    }
}
