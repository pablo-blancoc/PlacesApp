# FBU App: Places

## Table of Contents
1. [Overview](#overview)
    * [Description](#description)
    * [App Evaluation](#app-evaluation)
2. [Product Spec](#product-spec)
    * [User Stories](#user-stories)
    * [Requirements](#basic-requirements)
    * [Screen Archetypes](#screen-archetypes)
    * [Navigation](#navigation)
    * [Wireframes](#wireframes)

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
![Wireframes image](https://github.com/pablo-blancoc/PlacesApp/blob/main/files/wireframes.png | width=100)
