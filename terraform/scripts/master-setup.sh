#!/bin/bash
sudo apt-get update -y -qq
sudo apt-get install -y -qq apt-transport-https ca-certificates curl unzip net-tools

# Install aws cli
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip -q awscliv2.zip
sudo ./aws/install
rm -rf awscliv2.zip aws


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

# Install Cillium CNI
CILIUM_CLI_VERSION=$(curl -s https://raw.githubusercontent.com/cilium/cilium-cli/main/stable.txt)
CLI_ARCH=amd64
if [ "$(uname -m)" = "aarch64" ]; then CLI_ARCH=arm64; fi
curl -L --fail --remote-name-all https://github.com/cilium/cilium-cli/releases/download/${CILIUM_CLI_VERSION}/cilium-linux-${CLI_ARCH}.tar.gz{,.sha256sum}
sha256sum --check cilium-linux-${CLI_ARCH}.tar.gz.sha256sum
sudo tar xzfC cilium-linux-${CLI_ARCH}.tar.gz /usr/local/bin
rm cilium-linux-${CLI_ARCH}.tar.gz{,.sha256sum}

# Install Helm
curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh
rm get_helm.sh




IPV4=$(hostname -I | cut -d ' ' -f1)

#sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --service-cidr=10.0.0.0/12 --cri-socket unix:///run/containerd/containerd.sock
sudo kubeadm init \
    --skip-phases=addon/kube-proxy \
    --apiserver-advertise-address=$IPV4 \
    --cri-socket unix:///run/containerd/containerd.sock
#Force to listen to IPV4
sudo sed -i "/- kube-apiserver/a\ \ \ \ - --bind-address=$IPV4" /etc/kubernetes/manifests/kube-apiserver.yaml
sudo systemctl restart kubelet
echo "Api server listening to: $(sudo netstat -tuln | grep 6443)"

mkdir -p $HOME/.kube
sudo cp -f /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

echo "Waiting for Kubernetes API server to become available..."
while ! kubectl version  &>/dev/null; do
  echo "Waiting..."
  sleep 5  # Check every 5 seconds
done
echo "Kubernetes API server is available!"

#kubectl taint nodes --all node.cilium.io/agent-not-ready=true:NoExecute
kubectl taint nodes --all node-role.kubernetes.io/control-plane-
# Label node for monitoring
kubectl label nodes --all springbit.org/publichost=yes
kubectl label nodes --all springbit.org/monitoring=yes

# Cilium CNI setup
kubectl apply -f https://raw.githubusercontent.com/kubernetes-sigs/gateway-api/v1.1.0/config/crd/standard/gateway.networking.k8s.io_gatewayclasses.yaml
kubectl apply -f https://raw.githubusercontent.com/kubernetes-sigs/gateway-api/v1.1.0/config/crd/standard/gateway.networking.k8s.io_gateways.yaml
kubectl apply -f https://raw.githubusercontent.com/kubernetes-sigs/gateway-api/v1.1.0/config/crd/standard/gateway.networking.k8s.io_httproutes.yaml
kubectl apply -f https://raw.githubusercontent.com/kubernetes-sigs/gateway-api/v1.1.0/config/crd/standard/gateway.networking.k8s.io_referencegrants.yaml
kubectl apply -f https://raw.githubusercontent.com/kubernetes-sigs/gateway-api/v1.1.0/config/crd/standard/gateway.networking.k8s.io_grpcroutes.yaml
kubectl apply -f https://raw.githubusercontent.com/kubernetes-sigs/gateway-api/v1.1.0/config/crd/experimental/gateway.networking.k8s.io_tlsroutes.yaml


cilium install --version 1.16.5 \
  --set kubeProxyReplacement=true \
  --set k8sServiceHost=$IPV4 \
  --set k8sServicePort=6443

cilium status --wait


helm repo add cilium https://helm.cilium.io/

cat >>cilium_gateway_host.yaml<<EOF
kubeProxyReplacement: true
k8sServiceHost: $IPV4
k8sServicePort: 6443
gatewayAPI:
  enabled: true
  hostNetwork:
    enabled: true
envoy:
  enabled: true
  securityContext:
    capabilities:
      keepCapNetBindService: true
      envoy:
      # Add NET_BIND_SERVICE to the list (keep the others!)
      - NET_BIND_SERVICE
      - NET_ADMIN
      - BPF
EOF

helm upgrade cilium cilium/cilium --version 1.16.5 \
    --namespace kube-system \
    --reuse-values \
    -f cilium_gateway_host.yaml

rm cilium_gateway_host.yaml



kubectl -n kube-system rollout restart deployment/cilium-operator
kubectl -n kube-system rollout restart ds/cilium
kubectl -n kube-system rollout restart ds/cilium-envoy


# Set cilium Envoy only to bind to control plane
#kubectl get ds cilium-envoy -n kube-system -o yaml > cilium-envoy-backup.yaml
#sed -i '/affinity:/a\
#        nodeAffinity:\
#          requiredDuringSchedulingIgnoredDuringExecution:\
#            nodeSelectorTerms:\
#              - matchExpressions:\
#                - key: springbit.org/publichost\
#                  operator: In\
#                  values:\
#                    - "yes"
#' cilium-envoy-backup.yaml

#kubectl apply -f cilium-envoy-backup.yaml
#rm cilium-envoy-backup.yaml

#kubectl -n kube-system rollout restart deployment/cilium-operator
#kubectl -n kube-system rollout restart ds/cilium
#kubectl -n kube-system rollout restart ds/cilium-envoy


