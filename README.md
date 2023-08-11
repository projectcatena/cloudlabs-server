# CloudLabs Server

This repository contains the source code for the backend system of the CloudLabs project.
The backend system of the project is based on the Spring Boot framework, developed using
Java.

## How to Deploy
You may easily deploy a local instance of the project using docker/podman.
```shell
# Build the docker image (include --progress=plain --no-cache to observe stdout)
docker build  -t cloudlabs/server .

# Run guacd
docker run --name some-guacd -d -p 4822:4822 guacamole/guacd

# Run the image on default port (or add --server.port=9000 for custom port)
docker run -p 8080:8080 cloudlabs/server 
```

## How to Contribute
1. Clone the repository.
```shell
git clone -b staging git@link_of_repo
```
2. Create and switch to a new branch
```shell
git switch -c your-new-branch-name
```
3. Commit changes after development
```shell
git commit -m "Describe your changes"
```
4. Push to remote repository
```shell
git push -u origin your-branch-name
```