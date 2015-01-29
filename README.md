[![Build Status](https://travis-ci.org/HTW-Projekt-2014-Commercetools/api.svg?branch=master)](https://travis-ci.org/HTW-Projekt-2014-Commercetools/api)

[![Dependency Status](https://www.versioneye.com/user/projects/54bfae036c0035c592000069/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54bfae036c0035c592000069)

[![codecov.io](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api/coverage.svg?branch=master)](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api?branch=master)

![codecov.io](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api/branch.svg?branch=master)

Installation
----------

To run this project [Java JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or higher is required.

Checkout the project and create a `custom-application.conf`

```bash
git clone git@github.com:HTW-Projekt-2014-Commercetools/api.git
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

- [Stubs](#stubs)
- [Demands](#demands)
	- [Query all Demands](#query-all-demands)
		- [Resource](#resource)
		- [Response](#response)
		- [Example](#example)
	- [Query single Demand](#query-single-demand)
		- [Resource](#resource-1)
		- [URL Parameters](#url-parameters)
		- [Response](#response-1)
	- [Create Demand](#create-demand)
		- [Resource](#resource-2)
		- [Body](#body)
		- [Response](#response-2)
		- [Example](#example)
	- [Update Demand](#update-demand)
		- [Resource](#resource-3)
		- [URL Parameters](#url-parameters-1)
		- [Body](#body-1)
		- [Response](#response-3)
		- [Example](#example-2)
	- [Delete Demand](#delete-demand)
		- [Resource](#resource-4)
		- [URL Parameters](#url-parameters-2)
		- [Response](#response-4)
		- [Example](#example-3)
- [Offers](#offers)
	- [Query single Offer](#query-single-offer)
		- [Resource](#resource-5)
		- [URL Parameters](#url-parameters-3)
		- [Response](#response-5)
	- [Create Offer](#create-offer)
		- [Resource](#resource-6)
		- [Body](#body)
		- [Response](#response-6)
		- [Example](#example-4)
	- [Update Offer](#update-offer)
		- [Resource](#resource-7)
		- [URL Parameters](#url-parameters-4)
		- [Body](#body-1)
		- [Response](#response-7)
		- [Example](#example-5)
	- [Delete Offer](#delete-offer)
		- [Resource](#resource-8)
		- [URL Parameters](#url-parameters-6)
		- [Response](#response-8)
		- [Example](#example-6)
	

# Stubs
For the latter there are also stub implementations. You can access them by prepending `stub/` to the resource identifier.

Demands: `http://dry-depths-2035.herokuapp.com/stub/demands`

Offers: `http://dry-depths-2035.herokuapp.com/stub/offers`


# Demands

## Query all Demands
### Resource
GET `http://dry-depths-2035.herokuapp.com/matching/demands`

### Response
200 Ok

### Example
    curl -XGET http://dry-depths-2035.herokuapp.com/matching/demands -v
    
## Query single Demand
### Resource
GET `http://dry-depths-2035.herokuapp.com/demands/{id}`

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
            "tags":"socken bekleidung wolle",
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
POST `http://dry-depths-2035.herokuapp.com/demands`

### Body
The request body must contain a valid DemandDraft json object

    {
        "userId":"1",
        "tags":"socken bekleidung wolle",
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
            "tags": "socken bekleidung wolle",
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

    curl -XPOST -H "Content-Type: application/json" -d '{"userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' http://dry-depths-2035.herokuapp.com/demands -v

## Update Demand
### Resource
PUT `http://dry-depths-2035.herokuapp.com/demands/{id}/{version}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Body
The request body must contain a valid DemandDraft json object

    {
        "userId":"1",
        "tags":"socken bekleidung wolle rot",
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
            "tags": "socken bekleidung wolle rot",
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

    curl -XPUT -H "Content-Type: application/json" -d '{"userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' http://dry-depths-2035.herokuapp.com/demands/1/1 -v 

## Delete Demand
### Resource
DELETE `http://dry-depths-2035.herokuapp.com/demands/{id}/{version}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Response

200 Ok

404 Not Found - Entity was not found

### Example

    curl -XDELETE http://dry-depths-2035.herokuapp.com/demands/9dfa3c90-85c8-46ce-b50c-3ecde596bc90/2 -v
    
# Offers

## Query single Offer
### Resource
GET `http://dry-depths-2035.herokuapp.com/offers/{id}`

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
            "tags":"socken bekleidung wolle",
            "location":{
                "lat":13.534212,
                "lon":52.468562
            },
            "price":{
                "min":25.0,
                "max":77.0
            }
        }
    }
    
404 - Not Found

## Create Offer
### Resource
POST `http://dry-depths-2035.herokuapp.com/offers`

### Body
The request body must contain a valid OfferDraft json object

    {
        "userId":"1",
        "tags":"socken bekleidung wolle",
        "location":{
            "lat":13.534212,
            "lon":52.468562
        },
        "price":{
            "min":25.0,
            "max":77.0
        }
    }

### Response
201 Created

    {
        "offer": {
            "id": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1
            "userId": "1",
            "tags": "socken bekleidung wolle",
            "location": {
                "lat":13.534212,
                "lon":52.468562
            },
            "price": {
                "min":25.0,
                "max":77.0
            }
        }
    }
    
400 Bad Request - Empty Body

400 Bad Request - Invalid Json

### Example

    curl -XPOST -H "Content-Type: application/json" -d '{"userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212,"lon":52.468562},"price":{"min":25.0,"max":77.0}}' http://dry-depths-2035.herokuapp.com/offers -v

## Update Offer
### Resource
PUT `http://dry-depths-2035.herokuapp.com/offers/{id}/{version}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Body
The request body must contain a valid OfferDraft json object

    {
        "userId":"1",
        "tags":"socken bekleidung wolle rot",
        "location":{
            "lat":13.534212,
            "lon":52.468562
        },
        "price":{
            "min":25.0,
            "max":77.0
        }
    }
	

### Response
200 Ok

    {
        "offer": {
            "id": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 2
            "userId": "1",
            "tags": "socken bekleidung wolle rot",
            "location": {
                "lat":13.534212,
                "lon":52.468562
            },
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

    curl -XPUT -H "Content-Type: application/json" -d '{"userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212,"lon":52.468562},"price":{"min":25.0,"max":77.0}}' http://dry-depths-2035.herokuapp.com/offers/1/1 -v 

## Delete Demand
### Resource
DELETE `http://dry-depths-2035.herokuapp.com/offers/{id}/{version}`

### URL Parameters

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Response

200 Ok

404 Not Found - Entity was not found

### Example

    curl -XDELETE http://dry-depths-2035.herokuapp.com/offers/9dfa3c90-85c8-46ce-b50c-3ecde596bc90/2 -v
