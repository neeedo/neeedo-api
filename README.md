[![Build Status](https://travis-ci.org/neeedo/neeedo-api.svg)](https://travis-ci.org/neeedo/neeedo-api)

[![codecov.io](https://codecov.io/github/neeedo/neeedo-api/coverage.svg?branch=master)](https://codecov.io/github/neeedo/neeedo-api?branch=master)

![codecov.io](https://codecov.io/github/neeedo/neeedo-api/branch.svg?branch=master)

Installation
----------

To run this project [Java JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or higher is required.

Checkout the project and create a `custom-application.conf`

```bash
git clone https://github.com/neeedo/neeedo-api.git
cd api
cp conf/custom-application-dummy.conf conf/custom-application.conf
```

If you want to use your own shop, copy your credentials to your `custom-application.conf`.
You can find them in your [SPHERE.IO](http://admin.sphere.io) backend account under "Developers -> API Clients"

*Note that it's currently mandatory to provide an own SPHERE.IO-shop* 

###How to run (Windows)
*Make sure you have sbt intalled on your local maschine*
```bash
sbt
run -Dhttps.port=9443
```
###How to run (Unix)
```bash
./sbt
run -Dhttps.port=9443
```

###Access
The Live Api is accessable under

`https://api.neeedo.com/`

If you run the application on your local machine access it under

`https://localhost:9443/`

API-Documentation
----------
- [Common Errors](#common-errors)
- [Status (unsecured)](#status)
- [Matching](#matching)
    - [Get all offers for one demand (unsecured)](#get-all-offers-for-one-demand)
- [Tag Suggester](#tag-suggester)
    - [Get Tag suggestions (unsecured)](#get-tag-suggestions-unsecured)
    - [Get Tag completions (unsecured)](#get-tag-completions-unsecured)
- [Messages](#messages)
    - [Write Message](#write-message)
    - [Read Message](#read-message)
    - [Mark Message as read](#mark-message-as-read)
    - [Get all conversations](#get-all-conversations)
- [Favorites](#favorites)
    - [Add Favorite](#add-favorite)
    - [Get Favorites By User](#get-favorites-by-user)
    - [Remove Favorite](#remove-favorite)
- [Demands](#demands)
	- [Query single Demand](#query-single-demand)
	- [Query Demands for User](#query-demands-for-user)
	- [Query all Demands (unsecured)](#query-all-demands)
	- [Create Demand](#create-demand)
	- [Update Demand](#update-demand)
	- [Delete Demand](#delete-demand)
- [Offers](#offers)
	- [Query single Offer](#query-single-offer)
 	- [Query Offers for User](#query-offers-for-user)
	- [Query all Offers (unsecured)](#query-all-offers)
	- [Create Offer](#create-offer)
	- [Update Offer](#update-offer)
	- [Delete Offer](#delete-offer)
- [Users](#users)
	- [Query singer User](#query-single-user-by-email)
 	- [Create User (unsecured)](#create-user)
	- [Delete User](#delete-user)
- [Images](#images)
    - [Upload Image](#upload-image)
    - [Get Image (unsecured)](#get-image)

# Common Errors
Each POST or PUT request sent to this api will respond with these error codes
when the body is incorrect:

If you send a json object that can't be parsed and is not as expected you will get:

    400 BadRequest
    {"error":"Invalid json body"}

If you dont send any json body at all you will get:

    400 BadRequest
    {"error":"Missing body json object"}

# Status
The status will be reported under `/status`.
Currently responds 200 OK if application is online.

# Matching

## Get all offers for one demand

### Resource
GET `/matching/demand/{from}/{pageSize}`

### URL Parameters

| Name | Mandatory | Value Type | Description |
| ---- | --------- | ---------- | ----------- |
| from | Mandatory | Integer    | Offset value for paging(not yet implemented just pass a number) |
| pageSize | Mandatory | Integer    | Page size for paging (not yet implemented just pass a number) |

### Body
The request body must contain a valid Demand json object

    {
        "id":"984730ec-2778-4c5d-ab71-19128c738729",
        "version":1,
        "user": {
            "id":"f8b3dddf-1943-4371-aaa4-2be98fe4ee54",
            "name":"neu"
        },
        "mustTags":["rahmen"],
        "shouldTags":[],
        "location": {
            "lat":52.4907453,
            "lon":13.5210239
        },
        "distance":30,
        "price": {
            "min":0,
            "max":50
        }
    }

### Response
200 Ok

### Example
    curl -XPOST -H "Content-Type: application/json" -d '{"mustTags":["rahmen"],"shouldTags":[],"location": {"lat":52.4907453,"lon":13.5210239},"distance":30,"price": {"min":0,"max":50},"id":"984730ec-2778-4c5d-ab71-19128c738729","version":1,"user": {"id":"f8b3dddf-1943-4371-aaa4-2be98fe4ee54","name":"neu"}}' https://localhost:9443/matching/demand/0/0 -v

# Tag Suggester

## Get Tag suggestions (unsecured)

### Resource
GET `/completion/suggest/{phrase}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| phrase | Mandatory | url encoded phrase string ("tag1,tag2,tag3") |

### Response
200 Ok

    {
        "suggestedTags":[
            "Rahmen",
            "Radkappe"
        ]
    }

## Get Tag completions (unsecured)

### Resource
GET `/completion/tag/{tag}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| tag | Mandatory | url encoded tag string |

### Response
200 Ok

    {
        "completedTags":[
            "Rahmen",
            "Radkappe"
        ]
    }

# Messages

## Write Message

### Resource
POST `/messages`

### Body
The request body must contain a valid message draft json object

    {
        "senderId":"56530372-aa7f-41d7-b1b0-c09931fdbc08",
        "recipientId": "98d40e3e-1c50-43f3-9ba5-58497b417d01",
        "body": "hey there"
    }

### Response
200 Ok

    {
        "message": {
            "id": "509cfcc8-3a1f-450b-a088-fad8cd3d6032",
            "sender": {
                "id": "56530372-aa7f-41d7-b1b0-c09931fdbc08",
                "name": "Test"
            },
            "recipient": {
                "id": "98d40e3e-1c50-43f3-9ba5-58497b417d01",
                "name": "Blub"
            },
            "body": "hey there",
            "timestamp": 1434802592926,
            "read": false
        }
    }

## Read Messages

### Resource
GET `/messages/{userId1}/{userId2}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| userId1 | Mandatory | alphanumeric |
| userId2 | Mandatory | alphanumeric |

### Response
200 Ok

    {
        "messages": [
            {
                "id": "2bf41679-e3c4-45fb-89df-a1ff6bad4b2d",
                "sender": {
                    "id": "98d40e3e-1c50-43f3-9ba5-58497b417d01",
                    "name": "Blub"
                },
                "recipient": {
                    "id": "3000dfdd-9acf-42c8-9768-f4f6b3fd221d",
                    "name": "Test"
                },
                "body": "hey there yourself",
                "timestamp": 1434463375027,
                "read": false
            },
            {
                "id": "24c5e9ff-eed3-4b15-b6d6-4cee5d8f1616",
                "sender": {
                    "id": "98d40e3e-1c50-43f3-9ba5-58497b417d01",
                    "name": "Blub"
                },
                "recipient": {
                    "id": "3000dfdd-9acf-42c8-9768-f4f6b3fd221d",
                    "name": "Test"
                },
                "body": "hey there",
                "timestamp": 1434463368196,
                "read": false
            },
            {
                "id": "73340fa6-faa9-4c49-8bf7-f9e85ad941a4",
                "sender": {
                    "id": "3000dfdd-9acf-42c8-9768-f4f6b3fd221d",
                    "name": "Test"
                },
                "recipient": {
                    "id": "98d40e3e-1c50-43f3-9ba5-58497b417d01",
                    "name": "Blub"
                },
                "body": "hey there",
                "timestamp": 1434463349075,
                "read": false
            }
        ]
    }

## Mark Message as read

### Resource
PUT `/messages/{messageId}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| messageId | Mandatory | alphanumeric |

### Response

    {
        "messageId": "73340fa6-faa9-4c49-8bf7-f9e85ad941a4"
    }

## Get all conversations
Get all userIds that have a dialogue with a specific user with unread/read messages

### Resource
GET `/conversations/{userId}?read={read}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| userId | Mandatory | alphanumeric |
| read | Mandatory | boolean |

### Response

    {
        "users": [
            {
                "id": "98d40e3e-1c50-43f3-9ba5-58497b417d01",
                "name": "Blub"
            }
        ]
    }
    
# Favorites
## Add Favorite
### Resource
POST `/favorites`

### Body
The request body must contain a valid Favorite json object

    {
        "userId":"09d31dae-51b1-45e5-a343-56a3462e4053",
        "offerId": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90"
    }

### Response
201 Created
    
    {
        "userId":"09d31dae-51b1-45e5-a343-56a3462e4053",
        "offerId": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90"
    }

## Get Favorites By User
### Resource
GET `/favorites/{userId}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| userId | Mandatory | alphanumeric |

### Response
200 Ok

    "favorites": [
        {
            "id":"9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1,
            "user": {
                "id":"09d31dae-51b1-45e5-a343-56a3462e4053",
                "name":"Stefan"
            },
            "tags":["socken", "bekleidung", "wolle"],
            "location":{
                "lat":13.534212,
                "lon":52.468562
            },
            "price":25.0
        },
        ...
    ]

## Remove Favorite
### Resource
DELETE `/favorites`

### Body
The request body must contain a valid Favorite json object

    {
        "userId":"09d31dae-51b1-45e5-a343-56a3462e4053",
        "offerId": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90"
    }

### Response
200 Ok
    
    {
        "userId":"09d31dae-51b1-45e5-a343-56a3462e4053",
        "offerId": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90"
    }

# Demands
## Query single Demand
### Resource
GET `/demands/{id}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |

### Response
200 Ok
    
    {
        "demand":{
            "id":"9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1,
            "user" : {
                "id":"1",
                "name":"Stefan"
            },
            "mustTags":["socken", "bekleidung", "wolle"],
            "shouldTags":["rot", "weich", "warm"],
            "location":{
                "lat":13.534212,
                "lon":52.468562
            },
            "distance":30,
            "price":{
                "min":25.0,
                "max":77.0
            }
        }
    }
    
404 - Not Found

## Query Demands for User
### Resource
GET `/demands/users/{userId}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| userId | Mandatory | alphanumeric |

### Response
200 Ok

	{
		"demands": [ 
			{...},
			{...}
		]
	}

## Query all Demands
### Resource
GET `/demands`

### Response
200 Ok

	{
		"demands": [ 
			{...},
			{...}
		]
	}

## Create Demand
### Resource
POST `/demands`

### Body
The request body must contain a valid DemandDraft json object

    {
        "userId":"1",
        "mustTags":["socken", "bekleidung", "wolle"],
        "shouldTags":["rot", "weich", "warm"],
        "location":{
            "lat":13.534212,
            "lon":52.468562
        },
        "distance":30,
        "price":{
            "min":25.0,
            "max":77.0
        }
    }

### Response
201 Created

    {
        "demand": {
            "id": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1,
            "user" : {
                "id": "1",
                "name": "Stefan"
            },
            "mustTags":["socken", "bekleidung", "wolle"],
            "shouldTags":["rot", "weich", "warm"],
            "location": {
                "lat":13.534212,
                "lon":52.468562
            },
            "distance": 30,
            "price": {
                "min":25.0,
                "max":77.0
            }
        }
    }
    
400 Bad Request - Empty Body

400 Bad Request - Invalid Json

### Example

    curl -XPOST -H "Content-Type: application/json" -d '{"userId":"1","mustTags":["socken","bekleidung","wolle"],"shouldTags":["rot","weich","warm"],"location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' https://localhost:9443/demands -v

## Update Demand
### Resource
PUT `/demands/{id}/{version}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Body
The request body must contain a valid DemandDraft json object

    {
        "userId":"1",
        "mustTags":["socken", "bekleidung", "wolle"],
        "shouldTags":["rot", "weich", "warm"],
        "location":{
            "lat":13.534212,
            "lon":52.468562
        },
        "distance":30,
        "price":{
            "min":25.0,
            "max":77.0
        }
    }
	

### Response
200 Ok

    {
        "demand": {
            "id": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 2,
            "user": {
                "id": "1",
                "name": "1"
            },
            "mustTags":["socken", "bekleidung", "wolle"],
            "shouldTags":["rot", "weich", "warm"],
            "location": {
                "lat":13.534212,
                "lon":52.468562
            },
            "distance": 30,
            "price": {
                "min":25.0,
                "max":77.0
            }
        }
    }	
	
400 Bad Request - Missing body

400 Bad Request - Cannot parse json

404 Not Found - Entity was not found

### Example

    curl -XPUT -H "Content-Type: application/json" -d '{"userId":"1","mustTags":["socken","bekleidung","wolle"], "shouldTags":["rot","weich","warm"],"location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' https://localhost:9443/demands/1/1 -v

## Delete Demand
### Resource
DELETE `/demands/{id}/{version}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Response

200 Ok

404 Not Found - Entity was not found

### Example

    curl -XDELETE https://localhost:9443/demands/9dfa3c90-85c8-46ce-b50c-3ecde596bc90/2 -v
    
# Offers

## Query single Offer
### Resource
GET `/offers/{id}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |

###Response
200 Ok
    
    {
        "offer":{
            "id":"9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1,
            "user": {
                "id":"1",
                "name":"Stefan"
            },
            "tags":["socken", "bekleidung", "wolle"],
            "location":{
                "lat":13.534212,
                "lon":52.468562
            },
            "price":25.0
        }
    }
    
404 - Not Found

## Query Offers for User
### Resource
GET `/offers/users/{userId}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| userId | Mandatory | alphanumeric |

### Response
200 Ok

	{
		"offers": [ 
			{...},
			{...}
		]
	}

## Query all Offers
### Resource
GET `/offers`

### Response
200 Ok

	{
		"offers": [ 
			{...},
			{...}
		]
	}

## Create Offer
### Resource
POST `/offers`

### Body
The request body must contain a valid OfferDraft json object

    {
        "userId":"1",
        "tags":["socken", "bekleidung", "wolle"],
        "location":{
            "lat":13.534212,
            "lon":52.468562
        },
        "price":25.0,
        "images":["test.jpg", "blub.jpg"]
    }

The images array can be empty. If you add imagenames make sure that you have [uploaded](#upload-image)
the pictures and use the correct name that is returned by the api.

### Response
201 Created

    {
        "offer": {
            "id": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1,
            "user": {
                "id": "1",
                "name": "Stefan",
            },
            "tags":["socken", "bekleidung", "wolle"],
            "location": {
                "lat":13.534212,
                "lon":52.468562
            },
            "price":25.0
        }
    }
    
400 Bad Request - Empty Body

400 Bad Request - Invalid Json

### Example

    curl -XPOST -H "Content-Type: application/json" -d '{"userId":"1","tags":["socken","bekleidung","wolle"],"location":{"lat":13.534212,"lon":52.468562},"price":25.0}' https://localhost:9443/offers -v

## Update Offer
### Resource
PUT `/offers/{id}/{version}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Body
The request body must contain a valid OfferDraft json object

    {
        "userId":"1",
        "tags":["socken", "bekleidung", "wolle"],
        "location":{
            "lat":13.534212,
            "lon":52.468562
        },
        "price":25.0
    }
	

### Response
200 Ok

    {
        "offer": {
            "id": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 2,
            "user": {
                "id": "1",
                "name": "1",
            },
            "tags":["socken", "bekleidung", "wolle"],
            "location": {
                "lat":13.534212,
                "lon":52.468562
            },
            "price":25.0
        }
    }	
	
400 Bad Request - Missing body

400 Bad Request - Cannot parse json

404 Not Found - Entity was not found

### Example

    curl -XPUT -H "Content-Type: application/json" -d '{"userId":"1","tags":["socken","bekleidung","wolle"],"location":{"lat":13.534212,"lon":52.468562},"price":25.0}' https://localhost:9443/offers/1/1 -v

## Delete Offer
### Resource
DELETE `/offers/{id}/{version}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Response

200 Ok

404 Not Found - Entity was not found

### Example

    curl -XDELETE https://localhost:9443/offers/9dfa3c90-85c8-46ce-b50c-3ecde596bc90/2 -v
    
# Users
## Query single User by Email

GET `/users/mail/{mail}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| mail | Mandatory | alphanumeric |

###Response
200 Ok

    {
        "user":{
            "id":"9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1,
            "name":"test",
            "email":"test@web.de"
        }
    }

404 - Not Found

## Create User
### Resource
POST `/users`

### Body
The request body must contain a valid UserDraft json object

    {
        "name":"Test",
        "email":"test@web.com",
        "password":"12345"
    }

### Response
201 Created

    {
        "user": {
            "id": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1,
            "name":"Test",
            "email":"test@web.com"
        }
    }

400 Bad Request

    {
        "error" : "A user with this email address already exists"
    }

### Example

    curl -XPOST -H "Content-Type: application/json" -d '{"username":"Test", "email":"test@gmail.com", "password":"test"}' https://localhost:9443/users -v

## Delete User
### Resource
DELETE `/usersd/{id}/{version}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Response

200 Ok

404 Not Found - Entity was not found

### Example

    curl -XDELETE https://localhost:9443/users/9dfa3c90-85c8-46ce-b50c-3ecde596bc90/2 -v

# Images

## Upload Image
### Resource
POST `/images`

The image must be sent in the body multipart/form-data encoded.

### Response
201 Created

    {
        "image" : "10e5b696-2e21-488e-9857-29d94da95ee3.jpg"
    }

## Get Image
GET `/images/{filename}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| filename | Mandatory | example.jpg |

### Response
Response of the action is a chunked http stream that returns the image with the correct mimetype.
This means you can use these url's as source in img tags for example.
