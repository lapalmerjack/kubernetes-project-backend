package com.mooc.kubernetes;


import java.time.LocalDateTime;
import java.util.List;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

@Service
public class ServiceClass  {


    private final Path path = Paths.get( "image.jpg");

   private final NoteRepository noteRepository;

   @Autowired
  public ServiceClass(NoteRepository noteRepository) {
    this.noteRepository = noteRepository;
  }


  public void saveToDo(NoteEntity noteEntity) {

      if (noteEntity.getNote().length() < 141) {
        noteRepository.save(noteEntity);
      } else {
        throw new RuntimeException("Length of the to do memo is too long");
      }

    }

    public List<NoteEntity> getAllNotes() {

      return noteRepository.findAll();
    }

    public Resource getImage() throws IOException {
        addFileIfNotExist();
       return retrieveImageFromResource();

    }

    private void saveImageFromUrl() throws IOException {
        URL imageUrl = new URL("https://picsum.photos/300");
        InputStream is = imageUrl.openStream();
        OutputStream os = new FileOutputStream(path.toFile());

        byte [] b = new byte[2048];
        int length;
        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
    }

    private boolean isTimeStampValid() {
        Instant currentTimeStamp = Instant.now();
        String oldTimeStamp = retrieveTimeStamp();
        System.out.println("THIS IS THE TIMESTAMP " + oldTimeStamp);
        Instant oldInstant = Instant.parse(oldTimeStamp);
        System.out.println("OLD TIME STAMP " + oldInstant);
        System.out.println("CURRENT TIME STAMP " + currentTimeStamp);
        Duration duration = Duration.between(oldInstant, currentTimeStamp);

        long durationLooksGood =  duration.toHours();
        System.out.println("DURATION " + durationLooksGood);

        return duration.toMinutes() <= 70;


    }



    private Resource retrieveImageFromResource() {
        try {
            System.out.println("RETRIEVING IMAGE");
            return new ByteArrayResource(
                    Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException("Problem finding image:  " + e.getLocalizedMessage());
        }

    }

    private void addFileIfNotExist() throws IOException {

        if (!Files.exists(path) || !isTimeStampValid()) {
            saveImageFromUrl();
            Instant newTimeStamp = Instant.now();
            saveTimeStamp(newTimeStamp.toString());

        }
    }


    private void saveTimeStamp(String timestamp) {
        File file = new File("timestamp.txt");
        System.out.println("File written to: " + file.getAbsolutePath());

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(timestamp);
            System.out.println("FILE WRITTEN");
        } catch (IOException e) {
            System.out.println("FILE NOT WRITTEN" + e.getLocalizedMessage());

        }
    }

    private String retrieveTimeStamp() {

        File file = new File("timestamp.txt");
        try {
            if (!file.exists()) {
                return null; // Or throw an exception or return a default value
            }
            byte[] data = FileCopyUtils.copyToByteArray(file);
            return new String(data);
        } catch (IOException e) {
            // Handle the exception
            return null;
        }
    }
}
