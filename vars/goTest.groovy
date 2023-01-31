#!/usr/bin/env groovy

def call() {
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
            sh 'gocov test ./... | gocov-xml > coverage.xml'

            publishCoverage adapters: [cobertura('coverage.xml')]
        }
    }
    stage('Unit Tests') {
        steps {
            sh 'go test -v 2>&1 ./... | go-junit-report -set-exit-code > report.xml'

            junit testResults: 'report.xml', skipPublishingChecks: false
        }
    }
}
