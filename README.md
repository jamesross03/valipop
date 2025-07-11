# ValiPop

[![Build and test](https://github.com/stacs-srg/valipop/actions/workflows/push-jobs.yml/badge.svg)](https://github.com/stacs-srg/valipop/actions/workflows/push-jobs.yml)

_ValiPop_ is a micro-simulation model for generating synthetic genealogical populations
from a set of desired statistics, developed during [Tom Dalton](https://github.com/tomsdalton)'s
[PhD research](https://doi.org/10.17630/sta/247). _ValiPop_ verifies that the 
desired properties exist in the generated populations. It is reasonably scalable and 
customisable, and able to create populations for a wide range of purposes. The intended
purpose is to enable generation of multiple synthetic genealogical populations to evaluate and 
improve data linkage algorithms.

_ValiPop_'s micro-simulation model is written in Java. The supporting verification analysis 
and statistical code is written in R.

## Running ValiPop

### Via Java

To run ValiPop from the release JAR file, the following need to be installed:

- Java 21 or higher
- [R 4.4.2 or higher](https://cran.r-project.org/)
- R [geepack](https://cran.r-project.org/web/packages/geepack/index.html) package

To install _geepack_:

```shell
# Install the geepack R package.
R -e "install.packages('geepack', repos = c(CRAN = 'https://cloud.r-project.org'))"
```
To download and run ValiPop:

```shell
# Download the JAR file.
wget https://github.com/stacs-srg/valipop/releases/latest/download/valipop.jar

# Run the simulation.
java -jar valipop.jar <valipop-args>
```

[Learn more about running with Java](https://stacs-srg.github.io/valipop/usage/execution/java.html)


### Via Docker

To run ValiPop from a docker container, the following need to be installed:

- [Docker](https://www.docker.com/)

To run ValiPop:

```shell
# Pull the container.
docker pull ghcr.io/stacs-srg/valipop:main

# Run the container.
docker run ghcr.io/stacs-srg/valipop:main <valipop-args>
```

[Learn more about running with Docker](https://stacs-srg.github.io/valipop/usage/execution/docker.html)


## Building ValiPop from source

To build ValiPop, the following need to be installed:

- [Git](https://git-scm.com/)
- Java 21 or higher
- [Maven](https://maven.apache.org/)

To build ValiPop:

```shell
# Clone the repository.
# Add flag --depth 1 to reduce download size if you don't care about getting the whole repository history.
git clone https://github.com/stacs-srg/valipop

# Navigate to the repository directory.
cd valipop

# Install gedinline local dependency
mvn install:install-file \
  -Dfile=lib/gedinline-4.0.1.jar \
  -DgroupId=com.github.nigel-parker \
  -DartifactId=gedinline \
  -Dversion=4.0.1 \
  -Dpackaging=jar

# Install dependencies, compile, and package into JARs.
mvn clean package -Dmaven.test.skip

# The build should be created in 'target/', including the runnable JARs.
```
