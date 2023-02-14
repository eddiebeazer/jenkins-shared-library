#!/usr/bin/env groovy

def call(Map pipelineParams) {
    pipeline {
        agent {
            label 'Linux'
        }
        environment {
            FIREBASE_CI_TOKEN     = credentials('FIREBASE_CI_TOKEN')
        }
        tools { nodejs pipelineParams.nodeVersion }
        options {
            disableConcurrentBuilds()
            parallelsAlwaysFailFast()
            timeout(time: 15, unit: 'MINUTES')
        }
        stages {
            stage('Installing Dependencies') {
                steps {
                    sh 'yarn --production=false'
                }
            }
            stage('Testing') {
                stages {
                    stage('Linting') {
                        when {
                            expression {
                                pipelineParams.linting == true
                            }
                        }
                        steps {
                            sh 'yarn lint'
                        }
                    }
                    stage('Dependency Check') {
                        when {
                            expression {
                                pipelineParams.dependencyCheck == true
                            }
                        }
                        steps {
                            dependencyCheck additionalArguments: '--disableYarnAudit', odcInstallation: '8.0.1', stopBuild: true
                            dependencyCheckPublisher unstableTotalCritical: 1, unstableTotalHigh: 1, unstableTotalLow: 10, unstableTotalMedium: 5
                        }
                    }
                    stage('Unit Tests') {
                        when {
                            expression {
                                pipelineParams.unitTests == true
                            }
                        }
                        steps {
                            sh 'yarn test'
                        }
                    }
                }
            }
            stage('Build') {
                when {
                    expression {
                        pipelineParams.build == true
                    }
                }
                steps {
                    sh 'yarn build'
                }
            }
            stage('Firebase Deploy') {
                when {
                    expression {
                        pipelineParams.firebaseDeploy == true
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
