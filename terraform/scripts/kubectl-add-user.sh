#!/bin/bash
if [[ $# -ne 1 ]]; then
  echo "Usage: $0 user"
  exit 1
fi

user=$1

sudo su - $user -c "mkdir -p ~/.kube"
sudo cp -f /etc/kubernetes/admin.conf /home/$user/.kube/config
sudo chown $(id -u $user):$(id -g $user) /home/$user/.kube/config