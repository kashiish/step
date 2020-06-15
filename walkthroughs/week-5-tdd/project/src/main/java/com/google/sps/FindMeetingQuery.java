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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;


public final class FindMeetingQuery {
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        ArrayList<TimeRange> availableTimes = new ArrayList<TimeRange>();

        //if there are no events or no attendees, the entire day is available
        if(events.size() == 0 || request.getAttendees().size() == 0) {
            availableTimes.add(TimeRange.WHOLE_DAY);
            return availableTimes;
        }

        ArrayList<Event> sortedEventsList = sortEventsByTime(events);


    }

    /**
    * Creates a sorted list of events based on the event's start time.
    * @return ArrayList<Event>
    */
    private ArrayList<Event> sortEventsByStartTime(Collection<Event> events) {
        ArrayList<Event> eventsList = new ArrayList<Event>(events);

        Collections.sort(eventsList, new Comparator<Event>() {
            @Override
            public int compare(Event a, Event b) {
                return TimeRange.ORDER_BY_START.compare(a.getWhen(), b.getWhen());
            }
        });

        return eventsList;

    }
}
