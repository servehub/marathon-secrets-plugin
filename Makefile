.PHONY: build release

build:
	gradle build

release: build
	cp ./libs/*.jar build/libs/
	cd ./build/libs/ && tar -czf marathon-secrets-plugin.tar.gz *.jar

	git tag v${version}
	git push && git push --tags

	-github-release release \
		--user servehub \
		--repo marathon-secrets-plugin \
		--tag v${version}

	github-release upload \
		--user servehub \
		--repo marathon-secrets-plugin \
		--tag v${version} \
		--name "marathon-secrets-plugin-${version}.tar.gz" \
		--file "build/libs/marathon-secrets-plugin.tar.gz" \
		--replace
