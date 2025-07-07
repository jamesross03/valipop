FROM spark:3.5.6-scala2.12-java17-ubuntu as spark

FROM valipop-base as run

COPY --from=spark /opt/spark/bin spark/bin
COPY --from=spark /opt/spark/sbin spark/sbin
COPY --from=spark /opt/spark/data spark/data
COPY --from=spark /opt/spark/jars spark/jars
COPY --from=spark /opt/spark/RELEASE spark/RELEASE

ENV SPARK_NO_DAEMONIZE=true
ENV SPARK_WORKER_MEMORY=30G
ENV SPARK_WORKER_CORES=12
ENV SPARK_WORKER_INSTANCES=1

EXPOSE 8081

COPY docker/cluster/worker/entrypoint.sh .

ENTRYPOINT [ "./entrypoint.sh" ]
