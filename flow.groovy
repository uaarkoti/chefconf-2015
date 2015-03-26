node('master') {

  sh 'rm -rf chefconf-2015'
  stage "Dev"
  dir('chefconf-2015') {
    ROOT=pwd()
    git 'https://github.com/uaarkoti/chefconf-2015.git'

    dir('chef-ci-tools') {
      git 'https://github.com/woohgit/chef-ci-tools.git'
    }

    sh './chef-ci-tools/bin/chef-foodcritic-publisher.sh -X spec -f any -t "~FC003"'
    archive 'junit_reports/foodcritic-*.xml'
   
    stage "QA"
    sh 'rm -rf rspec_results'
    sh 'mkdir rspec_results'
    sh 'for cbname in `find cookbooks -maxdepth 1 -mindepth 1 -type d` ; do rspec $cbname --format RspecJunitFormatter --out rspec_results/${cbname}-results.xml; done;'

    // Archive RSpec results
    archive 'rspec_results/*/*.xml'

    dir('cookbooks') {
      sh 'git submodule init'
      sh 'git submodule update'
    }

    stage "Integration"
    dir('tests/webserver') {

      sh 'vagrant destroy -f'
      sh 'vagrant up'

      //sh 'OPTIONS=`vagrant ssh-config | awk -v ORS=\' \' \'{print "-o " $1 "=" $2}\'`'
      sh 'vagrant ssh-config | awk -v ORS=\' \' \'{print "-o " $1 "=" $2}\' > result'
      def OPTIONS = readFile('result')
      echo "${OPTIONS}"

      // get stactrace
      sh "ssh ${OPTIONS} vagrant@127.0.0.1 \"sudo chmod -R a+r /var/chef/\""

      echo "copy strace out.. if any... to ${ROOT}"
      //sh "scp ${OPTIONS} vagrant@127.0.0.1:/var/chef/cache/chef-stacktrace.out ${ROOT}/chef-stacktrace.out"

      echo 'copy chef-solo-ci-reports...'
      sh 'scp -r ' + OPTIONS + ' vagrant@$127.0.0.1:/tmp/chef-solo-ci-reports ' + ROOT + '/'

      sh 'vagrant destroy -f'
    }
  }
}
