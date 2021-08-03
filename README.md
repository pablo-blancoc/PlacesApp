# FBU App: Places

## Table of Contents
1. [Overview](#overview)
    * [Description](#description)
    * [App Evaluation](#app-evaluation)
    * [Facebook requirements](#facebook-requirements)
    * [Demo video](#demo)
2. [Product Spec](#product-spec)
    * [User Stories](#user-stories)
    * [Requirements](#basic-requirements)
    * [Screen Archetypes](#screen-archetypes)
    * [Navigation](#navigation)
    * [Wireframes](#wireframes)
    * [Schema](#schema)
3. [Important features](#links)
    * [Deep learning model](#deep-learning-model)
    * [Backend server](#backend-server)

## Overview

### Description
This apps allows you to save restaurants, coffee-shops, and more in your own personal cool-places directory. This way you will always have places you really liked and would like to go again at 3-clicks distance. Also, you can share any place with your friends so they can go to, get directions to any place you have liked, and also get to see what's new near your location, and places you may like in your feed.

### App Evaluation
- **Category:** Utilities (because you save what you like to keep it handy) and social (because you can share with your friends and see what they like) and follow people to get places they have gone too
- **Mobile:** Mobile-first (android)
- **Story:** As a user you can save your created places privately or share them with your followers (or external apps like facebook). Also you can view your friends profile's to see what they like and what have they shared with the world. People can follow you to see what you share. 
- **Market:** People looking for cool places to dine, drink a coffee, get a drink, or spend some time in a city. "Foodies".
- **Habit:** People come back because they want to see what cool places can they visit next, where to eat according to their interests, find places nearby, etc.
- **Scope:** Starting it is a place where you share your likes and cool places you know, and follow people that do that too. Later on, the app could be monetized by selling place-owners spaces on people's feed to show their place like advertisements, according to what people tend to like (interests). Get directions using Google Maps, or notifications whenever someone you follow posts something you may like. 

### Facebook requirements
- [x] Your app has multiple views
- [x] Your app interacts with a database
    - Parse server
- [x] You can log in/log out of your app as a user
- [x] You can sign up with a new user profile
- [x] Your app integrates with at least one SDK (e.g. Google Maps SDK, Facebook SDK) or API (that you didn’t learn about in CodePath)
    - OneSignal SDK and Rest API for notifications
    - My own bakcned's API (running locally on my network)
- [x] Your app uses at least one gesture (e.g. double tap to like, e.g. pinch to scale)
    - Long click to add a pin to a map
- [x] Your app uses at least one animation (e.g. fade in/out, e.g. animating a view growing and shrinking)
    - Like and Call buttons grow & shrink when entering place's detail page
    - Map marker falls when creating a marker
- [x] Your app incorporates at least one external library to add visual polish
    - Toasty was added to show information to the user in a better way
- [x] Your app provides opportunities for you to overcome difficult/ambiguous technical problems (more below)
    - **My own Convolutional Neural Network based on MobileNetV2 architecture was embedded into the app to classify images into the app's 5 categories**
        - Used a scrapper on Javascript to get up to 500 images from a search on Google Images
        - Used python to download the images, create the model, preprocess the images, train the model, and export the model as TensorFlow Lite
        - Used Visual Studio tools to embedd the model into the app
        - Used Java to preprocess the image taken from the cell-phone and process the output from the model in order to predict the category
        - [More information](https://github.com/pablo-blancoc/PlacesApp-ml)
    - **Created a Recommendation System using k nearest-neighbors algorithm**
        - Get all information about the users using the app
        - Find 4 most similar users to you
        - Recommend up to 5 places they have liked but you haven't
        - [More information](https://github.com/pablo-blancoc/PlacesApp-server)
    - **Implemented tf-idf and cosine similarity algorithms to enhance search**
        - [More information](https://github.com/pablo-blancoc/PlacesApp-server)


### Demo
![[Demo video](https://github.com/pablo-blancoc/PlacesApp/blob/main/files/video_thumbnail.png)](https://www.youtube.com/watch?v=1xeu8BVRBQQ)


## Product Spec

### User Stories

**Required Stories**
* Log in / Log out
* Register
* Create a place and share with the world or save privately
* Get places people you follow have posted on feed (chronological order)
* Follow people
* See people's profiles
* Get directions to a place using Google Maps

**Nice to have Stories**
* Find nearby places using Google Maps API
* See places you may like using a Recommendation System
* Get notifications whenever people you decide post new things

### Basic Requirements
- Parse as database and backend
- Google Maps SDK or API to find directions and near me

### Screen Archetypes
* Login 
    * Login to app
* Register 
    * Fill your data and register as a new user
* Home 
    * See your feed with post from people you follow
* Place Detail 
    * See all the information from a place someone has posted
* Liked 
    * Places you have liked but you didn't create
* Saved 
    * Places you created privately for yourself
* Profile 
    * See other peoples profiles to follow them, see their posts
    * See your profile to remove a place you created or edit it
* Create place
    * Fill information to create a new place

### Navigation
* Tabs
    * Home / Feed
    * My likes
    * Saved
    * My profile

* Internal navigation (Where each screen can take you)
    * Login
        * Sign up
	* Sign in
    * Sign up
        * Internal signup
    * Home
        * Detail
        * Profile
    * Detail
        * Directions to place
        * Call if has phone
    * Liked
        * Detail
	* Unlike
    * Saved
        * Detail
	* Delete or edit
    * Profile
        * Detail
    * Create place 
        * Fill information and create a place
 
### Wireframes
![Wireframes image](https://github.com/pablo-blancoc/PlacesApp/blob/main/files/wireframes.png)
 
### Schema
![Schema image](https://github.com/pablo-blancoc/PlacesApp/blob/main/files/db_schema.png)

## Links

### Deep Learning model

In this [repository](https://github.com/pablo-blancoc/PlacesApp-ml) it is all the information of the Deep Learning model implemented inside the app which classifies an image taken into the 5 categories the app has.

### Backend server

In this [repository](https://github.com/pablo-blancoc/PlacesApp-server) it is all the information of the backend server that runs locally and that allows the app do things like recommend a place to a user, and send notifications to users.


