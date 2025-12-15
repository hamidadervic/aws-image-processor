resource "aws_appsync_graphql_api" "image-resizer-api" {
  authentication_type = "API_KEY"
  name                = "image-resizer-api"
  schema              = file("image-resizer-api.schema.graphql")
}

# Create API key for the AppSync API
resource "aws_appsync_api_key" "image-resizer-api-key" {
  api_id  = aws_appsync_graphql_api.image-resizer-api.id
  expires = timeadd(timestamp(), "720h") # expires in 90 days
}

resource "aws_appsync_datasource" "none" {
  api_id = aws_appsync_graphql_api.image-resizer-api.id
  name   = "NoneDataSource"
  type   = "NONE"
}

resource "aws_appsync_resolver" "image-resizer-api-resolver" {
  api_id      = aws_appsync_graphql_api.image-resizer-api.id
  type        = "Mutation"
  field       = "publishImageResized"
  data_source = aws_appsync_datasource.none.name

  request_template = <<EOF
  {
    "version": "2018-05-29",
    "payload": {
        "imageName": "$ctx.arguments.imageName",
        "base64Data": "$ctx.arguments.base64Data"
    }
  }
  EOF

  response_template = <<EOF
    $util.toJson($context.result)
  EOF
}
