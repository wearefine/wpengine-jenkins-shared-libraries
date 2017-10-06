#!/usr/bin/env groovy

def call(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  if (!config.DEBUG) {
    config.DEBUG = 'false'
  }
  if (!config.SLACK_CHANNEL) {
    config.SLACK_CHANNEL = '#deploys'
  }
  if (!config.WPENGINE_PROD_ENV) {
    error 'WPENGINE_PROD_ENV is a required field.'
  }
  if (!config.WPENGINE_STAGE_ENV) {
    error 'WPENGINE_STAGE_ENV is a required field.'
  }
  if (!config.REPO_URL) {
    error 'REPO_URL is a required field.'
  }
  if (!config.REPO_CRED_ID) {
    error 'REPO_CRED_ID is a required field.'
  }

  node {
    timestamps {
      if (config.DEBUG == 'false') {
        notifySlack(config.SLACK_CHANNEL)
      }

      if (config.DEBUG == 'true') {
        echo '***********************************'
        echo "CHECKOUT BRANCH: ${env.BRANCH_NAME}"
        echo '***********************************'
      }
      
      try {
        stage('Checkout') {
          git branch: env.BRANCH_NAME, credentialsId: config.REPO_CRED_ID, url: config.REPO_URL
          currentBuild.result = 'SUCCESS'
        }
      } catch(Exception e) {
        currentBuild.result = 'FAILURE'
        if (config.DEBUG == 'false') {
          notifySlack(config.SLACK_CHANNEL)
        }
        emailext attachLog: true, body: "${env.JOB_NAME} - ${env.BUILD_DISPLAY_NAME} *${currentBuild.result}* - ${env.BUILD_URL}", compressLog: true, subject: "${env.JOB_NAME} - *${currentBuild.result}*", recipientProviders: [[$class: 'DevelopersRecipientProvider']]
        throw e
      }

      try {
        stage('Deploy'){
          milestone label: 'Deploy'
          if(env.BRANCH_NAME == 'master'){
            sh "git remote add production ${config.WPENGINE_PROD_ENV} || git push -f production master"
          }
          else if(env.BRANCH_NAME == 'stage') {
            sh "git remote add staging ${config.WPENGINE_STAGE_ENV} || git push -f staging stage"
          }
          currentBuild.result = 'SUCCESS'
        }
      } catch(Exception e) {
        currentBuild.result = 'FAILURE'
        if (config.DEBUG == 'false') {
          notifySlack(config.SLACK_CHANNEL)
        }
        emailext attachLog: true, body: "${env.JOB_NAME} - ${env.BUILD_DISPLAY_NAME} *${currentBuild.result}* - ${env.BUILD_URL}", compressLog: true, subject: "${env.JOB_NAME} - *${currentBuild.result}*", recipientProviders: [[$class: 'DevelopersRecipientProvider']]
        throw e
      }

    } // timestamps
    if (config.DEBUG == 'false') {
      notifySlack(config.SLACK_CHANNEL)
    }
    emailext attachLog: true, body: "${env.JOB_NAME} - ${env.BUILD_DISPLAY_NAME} *${currentBuild.result}* - ${env.BUILD_URL}", compressLog: true, subject: "${env.JOB_NAME} - *${currentBuild.result}*", recipientProviders: [[$class: 'DevelopersRecipientProvider']]
  } //node
}
