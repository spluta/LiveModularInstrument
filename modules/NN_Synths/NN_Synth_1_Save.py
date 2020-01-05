# MLP for Pima Indians Dataset saved to single file
from numpy import loadtxt
from keras.models import Sequential
from keras.layers import Dense
import argparse
from pythonosc import udp_client
import os

if __name__ == "__main__":
	parser = argparse.ArgumentParser()
	parser.add_argument("--numbersFile",
		default="trainingFile0.csv", help="The file")
	parser.add_argument("--modelFile",
		default="crossModel0.h5", help="The file")
	parser.add_argument("--num",
		type=int, default=0, help="The model number")
	parser.add_argument("--sendPort",
		type=int, default=0, help="The port number to send on")
	args = parser.parse_args()

	# load the data set for one setting
	dataset = loadtxt(args.numbersFile, delimiter=",")

	# split into input (X) and output (Y) variables
	X = dataset[:,0:16]
	Y = dataset[:,16:20]

	# define the model
	model = Sequential()
	model.add(Dense(6, input_dim=4, activation='relu'))
	model.add(Dense(10, activation='relu'))
	model.add(Dense(13, activation='relu'))
	model.add(Dense(16, activation='sigmoid'))

	# compile model
	model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])
	# Fit the model
	model.fit(Y, X, epochs=2000, batch_size=10, verbose=0)
	# evaluate the model
	scores = model.evaluate(Y, X, verbose=0)
	#print("%s: %.2f%%" % (model.metrics_names[1], scores[1]*100))
	# save model and architecture to single file
	model.save(args.modelFile)
	#json_config = model.to_json()
	#with open('model_config.json', 'w') as json_file:
	#	json_file.write(json_config)
	#model.save_weights('path_to_my_weights.h5')
	#client = udp_client.SimpleUDPClient(args.ip, args.sendPort)
	client = udp_client.SimpleUDPClient("127.0.0.1", args.sendPort)
	client.send_message("/trained", args.num)
	#os._exit(1)
	#print("Saved model to disk")