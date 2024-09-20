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
    private String title;             // Title of the ad
    private String description;       // Description of the ad
    private String media;             // Media link (image/video)
    private String accountName;       // Name of the Facebook account
    private String libraryID;         // Unique identifier for the ad in the library
    private String fbLink;            // Facebook link for the ad/account
    private String igLink;            // Instagram link for the ad/account
}
