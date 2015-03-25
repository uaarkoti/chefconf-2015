#
# Cookbook Name:: webserver
# Recipe:: default
#
# Copyright 2015, CloudBees
#
# All rights reserved - Do Not Redistribute
#

execute 'apt-get update' do
	command 'apt-get update'
	action :run
end

package 'nginx' do
	action :install
end

template '/etc/nginx/nginx.conf' do
	source 'nginx.conf.erb'
	owner 'root'
	group 'root'
	mode '0644'
	variables( :hostname => 'www.cloudbees.com')
end

service 'nginx' do
	supports :status => true, :restart => true, :reload => true
	action [ :start, :enable ]
end
