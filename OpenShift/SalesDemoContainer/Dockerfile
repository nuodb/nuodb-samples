FROM docker.io/centos:7

ARG NUODB_BAMBOO_RPM

RUN yum -y install --setopt=tsflags=nodocs openssh-server java-1.8.0-openjdk net-tools which emacs dstat htop strace gdb iotop python vim && yum clean all
RUN curl -SL http://bamboo.bo2.nuodb.com/bamboo/artifact/MASTER-NP/shared/build-01588/Linux-.rpm/nuodb-3.3.0.master.5609.x86_64.rpm -o /tmp/nuodb.rpm \
       && rpm -i --excludedocs /tmp/nuodb.rpm && rm -f /tmp/nuodb.rpm \
       && /usr/sbin/chkconfig --level 2345 nuoagent on \
       && /opt/nuodb/etc/nuoadmin-enable

ADD scripts /scripts
COPY show /usr/bin/

RUN chmod -R +x /scripts/ && mkdir /archive

EXPOSE 2222

ENV PATH=/opt/nuodb/bin:${PATH}
ENV HOME=/scripts

ENTRYPOINT /scripts/entrypoint.sh