#!/usr/bin/env groovy

def call(Map pipelineParams) {
    pipeline {
        agent any
        environment {
            PLAYFAB_SECRET     = credentials('playfab-test-secret-key')
            PLAYFAB_TITLE_ID = credentials('playfab-test-title-id')
        }
        tools {
            go '1.18'
        }
        stages {
            stage('Installing Dependencies') {
                steps {
                    sh 'go get -u -d ./...'
                    sh 'cd $GOROOT/bin'
                    sh 'ls -al'
                }
            }
            stage('Testing') {
                stages {
                    stage('Dependency Check') {
                        when {
                            changeRequest()
                        }
                        steps {
                            dependencyCheck additionalArguments: '', odcInstallation: '8.0.1', stopBuild: true
                            dependencyCheckPublisher failedTotalCritical: 1, failedTotalHigh: 1, unstableTotalLow: 10, unstableTotalMedium: 5
                        }
                    }
                    stage('Code Coverage') {
                        steps {
                            sh 'go install github.com/axw/gocov/gocov@latest'
                            sh 'go install github.com/AlekSi/gocov-xml@latest'
                            sh '$GOROOT/bin/gocov test ./... | $GOROOT/bin/gocov-xml > coverage.xml'

                            publishCoverage adapters: [cobertura('coverage.xml')]
                        }
                    }
                    stage('Unit Tests') {
                        steps {
                            sh 'go install github.com/jstemmer/go-junit-report/v2@latest'
                            sh 'go test -v 2>&1 ./... | $GOROOT/bin/go-junit-report -set-exit-code > report.xml'

                            junit testResults: 'report.xml', skipPublishingChecks: false
                        }
                    }
                }
            }
            stage('Build') {
                when {
                    expression {
                        pipelineParams.deploy == true
                    }
                }
                steps {
                    echo 'building'
                }
            }
        }
    }
}
