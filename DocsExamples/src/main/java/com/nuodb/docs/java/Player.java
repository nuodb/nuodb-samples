package com.nuodb.docs.java;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Java entity corresponding to a row in the Players table.
 * 
 * @author Paul Chapman
 */
@Entity
@Table(name = "Players")
public class Player {
    @Id
    @Column(name = "PLAYERID")
    String playerId;
    String firstName;
    String lastName;
    int height;
    int weight;
    int firstNHL;
    int lastNHL;

    String position;
    int birthYear;
    int birthMon;
    int birthDay;
    String birthCountry;
    String birthState;
    String birthCity;

    public Player() {
        // Hibernate/JPA require a default constructor
    }
}