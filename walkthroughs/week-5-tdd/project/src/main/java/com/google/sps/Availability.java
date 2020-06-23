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

package com.google.sps;

import java.util.ArrayList;

/** Stores a time range and a list of people available in that time range. */
public final class Availability {

    private TimeRange time;
    private int numAvailablePeople;

    public Availability(TimeRange time) {
        this.time = time;
        numAvailablePeople = 0;
    }

    public TimeRange getTime() {
        return this.time;
    }

    public void incrementNumAvailablePeople() {
        this.numAvailablePeople++;
    }

    public int getNumAvailablePeople() {
        return this.numAvailablePeople;
    }

}
