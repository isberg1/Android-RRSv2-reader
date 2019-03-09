# IMT3673 2019 lab 2 Simple NEWS reader

## About

This is a simple RSS news reader using rss2.0.
It has been tested on the rss feeds form:

https://www.nrk.no/nyheter/siste.rss
https://www.nrk.no/toppsaker.rss                    # has images
https://www.vg.no/rss/feed/forsiden/                # has images
https://www.cisco.com/c/dam/global/no_no/about/rss.xml
http://teknobygg.no/rss/articles/3108

the app consists of one Activity that uses 2 fragments, List and preferences.
A SQLite database to store rss articles. One background service that downloads rss
articles and stores them in a database.

### List fragment
in the list fragment you may read the tittle, publication date and descriptions of rss articles.
by long-clicking on a item in the list the app will open the rss link in a web browser
over the rss list is a edit text field, where you may search all the articles stored in the
database based on a regex pattern. the button next to th field activates the search. A empty
search will refresh the rss list(by checking the database for new entries).

### Preference fragment
In the preference fragment you may specify the following things for the apps behavior.

* the source of the rss feed to be displayed in the
list fragment.

* how often the background service is to run

* how many entries to be displayed in the list fragment

* a editText field to add new rss source url

* a button for applying changes

invalid entries are rejected, and will result in a snackBar displaying an error message.


## Technical implementation

The app is organized by a tab layout. in order to move between the fragments you may push
one of the tabs, or you may swipe left of right.

#### about the rss list
the list fragment is made using a recyclerView with a card layout. In order to perform the
required regex search. the app extract entries from the database and does a regex match in java.
I originally wanted to perform the regex search in the database directly. but found that
SQLite does not support such functionality. it has a function for searching for a word pattern
by using "LIKE %pattern%", but this would not be a fully functional regex search.


#### about preferences
i have only tested the app for use with rss2.0.
adding a new entry to the rss url source will also add it to a list of url that a background service
monitors for new content.
in order to apply any changes you must press the "Apply Changes" button. only then will the app
try to validate and activate the changes. preferences are stored in DefaultSharedPreferences. this
is because it is the recommended way to do it in android. and because DefaultSharedPreferences are
easily accessible to all activities, fragments and classes.

#### about the database
for the database implementation i used the ROOM feature of SQLite. the app only uses one table
that look like this:

        link (primary key), title, pubDate, description, origin

#### about the background service
the background service is implemented as a android jobservice, using jobScheduler.
this allows the service to run only if several criteria are met. criteria like time or if
network is available. the drawback of using this method is that android may not run the
service exactly when it is suppose to. for example you may specify the app to run every 30 minutes.
but android may run it every 29, 31, 33 minutes or their about in stead. also the minimum time
between runs are 15 minutes.


#### about other things
in order to parse the rss XML feed. i use XML pull parsing. I originally thought about using
XML DOM paring instead. by i found that this is so memory consuming it is not the recommended
way to do XML paring in android.


# Checklist

* [ ] The git repository URL is correctly provided, such that command works: `git clone <url> `
* [ ] The code is well, logically organised and structured into appropriate classes. Everything should be in a single package.
* [ ] It is clear to the user what RSS feed formats are supported (RSS2.0 and/or Atom)
* [x] The user can go to Preferences and set the URL of the RSS feed.
* [x] The user can go to Preferences and set the feed item limit.
* [x] The user can go to Preferences and set the feed refresh frequency.
* [x] The user can see the list of items from the feed on the home Activity ListView.
* [x] The user can go to a particular item by clicking on it. The content will be displayed in newly open activity. The back button puts the user back onto the main ListView activity to select another item.
* [x] The user can press the back button from the main activity to quit the app.
* [ ] When the content article has graphics, it is rendered correctly.
* [x] The Filter EditText works as expected.
* [ ] The app has JUnit Tests for testing the parsing, and the filtering functionality.