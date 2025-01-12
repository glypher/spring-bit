variable "region"  {
  description = "The AWS region to deploy resources in"
  type        = string
  default     = "us-east-1"
}

variable "instance_key_pair"  {
  description = "The EC2 instance key pair"
  type        = string
  default     = "springbit-key"
}

variable "availability_zone"  {
  description = "The AWS region availability zone to deploy resources in"
  type        = string
  default     = "us-east-1c"
}

variable "instance_ssh_key"  {
  description = "The private key in the used key pair to ssh to the ec2 instances"
  type        = string
  default     = "~/.ssh/springbit.key"
}

variable "master_instance_size"  {
  description = "The EC2 instance to run k8s control plane"
  type        = string
  default     = "t3.medium"
}

variable "worker_instance_size"  {
  description = "The EC2 instance to run k8s node"
  type        = string
  default     = "t3.small"
}


variable "springbit_s3_bucket"  {
  description = "The S3 bucket to pull data from"
  type        = string
  default     = "springbit"
}

variable "springbit_tag"  {
  description = "The aws tag prefix to use"
  type        = string
  default     = "springbit"
}


variable "springbit_domain"  {
  description = "The web domain for the deployment"
  type        = string
  default     = "springbit.org"
}

variable "springbit_route53_domain_ns"  {
  description = "The NS domains of the registered domain"
  type        = list(string)
  default     = [
    "ns-2024.awsdns-61.co.uk",
    "ns-212.awsdns-26.com",
    "ns-874.awsdns-45.net",
    "ns-1491.awsdns-58.org"
  ]
}
