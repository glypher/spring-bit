FROM mysql:9.1

ARG ROOT_PASSWORD=spring-bit-mysql-root
ARG USER_PASSWORD=spring-bit-mysql

# Set environment variables for MySQL root user
ENV MYSQL_ROOT_PASSWORD=$ROOT_PASSWORD \
    MYSQL_USER=spring-bit-user \
    MYSQL_PASSWORD=$USER_PASSWORD


# Copy initialization scripts to the container
COPY config/init.sql /docker-entrypoint-initdb.d/

# Copy custom MySQL configuration
COPY config/my.cnf /etc/mysql/conf.d/my.cnf

# Expose MySQL default port
EXPOSE 3306

# Create a volume for MySQL data
VOLUME ["/var/lib/mysql"]

# Run the MySQL server
CMD ["mysqld"]
