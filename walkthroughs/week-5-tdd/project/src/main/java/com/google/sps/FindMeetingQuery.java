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
import java.util.Arrays;


public final class FindMeetingQuery {
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        //if the duration of the meeting is over a day --> no available times
        if(request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
            return new ArrayList<TimeRange>();
        }

        //if there are no events or no attendees, the entire day is available
        if(events.size() == 0 || request.getAttendees().size() == 0) {
            return Arrays.asList(TimeRange.WHOLE_DAY);;
        }

        return getAvailableTimes(request);

    }

    /**
    * Gets all the available time ranges for the meeting request. 
    * @return ArrayList<TimeRange>
    */
    private ArrayList<TimeRange> getAvailableTimes(MeetingRequest request) {
        ArrayList<TimeRange> availableTimes = new ArrayList<TimeRange>();

        ArrayList<Event> sortedEventsList = sortEventsByStartTime(events);

        //start time of the time range
        int startTime = TimeRange.START_OF_DAY;
        long duration = request.getDuration();
        int i = 0;

        while(startTime + duration <= TimeRange.END_OF_DAY) {
            int endTime = startTime;

            TimeRange currentEventTime = sortedEventsList.get(i).getWhen();

            //currentEventTime.start() > startTime + duration checks if there is enough time for the meeting
            while(currentEventTime.start() > startTime + duration && i < sortedEventsList.size()) {
                i++;
                //set the end time of the time range to the start of this meeting
                endTime = currentEventTime.start();
                if(i < sortedEventsList.size()) {
                    currentEventTime = sortedEventsList.get(i).getWhen();
                }
            }

            //if the endTime of the time range (which was set to startTime) is greater than or equal to the duration of the meeting, it can be added as a new time range
            if(endTime >= startTime + duration) {
                availableTimes.add(TimeRange.fromStartEnd(startTime, endTime, false));
            }

            //if we haven't looked at all events, set the next time range's start time to the end of the current event 
            if(i < sortedEventsList.size()) {
                startTime = currentEventTime.end();
            }

            //if we've looked at all meetings and there is enough time (until the end of the day) for the new meeting, create a new time range until the end of the day
            if(i == sortedEventsList.size() && TimeRange.END_OF_DAY - startTime >= duration) {
                availableTimes.add(TimeRange.fromStartEnd(currentEventTime.end(), TimeRange.END_OF_DAY, true));
                break;
            }

        }

        return availableTimes;

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
