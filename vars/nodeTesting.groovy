#!/usr/bin/env groovy

def call() {
    parallel(
        Linting: {
            stage('Linting') {
                bat 'yarn --production=false'
                bat 'yarn lint'
            }
        }
        DependencyCheck: {
            stage('Dependency Check') {
                bat 'yarn --production=false'
                dependencyCheck additionalArguments: '', odcInstallation: '8.0.1', stopBuild: true
                dependencyCheckPublisher failedTotalCritical: 1, failedTotalHigh: 1, unstableTotalLow: 10, unstableTotalMedium: 5
            }
        }
        UnitTests: {
            stage('Unit Tests') {
                bat 'yarn --production=false'
                bat 'yarn test'
            }
        }
    )
}
