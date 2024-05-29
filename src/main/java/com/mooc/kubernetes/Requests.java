package com.mooc.kubernetes;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    public Requests(ServiceClass service) {
        this.service = service;
    }

    private List<NoteEntity> toDos = new ArrayList<>();

    @Autowired
    private DatabaseHealthIndicator databaseHealthIndicator;



    @GetMapping("/")
    public String response() {

        return "Server started in port " + serverPort;
    }

    @GetMapping("/health")
    public void readiness() throws SQLException {
        if(databaseHealthIndicator.health().getStatus().equals(Status.UP)) {
            System.out.println("HEALTH CHECK PASSED");
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
    public ResponseEntity<NoteEntity> addToDo (@RequestBody NoteEntity toDo) {
        System.out.println(toDo.getNote() + " is being sent from frontend");
        System.out.println(toDo.getIsDone() + " is the boolean");

        service.saveToDo(toDo);

        return new ResponseEntity<>(toDo, HttpStatus.OK);
    }

    @PutMapping("/isDone/?")
    public ResponseEntity<NoteEntity> ChangeIfDoneOrNotDone(@PathVariable Long id) {
        NoteEntity updatedTask = service.updateTaskToDoneOrNotDone(id);

        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

    @GetMapping("/getToDos")
    public ResponseEntity<List<NoteEntity>> getToDos () {

        List<NoteEntity> notes = service.getAllNotes();

        System.out.println("Retrieved todos");


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
