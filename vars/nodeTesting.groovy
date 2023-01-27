#!/usr/bin/env groovy

def call(Closure closure) {
    parallel({
        Linting: {
            node("test && san-jose") {
stage('Linting') {
        //    steps {
                bat 'yarn --production=false'
                bat 'yarn lint'
                closure()
           // }
        }
            }
        } 
        // stage('Unit Tests') {
        //     steps {
        //         bat 'yarn --production=false'
        //         bat 'yarn test'
        //     }
        // }
        // stage('Dependency Check') {
        //     steps {
        //         bat 'yarn --production=false'

        //         dependencyCheck additionalArguments: '', odcInstallation: '8.0.1', stopBuild: true
        //         dependencyCheckPublisher failedTotalCritical: 1, failedTotalHigh: 1, unstableTotalLow: 10, unstableTotalMedium: 5
        //     }
        // }
    })
}

// def call(Closure closure) {
//   parallel({
//     SanJose: {
//       node("test && san-jose") {
//         stage('SanJose') {
//           closure()
//         }
//       } 
//     },
//     Dallas: {
//       node('test dallas') {
//         stage('Dallas') {
//           closure()
//         }
//       } 
//     } 
//   } 
// }