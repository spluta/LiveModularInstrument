(
f = File("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/99_NN_Prototype/tv_nn.csv", "w");

6.do{|i|
	f.write(("inGain"++i++", "++0++", "++4++", "++2++", "++0++"\n").asString);
};
f.write(("tvVol"++", "++0++", "++1++", "++2++", "++0++"\n").asString);
f.write(("msg"++", "++0++", "++1++", "++0++", "++0++"\n").asString);
6.do{|i|
	f.write(("lowFrq"++i++", "++60++", "++200++", "++1++", "++80++"\n").asString);
	f.write(("lowDB"++i++", "++25.neg++", "++25++", "++0++", "++0++"\n").asString);
	f.write(("midFrqA"++i++", "++800++", "++2000++", "++1++", "++1250++"\n").asString);
	f.write(("midDBA"++i++", "++25.neg++", "++25++", "++0++", "++0++"\n").asString);
	f.write(("midFrqB"++i++", "++2000++", "++5000++", "++1++", "++3250++"\n").asString);
	f.write(("midDBB"++i++", "++25.neg++", "++25++", "++0++", "++0++"\n").asString);
	f.write(("hiFrq"++i++", "++8000++", "++15000++", "++1++", "++12500++"\n").asString);
	f.write(("hiDB"++i++", "++25.neg++", "++25++", "++0++", "++0++"\n").asString);
};

f.close
)

f = CSVFileReader.read("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/99_NN_Prototype/tv_nn.csv")

f[0 