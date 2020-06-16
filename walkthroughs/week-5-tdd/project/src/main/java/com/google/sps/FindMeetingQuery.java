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
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public final class FindMeetingQuery {
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

        List<String> allAttendees = Stream.concat(request.getAttendees().stream(), request.getOptionalAttendees().stream())
                                        .collect(Collectors.toList());
        ArrayList<Event> validEventsAllAttendees = ignoreEventsWithoutRequestAttendees(events, allAttendees);

        //if the duration of the meeting is over a day --> no available times
        if(request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
            return new ArrayList<TimeRange>();
        }

        //if there are no events or no attendees, the entire day is available
        if(validEventsAllAttendees.size() == 0 || allAttendees.size() == 0) {
            return Arrays.asList(TimeRange.WHOLE_DAY);
        }

        ArrayList<TimeRange> availableTimesAllAttendees = getAvailableTimes(validEventsAllAttendees, request);

        //if there was one or more times available with optional attendees, return those times
        if(availableTimesAllAttendees.size() > 0) {
            return availableTimesAllAttendees;
        }

        //if there is no time with both mandatory and optional, just look at mandatory attendees
        ArrayList<Event> validEventsMandatoryAttendees = ignoreEventsWithoutRequestAttendees(events, request.getAttendees());
        
        //if we got to this statement, that means there were only optional attendees and no mandatory attendees,
        //so there is no time available
        if(request.getAttendees().size() == 0) {
            return new ArrayList<TimeRange>();
        }

        //there are mandatory employees with no events --> the whole day is available
        if(validEventsMandatoryAttendees.size() == 0) {
            return Arrays.asList(TimeRange.WHOLE_DAY);
        }

        return getAvailableTimes(validEventsMandatoryAttendees, request);

    }


    /**
    * Gets all the available time ranges for the meeting request. 
    * @return ArrayList<TimeRange>
    */
    private ArrayList<TimeRange> getAvailableTimes(Collection<Event> events, MeetingRequest request) {
        ArrayList<TimeRange> availableTimes = new ArrayList<TimeRange>();

        ArrayList<Event> sortedEventsList = sortEventsByStartTime(events);

        //start time of the time range
        int startTime = TimeRange.START_OF_DAY;
        long duration = request.getDuration();
        int i = 0;

        //while there is enough time for the requested meeting from the startTime of the timerange till the end of the day
        while(startTime + duration <= TimeRange.END_OF_DAY) {

            int endTime = startTime;
            TimeRange currentEventTime = sortedEventsList.get(i).getWhen();

            while(i < sortedEventsList.size()) {
                
                currentEventTime = sortedEventsList.get(i).getWhen();
                
                i++;
                //if there is enough time to have the requested meeting from startTime to the beginning of the event we're currently looking at, we can create a new timerange
                //from startTime - the end of the current event time
                if(currentEventTime.start() >= startTime + duration) {
                    endTime = currentEventTime.start();
                    break;
                //if there is an overlap between the current event and the requested event, make the new startTime the end of the current event
                } else if(currentEventTime.start() >= startTime && currentEventTime.start() < startTime + duration) {
                    startTime = currentEventTime.end();
                } 

            }

            //if the endTime of the time range (which was set to startTime) is greater than or equal to the duration of the meeting, it can be added as a new time range
            if(endTime >= startTime + duration) {
                availableTimes.add(TimeRange.fromStartEnd(startTime, endTime, false));
            }

            //takes care of overlapping meetings
            //for example, currentEvent = meeting 2
            //meeting 1: 8:30am - 10am
            //meeting 2: 9am - 9:30am
            //we want the startTime of the next timerange to still be 10am, not 9:30am because meeting 1 is still going on
            startTime = currentEventTime.end() > startTime ? currentEventTime.end() : startTime;

            //if we've looked at all meetings and there is enough time (until the end of the day) for the new meeting, create a new time range until the end of the day
            if(i == sortedEventsList.size() && TimeRange.END_OF_DAY - startTime >= duration) {
                availableTimes.add(TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true));
                break;
            }

        }

        return availableTimes;

    }

    /**
    * Creates a new ArrayList containing events with attendee overlap with the requested meeting. If no required attendees in the requested meeting
    * are required at a specific event, that event will not be considered when creating TimeRange for the requested meeting.
    * @return ArrayList<Event>
    */
    private ArrayList<Event> ignoreEventsWithoutRequestAttendees(Collection<Event> events, Collection<String> requestAttendees) {
        ArrayList<Event> validEvents = new ArrayList<Event>();

        for(Event event : events) {
            if(anyAttendeeBusy(event, requestAttendees)) {
                validEvents.add(event);
            }
        }

        return validEvents;

    }

    /**
    * Checks if there are any attendees required at the requested meeting that are also required at the specified Event.
    * @return boolean, true if there is an attendee overlap, false otherwise
    */
    private boolean anyAttendeeBusy(Event event, Collection<String> attendees) {
        Set<String> requestAttendees = new HashSet(attendees);
        Set<String> eventAttendees = event.getAttendees();

        for(String attendee : requestAttendees) {
            if(eventAttendees.contains(attendee)) {
                return true;
            }
        }

        return false;
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
