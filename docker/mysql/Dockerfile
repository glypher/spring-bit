FROM mysql:9.1-oracle

ARG ROOT_PASSWORD=spring-bit-mysql-root
ARG USER_PASSWORD=spring-bit-mysql

# Set environment variables for MySQL root user
ENV MYSQL_ROOT_PASSWORD=$ROOT_PASSWORD \
    MYSQL_USER=spring-bit-user \
    MYSQL_PASSWORD=$USER_PASSWORD


# Copy initialization scripts to the container
COPY config/init.sql /docker-entrypoint-initdb.d/
COPY config/my.cnf /etc/mysql/conf.d/my.cnf
COPY config/spring-bit-start.sh /usr/local/bin/
RUN chmod a+x /usr/local/bin/spring-bit-start.sh

# Copy certificates
COPY certs/ /etc/mysql/certs
RUN chown -R mysql:mysql /etc/mysql/certs

# Expose MySQL default port
EXPOSE 3306

# Create a volume for MySQL data
VOLUME ["/mysql-data"]

CMD ["mysqld"]

ENTRYPOINT ["spring-bit-start.sh"]

# Run the MySQL server
CMD ["docker-entrypoint.sh", "mysqld"]

# Install promtail to send mysql logs to loki
RUN curl -s https://api.github.com/repos/grafana/loki/releases/latest | \
        grep browser_download_url | \
        cut -d '"' -f 4 | grep promtail-linux-amd64.zip | \
        xargs curl -O -L

RUN microdnf install -y unzip util-linux
RUN unzip promtail-linux-amd64.zip
RUN mv promtail-linux-amd64 /usr/local/bin/promtail
RUN promtail --version

RUN useradd -r promtail
RUN usermod -a -G mysql promtail

COPY config/promtail-config.yml /etc/promtail/.
