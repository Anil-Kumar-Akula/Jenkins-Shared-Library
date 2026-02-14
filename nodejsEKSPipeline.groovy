// call is the default function name
def call (Map configMap) {
    pipeline {
    agent {
       node {
          label 'AGENT-1'
       }
    }
    environment {
         COURSE = "jenkins"
         appVersion = ""
         ACC_ID = 193685726527
         PROJECT=  configMap.get("project") //refering form catalogue jenkins file
         COMPONENT= configMap.get("component")  //refering form catalogue jenkins file
    }
    options {
        timeout(time: 60, unit: 'MINUTES') 
        disableConcurrentBuilds()
    }
   
    // This is building stage
    stages {
       stage('Read Version')  {
         steps {
           script {
             def packageJSON = readJSON file: 'package.json'
             appVersion = packageJSON.version
             echo "app version : ${appVersion}"
           }
         }
       }
       // This is Testing stage
        stage('install dependencies') { 
          steps {
            script {
              sh """
                 npm install
                 
                 """
            }
           
          }
        }
        stage('Unit Test') { 
          steps {
            script {
              sh """
                 npm test
                 
                 """
            }
           
          }
        }
        // stage('Sonarqube scan') { 
        //   environment {
        //     def scannerHome = tool 'sonar-8.0'
        //   }
        //   steps {
        //     script {
        //       withSonarQubeEnv('sonar-server') {
        //         sh "${scannerHome}/bin/sonar-scanner"
        //       }
        //     }
        //   }
        // }
        // stage ('quality Gate') {
        //   steps {
        //     timeout(time: 1,unit: 'HOURS') {
        //       waitForQualityGate abortpipeline: true
        //     }
        //   }
        // }

        // stage('Dependabot Security Gate') {
        //     environment {
        //         GITHUB_OWNER = 'daws-86s'
        //         GITHUB_REPO  = 'catalogue'
        //         GITHUB_API   = 'https://api.github.com'
        //         GITHUB_TOKEN = credentials('GITHUB_TOKEN')
        //     }

        // //     steps {
        // //         script{
        // //             /* Use sh """ when you want to use Groovy variables inside the shell.
        // //             Use sh ''' when you want the script to be treated as pure shell. */
        // //             sh '''
        // //             echo "Fetching Dependabot alerts..."

        // //             response=$(curl -s \
        // //                 -H "Authorization: token ${GITHUB_TOKEN}" \
        // //                 -H "Accept: application/vnd.github+json" \
        // //                 "${GITHUB_API}/repos/${GITHUB_OWNER}/${GITHUB_REPO}/dependabot/alerts?per_page=100")

        // //             echo "${response}" > dependabot_alerts.json

        // //             high_critical_open_count=$(echo "${response}" | jq '[.[] 
        // //                 | select(
        // //                     .state == "open"
        // //                     and (.security_advisory.severity == "high"
        // //                         or .security_advisory.severity == "critical")
        // //                 )
        // //             ] | length')

        // //             echo "Open HIGH/CRITICAL Dependabot alerts: ${high_critical_open_count}"

        // //             if [ "${high_critical_open_count}" -gt 0 ]; then
        // //                 echo "❌ Blocking pipeline due to OPEN HIGH/CRITICAL Dependabot alerts"
        // //                 echo "Affected dependencies:"
        // //                 echo "$response" | jq '.[] 
        // //                 | select(.state=="open" 
        // //                 and (.security_advisory.severity=="high" 
        // //                 or .security_advisory.severity=="critical"))
        // //                 | {dependency: .dependency.package.name, severity: .security_advisory.severity, advisory: .security_advisory.summary}'
        // //                 exit 1
        // //             else
        // //                 echo "✅ No OPEN HIGH/CRITICAL Dependabot alerts found"
        // //             fi
        // //             '''
                    
        // //         }
        // //     }
        // // }

        stage ('build image') {
          steps {
            script {
              withAWS(region:'us-east-1', credentials:'aws-creds') {
                sh """
                aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${ACC_ID}.dkr.ecr.us-east-1.amazonaws.com
                docker build -t ${ACC_ID}.dkr.ecr.us-east-1.amazonaws.com/${PROJECT}/${COMPONENT}:${appVersion} .
                docker images
                docker push ${ACC_ID}.dkr.ecr.us-east-1.amazonaws.com/${PROJECT}/${COMPONENT}:${appVersion}
                """
              }
            }
          }
        }

        // stage('Deploy') {
        // //    input {
        // //         message "Should we continue?"
        // //         ok "Yes, we should."
        // //         submitter "alice,bob"
        // //         parameters {
        // //             string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
        // //         }
        // //     }

        //    steps {
        //      script {
        //         sh """
        //         echo "By using the Hybrid method deploying the pipeline"
        //         echo $COURSE
        //         """
        //      }
        //    }
        // }
 
    }
    post {
        always {
            echo "I always say Hello..!"
            cleanWs()
        }
        success {
             echo "i will run if successfull"
        }
        failure {
             echo "i will run if failure"
        }
    }
     
}
}