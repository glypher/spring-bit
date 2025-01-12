#!/bin/bash
sudo apt-get update -y
sudo apt-get install -y apt-transport-https ca-certificates curl unzip

# Install containerd as the container runtime
curl -O -L https://github.com/containerd/containerd/releases/download/v1.7.25/containerd-1.7.25-linux-amd64.tar.gz
sudo tar Cxzvf /usr/local containerd-1.7.25-linux-amd64.tar.gz
rm containerd-1.7.25-linux-amd64.tar.gz
sudo curl -L https://raw.githubusercontent.com/containerd/containerd/main/containerd.service -o /usr/lib/systemd/system/containerd.service
sudo systemctl daemon-reload
sudo systemctl enable --now containerd
curl -O -L https://github.com/opencontainers/runc/releases/download/v1.2.4/runc.amd64
sudo install -m 755 runc.amd64 /usr/local/sbin/runc
rm runc.amd64
curl -O -L https://github.com/containernetworking/plugins/releases/download/v1.6.2/cni-plugins-linux-amd64-v1.6.2.tgz
sudo mkdir -p /opt/cni/bin
sudo tar Cxzvf /opt/cni/bin cni-plugins-linux-amd64-v1.6.2.tgz
rm cni-plugins-linux-amd64-v1.6.2.tgz
# Set systemd as the default cgroup
sudo containerd config default > config.toml ; sudo mkdir -p /etc/containerd ; sudo mv config.toml /etc/containerd/config.toml
sudo sed -i -e "s/SystemdCgroup.*=.*/SystemdCgroup = true/g" /etc/containerd/config.toml
sudo sed -i -e "s|sandbox_image.*=.*|sandbox_image = \"registry.k8s.io/pause:3.10\"|g" /etc/containerd/config.toml
sudo systemctl restart containerd

echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.32/deb/ / " | sudo tee /etc/apt/sources.list.d/kubernetes.list
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.32/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
sudo apt-get update -y
sudo apt-get install -y kubelet kubeadm kubectl

# Packet forward between k8s clusters
cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.ipv4.ip_forward = 1
EOF
sudo sysctl --system

# Disable swap
sudo swapoff -a

sudo kubeadm init --node-name control-plane --pod-network-cidr=10.0.0.0/16
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

sleep 3
# install the network CNI policy in k8s
# https://kubernetes.io/docs/concepts/cluster-administration/addons/#networking-and-network-policy
kubectl apply -f https://reweave.azurewebsites.net/k8s/v1.32/net.yaml

# Install aws cli
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
rm -rf awscliv2.zip aws
