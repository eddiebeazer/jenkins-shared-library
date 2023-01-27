#!/usr/bin/env groovy

def call() {
    parallel(
        'Coverage': {
            stage('Coverage') {
                echo 'Getting modules'
                bat 'go get -u -d ./...'

                echo 'Code Coverage'
                bat 'gocov test ./... | gocov-xml > coverage.xml'

                publishCoverage adapters: [cobertura('coverage.xml')]
            }
        },
        'Dependency Check': {
            stage('Dependency Check') {
                echo 'Getting modules'
                bat 'go get -u -d ./...'

                dependencyCheck additionalArguments: '', odcInstallation: '8.0.1', stopBuild: true
                dependencyCheckPublisher unstableTotalCritical: 1, unstableTotalHigh: 1, unstableTotalLow: 10, unstableTotalMedium: 5
            }
        },
        'Unit Tests': {
            stage('Unit Tests') {
                echo 'Getting modules'
                bat 'go get -u -d ./...'

                echo 'JUnit Report'
                bat 'go test -v 2>&1 ./... | go-junit-report -set-exit-code > report.xml'

                junit testResults: 'report.xml', skipPublishingChecks: false
            }
        }
    )
}
