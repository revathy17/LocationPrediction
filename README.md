# Location Prediction of Twitter Users using Wikipedia

Predict the location of a user based on the content of their tweets using a knowledgebase created from Wikipedia. 

This project has three modules:
### Knowledge-base Generator
For a given list of locations, create the knowlegebase of local entities using the corresponding Wikipedia pages of the location.
Furthermore, the local entities can be scored using one of the following scores: (1) Tversky Index, (2) Jaccard Index, (3) Betweenness Centrality or (4) Pointwise Mutual Information

Tversky Index has shown the best performance in ranking local entities that can be used to predict the location of a user accurately.

### User Profile Creator
The input to this module is a set of tweets (approximately 1000) of the user whose location needs to be predicted. You can fetch the tweets of a given user using the Twitter Search API. This module spots Wikipedia entities in the tweets using [DBPedia Spotlight](http://spotlight.dbpedia.org/demo/). This can be modified to use any other Wikipedia Entity Spotter like [Zemanta](http://www.zemanta.com/blog/demo/) or [Text Razor](https://www.textrazor.com/demo).


### Location Prediction
Predict the location of a Twitter user using the Wikipedia entities spotted in "User Profile Generator" and the knowledge base created in "Knowledge-base Generator" module.




