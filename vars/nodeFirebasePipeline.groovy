#!/usr/bin/env groovy

def call(Map pipelineParams) {
    pipeline {
        agent {
            label 'Linux'
        }
        environment {
            FIREBASE_CI_TOKEN     = credentials('FIREBASE_CI_TOKEN')
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
                        if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master') {
                            sh 'firebase deploy --token $FIREBASE_CI_TOKEN'
                        } else {
                            sh 'firebase hosting:channel:deploy $BRANCH_NAME --expires 7d --token $FIREBASE_CI_TOKEN'
                        }
                    }
                }
            }
        }
    }
}