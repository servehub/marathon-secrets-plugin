
default: build

build:
	sbt clean test assembly
