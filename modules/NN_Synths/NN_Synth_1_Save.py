# Training for NN_Synth - @Sam Pluta
from numpy import loadtxt
from math import floor
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
	# parser.add_argument("--num",
	# 	type=int, default=0, help="The model number")
	parser.add_argument("--sendPort",
		type=int, default=0, help="The port number to send on")
	parser.add_argument("--sizeOfNN",
		type=int, default=16, help="Size of Neural Net")
	parser.add_argument("--sizeOfControl",
		type=int, default=4, help="Size of Control Set")
	args = parser.parse_args()

	# load the data set for one setting
	dataset = loadtxt(args.numbersFile, delimiter=",")

	totalSize = args.sizeOfNN+args.sizeOfControl

	# split into input (X) and output (Y) variables
	X = dataset[:,0:args.sizeOfNN]
	Y = dataset[:,args.sizeOfNN:totalSize]

	denseSize1 = floor(totalSize/4)
	denseSize2 = floor(totalSize/2)
	denseSize3 = floor(3*totalSize/4)

	# define the model
	model = Sequential()
	model.add(Dense(denseSize1, input_dim=4, activation='relu'))
	model.add(Dense(denseSize2, activation='relu'))
	model.add(Dense(denseSize3, activation='relu'))
	model.add(Dense(args.sizeOfNN, activation='sigmoid'))

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
	client.send_message("/trained", 1)
	#os._exit(1)
	#print("Saved model to disk")