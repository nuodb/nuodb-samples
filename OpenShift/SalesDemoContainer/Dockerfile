FROM docker.io/centos:7

ARG NUODB_BAMBOO_RPM

RUN yum -y install --setopt=tsflags=nodocs openssh-server java-1.8.0-openjdk net-tools which emacs dstat htop strace gdb iotop python vim && yum clean all
#RUN curl -SL $NUODB_BAMBOO_RPM -o /tmp/nuodb.rpm \
COPY nuodb-3.1.0.rpm /tmp/nuodb.rpm
RUN  rpm -i --excludedocs /tmp/nuodb.rpm && rm -f /tmp/nuodb.rpm && rm -rf /var/cache/yum

ADD scripts /scripts
COPY show /usr/bin/

RUN chmod -R +x /scripts/ && mkdir /archive

EXPOSE 2222

ENV PATH=/opt/nuodb/bin:${PATH}
ENV HOME=/scripts

ENTRYPOINT /scripts/entrypoint.sh