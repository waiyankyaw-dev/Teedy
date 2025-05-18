package com.sismics.docs.core.service;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

//AIzaSyC3bZnXH1CrrAZLgcPEe-NJRvbojFBPIUU
//AIzaSyDOjDKYJjWQ4agveaXdR9RmWqdGrS7fsE8(malaysia)
public class TranslationService {
    private static final String GOOGLE_API_KEY = "AIzaSyC3bZnXH1CrrAZLgcPEe-NJRvbojFBPIUU";

    private static Translate translate = TranslateOptions.newBuilder()
            .setApiKey(GOOGLE_API_KEY)
            .build()
            .getService();

    public static String translateText(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        try {
            Translation translation = translate.translate(
                    text,
                    Translate.TranslateOption.targetLanguage(targetLanguage)
            );
            return translation.getTranslatedText();
        } catch (Exception e) {
            e.printStackTrace();
            return "Translation error: " + e.getMessage();
        }
    }

}