package com.mooc.kubernetes;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@CrossOrigin(origins = "*")
public class Requests {

    private final ServiceClass service;

    @Value("${server.port}")
    private int serverPort;



    private static final Logger logger = LoggerFactory.getLogger(Requests.class);

    private List<NoteEntity> toDos = new ArrayList<>();

    @Autowired
    private DatabaseHealthIndicator databaseHealthIndicator;

    @Value("${nats.url}")
    private String natsUrl;

    @Value("${nats.subject}")
    private String natsSubject;

    private final NatsService natsService;

    @Autowired
    public Requests(ServiceClass service, NatsService natsService) {
        this.service = service;
        this.natsService = natsService;

    }


    @GetMapping("/")
    public String response() {

        return "Server started in port " + serverPort;
    }

    @GetMapping("/frontend-health")
    public ResponseEntity<String> ingressResponse() {
        return new ResponseEntity<>("Frontend may connect", HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToDo (@RequestBody NoteEntity toDo) {
        String message = "New task added to list: " + toDo.getNote();
        logger.info("{} is being sent from frontend", toDo.getId());

        service.saveToDo(toDo);
        natsService.publishMessageToNats(message);

        return new ResponseEntity<>(toDo, HttpStatus.OK);
    }

    @PutMapping("/isDone/{id}")
    public ResponseEntity<?> ChangeIfDoneOrNotDone(@PathVariable Long id) {

        NoteEntity updatedTask = service.updateTaskToDoneOrNotDone(id);

        String message = "Task " + updatedTask.getNote() + " has been updated to " + updatedTask.getIsDone();

        natsService.publishMessageToNats(message);
        logger.info("Task {} has been updated to {}", updatedTask.getNote(), updatedTask.getIsDone());


        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

    @GetMapping("/getToDos")
    public ResponseEntity<List<NoteEntity>> getToDos () {

        List<NoteEntity> notes = service.getAllNotes();
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }



    @GetMapping("/image")
    public ResponseEntity<Resource> getImage() throws IOException {
        Resource image = service.getImage();

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.IMAGE_JPEG);

        return new ResponseEntity<>(image, header, HttpStatus.OK);

    }



}
