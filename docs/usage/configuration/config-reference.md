---
layout: default
title: ValiPop Configuration Reference
markdown: kramdown
---

# ValiPop Configuration Reference

These are all the configuration options supported by ValiPop. Options suffixed with a '*' are required.

**Configuration Options**

- [Locations](#locations)
    - [`run_purpose`](#run_purpose)
    - [`results_save_location`](#results_save_location)
    - [`summary_results_save_location`](#summary_results_save_location)
    - [`var_data_files`*](#var_data_files)
    - [`project_location`](#project_location)
- [Dates and Periods](#dates-and-periods)
    - [`tS`*](#tS)
    - [`t0`*](#t0)
    - [`tE`*](#tE)
    - [`simulation_time_step`](#simulation_time_step)
    - [`min_birth_spacing`](#min_birth_spacing)
    - [`min_gestation_period`](#min_gestation_period)
    - [`input_width`](#input_width)
- [Simulation Factors](#simulation-factors)
    - [`t0_pop_size`*](#t0_pop_size)
    - [`set_up_br`](#set_up_br)
    - [`set_up_dr`](#set_up_dr)
    - [`recovery-factor`](#recovery-factor)
    - [`proportional_recovery_factor`](#proportional_recovery_factor)
- [Results](#results)
    - [`output_record_format`](#output_record_format)
    - [`output_graph_format`](#output_record_format)
    - [`output_table`](#output_table)
    - [`ct_tree_stepback`](#ct_tree_stepback)
    - [`ct_tree_precision`](#ct_tree_precision)
- [Miscellaneous](#miscellanious)
    - [`deterministic`](#deterministic)
    - [`seed`](#seed)
    - [`binomial_sampling`](#binomial_sampling)
    - [`over_sized_geography_factor`](#over_sized_geography_factor)

***

## Locations

<dl>


<dt>
<a name="run_purpose">
<code>run_purpose</code>
</a>
</dt>

<dd markdown="1">
A name used for a grouping of runs. Must be a valid name for files and directories. 

Results of a specific run are written to

`<results_save_location>/<run_purpose>/<timestamp>`

`timestamp` represents the datetime the runs was executed at in the form `yyyy-mm-ddThh-mm-ss-sss`.

Defaults to `default`
</dd>

<dt>
<a name="results_save_location">
<code>results_save_location</code>
</a>
</dt>

<dd markdown="1">
Path to the root results directory of runs.

Defaults to `results/`.
</dd>

<dt>
<a name="summary_results_save_location">
<code>summary_results_save_location</code>
</a>
</dt>

<dd markdown="1">
Path to the directory where summarisations of runs are written.

A summary is a CSV file where each row is represented as a run. Each row contains information about the run configuration and results.

There is a global summary file shared by all runs at 

```<summary_results_save_location>/global-results-summary.csv```


There is local summary file shared by all runs of the same run purpose at

 ```<summary_results_save_location>/<run_purpose>/<run_purpose>-results-summary.csv```

Defaults to `results/`.
</dd>

<dt>
<a name="var_data_files">
<code>var_data_files</code>
</a>
</dt>

<dd markdown="1">
Path to the input distribution directory.

This is required.
</dd>

<dt>
<a name="project_location">
<code>project_location</code>
</a>
</dt>

<dd markdown="1">
Path to the project directory.

Defaults to `.` (current directory).
</dd>

</dl>

## Dates and Periods

<dl>

<dt>
<a name="tS">
<code>tS</code>
</a>
</dt>

<dd markdown="1">
The start date of the initialisation phase, where an initial population is generated and simulated until `t0`. The period between `tS` and `t0` must be at least 150 years to allow enough time for the preliminary population to settle.

At `tS` an initial population is first spawned, of which its size is based on [`set_up_br`](#set_up_br) and [`set_up_dr`](#set_up_dr), and the duration from [`t0`](#t0). The population is then simulated regularly until [`t0`](#t0). 

This is required.
</dd>

<dt>
<a name="t0">
<code>t0</code>
</a>
</dt>

<dd markdown="1">
The start date of the main phase, where records of events occurring between `t0` until [`tE`](#tE) will be recorded.

This is required.
</dd>

<dt>
<a name="tE">
<code>tE</code>
</a>
</dt>

<dd markdown="1">
The end date of the main phase, and end of the simulation in total.

This is required.
</dd>

<dt>
<a name="simulation_time_step">
<code>simulation_time_step</code>
</a>
</dt>

<dd markdown="1">
The time interval used for each simulation step. This is a Java period string of the form `P<year>Y<month>M<day>D`.

Defaults to `P1Y` (1 year).
</dd>

<dt>
<a name="min_birth_spacing">
<code>min_birth_spacing</code>
</a>
</dt>

<dd markdown="1">
The minimum time interval parents must wait since their last child before having another child. This is a Java period string of the form `P<year>Y<month>M<day>D`.

Defaults to `P147D` (147 days).
</dd>

<dt>
<a name="min_gestation_period">
<code>min_gestation_period</code>
</a>
</dt>

<dd markdown="1">
The minimum time interval the child must have been conceived at before their birth. This is a Java period string of the form `P<year>Y<month>M<day>D`.

Defaults to `P147D` (147 days).
</dd>

<dt>
<a name="input_width">
<code>input_width</code>
</a>
</dt>

<dd markdown="1">
The time intervals for which the given input distributions are divided into between [`tS`](#tS) and [`tE`](#tE). Input distributions for the same properties over multiple different years are the separated into their respective time_intervals. This is a Java period string of the form `P<year>Y<month>M<day>D`.

Defaults to `P1Y` (1 year).
</dd>

</dl>

## Simulation Factors

<dl>

<dt>
<a name="t0_pop_size">
<code>t0_pop_size</code>
</a>
</dt>

<dd markdown="1">
The desired population size at [`t0`](#t0). The initialisation phase will aim to generate an initial population of this size from [`tS`](#tS) until [`t0`](#t0).

This is required.
</dd>


<dt>
<a name="set_up_br">
<code>set_up_br</code>
</a>
</dt>

<dd markdown="1">
The flat birth rate used for the initial population between [`tS`](#tS) and [`t0`](#t0). It represents the percentage increase of the population in one time step as a decimal. 

Defaults to `0.133`.
</dd>

<dt>
<a name="set_up_dr">
<code>set_up_dr</code>
</a>
</dt>

<dd markdown="1">
The flat death rate used for the initial population between [`tS`](#tS) and [`t0`](t0). It represents the percentage decrease of the population in one time step as a decimal. 

Defaults to `0.122`.
</dd>

<dt>
<a name="recovery_factor">
<code>recovery_factor</code>
</a>
</dt>

<dd markdown="1">
A multiplier determining how strongly the simulation should compensate for deviations from given one dimensional input distributions

Defaults to `1`.
</dd>

<dt>
<a name="proportional_recovery_factor">
<code>proportional_recovery_factor</code>
</a>
</dt>

<dd markdown="1">
A multiplier determining how strongly the simulation should compensate for deviations from given two dimensional input distributions

Defaults to `1`.
</dd>

</dl>

## Results

<dl>

<dt>
<a name="output_record_format">
<code>output_record_format</code>
</a>
</dt>

<dd markdown="1">
The output format of the target population records. Can be one of:

- `NONE`          : Does not generate.
- `TD`            : Custom record format created by Tom Dalton.
- `DS`            : Record format used by Digitising Scotland.
- `EG_SKYE`       : Subset of the `DS` format.
- `VIS_PROCESSING`: Simplified record format used by Digitising Scotland.

Defaults to `NONE`.
</dd>

<dt>
<a name="output_graph_format">
<code>output_graph_format</code>
</a>
</dt>

<dd markdown="1">
The output format of the target population graphic. Can be one of:

- `NONE`: Does not generate.
- `GRAPHVIZ`: a Graphviz `.dot` file to render a family tree graph
- `GEDCOM`: a GEDCOM family tree file
- `GEOJSON`: a Geojoson file showing the birth adresses of each person

Defaults to `None`.
</dd>

<dt>
<a name="output_table">
<code>output_table</code>
</a>
</dt>

<dd markdown="1">
When `true`, this creates contingency tables required by the population analysis.

When `false`, the contingency tables are not created and the population analysis is skipped.

A contingency table is a collection of expected and actual frequencies for events. Contingency tables for birth orders, multiple births, partnership, deaths, and separations are generated.

Defaults to `true`.
</dd>

<dt>
<a name="ct_tree_stepback">
<code>ct_tree_stepback</code>
</a>
</dt>

<dd markdown="1">
The stepback used in the contingency table calculation.

Defaults to `1`.
</dd>

<dt>
<a name="ct_tree_precision">
<code>ct_tree_precision</code>
</a>
</dt>

<dd markdown="1">
The precision used in the contingency table calculation.

Defaults to `1E-66`.
</dd>

</dl>

## Miscellaneous

<dl>

<dt>
<a name="deterministic">
<code>deterministic</code>
</a>
</dt>

<dd markdown="1">
When `true`, the program seeds its random generator with the value of [`seed`](#seed). This will yield the same result for every run using the same [`seed`](#seed).

When `false`, it will use the system time. This will likely yield different results on every run.

Defaults to `false`.
</dd>


<dt>
<a name="seed">
<code>seed</code>
</a>
</dt>

<dd markdown="1">
The value used to seed random generator. This will be ignored if [`deterministic = false`](#deterministic).

Defaults to `56854687`.
</dd>

<dt>
<a name="binomial_sampling">
<code>binomial_sampling</code>
</a>
</dt>

<dd markdown="1">
When `true`, counts determined by the given input distributions are sampled from binomial distributions.

When `false`, counts determined by the given input distributions are sampled from normal distributions.

Defaults to `true`.
</dd>

<dt>
<a name="over_sized_geography_factor">
<code>over_sized_geography_factor</code>
</a>
</dt>

<dd markdown="1">
The multiplier applied when determining the number of house addresses in a given area.

This is used for determining moving addresses and partnering.

Defaults to `1`.
</dd>

</dl>
