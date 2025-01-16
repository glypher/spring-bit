output "springbit_k8s_worker_node_public_ip" {
  value = aws_instance.k8s_node[0].public_ip
  description = "The public IP of the EC2 k8s node"
}