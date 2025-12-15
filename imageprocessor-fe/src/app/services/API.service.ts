/* tslint:disable */
/* eslint-disable */
//  This file was automatically generated and should not be edited.
import { Injectable } from "@angular/core";
import { generateClient, GraphQLResult } from "aws-amplify/api";
import { Observable } from "rxjs";

export type __SubscriptionContainer = {
  imageResized: ImageResizedSubscription;
};

export type Image = {
  __typename: "Image";
  imageName: string;
  base64Data: string;
};

export type PublishImageResizedMutation = {
  __typename: "Image";
  imageName: string;
  base64Data: string;
};

export type ImageResizedSubscription = {
  __typename: "Image";
  imageName: string;
  base64Data: string;
};

@Injectable({
  providedIn: "root"
})
export class APIService {
  public client;
  constructor() {
    this.client = generateClient();
  }
  async PublishImageResized(
    imageName: string,
    base64Data: string
  ): Promise<PublishImageResizedMutation> {
    const statement = `mutation PublishImageResized($imageName: String!, $base64Data: String!) {
        publishImageResized(imageName: $imageName, base64Data: $base64Data) {
          __typename
          imageName
          base64Data
        }
      }`;
    const gqlAPIServiceArguments: any = {
      imageName,
      base64Data
    };
    const response = (await this.client.graphql({
      query: statement,
      variables: gqlAPIServiceArguments
    })) as any;
    return <PublishImageResizedMutation>response.data.publishImageResized;
  }
  async Dummy(): Promise<string | null> {
    const statement = `query Dummy {
        dummy
      }`;
    const response = (await this.client.graphql({ query: statement })) as any;
    return <string | null>response.data.dummy;
  }
  ImageResizedListener(): Observable<
    GraphQLResult<Pick<__SubscriptionContainer, "imageResized">>
  > {
    return this.client.graphql({
      query: `subscription ImageResized {
        imageResized {
          __typename
          imageName
          base64Data
        }
      }`
    }) as any;
  }
}
