---
layout: default
title: Running with Docker 
markdown: kramdown
---

# Running ValiPop with Docker

## Dependencies

You will need to have either [docker](https://www.docker.com/), [podman](https://podman.io/) or any other container management tool installed on your system.

## Installing the image

To run ValiPop with docker, you first you need to run the following command to pull the Docker image

```sh
docker pull ghcr.io/stacs-srg/valipop:master
```

## Running ValiPop

To run ValiPop, you need to specify which directories you want the ValiPop container to access while running. This can be done with the following command template

```sh
docker run -v <absolute-path-on-your-computer>:<absolute-path-in-container> ghcr.io/stacs-srg/valipop:master <valipop-args>
```

`absolute-path-on-your-computer` is the absolute path to a directory you want to give access to.

`absolute-path-in-container` is the absolute path the directory should be bound to in the container. The container is running ValiPop in `/app/`

`valipop-args` are the remaining arguments you want to pass directly to ValiPop

The following are examples of running ValiPop with docker:

```sh
docker run -v /home/user/configs:/app/configs ghcr.io/stacs-srg/valipop:master configs/config.txt
```

```sh
docker run -v C:\\Users\\user\\Documents\\configs:/app/configs -v C:\\Users\\user\\Documents\\inputs:/app/inputs ghcr.io/stacs-srg/valipop:master configs/config.txt
```
