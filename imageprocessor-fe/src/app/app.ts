import { Component, signal, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Upload } from './upload/upload';
import { Amplify } from "aws-amplify";
import { APIService } from './services/API.service';
import { CommonModule } from '@angular/common';
import { environment } from '../environments/environment.development';
import { ResolvedGraphQLAuthModes } from '@aws-amplify/api-graphql';

Amplify.configure({
  API: {
    GraphQL: {
      endpoint: environment.graphqlEndpoint,
      region: environment.graphqlRegion,
      defaultAuthMode: environment.graphqlDefaultAuthMode as ResolvedGraphQLAuthModes,
      apiKey: environment.graphqlApiKey
    }
  }
});

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Upload, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected imageSrc = signal<string>("");
  private APIService = inject(APIService);

  ngOnInit(): void {
    this.APIService.ImageResizedListener().subscribe(data => {
      this.imageSrc.set(`data:image/png;base64,${data.data.imageResized.base64Data}`);
    });
  };
}
