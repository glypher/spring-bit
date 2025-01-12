# Data source for the existing Route 53 DNS A record
#data "aws_route53_zone" "springbit_org" {
#  name = var.springbit_route53_zone
#}

#data "aws_route53_record" "springbit_org_a_record" {
#  zone_id = data.aws_route53_zone.springbit_org.zone_id
#  name    = var.springbit_route53_A_record
#  type    = "A"
#}

# Data source for the Elastic IP
data "aws_eip" "springbit_ip" {
  #public_ip = data.aws_route53_record.springbit_org_a_record.records[0]
  public_ip = var.springbit_ip
}
