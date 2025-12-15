resource "aws_dynamodb_table" "resized_images" {
  name         = "hadervic-resized-images-meta"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "imageId"

  attribute {
    name = "imageId"
    type = "S"
  }

  attribute {
    name = "resizedKey"
    type = "S"
  }

  global_secondary_index {
    name            = "resizedKey-index"
    hash_key        = "resizedKey"
    projection_type = "ALL"
  }
}
