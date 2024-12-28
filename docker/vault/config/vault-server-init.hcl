api_addr                = "http://127.0.0.1:8200"
disable_mlock           = true
ui                      = false

listener "tcp" {
address       = "127.0.0.1:8200"
tls_disable   = "true"
}

storage "file" {
  path = REPLACE_ME
}