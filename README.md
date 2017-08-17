# wpengine-jenkins-shared-libraries

Deploying your WPEnigne site on every change should be a smooth process. With this pipeline you can deploy to your main repo and let Jenkins take care of deploying it to WPEnigne.

## Deployment Strategy

This pipeline makes the assumption that you're using the staging feature in WPEngine. Since WPEngine creates a separate repository for production and staging we need a way to differentiate the two. This setup assumes master is deployed to production and stage is deployed to staging. The very first time you run this job it will add the remote origin but not push changes. All consecutive runs will make sure the remote is there and push changes.

Branch | Env
-------|-----------
master | production
stage  | staging

## Prerequisites

If you're new to Jenkins pipelines you should go read the [documentation](https://jenkins.io/doc/book/pipeline/) before proceeding to get a sense for what to expect using this code. The rest of the setup process will assume you have basic knowledge of Jenkins or CI/CD jobs in general.

OS
  - git

Jenkins
  - Version: > 2.7.3 - tested on (2.19.4 LTS)
  
Plugins
  - slack
  - Pipeline (ID: workflow-aggregator)
  - git
  - timestamper
  - credentials

Scripts Approval
- When the job runs the first time you will need to work through allowing certain functions to execute in the groovy sandbox. This is normal as not all high use groovy functions are in the default safelist but more are added all the time.

### Manage with Puppet
The following modules work great to manage a Jenkins instance.

- puppetlabs/apache
- rtyler/jenkins

## Jenkinsfile

``` groovy
wpengine {
  WPENGINE_PROD_ENV = 'git@git.wpengine.com:production/install-name.git'
  WPENGINE_STAGE_ENV = 'git@git.wpengine.com:staging/install-name.git'
  REPO_URL = 'https://github.com/username/repo-name.git'
  REPO_CRED_ID = 'git-login-id'
  SLACK_CHANNEL = '#deploys'
  DEBUG = 'true'
}
```

### Required Parameters

- WPENGINE_PROD_ENV: SSH URL given by WPEngine for the production repo [String]
- WPENGINE_STAGE_ENV: SSH URL given by WPEngine for the staging repo [String]
- REPO_URL: This is your main repo URL on Github, Bitbucket, Gitlab, etc. [String]
- REPO_CRED_ID: This is the credentialsId set in the Jenkins credentials plugin for data authentication. [String]

### Optional Parameters

- SLACK_CHANNEL: Specify the Slack channel to use for notifications. Default: #deploys
- DEBUG: Turn off Slack notifications and turn on more console output. Default: false

## [Changelog](CHANGELOG.md)

## [MIT License](LICENSE)

