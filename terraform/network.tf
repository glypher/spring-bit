# Create a VPC
resource "aws_vpc" "k8s_vpc" {
  cidr_block           = "172.20.0.0/16"

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
  cidr_block              = "172.20.1.0/24"

  map_public_ip_on_launch = false
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
    Name  = var.springbit_tag
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


  # Allow all traffic within the VPC
  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [aws_vpc.k8s_vpc.cidr_block]
  }

  # Allow ICMP (ping) within the VPC
  ingress {
    from_port   = -1
    to_port     = -1
    protocol    = "icmp"
    cidr_blocks = [aws_vpc.k8s_vpc.cidr_block]
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
