# Lambda function for image processing
resource "aws_lambda_function" "image_processor" {
  function_name = "imageProcessorLambda"
  role          = aws_iam_role.hadervic-aws-image-processor-lambda-role.arn
  handler       = "com.example.imageresizer.App::handleRequest"
  runtime       = "java17"
  timeout       = 90

  filename         = "${path.module}/../imageresizer/target/imageresizer.jar"
  source_code_hash = filebase64sha256("${path.module}/../imageresizer/target/imageresizer.jar")

  environment {
    variables = {
      DESTINATION_BUCKET = aws_s3_bucket.resized_images.id
    }
  }
}

# IAM Role for Lambda execution
resource "aws_iam_role" "hadervic-aws-image-processor-lambda-role" {
  name = "hadervic-aws-image-processor-lambda-role"
  assume_role_policy = jsonencode(({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "lambda.amazonaws.com"
      }
    }]
  }))
}

# Attach the role 
resource "aws_iam_role_policy_attachment" "lambda_basic_exection" {
  role       = aws_iam_role.hadervic-aws-image-processor-lambda-role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy_attachment" "lambda_s3_access" {
  role       = aws_iam_role.hadervic-aws-image-processor-lambda-role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

resource "aws_lambda_permission" "allow_execution_of_lambda_from_s3" {
  statement_id  = "AllowExecutionFromS3"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.image_processor.function_name
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.original_images.arn
}


# Presigned URL Lambda
resource "aws_iam_role" "s3presigned_url_lambda_role" {
  name = "hadervic-aws-image-s3presigned-url-lambda-role"
  assume_role_policy = jsonencode(({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "lambda.amazonaws.com"
      }
    }]
  }))
}

resource "aws_iam_policy" "s3presigned_url_lambda_policy" {
  name        = "hadervic-aws-image-presigned-url-lambda-policy"
  description = "Allow Lambda to generate pre-signed URLs for uploads bucket"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:PutObject"
        ]
        Resource = "arn:aws:s3:::${aws_s3_bucket.original_images.id}/*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "s3presigned_url_lambda_role_attach" {
  role       = aws_iam_role.s3presigned_url_lambda_role.name
  policy_arn = aws_iam_policy.s3presigned_url_lambda_policy.arn
}

resource "aws_iam_role_policy_attachment" "s3presigned_url_lambda_role_attach_basic_execution" {
  role       = aws_iam_role.s3presigned_url_lambda_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}


resource "aws_lambda_function" "s3_presigned_url_lambda_function" {
  function_name = "s3PresignedUrl"
  role          = aws_iam_role.s3presigned_url_lambda_role.arn
  handler       = "com.example.imagepresignedurl.App::handleRequest"
  runtime       = "java17"
  timeout       = 30

  filename         = "${path.module}/../imagepresignedurl/target/imagepresignedurl.jar"
  source_code_hash = filebase64sha256("${path.module}/../imagepresignedurl/target/imagepresignedurl.jar")

  environment {
    variables = {
      UPLOAD_BUCKET = aws_s3_bucket.original_images.id
    }
  }
}

# Lambda for saving meta data in DynamocDB
resource "aws_lambda_function" "resized_image_meta_processor" {
  function_name = "resizedImageMetaProcessorLambda"
  role          = aws_iam_role.hadervic_aws_resized_image_meta_processor_lambda_role.arn
  handler       = "com.example.resizedimagehandler.App::handleRequest"
  runtime       = "java17"
  timeout       = 120

  filename         = "${path.module}/../resizedimagehandler/target/resizedimagehandler.jar"
  source_code_hash = filebase64sha256("${path.module}/../resizedimagehandler/target/resizedimagehandler.jar")

  environment {
    variables = {
      APPSYNC_API_URL = aws_appsync_graphql_api.image-resizer-api.uris["GRAPHQL"]
      APPSYNC_API_KEY = split(":", aws_appsync_api_key.image-resizer-api-key.id)[1]
      DYNAMO_DB_TABLE_FOR_IMG_META = "hadervic-resized-images-meta"
    }
  }
}

resource "aws_iam_role" "hadervic_aws_resized_image_meta_processor_lambda_role" {
  name = "hadervic_aws_resized_image_meta_processor_lambda_role"
  assume_role_policy = jsonencode(({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "lambda.amazonaws.com"
      }
    }]
  }))
}

# Inline policy to allow S3 GetObject
resource "aws_iam_role_policy" "hadervic_aws_resized_image_processor_lambda_s3_get_object" {
  name = "hadervic_aws_resized_image_processor_lambda_s3_get_object"
  role = aws_iam_role.hadervic_aws_resized_image_meta_processor_lambda_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject"
        ]
        Resource = "arn:aws:s3:::${aws_s3_bucket.resized_images.id}/*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "resized_image_meta_handler_lambda_basic_exection" {
  role       = aws_iam_role.hadervic_aws_resized_image_meta_processor_lambda_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_lambda_permission" "allow_execution_of_lambda_from_resized_s3" {
  statement_id  = "AllowExecutionFromResizedS3"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.resized_image_meta_processor.function_name
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.resized_images.arn
}


resource "aws_iam_role_policy" "resized_image_meta_handler_dynamodb_write" {
  name = "lambda-dynamodb-write"
  role = aws_iam_role.hadervic_aws_resized_image_meta_processor_lambda_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "dynamodb:PutItem",
          "dynamodb:UpdateItem"
        ]
        Resource = aws_dynamodb_table.resized_images.arn
      }
    ]
  })
}