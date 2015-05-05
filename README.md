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
- [Status (unsecured)](#status)
- [Matching (experimental)](#matching)
    - [Get all offers for one demand (unsecured)](#get-all-offers-for-one-demand)
- [Demands](#demands)
	- [Query single Demand](#query-single-demand)
	- [Create Demand](#create-demand)
	- [Update Demand](#update-demand)
	- [Delete Demand](#delete-demand)
- [Offers](#offers)
	- [Query single Offer](#query-single-offer)
	- [Create Offer](#create-offer)
	- [Update Offer](#update-offer)
	- [Delete Offer](#delete-offer)
- [Users](#users)
	- [Query singer User](#query-single-user-by-email)
 	- [Create User (unsecured)](#create-user)
 	- [Update User](#update-user)
	- [Delete User](#delete-user)

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
The request body must contain a valid DemandDraft json object

    {
        "id" : "c1ef9724-935e-4455-854e-96b99eec555d",
        "version" : 1,
        "userId" : "1",
        "mustTags" : ["iphone"],
        "shouldTags" : ["neuwertig","schwarz"],
        "location" : {
            "lat" : 35.92516,
            "lon" : 12.37528
        },
        "distance" : 30,
        "price" : {
            "min" : 100.0,
            "max" : 340.0
        }
    }

### Response
200 Ok

### Example
    curl -XPOST -H "Content-Type: application/json" -d '{"id":"c1ef9724-935e-4455-854e-96b99eec555d","version":1,"userId":"1","mustTags":["iphone"],"shouldTags":["neuwertig","schwarz"],"location":{"lat":35.92516,"lon":12.37528},"distance":30,"price":{"min":100.0,"max":340.0}}' https://localhost:9443/matching/demand/0/0 -v

# Demands
## Query single Demand
### Resource
GET `/demands/{id}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |

###Response
200 Ok
    
    {
        "demand":{
            "id":"9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1
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
    }
    
404 - Not Found

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
            "version": 1
            "userId": "1",
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
            "version": 2
            "userId": "1",
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
            "version": 1
            "userId":"1",
            "tags":["socken", "bekleidung", "wolle"],
            "location":{
                "lat":13.534212,
                "lon":52.468562
            },
            "price":25.0
        }
    }
    
404 - Not Found

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
        "price":25.0
    }

### Response
201 Created

    {
        "offer": {
            "id": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1
            "userId": "1",
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
            "version": 2
            "userId": "1",
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
            "version": 1
            "username":"test",
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
        "username":"Test",
        "email":"test@web.com",
        "password":"12345"
    }

### Response
201 Created

    {
        "user": {
            "id": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1,
            "username":"Test",
            "email":"test@web.com"
        }
    }

400 Bad Request - Empty Body

400 Bad Request - Invalid Json

### Example

    curl -XPOST -H "Content-Type: application/json" -d '{"username":"Test", "email":"test@gmail.com", "password":"test"}' https://localhost:9443/users -v


## Update User
### Resource
PUT `/users/{id}/{version}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Body
The request body must contain a valid UserDraft json object

    {
        "username":"Test",
        "email":"updated@web.com",
        "password", "password"
    }


### Response
200 Ok

    {
        "user": {
            "id": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 2
            "username":"Test",
            "email":"updated@web.com"
        }
    }

400 Bad Request - Missing body

400 Bad Request - Cannot parse json

404 Not Found - Entity was not found

### Example

    curl -XPUT -H "Content-Type: application/json" -d '{"username":"Test","email":"updated@web.com","password":"12345"}' https://localhost:9443/users/1/1 -v

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
