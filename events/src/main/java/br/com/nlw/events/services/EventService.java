package br.com.nlw.events.services;

import br.com.nlw.events.model.Event;
import br.com.nlw.events.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public Event addNewEvent(Event event){
        event.setPrettyName(event.getTitle().toLowerCase().replaceAll(" ", "-"));
        return eventRepository.save(event);
    }

    public List<Event> getAllEvents(){
        return (List<Event>)eventRepository.findAll();
    }

    public ResponseEntity<Event> getByPrettyName(String prettyName){
        Event evt = eventRepository.findByPrettyName(prettyName);
        if(evt != null){
            return ResponseEntity.ok().body(evt);
        }
        return ResponseEntity.notFound().build();
    }
}

