# Define the provider
provider "aws" {
  region = "us-east-1" # Adjust this to your preferred AWS region
}

# Create a VPC
resource "aws_vpc" "springbit_k8s_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = "springbit"
    Value = "springbit-k8s-vpc"
  }
}

# Create a subnet
resource "aws_subnet" "springbit_k8s_subnet" {
  vpc_id                  = aws_vpc.springbit_k8s_vpc.id
  cidr_block              = "10.0.1.0/24"
  map_public_ip_on_launch = true
  availability_zone       = "us-east-1c"
  tags = {
    Name = "springbit"
    Value = "springbit-k8s-subnet"
  }
}

# Create an Internet Gateway
resource "aws_internet_gateway" "springbit_k8s_igw" {
  vpc_id = aws_vpc.springbit_k8s_vpc.id
  tags = {
    Name = "springbit"
    Value = "springbit-k8s-igw"
  }
}

# Create a Route Table
resource "aws_route_table" "springbit_k8s_route_table" {
  vpc_id = aws_vpc.springbit_k8s_vpc.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.springbit_k8s_igw.id
  }
  tags = {
    Name = "springbit"
    Value = "springbit-k8s-route-table"
  }
}

# Associate Route Table with Subnet
resource "aws_route_table_association" "k8s_route_table_association" {
  subnet_id      = aws_subnet.springbit_k8s_subnet.id
  route_table_id = aws_route_table.springbit_k8s_route_table.id
}

# Security Group for Kubernetes Nodes
resource "aws_security_group" "springbit_k8s_sg" {
  name_prefix = "springbit-k8s-sg-"
  description = "Allow Kubernetes traffic"
  vpc_id      = aws_vpc.springbit_k8s_vpc.id

  ingress {
    description      = "Allow SSH"
    from_port        = 22
    to_port          = 22
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

  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
  }

  tags = {
    Name = "springbit"
    Value = "springbit-k8s-security-group"
  }
}


# Launch EC2 Instances for Master and Worker Nodes
resource "aws_instance" "springbit_k8s_master" {
  ami           = "ami-079cb33ef719a7b78" # Canonical, Ubuntu, 24.04, amd64 noble image
  instance_type = "t3.medium"
  subnet_id     = aws_subnet.springbit_k8s_subnet.id
  key_name      = "springbit-key"
  security_groups = [aws_security_group.springbit_k8s_sg.id]

#  instance_market_options {
#    market_type = "spot"
#    spot_options {
#      max_price = "0.03" # Maximum price you're willing to pay for the instance
#    }
#  }

  tags = {
    Name = "springbit"
    Value = "springbit-k8s-master"
  }

  provisioner "file" {
    source      = "scripts/master-setup.sh"
    destination = "/tmp/script.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "chmod +x /tmp/script.sh",
      "/tmp/script.sh",
    ]
  }
  connection {
    type        = "ssh"
    user        = "ubuntu"
    private_key = file("~/.ssh/springbit.key")
    host        = self.public_ip
  }
}

output "springbit_k8s_master_public_ip" {
  value = aws_instance.springbit_k8s_master.public_ip
  description = "The public IP of the EC2 k8s master instance"
}

resource "aws_instance" "springbit_k8s_worker" {
  count         = 0
  ami           = "ami-079cb33ef719a7b78" # Canonical, Ubuntu, 24.04, amd64 noble image
  instance_type = "t3.small"
  subnet_id     = aws_subnet.springbit_k8s_subnet.id
  key_name      = "springbit-key"
  security_groups = [aws_security_group.springbit_k8s_sg.id]


  tags = {
    Name = "springbit"
    Value = "springbit-k8s-worker-${count.index + 1}"
  }


  provisioner "file" {
    source      = "scripts/worker-setup.sh"
    destination = "/tmp/script.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "chmod +x /tmp/script.sh",
      "/tmp/script.sh",
    ]
  }
  connection {
    type        = "ssh"
    user        = "ubuntu"
    private_key = file("~/.ssh/springbit.key")
    host        = self.public_ip
  }
}
