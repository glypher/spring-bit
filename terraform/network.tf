# Create a VPC
resource "aws_vpc" "k8s_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = var.springbit_tag
    Value = "${var.springbit_tag}-k8s-vpc"
  }
}

# Create a subnet
resource "aws_subnet" "k8s_subnet" {
  vpc_id                  = aws_vpc.k8s_vpc.id
  cidr_block              = "10.0.1.0/24"
  map_public_ip_on_launch = true
  availability_zone       = var.availability_zone
  tags = {
    Name = var.springbit_tag
    Value = "${var.springbit_tag}-k8s-subnet"
  }
}

# Create an Internet Gateway
resource "aws_internet_gateway" "k8s_igw" {
  vpc_id = aws_vpc.k8s_vpc.id
  tags = {
    Name = var.springbit_tag
    Value = "${var.springbit_tag}-k8s-igw"
  }
}

# Create a Route Table
resource "aws_route_table" "k8s_route_table" {
  vpc_id = aws_vpc.k8s_vpc.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.k8s_igw.id
  }
  tags = {
    Name = var.springbit_tag
    Value = "${var.springbit_tag}-k8s-route-table"
  }
}

# Associate Route Table with Subnet
resource "aws_route_table_association" "k8s_route_table_association" {
  subnet_id      = aws_subnet.k8s_subnet.id
  route_table_id = aws_route_table.k8s_route_table.id
}

# Security Group for Kubernetes Nodes
resource "aws_security_group" "k8s_sg" {
  name_prefix = "springbit-k8s-sg-"
  description = "Allow Kubernetes traffic"
  vpc_id      = aws_vpc.k8s_vpc.id

  ingress {
    description      = "Allow SSH"
    from_port        = 22
    to_port          = 22
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
  }

  ingress {
    description      = "Allow http"
    from_port        = 80
    to_port          = 80
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
  }

  ingress {
    description      = "Allow https"
    from_port        = 443
    to_port          = 443
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
  }

  ingress {
    description      = "Allow Kubernetes API server"
    from_port        = 6443
    to_port          = 6443
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
  }


  # Inbound rules for Cilium communication and Kubernetes API access
  ingress {
    description = "Port 4240 is used for internal Cilium communication between nodes (Cilium DaemonSet communication)."
    from_port   = 4240
    to_port     = 4240
    protocol    = "tcp"
    cidr_blocks = [aws_subnet.k8s_subnet.cidr_block]  # Allow traffic from subnet only
  }

  ingress {
    description = "Port 61678 is used for communication with Cilium API server."
    from_port   = 61678
    to_port     = 61678
    protocol    = "tcp"
    cidr_blocks = [aws_subnet.k8s_subnet.cidr_block]
  }

  ingress {
    description = "Port 8472 (UDP) is used for VXLAN communication between nodes for pod-to-pod networking (overlay network in Cilium)."
    from_port   = 8472
    to_port     = 8472
    protocol    = "udp"
    cidr_blocks = [aws_subnet.k8s_subnet.cidr_block]
  }

  ingress {
    description = "Port 1344 (UDP) is used for eBPF (BPF) communication for network enforcement between nodes."
    from_port   = 1344
    to_port     = 1344
    protocol    = "udp"
    cidr_blocks = [aws_subnet.k8s_subnet.cidr_block]
  }

  # Optional: Inbound rule for Prometheus metrics if needed
  ingress {
    description = "Port 9090 is used for Prometheus metrics exposed by Cilium"
    from_port   = 9090
    to_port     = 9090
    protocol    = "tcp"
    cidr_blocks = [aws_subnet.k8s_subnet.cidr_block]
  }

  # Kubelet API port for management communication
  ingress {
    description = "Port 10250 is used by the Kubernetes Kubelet API for node management (required for managing nodes)."
    from_port   = 10250
    to_port     = 10250
    protocol    = "tcp"
    cidr_blocks = [aws_subnet.k8s_subnet.cidr_block]
  }

  # Kubernetes NodePort Services (default 30000-32767 range)
  ingress {
    description = "Ports 30000-32767 are used by Kubernetes NodePort services."
    from_port   = 30000
    to_port     = 32767
    protocol    = "tcp"
    cidr_blocks = [aws_subnet.k8s_subnet.cidr_block]
  }

  # Kubernetes DNS (CoreDNS) and ClusterIP Service communication
  ingress {
    description = "Port 53 (TCP) is used for DNS queries (CoreDNS)."
    from_port   = 53
    to_port     = 53
    protocol    = "tcp"
    cidr_blocks = [aws_subnet.k8s_subnet.cidr_block]
  }

  ingress {
    description = "Port 53 (UDP) is used for DNS queries (CoreDNS)."
    from_port   = 53
    to_port     = 53
    protocol    = "udp"
    cidr_blocks = [aws_subnet.k8s_subnet.cidr_block]
  }

  # Kube Proxy communication between nodes (this is optional but often used in multi-node setups)
  ingress {
    description = "Port 10256 is used by Kubernetes Kube Proxy for communication between nodes for service management."
    from_port   = 10256
    to_port     = 10256
    protocol    = "tcp"
    cidr_blocks = [aws_subnet.k8s_subnet.cidr_block]
  }

  # Kubernetes API server access (for kubelets and other components)
  ingress {
    description = "Port 443 is used for encrypted communication with the Kubernetes API server (HTTPS)."
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = [aws_subnet.k8s_subnet.cidr_block]
  }

  # All outbound access is allowed
  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
  }

  tags = {
    Name = var.springbit_tag
    Value = "${var.springbit_tag}-k8s-security-group"
  }
}
