# 1. S3 bucket for original image uploads
resource "aws_s3_bucket" "original_images" {
  bucket        = "hadervic-aws-image-processor-original-images"
  force_destroy = true
}

resource "aws_s3_bucket_cors_configuration" "original_images" {
  bucket = aws_s3_bucket.original_images.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["PUT"]
    allowed_origins = ["http://localhost:4200"]
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}

resource "aws_s3_bucket_notification" "s3_bucket_notification_original_images" {
  bucket = aws_s3_bucket.original_images.id

  lambda_function {
    lambda_function_arn = aws_lambda_function.image_processor.arn
    events              = ["s3:ObjectCreated:*"]
  }

  depends_on = [aws_lambda_permission.allow_execution_of_lambda_from_s3]
}

# 2. S3 bucket for resized image
resource "aws_s3_bucket" "resized_images" {
  bucket        = "hadervic-aws-image-processor-resized-images"
  force_destroy = true
}

resource "aws_s3_bucket_notification" "s3_bucket_notification_resized_images" {
  bucket = aws_s3_bucket.resized_images.id

  lambda_function {
    lambda_function_arn = aws_lambda_function.resized_image_meta_processor.arn
    events              = ["s3:ObjectCreated:*"]
  }

  depends_on = [aws_lambda_permission.allow_execution_of_lambda_from_resized_s3]
}
