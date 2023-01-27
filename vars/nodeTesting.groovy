#!/usr/bin/env groovy

def call() {
    parallel(
        Linting: {
            stage('Linting') {
                nodejs(nodeJSInstallationName: '16') {
                    bat 'yarn lint'
                }
            }
        },
        'Dependency Check': {
            stage('Dependency Check') {
                nodejs(nodeJSInstallationName: '16') {
                    bat 'yarn'
                    bat 'yarn npm audit'
                    dependencyCheck additionalArguments: '', odcInstallation: '8.0.1', stopBuild: true
                    dependencyCheckPublisher failedTotalCritical: 1, failedTotalHigh: 1, unstableTotalLow: 10, unstableTotalMedium: 5
                }
            }
        },
        'Unit Tests': {
            stage('Unit Tests') {
                nodejs(nodeJSInstallationName: '16') {
                    bat 'yarn test'
                }
            }
        }
    )
}
