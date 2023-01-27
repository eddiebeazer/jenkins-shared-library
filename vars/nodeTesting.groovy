#!/usr/bin/env groovy

def call() {
    parallel(
        Linting: {
            stage('Linting') {
               // agent any
                nodejs(nodeJSInstallationName: '16') {
                    echo pwd()
                    bat 'yarn --production=false'
                    bat 'yarn lint'
                }
            }
        },
        'Dependency Check': {
            stage('Dependency Check') {
                //agent any
                nodejs(nodeJSInstallationName: '16') {
                    echo pwd()
                    bat 'yarn --production=false'
                    dependencyCheck additionalArguments: '', odcInstallation: '8.0.1', stopBuild: true
                    dependencyCheckPublisher failedTotalCritical: 1, failedTotalHigh: 1, unstableTotalLow: 10, unstableTotalMedium: 5
                }
            }
        },
        'Unit Tests': {
            stage('Unit Tests') {
               // agent any
                nodejs(nodeJSInstallationName: '16') {
                    echo pwd()
                    bat 'yarn --production=false'
                    bat 'yarn test'
                }
            }
        }
    )
}
