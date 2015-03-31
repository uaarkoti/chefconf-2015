# ChefConf 2015 - Jenkins Workshop

Testing infrastructure as code

## Tools

[Jenkins Enterprise](http://nectar-downloads.cloudbees.com/jenkins-enterprise/1.580/)
- Install
  - Install JDK 7 or higher
  - Download latest jenkins enterprise
  - Start jenkins : 
    - java -jar jenkins.war 
    - service jenkins start
  - Register to get an eval license

[Chef](https://downloads.chef.io/chef-dk/)
```
curl -L http://www.getchef.com/chef/install.sh | sudo bash -s -- -P chefdk
```

## Overview of testing levels

Usually there are at least 3 levels of testing

1. Static code analysis and lint checking
2. Unit testing
3. Integration testing

### Lint

We're using [foodcritic](https://foodcritic.io) for static code analysis. Foodcritic is a helpful lint tool you can use to check your Chef cookbooks for common problems.

It comes with 47 built-in rules that identify problems ranging from simple style inconsistencies to difficult to diagnose issues that will hurt in production.

Here is a way to run foodcritic and publish the results to jenkins
```bash
if [[ ! -d chef-ci-tools ]]; then
  git clone https://github.com/woohgit/chef-ci-tools.git
fi

cd ${WORKSPACE}

bash chef-ci-tools/bin/chef-foodcritic-publisher.sh -X spec -f any -t "~FC003"
```

### Unit

We're using [ChefSpec](https://docs.chef.io/chefspec.html) for unit testing.

Use ChefSpec to simulate the convergence of resources on a node:

- Run the chef-client on your local machine
- Use chef-zero or chef-solo
- Is an extension of RSpec, a behavior-driven development (BDD) framework for Ruby
- Is the fastest way to test resources

Here is a way to run ChefSpec and publish the results to jenkins. The assumption is that you have several cookbooks you are testing together (not a single cookbook).
```bash
rm -rf rspec_results
mkdir rspec_results

for cbname in `find cookbooks -maxdepth 1 -mindepth 1 -type d`;
do
  rspec $cbname --format RspecJunitFormatter --out rspec_results/${cbname}-results.xml
done
```

### Integration

We're using [Minitest](https://github.com/seattlerb/minitest) for integration testing.

We're spinning up a Vagrant virtual instance. Run chef-solo on it and analyzing the results.

Why using integration tests if we have unit tests? Well to make sure it does what it should do in a clean environment each time.

Here is a way to run minitest and publish the results to jenkins
```bash
cd tests/webserver

vagrant destroy -f
vagrant up

OPTIONS=`vagrant ssh-config | awk -v ORS=' ' '{print "-o " $1 "=" $2}'`

ssh ${OPTIONS} vagrant@127.0.0.1 "sudo chmod -R a+r /var/chef/"

echo "copy strace out.. if any..."
scp ${OPTIONS} vagrant@127.0.0.1:/var/chef/cache/chef-stacktrace.out ${WORKSPACE}/chef-stacktrace.out

echo "copy chef-solo-ci-reports..."
scp -r ${OPTIONS} vagrant@$127.0.0.1:/tmp/chef-solo-ci-reports ${WORKSPACE}/

vagrant destroy -f
```

## Requirements

### On the host machine

- ChefDK
   - chef shell-init bash/zsh
- Ruby 2.0
	- If ruby/gem binaries point to older version, re-link them to 2.0 versions
	- Install ruby2.0-dev package as well - ohai depends on ruby2.0 >
	- Install ruby1.9.1-dev - vagrant-chef-zero depends on it
	- gem install rspec chefspec rspec_junit_formatter
- ChefSpec
- Jenkins
- VirtualBox
- Vagrant and plugins
	- vagrant-berkshelf
	- vagrant-chef-zero
	- vagrant-login
	- vagrant-omnibus
	- vagrant-share
	- vagrant-triggers
- Foodcritic
- Ruby-Dev
- Perl

## Jenkins job installation

First, start up your jenkins and create the jobs located in the `jenkins_jobs` directory.

```bash
$ wget --no-check-certificate http://localhost:8080/jnlpJars/jenkins-cli.jar
$ java -jar ./jenkins-cli.jar -s http://localhost:8080/ create-job cookbooks-lint-check < jenkins_jobs/cookbooks-lint-check.config.xml
$ java -jar ./jenkins-cli.jar -s http://localhost:8080/ create-job cookbooks-unit-tests < jenkins_jobs/cookbooks-unit-tests.config.xml
$ java -jar ./jenkins-cli.jar -s http://localhost:8080/ create-job cookbooks-integration-tests < jenkins_jobs/cookbooks-integration-tests.config.xml
```
