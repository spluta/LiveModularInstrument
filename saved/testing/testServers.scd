a = Server.new("a", NetAddr("localhost", 57110), Server.local.options).boot;
a.quit;

b = Server.new("b", NetAddr("localhost", 57111), Server.local.options).boot;
b.quit;

c = Server.new("c", NetAddr("localhost", 57112), Server.local.options).boot;
c.quit;

d = Server.new("d", nil, Server.local.options).boot;
d.quit;

Server.killAll


s.boot;
s.quit;