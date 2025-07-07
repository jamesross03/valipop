FROM spark:3.5.6-scala2.12-java17-ubuntu as spark

FROM valipop as run

COPY --from=spark /opt/spark/bin spark/bin
COPY --from=spark /opt/spark/sbin spark/sbin
COPY --from=spark /opt/spark/data spark/data
COPY --from=spark /opt/spark/jars spark/jars
COPY --from=spark /opt/spark/RELEASE spark/RELEASE

COPY docker/factor-search/entrypoint.sh .

ENV SPARK_NO_DAEMONIZE=true

EXPOSE 5055

ENTRYPOINT [ "./entrypoint.sh" ]
