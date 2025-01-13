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

variable "master_instance_ebs_size"  {
  description = "The EC2 instance disk size to run k8s control plane"
  type        = number
  default     = 20
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

variable "springbit_certs_s3_bucket"  {
  description = "The S3 bucket to pull domain certificates from"
  type        = string
  default     = "springbit-certs"
}

variable "springbit_tag"  {
  description = "The aws tag prefix to use"
  type        = string
  default     = "springbit"
}


variable "springbit_route53_zone"  {
  description = "The aws route53 zone name"
  type        = string
  default     = "springbit.org"
}
variable "springbit_route53_A_record"  {
  description = "The aws route53 zone A record"
  type        = string
  default     = "springbit.org"
}

variable "springbit_ip"  {
  description = "The aws public elastic ip"
  type        = string
  default     = "23.23.151.19"
}