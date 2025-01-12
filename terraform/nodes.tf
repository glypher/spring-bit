# Define the provider
provider "aws" {
  region = var.region
}


resource "aws_iam_instance_profile" "k8s_instance_profile" {
  name = "k8s_instance_profile"
  role = "SpringbitInstanceRole"
}

# Associate the Elastic IP with the EC2 instance
resource "aws_eip_association" "example" {
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
  associate_public_ip_address = true

#  instance_market_options {
#    market_type = "spot"
#    spot_options {
#      max_price = "0.03" # Maximum price you're willing to pay for the instance
#    }
#  }

  tags = {
    Name = var.springbit_tag
    Value = "${var.springbit_tag}-k8s-master"
  }

  provisioner "file" {
    source      = "scripts"
    destination = "/tmp"
  }

  provisioner "remote-exec" {
    inline = [
      "chmod -R +x /tmp/scripts/",
      "/tmp/scripts/master-setup.sh",
      "/tmp/scripts/pull-s3-bucket.sh ${var.springbit_s3_bucket}"
    ]
  }
  connection {
    type        = "ssh"
    user        = "ubuntu"
    private_key = file(var.instance_ssh_key)
    host        = self.public_ip
  }
}

output "springbit_k8s_master_public_ip" {
  value = aws_instance.k8s_control_plane.public_ip
  description = "The public IP of the EC2 k8s control plane"
}

resource "aws_instance" "k8s_node" {
  count         = 0
  ami           = "ami-079cb33ef719a7b78" # Canonical, Ubuntu, 24.04, amd64 noble image
  instance_type = var.worker_instance_size
  subnet_id     = aws_subnet.k8s_subnet.id
  key_name      = var.instance_key_pair
  security_groups = [aws_security_group.k8s_sg.id]

  iam_instance_profile = aws_iam_instance_profile.k8s_instance_profile.name

  tags = {
    Name = var.springbit_tag
    Value = "${var.springbit_tag}-k8s-worker-${count.index + 1}"
  }

  provisioner "file" {
    source      = "scripts"
    destination = "/tmp"
  }

  provisioner "remote-exec" {
    inline = [
      "chmod -R +x /tmp/scripts/",
      "/tmp/scripts/worker-setup.sh",
      "/tmp/scripts/pull-s3-bucket.sh ${var.springbit_s3_bucket}"
    ]
  }
  connection {
    type        = "ssh"
    user        = "ubuntu"
    private_key = file(var.instance_ssh_key)
    host        = self.public_ip
  }
}
