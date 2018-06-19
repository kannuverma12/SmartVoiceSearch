package com.smart.search.google;

import com.google.cloud.speech.spi.v1.SpeechClient;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GoogleSTT {
  public static void main(String... args) throws Exception {
	  //String fileName = "/Users/bng/Documents/remote/Monitor/mayIHelp.wav";
	  String fileName = "/Users/karan.verma/Downloads/testServer.wav";
	  speechToText(fileName);
  }
  
  public static String speechToText(String fileName){
	  String speechToText = "";
	  try{
			// Instantiates a client
			SpeechClient speech = SpeechClient.create();
			
			// The path to the audio file to transcribe
			//String fileName = "/Users/bng/Downloads/test.raw";
			
			//String fileName = "/Users/bng/Documents/remote/Monitor/mayIHelp.wav";
			
			
			// Reads the audio file into memory
			Path path = Paths.get(fileName);
			byte[] data = Files.readAllBytes(path);
			ByteString audioBytes = ByteString.copyFrom(data);
			
			// Builds the sync recognize request
			RecognitionConfig config = RecognitionConfig.newBuilder()
			    .setEncoding(AudioEncoding.LINEAR16)
				//.setEncoding(AudioEncoding.MULAW)
			    .setSampleRateHertz(16000)
			    .setLanguageCode("en-GB")
			    .build();
			RecognitionAudio audio = RecognitionAudio.newBuilder()
			    .setContent(audioBytes)
			    .build();
			
			// Performs speech recognition on the audio file
			RecognizeResponse response = speech.recognize(config, audio);
			List<SpeechRecognitionResult> results = response.getResultsList();
			
			
			for (SpeechRecognitionResult result: results) {
			  List<SpeechRecognitionAlternative> alternatives = result.getAlternativesList();
			  for (SpeechRecognitionAlternative alternative: alternatives) {
			    System.out.printf("Transcription: %s%n", alternative.getTranscript());
			    speechToText = alternative.getTranscript();
			  }
			}
			speech.close();
			
	  }
	  catch(Exception e){
		  e.printStackTrace();
	  }
	  return speechToText;
  }
}