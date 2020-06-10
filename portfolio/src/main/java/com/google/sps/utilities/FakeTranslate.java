package com.google.sps.utilities;

import java.lang.String;
import java.util.Random; 

public final class FakeTranslate implements CommentTranslate {

    @Override
    public String translateMessage(String message, String languageCode) {
        //Finnish for "This is a test."
        String translatedMessage = "Detta är ett prov.";
        return translatedMessage;
    }

    @Override
    public String detectLanguage(String message) {
        //return a random language code
        String[] codes = {"en", "fr", "es"};
        Random rand = new Random();
        return codes[rand.nextInt(codes.length)];
    }
}
