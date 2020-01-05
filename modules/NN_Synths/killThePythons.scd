100.do{|i|
	NetAddr("127.0.0.1", 5000+i).do{|i|i.sendMsg('/close')};
}