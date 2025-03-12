#!/bin/bash

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 s3-bucket-name"
  exit 1
fi

source "$(dirname "$0")/utils.sh"

sudo apt-get update -y -qq > /dev/null 2>&1
sudo apt-get install -y -qq apt-transport-https ca-certificates curl unzip net-tools

# Install aws cli
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip -q awscliv2.zip
sudo ./aws/install
rm -rf awscliv2.zip aws


##########################################################################
# Install K8s, containerd, runc, Cilium agent will be deployed by master #
##########################################################################

# Disable swap
sudo swapoff -a
sudo sed -i '/swap/d' /etc/fstab

#sudo bash -c 'cat >>/etc/modules-load.d/containerd.conf<<EOF
#overlay
#br_netfilter
#EOF'
#sudo modprobe overlay
#sudo modprobe br_netfilter

# Install containerd as the container runtime
curl -O -L https://github.com/containerd/containerd/releases/download/v2.0.1/containerd-2.0.1-linux-amd64.tar.gz
sudo tar Cxzf /usr/local containerd-2.0.1-linux-amd64.tar.gz
rm containerd-2.0.1-linux-amd64.tar.gz
sudo curl -L https://raw.githubusercontent.com/containerd/containerd/main/containerd.service -o /usr/lib/systemd/system/containerd.service
sudo systemctl daemon-reload
sudo systemctl enable --now containerd

curl -O -L https://github.com/opencontainers/runc/releases/download/v1.2.4/runc.amd64
sudo install -m 755 runc.amd64 /usr/local/sbin/runc
rm runc.amd64

curl -O -L https://github.com/containernetworking/plugins/releases/download/v1.6.2/cni-plugins-linux-amd64-v1.6.2.tgz
sudo mkdir -p /opt/cni/bin
sudo tar Cxzf /opt/cni/bin cni-plugins-linux-amd64-v1.6.2.tgz
rm cni-plugins-linux-amd64-v1.6.2.tgz
# Set systemd as the default cgroup
#sudo containerd config default > config.toml ; sudo mkdir -p /etc/containerd ; sudo mv config.toml /etc/containerd/config.toml
#sudo sed -i -e "s/SystemdCgroup.*=.*/SystemdCgroup = true/g" /etc/containerd/config.toml
#sudo sed -i -e "s|sandbox_image.*=.*|sandbox_image = \"registry.k8s.io/pause:3.10\"|g" /etc/containerd/config.toml
#sudo systemctl restart containerd


sudo bash -c 'sudo cat >>/etc/sysctl.d/kubernetes.conf<<EOF
#net.bridge.bridge-nf-call-ip6tables = 1
#net.bridge.bridge-nf-call-iptables  = 1
net.ipv4.ip_forward                 = 1
EOF'
sudo sysctl --system

echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.32/deb/ / " | sudo tee /etc/apt/sources.list.d/kubernetes.list
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.32/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
sudo apt-get update -y -qq > /dev/null 2>&1
sudo apt-get install -y -qq kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl
sudo systemctl enable --now kubelet

##########################################
# Registering K8s worker node            #
##########################################

# Join the cluster
aws s3 cp s3://"$1"/k8s_join.sh ./k8s_join.sh --quiet
chmod +x k8s_join.sh
until sudo ./k8s_join.sh ; [[ $? -eq 0 ]]
do
    echo "Trying to register worker node..."
    sleep 10
    rm k8s_join.sh
    aws s3 cp s3://"$1"/k8s_join.sh ./k8s_join.sh --quiet
    chmod +x k8s_join.sh
done
#rm k8s_join.sh

mkdir -p $HOME/.kube
sudo cp -f /etc/kubernetes/kubelet.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
sudo chmod a+r /var/lib/kubelet/pki/kubelet-client-current.pem

##########################################
# Node labels for scheduling pods        #
##########################################

NODE_NAME=$(hostname)
kubectl label nodes "$NODE_NAME" springbit.org/volume=yes
kubectl label nodes "$NODE_NAME" springbit.org/backend=yes