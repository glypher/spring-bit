FROM hashicorp/vault


COPY config/start-vault-unsealed.sh /usr/local/bin/.
RUN chmod 755 /usr/local/bin/start-vault-unsealed.sh

# Copy certificates
COPY certs /etc/vault/certs
RUN chown -R vault:vault /etc/vault/certs

# Copy init
COPY config/vault-server.hcl /etc/vault/.
RUN chown vault:vault /etc/vault/vault-server.hcl

ENTRYPOINT ["start-vault-unsealed.sh"]

# Run the vault server
CMD ["docker-entrypoint.sh", "server", "-config=/etc/vault/vault-server.hcl"]
