#!/usr/bin/env groovy

def call(Map pipelineParams) {
    pipeline {
        agent {
            label 'Linux'
        }
        tools { nodejs '16' }
        stages {
            stage('Installing Dependencies') {
                steps {
                    sh 'yarn --production=false'
                }
            }
            stage('Linting') {
                steps {
                    sh 'yarn lint'
                }
            }
            stage('Dependency Check') {
                when {
                    changeRequest()
                }
                steps {
                    dependencyCheck additionalArguments: '--disableYarnAudit', odcInstallation: '8.0.1', stopBuild: true
                    dependencyCheckPublisher unstableTotalCritical: 1, unstableTotalHigh: 1, unstableTotalLow: 10, unstableTotalMedium: 5
                }
            }
            stage('Unit Tests') {
                steps {
                    sh 'yarn test'
                }
            }
            stage('Build') {
                steps {
                    sh 'yarn build'
                }
            }
            stage('Deploy') {
                when {
                    expression {
                        pipelineParams.deploy == true
                    }
                }
                steps {
                    script {
                        withCredentials([
                            string(credentialsId: 'firebase-ci-token', variable: 'FIREBASE_CI_TOKEN'),
                            string(credentialsId: 'contentful-access-token', variable: 'CONTENTFUL_ACCESS_TOKEN'),
                            // string(credentialsId: 'api-token', variable: 'API_TOKEN'), MAYBE
                            string(credentialsId: 'firebase-api-key', variable: 'FIREBASE_API_KEY'),
                            string(credentialsId: 'firebase-auth-domain', variable: 'FIREBASE_AUTH_DOMAIN'),
                            string(credentialsId: 'firebase-project-id', variable: 'FIREBASE_PROJECT_ID'),
                            string(credentialsId: 'firebase-storage-bucket', variable: 'FIREBASE_STORAGE_BUCKET'),
                            string(credentialsId: 'firebase-mnessaging-sender-id', variable: 'FIREBASE_MESSAGING_SENDER_ID'),
                            string(credentialsId: 'firebase-app-id', variable: 'FIREBASE_APP_ID')
                        ]) {
                            if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master') {
                                sh 'firebase deploy --only hosting:$pipelineParams.projectName --token $FIREBASE_CI_TOKEN'
                            } else {
                                sh 'firebase hosting:channel:deploy $BRANCH_NAME --expires 7d --token $FIREBASE_CI_TOKEN'
                            }
                        }
                    }
                }
            }
        }
    }
}

// Use manual tagging for now.  We need to figure out how to get jenkins to download our custom auto tag library
// stage('Tagging') {
//     when {
//         expression {
//             env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master'
//         }
//     }
//     steps {
//         gitVersioning()
//     }
// }
