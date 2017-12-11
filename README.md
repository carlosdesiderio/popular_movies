
# popular movies project
Udacity Android Nanodegree Project submissions ONE and TWO

## Getting Started

### The Movie DB API key
In order to run this project locally, a Movie DB API key is required. The key is not provided in this repository. A new key should be requested from the Movie Database website and added to the project.

The key value should be stored locally in a `gradle.properties` file under the ‘app’ folder. The variable should be named as `theMovieDBToken`


## Implementation
As a requirement, this project is written using only Java.

#### Movie Database Service API
Movie data is queried from [themoviedb.org API](https://www.themoviedb.org/documentation/api)


The application targets the following endpoints:
* [/movie/popular](https://developers.themoviedb.org/3/movies/get-popular-movies) - request to fetch popular movies
* [/movie/top_rated](https://developers.themoviedb.org/3/movies/get-top-rated-movies) - request to fetch top rated movies
* [/movie/{id}/videos](https://developers.themoviedb.org/3/movies/get-movie-videos) - request to fetch trailers
* [/movie/{id}/reviews](https://developers.themoviedb.org/3/movies/get-movie-reviews) - request to fetch reviews

Movie Database request responses are served as JSON string. The response is parsed into ContentValues which facilitates the database insertion process. The application uses the `org.json` package to carry out the JSON deserialisation into ContentValues objects. 

Server error responses are handled in the parsing process but further action is not taken. This could be an area for further development where the user could be informed when a server error response is received.

The application checks whether there is an available connection before making any HTTP request to the service. The `ConnectionManager` is  used to get the device’s current connectivity status just before any view data is requested. Also activities listen for changes in the connectivity status by implementing the `OnNetworkActiveListener` so that they can react when a the connection is restablished .  A Snapbar is used to notify the user of these changes. The bar is customised so that it can show two different messages. 
* A 'No Connection' message which is shown for short period of time
* A 'Connection Available’ message which is shown indefinitely and provides and action to reload the data.

#### Network
All the data requests and consequent parsing of their response is done asynchronously with the use of an `IntentSevice`. There are three distinctive async tasks carried out by the service. Theses task are defined in the [MoviesRequestTasks](app/src/main/java/uk/me/desiderio/popularmovies/task/MoviesRequestTasks.java) class:
* movie request task. It deals with both popular and top rated movies requests
* trailer request task
* review request task.

#### Data Strategy
All requests to the Movie Database Service are cached which allows for offline use of the app and reduces the need for network requests.
The app firstly tries to shows cache data when available assuming the data is no older than maximum lifespan defined in the [MovieContract](app/src/main/java/uk/me/desiderio/popularmovies/data/MoviesContract.java). Data older than an hour is considered to be stale and will trigger a request call to the Movie Database API. However, the current ‘stale’ data is shown when there is no a connection to update the data. 

In the event that nor data or connection is available, A *“Can’t Load Movies"* message is shown in the MainActivity. The message is different when the activity shows the favourite movies as the view doesn’t load its content and is not dependant on the connection but on the user selecting favourite movies. The DetailsActivity will fail silently when a connection is not available by not showing the trailer and review list.

This approach minimises network calls as there will only be a request to the Movie Database API  when these three conditions occur:
* Data is requested by the view.
* Connection is available
* Cached data is stale.

#### Data Persistence
Data persistence is implemented using [SQLite database](app/src/main/java/uk/me/desiderio/popularmovies/data/MovieDBHelper.java). The Database has four tables:
* **Movies** - holds data about the movies that were parsed from he Movie Database response. It has a column for the last updated time that will be used to determine if the data is still up to date. A ‘feed’ column defines if the movie is part of the ‘most popular’ set or the ‘Top rated’ set
* **Trailers** - holds data about the movie trailers. The table has a one-to-many relationship with the movies table which is enforced with the use of a foreign key referring back to the movie  that it belongs to.
* **Reviews** - holds data about the movie reviews. The table has a one-to-many relationship with the movies table which is enforced with the use of a foreign key referring back to the movie  that it belongs to.
* **Favourites** - holds data about the user’s favorites movies. It has very similar design as the movies table. However, this table differs in that it has a different persistence strategy than the movies table.
The database also creates database triggers so that the last update time column is updated when the movie is newly inserted.

The data is made available to the rest of the application with the implementation of a `Provider`. The [MoviesContentProvider](app/src/main/java/uk/me/desiderio/popularmovies/data/MovieContentProvider.java) supports the following actions:
* *insert*
* *bulk insert*
* *delete*
* *query*

The provider also implements a technique where it registers interest to the database changes and notifies them to the ContentResolver so that the loaders are notified as soon the changes occur.

## UI 
The application follows the master-detail view pattern consisting of two views:
* Master view showing a list of movies. This is implemented in the MainActivity 
* Details view showing the details of the movie selected in the master view. This is implemented in the DetailsActivity

### MainActivity - *Master View*
Movies are displayed in the main activity using a list of the movie poster thumbnails. The list is implemented using the RecyclerView.
Three different type of movies views (feeds) are shown. These are defined in the [MovieFeedType](app/src/main/java/uk/me/desiderio/popularmovies/network/MovieFeedType.java) class:
* Most Popular 
* Top Rated
* Favorites

MovieActivity provides a setting menu where the user can choose between the different types of movie feeds. 

The MainActivity uses a `AsyncTaskLoader` to load its views data. The use of a AsyncTaskLoader provides the flexibility to load data from the different movie feed types maximising code reuse.

In order for the AsyncTaskLoader to load the relevant data at a user’s request, the loader has to be aware of the current feed type. A class extending the `CursorWrapper` makes it possible to inject the current feed type value into the loading process. This determines which data will be loaded from the database into the view. It also flags any new data inserted into the database with corresponding type (popular or top rated)

The loader registers a content observer to its cursor so that it is notified by the `ContentProvider` of any changes in the applications’s URI. This way the views will be updated as soon there are any changes to the app’s provider.



#### Activity Transition
Tapping on a movie poster the app transitions to a details screen with additional information. 

DetailsActivity is started by the MainActivity which passes the basic data related to the selected movie. The data is transferred as part of an intent which holds an a `Movie` object as one of its extras. With this purpose in mind, the [Movie](app/src/main/java/uk/me/desiderio/popularmovies/data/Movie.java) class implements the Parcelable interface.

A shared element transition between the MainActivity and the DetailsActivity is implemented for a smooth and contextualised transition.

### DetailsActivity - *Details View*
The detail view displays :
* Original title 
* Movie Poster thumbnail
* A synopsis of the movie’s plot
* User ratings
* Release year
* Favourite toggle button
* A list to show the movie’s Trailers & Reviews

Initially thumbnail, synopsis, ratings and year views are populated with data received in the intent. The Favourite button and the list are setup after querying the database.

A RecyclerView is implemented at the DetailsActivity to display multiple view types:
* header view
* trailer view
* review view


The `RecyclerView` instance has three lists to hold the data related to the different view types. Another list will hold the actual order in which the view should appear in the list view and the related data object index.

The `RecyclerView` should show the trailer before the the review items. A header should be added to the top of each section. Section headers are not shown when related section is empty. A helper class [DetailListViewItem](app/src/main/java/uk/me/desiderio/popularmovies/view/DetailListViewItem.java) was implemented to help with the ordering of the different view types in the `RecyclerView`.


Movies can be marked as favourites in the details view by selecting the button provided. The toggle button will trigger the ContentResolver's insert or delete actions in order to add or remove the selected movie from the favorites table.
A query to this table is also implemented so that the toggle button state is set accordingly  setting its label as either add or remove favourite.

Users can play trailers in YouTube by selecting one of its trailers. Only links to YouTube trailers are shown. Any non YouTube trailers are discarded when parsing the JSON response.
An Intent is used to open a YouTube link in either the native app or a web browser of choice.

DetailsActivity provides a setting menu option where the user can share the first of the movie’s trailers . A sent action intent is used to implement this feature.


### Libraries
[**Picasso**](https://square.github.io/picasso/) : handles image loading and caching

### Project Udacity Documentation
- [Rubric](https://review.udacity.com/#!/rubrics/67/view])
- [Implementation Guide](https://docs.google.com/document/d/1ZlN1fUsCSKuInLECcJkslIqvpKlP7jWL2TP9m6UiA6I/pub?embedded=true#h.7sxo8jefdfll)

### Contact:
labs@desiderio.me.uk

