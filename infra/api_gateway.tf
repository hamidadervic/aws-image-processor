
# Create API Gateway REST API
resource "aws_api_gateway_rest_api" "presign_api" {
  name        = "hadervic-presignurl-api"
  description = "API for generating S3 pre-signed URLs"
}

# Create resource /generate-url
resource "aws_api_gateway_resource" "generate_url" {
  rest_api_id = aws_api_gateway_rest_api.presign_api.id
  parent_id   = aws_api_gateway_rest_api.presign_api.root_resource_id
  path_part   = "generate-url"
}

# Create GET method
resource "aws_api_gateway_method" "post_generate_url" {
  rest_api_id   = aws_api_gateway_rest_api.presign_api.id
  resource_id   = aws_api_gateway_resource.generate_url.id
  http_method   = "GET"
  authorization = "NONE"
}

# Integrate Lambda with API Gateway
resource "aws_api_gateway_integration" "lambda_generate_url" {
  rest_api_id = aws_api_gateway_rest_api.presign_api.id
  resource_id = aws_api_gateway_resource.generate_url.id
  http_method = aws_api_gateway_method.post_generate_url.http_method

  integration_http_method = "POST" # Must always be POST
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.s3_presigned_url_lambda_function.invoke_arn
}

# Allow API Gateway to invoke Lambda
resource "aws_lambda_permission" "api_gateway_invoke" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.s3_presigned_url_lambda_function.function_name
  principal     = "apigateway.amazonaws.com"

  # Source ARN for this API method
  source_arn = "${aws_api_gateway_rest_api.presign_api.execution_arn}/*/GET/generate-url"
}

# Deploy the API (deployment)
resource "aws_api_gateway_deployment" "presign_api_deployment" {
  depends_on = [aws_api_gateway_integration.lambda_generate_url]
  rest_api_id = aws_api_gateway_rest_api.presign_api.id

  lifecycle {
    create_before_destroy = true
  }
}

# Create a stage
resource "aws_api_gateway_stage" "presign_api_stage" {
  stage_name    = "prod"
  rest_api_id   = aws_api_gateway_rest_api.presign_api.id
  deployment_id = aws_api_gateway_deployment.presign_api_deployment.id
}