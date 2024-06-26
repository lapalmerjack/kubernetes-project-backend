package com.mooc.kubernetes;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Entity(name = "notes")
public class NoteEntity {

  @jakarta.persistence.Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String note;
  private Boolean isDone;


  public void setDone() {
    isDone = !isDone;
  }
}
