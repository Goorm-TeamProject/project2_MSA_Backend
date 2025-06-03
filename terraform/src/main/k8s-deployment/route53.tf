resource "aws_route53_record" "api_record" {
  zone_id = var.hosted_zone_id
  name    = "api.eouil.com"
  type    = "A"

  alias {
    name                   = "Eouil-ALB-293085171.ap-northeast-2.elb.amazonaws.com"  # 실제 ALB DNS 이름 (AWS 콘솔에서 복사)
    zone_id                = "Z3W0JTWQ8ZTL29"  # ALB의 Hosted Zone ID (서울 리전 기준)
    evaluate_target_health = true
  }
}
