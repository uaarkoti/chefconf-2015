require_relative '../spec_helper'

describe 'webserver::default' do
  subject { ChefSpec::Runner.new.converge(described_recipe) }

  # Use an explicit subject
  let(:chef_run) { ChefSpec::Runner.new.converge(described_recipe) }

  it 'installs nginx' do
    expect(chef_run).to install_package('nginx')
  end

  it 'replaces nginx config file' do
    expect(chef_run).to render_file('/etc/nginx/nginx.conf')
  end
end
