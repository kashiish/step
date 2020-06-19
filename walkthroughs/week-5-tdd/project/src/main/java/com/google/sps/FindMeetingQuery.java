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
        List<Event> validEventsAllAttendees = ignoreEventsWithoutRequestAttendees(events, allAttendees);

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

        //if we got to this statement, that means there were only optional attendees and no mandatory attendees,
        //so there is no time available
        if(request.getAttendees().size() == 0) {
            return new ArrayList<TimeRange>();
        }

        return getTimesWithMaxOptionalAttendees(events, request);

    }

    private List<TimeRange> getTimesWithMaxOptionalAttendees(Collection<Event> events, MeetingRequest request) {

        //if there is no time with both mandatory and optional, just look at mandatory attendees
        List<Event> validEventsMandatoryAttendees = ignoreEventsWithoutRequestAttendees(events, request.getAttendees());

        //there are mandatory employees with no events --> the whole day is available
        if(validEventsMandatoryAttendees.size() == 0) {
            return Arrays.asList(TimeRange.WHOLE_DAY);
        }
        
        ArrayList<TimeRange> mandatoryAttendeeTimes = getAvailableTimes(validEventsMandatoryAttendees, request);
        ArrayList<TimeRange> optionalAttendeeTimes = getAvailableTimesForOptionalAttendees(events, request);

        ArrayList<TimeRange> overlappingTimes = getTimesThatOverlapWithMandatoryAttendees(mandatoryAttendeeTimes, optionalAttendeeTimes, request.getDuration());

        List<Availability> availabilityForTimes = getAvailabilityForTimes(overlappingTimes, request.getDuration());
        
        //if there are no times where optional attendees' availability coincides with mandatory attendees' availability, just return mandatory attendee times
        if(availabilityForTimes.size() == 0) {
            return mandatoryAttendeeTimes;
        }
        
        Collections.sort(availabilityForTimes, new Comparator<Availability>() {
            @Override
            public int compare(Availability a, Availability b) {
                return b.getNumAvailablePeople() - a.getNumAvailablePeople();
            }
        });

        int max = 0;

        ArrayList<TimeRange> results = new ArrayList<TimeRange>();

        //get the time ranges that have the most number of available optional attendees
        for(Availability a : availabilityForTimes) {
            if(a.getNumAvailablePeople() < max) {
                break;
            }

            max = a.getNumAvailablePeople();

            if(results.contains(a.getTime())) {
                continue;
            }

            results.add(a.getTime());
        }

        return results;
        
    }

    /**
    * The purpose of this method is to find times where more than one optional attendee is available. The parameter, times,
    * is a list of all times where one optional attendee is free.
    * @return List<Availability> 
    */
    private List<Availability> getAvailabilityForTimes(ArrayList<TimeRange> times, long duration) {

        Collections.sort(times, TimeRange.ORDER_BY_START);

        //create Availability objects with times
        List<Availability> sortedAvailability = times.stream().map(a -> new Availability(a)).collect(Collectors.toList());

        //this for loop finds new ranges where the times in the times list overlap with each other
        //for example, 
        //if times = 8:30-9, 8:30-9:30, 8:45-9, 9-9:30
        //we will find 8:30-9, 8:45-9, 9-9:30 as overlapping times where more than one optional attendee is free
        for(TimeRange time : times) {
            
            //stores new overlapped times where multiple optional attendees are free
            ArrayList<Availability> overlappedAvailability = new ArrayList<Availability>();

            for(Availability availability : sortedAvailability) {

                //if the times don't overlap, skip this availability
                if(!time.overlaps(availability.getTime())) {
                    continue;
                }

                TimeRange overlapTime = TimeRange.getOverlappedTime(time, availability.getTime());
                if(overlapTime.duration() >= duration) {
                    
                    //if the overlapped time is the same as the availability we're looking at, just increment the number of people that can attend at the availability's time
                    if(overlapTime.equals(availability.getTime())) {
                        availability.incrementNumAvailablePeople();
                    } else {
                        //create a new availability with the overlapped time
                        Availability overlapTimeAvailability = new Availability(overlapTime);
                        overlapTimeAvailability.incrementNumAvailablePeople();
                        overlappedAvailability.add(overlapTimeAvailability);
                    }


                }
            }

            //the next time in the times list will check if it overlaps with any of the other times in the list or any of the new overlapped times we created above
            sortedAvailability.addAll(overlappedAvailability);
            overlappedAvailability = new ArrayList<Availability>();

        }


        return sortedAvailability;
        
    }
 
    /**
    * Gets all the times where optional attendee times overlap with mandatory attendee times and are long enough for the requested meeting.
    * @return ArrayList<TimeRange>
    */
    private ArrayList<TimeRange> getTimesThatOverlapWithMandatoryAttendees(ArrayList<TimeRange> mandatoryAttendeeTimes, ArrayList<TimeRange> optionalAttendeeTimes, long duration) {
        ArrayList<TimeRange> availableTimes = new ArrayList<TimeRange>();

        for(TimeRange optional : optionalAttendeeTimes) {
            
            for(TimeRange mandatory : mandatoryAttendeeTimes) {

                if(optional.overlaps(mandatory)) {

                    TimeRange overlapTime = TimeRange.getOverlappedTime(optional, mandatory);
                    
                    if(overlapTime.duration() >= duration) {
                        availableTimes.add(overlapTime);
                    }

                }
            }
        }

        return availableTimes;

        
    }


    /**
    * Gets all times available for each optional attendee in one list. 
    * @return ArrayList<TimeRange>
    */
    private ArrayList<TimeRange> getAvailableTimesForOptionalAttendees(Collection<Event> events, MeetingRequest request) {
        //every time in this list is a time that one optional attendee is available
        ArrayList<TimeRange> timesForOptionalAttendees = new ArrayList<TimeRange>();

        for(String attendee : request.getOptionalAttendees()) {
            List<Event> relevantEvents = ignoreEventsWithoutRequestAttendees(events, Arrays.asList(attendee));
            ArrayList<TimeRange> times = getAvailableTimes(relevantEvents, request);

            for(TimeRange time : times) {
                timesForOptionalAttendees.add(time);
            }
        }

        return timesForOptionalAttendees;
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
            startTime = Math.max(currentEventTime.end(), startTime);

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
    * @return List<Event>
    */
    private List<Event> ignoreEventsWithoutRequestAttendees(Collection<Event> events, Collection<String> requestAttendees) {
        return events
        .stream()
        .filter(e -> anyAttendeeBusy(e, requestAttendees))
        .collect(Collectors.toList());
    }

    /**
    * Checks if there are any attendees required at the requested meeting that are also required at the specified Event.
    * @return boolean, true if there is an attendee overlap, false otherwise
    */
    private boolean anyAttendeeBusy(Event event, Collection<String> requestAttendees) {
        Set<String> eventAttendees = event.getAttendees();

        return requestAttendees
        .stream()
        .map(a -> eventAttendees.contains(a))
        .reduce(false, (a, b) -> a || b);
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
