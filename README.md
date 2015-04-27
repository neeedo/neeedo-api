[![Build Status](https://travis-ci.org/neeedo/neeedo-api.svg)](https://travis-ci.org/neeedo/neeedo-api)

[![Dependency Status](https://www.versioneye.com/user/projects/54bfae036c0035c592000069/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54bfae036c0035c592000069)

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
sbt run
```
###How to run (Unix)
```bash
./sbt run
```

API-Documentation
----------
- [Status](#status)
- [Matching](#matching)
    - [Get all offers for one demand](#get-all-offers-for-one-demand)
        - [Resource](#resource)
        - [URL Parameters](#url-parameters)
        - [Response](#response)
        - [Example](#example)
- [Demands](#demands)
	- [Query all Demands](#query-all-demands)
		- [Resource](#resource-1)
		- [Response](#response-1)
		- [Example](#example-1)
	- [Query single Demand](#query-single-demand)
		- [Resource](#resource-2)
		- [URL Parameters](#url-parameters-1)
		- [Response](#response-2)
	- [Create Demand](#create-demand-1)
		- [Resource](#resource-3)
		- [Body](#body-1)
		- [Response](#response-3)
		- [Example](#example-1)
	- [Update Demand](#update-demand)
		- [Resource](#resource-4)
		- [URL Parameters](#url-parameters-2)
		- [Body](#body-2)
		- [Response](#response-4)
		- [Example](#example-3)
	- [Delete Demand](#delete-demand)
		- [Resource](#resource-5)
		- [URL Parameters](#url-parameters-3)
		- [Response](#response-5)
		- [Example](#example-4)
- [Offers](#offers)
	- [Query single Offer](#query-single-offer)
		- [Resource](#resource-6)
		- [URL Parameters](#url-parameters-4)
		- [Response](#response-6)
	- [Create Offer](#create-offer)
		- [Resource](#resource-7)
		- [Body](#body-3)
		- [Response](#response-7)
		- [Example](#example-5)
	- [Update Offer](#update-offer)
		- [Resource](#resource-8)
		- [URL Parameters](#url-parameters-5)
		- [Body](#body-4)
		- [Response](#response-7)
		- [Example](#example-6)
	- [Delete Offer](#delete-offer)
		- [Resource](#resource-9)
		- [URL Parameters](#url-parameters-7)
		- [Response](#response-9)
		- [Example](#example-7)
- [Users](#users)
    - [Create User](#create-user)

# Status
The status will be reported under `http://46.101.162.213/status`.
Currently responds 200 OK if application is online.

# Matching

## Get all offers for one demand

### Resource
GET `http://46.101.162.213/matching/demand/{from}/{pageSize}`

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
    curl -XPOST -H "Content-Type: application/json" -d '{"id":"c1ef9724-935e-4455-854e-96b99eec555d","version":1,"userId":"1","mustTags":["iphone"],"shouldTags":["neuwertig","schwarz"],"location":{"lat":35.92516,"lon":12.37528},"distance":30,"price":{"min":100.0,"max":340.0}}' http://localhost:9000/matching/demand/0/0 -v

# Demands

## Query all Demands
### Resource
GET `http://46.101.162.213/matching/demands`

### Response
200 Ok

### Example
    curl -XGET http://46.101.162.213/matching/demands -v
    
## Query single Demand
### Resource
GET `http://46.101.162.213/demands/{id}`

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
POST `http://46.101.162.213/demands`

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

    curl -XPOST -H "Content-Type: application/json" -d '{"userId":"1","mustTags":["socken","bekleidung","wolle"],"shouldTags":["rot","weich","warm"],"location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' http://46.101.162.213/demands -v

## Update Demand
### Resource
PUT `http://46.101.162.213/demands/{id}/{version}`

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

    curl -XPUT -H "Content-Type: application/json" -d '{"userId":"1","mustTags":["socken","bekleidung","wolle"], "shouldTags":["rot","weich","warm"],"location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' http://46.101.162.213/demands/1/1 -v

## Delete Demand
### Resource
DELETE `http://46.101.162.213/demands/{id}/{version}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Response

200 Ok

404 Not Found - Entity was not found

### Example

    curl -XDELETE http://46.101.162.213/demands/9dfa3c90-85c8-46ce-b50c-3ecde596bc90/2 -v
    
# Offers

## Query single Offer
### Resource
GET `http://46.101.162.213/offers/{id}`

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
POST `http://46.101.162.213/offers`

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

    curl -XPOST -H "Content-Type: application/json" -d '{"userId":"1","tags":["socken","bekleidung","wolle"],"location":{"lat":13.534212,"lon":52.468562},"price":25.0}' http://46.101.162.213/offers -v

## Update Offer
### Resource
PUT `http://46.101.162.213/offers/{id}/{version}`

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

    curl -XPUT -H "Content-Type: application/json" -d '{"userId":"1","tags":["socken","bekleidung","wolle"],"location":{"lat":13.534212,"lon":52.468562},"price":25.0}' http://46.101.162.213/offers/1/1 -v

## Delete Offer
### Resource
DELETE `http://46.101.162.213/offers/{id}/{version}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Response

200 Ok

404 Not Found - Entity was not found

### Example

    curl -XDELETE http://46.101.162.213/offers/9dfa3c90-85c8-46ce-b50c-3ecde596bc90/2 -v


# Users
## Query single User by Email

GET `http://46.101.162.213/users/mail/{mail}`

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
POST `http://46.101.162.213/users`

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
            "uid": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1,
            "username":"Test",
            "email":"test@web.com"
        }
    }

400 Bad Request - Empty Body

400 Bad Request - Invalid Json

### Example

    curl -XPOST -H "Content-Type: application/json" -d '{"username":"Test", "email":"test@gmail.com", "password":"test"}' http://46.101.162.213/users -v


## Update User
### Resource
PUT `http://46.101.162.213/users/{id}/{version}`

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

    curl -XPUT -H "Content-Type: application/json" -d '{"username":"Test","email":"updated@web.com","password":"12345"}' http://46.101.162.213/users/1/1 -v

## Delete Offer
### Resource
DELETE `http://46.101.162.213/usersd/{id}/{version}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Response

200 Ok

404 Not Found - Entity was not found

### Example

    curl -XDELETE http://46.101.162.213/users/9dfa3c90-85c8-46ce-b50c-3ecde596bc90/2 -v
