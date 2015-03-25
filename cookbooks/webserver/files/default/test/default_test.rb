
include ::MiniTest::Chef::Assertions
include ::MiniTest::Chef::Context
include ::MiniTest::Chef::Resources

class TestNginx < MiniTest::Chef::Spec
  it "listens on port 80" do
      TCPSocket.open("localhost", 80)
  end
end