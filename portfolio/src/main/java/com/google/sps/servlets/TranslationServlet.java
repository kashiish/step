// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.sps.utilities.GoogleTranslate;
import com.google.sps.utilities.FakeTranslate;

@WebServlet("/translate")
public class TranslationServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String originalMessage = request.getParameter("message");
        String languageCode = request.getParameter("languageCode");

        //DEPLOY
        // GoogleTranslate gTranslate = new GoogleTranslate();
        // String translatedMessage = gTranslate.translateMessage(originalMessage, languageCode);

        //DEV
        FakeTranslate fakeTranslate = new FakeTranslate();
        String translatedMessage = fakeTranslate.translateMessage(originalMessage, languageCode);

        response.setContentType("plain/text; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(translatedMessage);
        
    }
}
