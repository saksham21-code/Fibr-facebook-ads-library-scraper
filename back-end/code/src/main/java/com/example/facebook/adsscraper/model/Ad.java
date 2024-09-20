package com.example.facebook.adsscraper.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Ad {
    private String title;             // Title 
    private String description;       // Description 
    private String media;             // Media link (image/video)
    private String accountName;       // Name of the Facebook account
    private String libraryID;         
    private String fbLink;            // Facebook link 
    private String igLink;            // Instagram link 
}
