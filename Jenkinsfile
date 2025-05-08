pipeline {
    agent any

    environment {
        EC2_1 = "${env.EC2_1_IP}"
        EC2_2 = "${env.EC2_2_IP}"
        REMOTE_PATH = "/home/ubuntu/S12P31S209"
    }

    stages {
        stage('Deploy to EC2-1') {
            steps {
                sshagent (credentials: ['yobi']) {
                    withCredentials([file(credentialsId: 'env-secret', variable: 'ENV_FILE')]) {
                        sh """
                        scp -o StrictHostKeyChecking=no \$ENV_FILE \$EC2_1:\$REMOTE_PATH/.env
                        ssh -o StrictHostKeyChecking=no \$EC2_1 '
                          cd \$REMOTE_PATH &&
                          docker compose -f docker-compose.ec2-1.yml up -d --build
                        '
                        """
                    }
                }
            }
        }

        stage('Deploy to EC2-2') {
            steps {
                sshagent (credentials: ['yobi']) {
                    withCredentials([file(credentialsId: 'env-secret', variable: 'ENV_FILE')]) {
                        sh """
                        scp -o StrictHostKeyChecking=no \$ENV_FILE \$EC2_2:\$REMOTE_PATH/.env
                        ssh -o StrictHostKeyChecking=no \$EC2_2 '
                          cd \$REMOTE_PATH &&
                          docker compose -f docker-compose.ec2-2.yml up -d --build
                        '
                        """
                    }
                }
            }
        }
    }
}
