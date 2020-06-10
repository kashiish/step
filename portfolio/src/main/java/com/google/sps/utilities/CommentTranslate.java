package com.google.sps.utilities;

import java.lang.String;

public interface CommentTranslate {

    public String translateMessage(String message, String languageCode);

    public String detectLanguage(String message);


}