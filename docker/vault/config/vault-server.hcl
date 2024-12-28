api_addr                = "https://vault-server:8200"
disable_mlock           = false
ui                      = false

listener "tcp" {
address       = "0.0.0.0:8200"
tls_cert_file = "/vault-data/vault-cert.pem"
tls_key_file  = "/vault-data/vault-key.pem"
}

listener "tcp" {
address       = "127.0.0.1:8201"
tls_disable   = true
}

storage "file" {
  path = "/vault-data/storage"
}

#log_level = "Debug"