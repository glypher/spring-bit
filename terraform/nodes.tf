# Define the provider
provider "aws" {
  region = var.region
}


resource "aws_iam_instance_profile" "k8s_instance_profile" {
  name = "k8s_instance_profile"
  role = "SpringbitInstanceRole"
}


# Associate the Elastic IP with the EC2 instance
resource "aws_eip_association" "control_plane_eip_association" {
  instance_id   = aws_instance.k8s_control_plane.id
  allocation_id = data.aws_eip.springbit_ip.id
}

# Launch EC2 Instances for Master and Worker Nodes
resource "aws_instance" "k8s_control_plane" {
  ami           = "ami-079cb33ef719a7b78" # Canonical, Ubuntu, 24.04, amd64 noble image
  instance_type = var.master_instance_size
  subnet_id     = aws_subnet.k8s_subnet.id
  key_name      = var.instance_key_pair
  security_groups = [aws_security_group.k8s_sg.id]

  iam_instance_profile = aws_iam_instance_profile.k8s_instance_profile.name

  # Associate the Elastic IP
  associate_public_ip_address = false

#  instance_market_options {
#    market_type = "spot"
#    spot_options {
#      max_price = "0.03" # Maximum price you're willing to pay for the instance
#    }
#  }

  # Configure the root EBS volume
  root_block_device {
    volume_size = var.master_instance_ebs_size
    volume_type = "gp3"
    delete_on_termination = true
  }

  tags = {
    Name = var.springbit_tag
    Value = "${var.springbit_tag}-k8s-master"
  }

  user_data = <<-EOF
              #!/bin/bash
              sudo apt-get update -y -qq > /dev/null 2>&1
              sudo apt-get install -y -qq apt-transport-https ca-certificates curl unzip net-tools

              curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
              unzip -q awscliv2.zip
              sudo ./aws/install
              rm -rf awscliv2.zip aws

              sudo mkdir -p /hostdata
              sudo aws s3 sync s3://${var.springbit_s3_bucket} /hostdata --quiet

              chmod -R +x /hostdata/scripts/
              /hostdata/scripts/master-setup.sh
              /hostdata/scripts/fix-coredns.sh
              /hostdata/scripts/create-k8s-join.sh ${var.springbit_s3_bucket}
              /hostdata/scripts/pull-s3-bucket.sh ${var.springbit_s3_bucket}
              /hostdata/scripts/create-certs-secret.sh ${var.springbit_certs_s3_bucket}
              /hostdata/scripts/springbit-k8s.sh
              /hostdata/scripts/kubectl-add-user.sh ubuntu
              EOF
}



resource "aws_instance" "k8s_node" {
  count         = 1
  ami           = "ami-079cb33ef719a7b78" # Canonical, Ubuntu, 24.04, amd64 noble image
  instance_type = var.worker_instance_size
  subnet_id     = aws_subnet.k8s_subnet.id
  key_name      = var.instance_key_pair
  security_groups = [aws_security_group.k8s_sg.id]

  depends_on = [aws_instance.k8s_control_plane]

  iam_instance_profile = aws_iam_instance_profile.k8s_instance_profile.name

  associate_public_ip_address = true

  # Configure the root EBS volume
  root_block_device {
    volume_size = var.worker_instance_ebs_size
    volume_type = "gp3"
    delete_on_termination = true
  }

  tags = {
    Name = var.springbit_tag
    Value = "${var.springbit_tag}-k8s-worker-${count.index + 1}"
  }

  user_data = <<-EOF
              #!/bin/bash
              sudo apt-get update -y -qq > /dev/null 2>&1
              sudo apt-get install -y -qq apt-transport-https ca-certificates curl unzip net-tools

              curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
              unzip -q awscliv2.zip
              sudo ./aws/install
              rm -rf awscliv2.zip aws

              sudo mkdir -p /hostdata
              sudo aws s3 sync s3://${var.springbit_s3_bucket} /hostdata --quiet

              chmod -R +x /hostdata/scripts/
              /hostdata/scripts/worker-setup.sh ${var.springbit_s3_bucket}
              /hostdata/scripts/pull-s3-bucket.sh ${var.springbit_s3_bucket}
              /hostdata/scripts/kubectl-add-user.sh ubuntu
              EOF
}
