FROM spark:3.5.6-scala2.12-java17-ubuntu

EXPOSE 8080

ARG HOST_NAME localhost

ENV SPARK_NO_DAEMONIZE=true

RUN ls

COPY docker/cluster/master/entrypoint.sh .

ENTRYPOINT [ "./entrypoint.sh" ]
