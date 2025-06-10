---
layout: default
title: Running with Java 
markdown: kramdown
---

# ValiPop with Java Guide

This guide will walk you through installing and running ValiPop in Java with some sample configurations. This guide will also demonstrate how to interpret the results of running ValiPop and how to configure it further.

## 1. Prerequisites

Before we begin, you will need to have installed the following tools on your system for this walkthrough:

- [Git](https://git-scm.com/)
- [Java 21 or greater](https://www.oracle.com/uk/java/)
- [R 4.4.2 or greater](https://cran.r-project.org/)

Once R is installed, you will also need the [geepack R package](https://cran.r-project.org/web/packages/geepack/index.html). You can install the package with the following command:

```shell
# In a terminal (Windows/MacOs/Linux)

# Install the geepack R package on your system
R -e "install.packages('geepack', repos = c(CRAN = 'https://cloud.r-project.org'))"
```

## 2. Installation

To run ValiPop with Java, you only need the ValiPop JAR file. However we will be running ValiPop with some sample configuration which needs to be installed separately.

### 2.1. Installing the configuration

We will use the config and input files from the [ValiPop repository](https://github.com/stacs-srg/population-model). To install the repository, run the following command

```shell
# In a terminal (Windows/macOS/Linux)

git clone https://github.com/stacs-srg/population-model.git
```

Within the repository, we will use the following config file

`src/main/resources/valipop/config/scot/config.txt`

### 2.2. Installing the JAR file

You can install the latest ValiPop JAR file from [the releases page](https://github.com/stacs-srg/population-model/releases).

For the following steps, the JAR file should be installed in the root of the ValiPop repository.

To verify that the JAR file is working, navigate to the directory it is in and run the following command

```shell
# In a terminal (Windows/MacOs/Linux)

java -jar valipop.jar
```

which should print the following message

```txt
No config file given as 1st arg
Incorrect arguments given
```

### 3. Execution

To run ValiPop with the configuration file, run the following command

```shell
# In a terminal (Windows/MacOs/Linux)

java -jar valipop.jar src/main/resources/valipop/config/scot/config.txt
```

By default, this will run ValiPop will a starting population size of 1000 between the years 1855 to 1973. This will usually take under 5 minutes to run, and should eventually print something like the following:

```
Running simulation with src/main/resources/valipop/config/scot/config.txt
Writing contingency tables
Writing records
2025/03/26 15:03:54.292 :: Generating birth records
Elapsed time: 00:00:00
2025/03/26 15:03:54.332 :: Generating death records
Elapsed time: 00:00:00
2025/03/26 15:03:54.367 :: Generating marriage records
Elapsed time: 00:00:00
Writing graph
Running validation with command: Rscript results/example/2025-03-24T10-50-39-702/analysis.R results/example/2025-03-24T10-50-39-702 50
Warning message:
In value[[3L]](cond) : Population size too small for partnering analysis
Validation score: 0.0 (good)
```

In the same directory as the ValiPop directory, there should also now exist a `results/` directory with the generated population records. This specific run should be located in a directory like

```
results/example/2025-03-24T10-50-39-702/
```

but the datetime may be different.

[Read more about ValiPop results.](../results.md)

### 3.1. Customisation

If you navigate to `src/main/resources/valipop/config/scot/config.txt` and open in it in a text editor, it should look like the following

```
#Properties File

var_data_files = src/main/resources/valipop/inputs/synthetic-scotland
tS = 1687-01-01
t0 = 1855-01-01
tE = 1973-01-01
t0_pop_size = 1000

output_record_format = TD
output_tables = true

results_save_location = results
run_purpose = example
```

You can make the following changes to the configuration file to alter ValiPop's behaviour

- Change the starting population size with [`t0_pop_size`](../configuration/config-reference.md#t0_pop_size).
- Change the start and end data (as written in the record files) with [`t0`](../configuration/config-reference.md#t0) and [`tE`](../configuration/config-reference.md#tE).
- Change input distributions used with [`var_data_files`](../configuration/config-reference.md#var_data_files). [Read more about input distributions.](../configuration/input-reference.md).
- Change output record format with [`output_record_format`](../configuration/config-reference.md#output_record_format).
- To disable the analysis portion of valiPop, set [`output_tables`](../configuration/config-reference.md#output_tables) to `false`.
- Change the location of the result directory with [`results_save_location`](../configuration/config-reference.md#results_save_location).
- Change the name of the run within the result directory with [`run_purpose`](../configuration/config-reference.md#run_purpose).

You may then save the changes and rerun the following command for different results

```sh
# In a terminal (Windows/MacOs/Linux)

java -jar valipop.jar src/main/resources/valipop/config/scot/config.txt
```

[Read more about the configuration options.](../configuration/config-reference.md)

