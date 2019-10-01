# DocuSign Spring Application
Custom Spring application which uses **DocuSign** API to generate specific link for *Angular* client, which will be used to sign template document.

## Application structure
In *config.properties* file there are properties which are needed to successfully log in as impersonated **Admin** user.
All those values can be found on *Admin Page profile* -> *APIs and Keys*
Under the hood, by using values from *config.properties* file, API will request **JWT** *Access Token*, which will be validated and *return URL* will be returned, which user can visit and sign specific document.
