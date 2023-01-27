#!/usr/bin/env groovy

def call() {
    parallel {
        stage('Linting') {
            steps {
                bat 'yarn --production=false'
                bat 'yarn lint'
            }
        }
        stage('Unit Tests') {
            steps {
                bat 'yarn --production=false'
                bat 'yarn test'
            }
        }
        stage('Dependency Check') {
            steps {
                bat 'yarn --production=false'

                dependencyCheck additionalArguments: '', odcInstallation: '8.0.1', stopBuild: true
                dependencyCheckPublisher failedTotalCritical: 1, failedTotalHigh: 1, unstableTotalLow: 10, unstableTotalMedium: 5
            }
        }
    }
}

