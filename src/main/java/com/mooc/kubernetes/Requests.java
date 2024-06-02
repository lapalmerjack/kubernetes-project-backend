package com.mooc.kubernetes;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.nats.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Status;
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


    private Connection natsConnection;

    private static final Logger logger = LoggerFactory.getLogger(Requests.class);

    @Autowired
    public Requests(ServiceClass service) {
        this.service = service;

    }

    private List<NoteEntity> toDos = new ArrayList<>();

    @Autowired
    private DatabaseHealthIndicator databaseHealthIndicator;

    @Value("${nats.url}")
    private String natsUrl;

    @Value("${nats.subject")
    private String natsSubject;


    @GetMapping("/")
    public String response() {

        return "Server started in port " + serverPort;
    }

    @GetMapping("/health")
    public void readiness() throws SQLException {
        if(databaseHealthIndicator.health().getStatus().equals(Status.UP)) {
            logger.info("Health check passed!");
            return;
        } else {
            throw new SQLException("Database is not available");
        }
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
        publishMessageToNats(message);

        return new ResponseEntity<>(toDo, HttpStatus.OK);
    }

    @PutMapping("/isDone/{id}")
    public ResponseEntity<?> ChangeIfDoneOrNotDone(@PathVariable Long id) {

        NoteEntity updatedTask = service.updateTaskToDoneOrNotDone(id);

        String message = "Task " + updatedTask.getNote() + " has been updated to " + updatedTask.getIsDone();

        publishMessageToNats(message);
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


    public void publishMessageToNats(String message) {
        try {
            natsConnection.publish(natsSubject, message.getBytes());
            logger.info("Published message to NATS: {}", message);
        } catch (Exception e) {
            logger.error("Failed to publish message to NATS", e);
        }
    }
}
