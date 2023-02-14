#!/usr/bin/env groovy

def call(Map pipelineParams) {
    pipeline {
        agent {
            label 'Linux'
        }
        environment {
            PLAYFAB_SECRET     = credentials('playfab-test-secret-key')
            PLAYFAB_TITLE_ID = credentials('playfab-test-title-id')
        }
        tools {
            go pipelineParams.goVersion
        }
        options {
            disableConcurrentBuilds()
            parallelsAlwaysFailFast()
            timeout(time: 15, unit: 'MINUTES')
        }
        stages {
            stage('Installing Dependencies') {
                steps {
                    sh 'go get -u -d ./...'
                    sh 'go install github.com/jstemmer/go-junit-report/v2@latest'
                    sh 'go install github.com/axw/gocov/gocov@latest'
                    sh 'go install github.com/AlekSi/gocov-xml@latest'
                }
            }
            stage('Testing') {
                stages {
                    stage('Dependency Check') {
                        when {
                            expression {
                                pipelineParams.dependencyCheck == true
                            }
                        }
                        steps {
                            dependencyCheck additionalArguments: '', odcInstallation: '8.0.1', stopBuild: true
                            dependencyCheckPublisher failedTotalCritical: 1, failedTotalHigh: 1, unstableTotalLow: 10, unstableTotalMedium: 5
                        }
                    }
                    stage('Code Coverage') {
                        when {
                            expression {
                                pipelineParams.codeCoverage == true
                            }
                        }
                        steps {
                            sh '$HOME/go/bin/gocov test ./... | $HOME/go/bin/gocov-xml > coverage.xml'
                            publishCoverage adapters: [cobertura('coverage.xml')]
                        }
                    }
                    stage('Unit Tests') {
                        when {
                            expression {
                                pipelineParams.unitTests == true
                            }
                        }
                        steps {
                            sh 'go test -v 2>&1 ./... | $HOME/go/bin/go-junit-report -set-exit-code > report.xml'
                            junit testResults: 'report.xml', skipPublishingChecks: false
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
                    echo 'building'
                }
            }
            stage('Deploy') {
                when {
                    expression {
                        pipelineParams.deploy == true
                    }
                }
                steps {
                    echo 'deploying'
                }
            }
        }
    }
}
