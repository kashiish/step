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

package com.google.sps.utilities;

import java.nio.charset.StandardCharsets;
import java.lang.String;
import java.util.HashMap;

public class InputCleaner {

    //HashMap that stores an ASCII value as a key and its HTML entity
    private static HashMap<Integer, String> charToEntity;

    public InputCleaner() {

        charToEntity = new HashMap<Integer, String>();

        //common HTML character ASCII codes and their HTML entities
        charToEntity.put(38, "&amp;"); // &
        charToEntity.put(60, "&lt;"); // <
        charToEntity.put(62, "&gt;"); // >
        charToEntity.put(34, "&quot"); // "
        charToEntity.put(39, "&#x27;"); // '
        charToEntity.put(47, "&#x2F;"); // /

    }

    /**
    * Replaces common HTML characters with their HTML entities to prevent HTML injection.
    * @return String
    */
    public static String clean(String str) {

        char[] characters = str.toCharArray();
        String[] cleanString = new String[str.length()];

        for(int i = 0; i < characters.length; i++) {
            //if the character is in the HashMap, it is a common HTML character and needs to be replaced
            if(charToEntity.containsKey(characters[i])) {
                cleanString[i] = charToEntity.get(characters[i]);
            } else {
                cleanString[i] = Character.toString(characters[i]);
            }
        }

        return String.join("", cleanString);
    }

}