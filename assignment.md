# Lab 02: Simple NEWS reader (RSS2.0 and/or Atom)

## The idea

Create an application that allows the user to read content from any RSS feed. The app will consist of 3 activities: one with the list of items (`ListView/RecyclerView`, for selecting content), one for article content display (for reading content), and User Preferences (for user to specify the preferences). 


## Preferences

The user should be able to specify in the preferences the URL to the RSS feed (RSS2.0-based or Atom-based), and, the limiting number of items that should be displayed in the News List (10, 20, 50, 100), and the frequency at which the app fetches the content (10min, 60min, once a day). The app will fetch the RSS feed and populate the list UP to the limit number. When user clicks on a particular item, a detailed view should be shown, with the content of the article for that item. 


## The News List

Use RecyclerView for that. On top of the UI, provide a simple `EditText` that will work like a filter to only show the articles that match the provided Regular Expression. If no text is given, all items should be visible, with the query string is provided, the articles should be filtered by the regular expression provided.


# Checklist

* [ ] The git repository URL is correctly provided, such that command works: `git clone <url> `
* [ ] The code is well, logically organised and structured into appropriate classes. Everything should be in a single package.
* [ ] It is clear to the user what RSS feed formats are supported (RSS2.0 and/or Atom)
* [ ] The user can go to Preferences and set the URL of the RSS feed.
* [ ] The user can go to Preferences and set the feed item limit.
* [ ] The user can go to Preferences and set the feed refresh frequency.
* [ ] The user can see the list of items from the feed on the home Activity ListView.
* [ ] The user can go to a particular item by clicking on it. The content will be displayed in newly open activity. The back button puts the user back onto the main ListView activity to select another item. 
* [ ] The user can press the back button from the main activity to quit the app. 
* [ ] When the content article has graphics, it is rendered correctly. 
* [ ] The Filter EditText works as expected.
* [ ] The app has JUnit Tests for testing the parsing, and the filtering functionality. 


## Hints

Make sure that the logic for the fetching of articles is done by the app automatically with the frequency given by the user Preferences. How would you schedule it? How would you prefetch the articles? For testing purposes, add a button to the main activity (the one with the list), to FORCE a fetch upon press of the button. Final app should not have the "fetch" button. Instead, replace it with the "endless scrolling" mode. How would you do that?

The content of the article should be rendered with the use of `WebView` such that graphics of the content article is rendered correctly, if the content was an URL. If the content was a plain text, a simple text view can be used.

Make sure you use appropriate facilities (a library?) to help you parsing XML content. Should the user specify if the feed is in one format or another, or can the app detect it automatically? Can you use a library that parses RSS2.0 and Atom? What would be the benefit? What would be the limitation?

How could you extend the app to make it the way YOU want it? What would be the features that would make it personalised JUST for YOU? 

How could you make it to handle multiple NEWS sources? 

How could you extend the SEARCH facilities to work more like your "Personal NEWS assistant?"  

Can you track how long do you use the app, how often, how long you read individual articles, which articles you read and which you do not?

Could you use AI/Machine Learning to build the model of which news you are most interested in, and the system to suggest those to you?