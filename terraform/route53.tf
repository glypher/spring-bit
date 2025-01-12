# Route 53 Hosted Zone
resource "aws_route53_zone" "springbit_org" {
  name = var.springbit_domain
  tags = {
    Name = var.springbit_tag
    Value = "${var.springbit_tag}-route53-zone"
  }
}

# DNS Record
resource "aws_route53_record" "example_com" {
  zone_id = aws_route53_zone.springbit_org.zone_id
  name    = var.springbit_domain
  type    = "A"
  ttl     = 300
  records = [aws_instance.k8s_control_plane.public_ip]
}

resource "aws_route53_record" "example" {
  allow_overwrite = true
  name            = var.springbit_domain
  ttl             = 172800
  type            = "NS"
  zone_id         = aws_route53_zone.springbit_org.zone_id

  records = [
    var.springbit_route53_domain_ns[0],
    var.springbit_route53_domain_ns[1],
    var.springbit_route53_domain_ns[2],
    var.springbit_route53_domain_ns[3]
  ]
}