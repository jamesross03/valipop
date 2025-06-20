---
layout: default
title: ValiPop Results
markdown: kramdown
---

# ValiPop Results

## Validation Results

Running ValiPop with [`output_tables=true`](configuration/config-reference.md#output_tables) will enable the validation phase of ValiPop. This will analyse the simulated target population to determine how similar it is to given input distributions. The result of the validation is written to the terminal output as the `Validation score`. The lower the score, the more similar the population is to the given input distributions, which is desired. 0 is the best achievable score. 

The following text shows sample terminal output from running ValiPop with Validation:

```
Running simulation with /app/src/test/resources/valipop/config/config-1.txt
Writing contingency tables
Writing records
2025/03/26 15:03:54.292 :: Generating birth records
Elapsed time: 00:00:00
2025/03/26 15:03:54.332 :: Generating death records
Elapsed time: 00:00:00
2025/03/26 15:03:54.367 :: Generating marriage records
Elapsed time: 00:00:00
Writing graph
Running validation with command: Rscript /app/results/test/2025-03-26T14-26-12-324/analysis.R /app/results/test/2025-03-26T14-26-12-324 50
Warning message:
In value[[3L]](cond) : Population size too small for partnering analysis
Validation score: 0.0 (good)
```

Notably, there may be warning messages, like in the terminal output above, that say the population is too small for some types of analysis. This means that some types of analysis may not be included in the validation score due to the lack of data to draw a meaningful conclusion. Generally population sizes of 10,000 and above are enough for all types of analysis.

## Simulation Results

All simulation results of running ValiPop are written to a single directory. The directory is saved in the following path structure

```
<results_save_location>/<run_purpose>/<datetime>/
```

[`results_save_location`](configuration/config-reference.md#results_save_location) and [`run_purpose`](configuration/config-reference.md#run_purpose) can be specified in the [config file](configuration/index.md) and `datetime` represents the datetime when ValiPop was executed in the form  `yyyy-mm-ddThh-mm-ss-sss`.

ValiPop will create the directory structure for the results if it does not exist already.

### Result Structure

The directory structure of the results of running ValiPop looks like the following:

```
<results_path>/<run_purpose>/<datetime>/
├───analysis.R
├───detailed-results-<datetime>.txt
│
├───dump/
│   └───order.csv
│
├───graphs/
│   └───graph.png
│
├───log/
│   └───trace.txt
│
├───records/
│   ├───birth_records.csv
│   ├───death_records.csv
│   └───marriage_records.csv
│
└───tables/
    ├───death-CT.csv
    ├───mb-CT.csv
    ├───ob-CT.csv
    ├───part-CT.csv
    └───sep-CT.csv
```

<dl>

<dt>
<a name="analysisr">
<code>analysis.R</code>
</a>
</dt>

<dd markdown="1">
This file is the analysis script executed to validate the simulated population with the given statistics.
</dd>

<dt>
<a name="detailed-results-datetimetxt">
<code>detailed-results-&lt;datetime&gt;.txt</code>
</a>
</dt>

<dd markdown="1">
This file is generated once the model and analysis has completed. It provides additional statistics on the simulated model such as fertility and death rates, number of remarriages, population sizes, and average children per marriage.
</dd>

<dt>
<a name="dump">
<code>dump/</code>
</a>
</dt>

<dd markdown="1">
The dump directory contains bulk information used for debugging.
</dd>

<dt>
<a name="graphs">
<code>graphs/</code>
</a>
</dt>

<dd markdown="1">
The graph directory contains any graphs generated once the model and analysis have completed. The type of graph generated can be specified in the configuration.
</dd>


<dt>
<a name="log">
<code>log/</code>
</a>
</dt>

<dd markdown="1">
The log directory contains files which gives more details about the model simulation than in standard output
</dd>

<dt>
<a name="records">
<code>records/</code>
</a>
</dt>

<dd markdown="1">
The records directory contains any records generated once the model and analysis have completed. The record format generated can be specified in the configuration. Generally only birth, death, and marriage records are recorded among the generated population.
</dd>

<dt>
<a name="tables">
<code>tables/</code>
</a>
</dt>

<dd markdown="1">
The tables directory contains contingency tables on birth, death, partnership, and separation. They are used by the analysis to validate the simulated population with the given statistics.
</dd>

</dl>
