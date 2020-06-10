package com.google.sps.utilities;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.Detection;
import java.lang.String;

public final class GoogleTranslate implements CommentTranslate {
    
    @Override
    public String translateMessage(String message, String languageCode) {
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        Translation translation = translate.translate(message, Translate.TranslateOption.targetLanguage(languageCode));
        String translatedMessage = translation.getTranslatedText();
        return translatedMessage;
    }

    @Override
    public String detectLanguage(String message) {
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        Detection detection = translate.detect(message);
        return detection.getLanguage();
    }
}
