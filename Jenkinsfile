pipeline {
    agent any

    environment {
        EC2_1 = "${env.EC2_1_IP}"
        EC2_2 = "${env.EC2_2_IP}"
    }

    stages {
        stage('Deploy to EC2-1') {
            steps {
                sshagent (credentials: ['your-ssh-credential-id']) {
                    sh """
                    ssh -o StrictHostKeyChecking=no $EC2_1 '
                      cd ~/S12P31S209 &&
                      docker compose -f docker-compose.ec2-1.yml up -d --build
                    '
                    """
                }
            }
        }

        stage('Deploy to EC2-2') {
            steps {
                sshagent (credentials: ['your-ssh-credential-id']) {
                    sh """
                    ssh -o StrictHostKeyChecking=no $EC2_2 '
                      cd ~/S12P31S209 &&
                      docker compose -f docker-compose.ec2-2.yml up -d --build
                    '
                    """
                }
            }
        }
    }
}
